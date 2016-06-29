package com.example.wisdomschool2;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class MainActivity extends Activity implements OnItemClickListener {
	private HttpClient client;

	private AutoCompleteTextView validateCodeAc;
	private ImageView validateIv;
	private EditText validateEt;
	private ViewFlipper flipper;
	private ProgressDialog dialog;// 当点击查询时，跳出的对话框
	
	private Handler handler;
	private GestureDetector detector;

	private String currentGh;//当前自动完成框选择的工号
	
	private List<View> views=new ArrayList<View>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		client = new DefaultHttpClient();// 保证是唯一的初始化动作

		validateCodeAc = (AutoCompleteTextView) findViewById(R.id.validateCodeAc);
		validateIv = (ImageView) findViewById(R.id.validateCodeIv);
		validateEt = (EditText) findViewById(R.id.validateCodeEt);
		flipper=(ViewFlipper) findViewById(R.id.flipper);
		dialog=new ProgressDialog(this);
		
		// 初始化自动完成框
		validateCodeAc.setOnItemClickListener(this);
		initDataForValidateAc();

		handler = new MyHandler();// 更新界面上的组件
		getValidateCode();// 获取验证码
		
		detector=new GestureDetector(this,new MyGestureListener(flipper));//手势检测器
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return detector.onTouchEvent(event);
	}

	// 自动完成框的点击事件，结果找出所选择老师的工号
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		List<String> names = DataUtil.getNames();
		List<String> ghs = DataUtil.getGhs();
		String selectedName = parent.getItemAtPosition(position).toString();

		for (int i = 0; i < ghs.size(); i++) {
			if (selectedName.equals(names.get(i))) {
				currentGh = ghs.get(i);
				break;
			}
		}
	}

	// 为自动完成框填充数据
	private void initDataForValidateAc() {
		InputStream in = null;
		try {
			in = getAssets().open("teacherName.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		DataUtil.init(in);
		List<String> names = DataUtil.getNames();

		ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
				android.R.layout.simple_dropdown_item_1line, names);

		validateCodeAc.setAdapter(adapter);
	}

	// 查询按钮的事件监听
	public void query(View v) {
		// 查询参数的准备

		String validateCode = validateEt.getEditableText().toString();
		String type = "1";
		String term = "20141";
		dialog.show();
		
		queryCourseByTeacher(currentGh, validateCode, term, type);

	}

	public void changeValidateCode(View v) {
		getValidateCode();
	}
	//对验证码的查询
	private void getValidateCode() {
		new Thread() {
			@Override
			public void run() {
				HttpGet request = null;
				try {
					request = new HttpGet(Constants.VALIDATE_URL);
					HttpResponse resp = client.execute(request);
					InputStream in = resp.getEntity().getContent();
					Bitmap bitmap = BitmapFactory.decodeStream(in);
					Message msg = new Message();
					msg.getData().putParcelable("validate", bitmap);
					handler.sendMessage(msg);

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					request.abort();
				}
			}

		}.start();
	}

	// 完成数据的post查询
	private void queryCourseByTeacher(String gh, String validateCode,
			String term, String type) {
		final HttpPost request = new HttpPost(Constants.QUERY_BY_TEACHER_URL);

		// ==================查询参数的准备=========================================

		List<NameValuePair> list = new ArrayList<NameValuePair>();
		NameValuePair XNXQPair = new BasicNameValuePair("Sel_XNXQ", term);// 学期
		NameValuePair JSPair = new BasicNameValuePair("Sel_JS", gh);// 老师工号
		NameValuePair typePair = new BasicNameValuePair("type", type);
		NameValuePair yzmPair = new BasicNameValuePair("txt_yzm", validateCode);
		list.add(yzmPair);
		list.add(XNXQPair);
		list.add(JSPair);
		list.add(typePair);

		// ==================初始化post请求=========================================

		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list);
			request.setEntity(entity);
			request.setHeader("referer",
					"http://gl.sycm.com.cn/Jwweb/ZNPK/TeacherKBFB.aspx");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		//执行查询
		new Thread() {
			@Override
			public void run() {
				try {
					HttpResponse resp = client.execute(request);

					Document doc = Jsoup.parse(resp.getEntity().getContent(),
							"gbk", "");
					ByTeacherParser parser = new ByTeacherParser(doc);

					Message msg = new Message();
					if (parser.validateError()) {
						msg.getData().putString("exception", "校验码错误！");
					} else if (parser.noCourse()) {
						msg.getData().putString("exception", "此老师无课程！");
					} else {
						CourseOfTeacher teacher = parser.getCourse();
						msg.getData().putSerializable("teacher", teacher);
					}
					handler.sendMessage(msg);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}.start();

	}
	
	//出错信息的弹出框
	private void popDialog(String info){
		AlertDialog.Builder builder = new AlertDialog.Builder(
				MainActivity.this);
		builder.setTitle("重要提示");
		builder.setMessage(info);
		builder.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getValidateCode();
			}
		});
		builder.create().show();
	}
	
	
	
	
	//对返回 的课程表进行处理
	private void teacherInfoProcess(CourseOfTeacher teacher){
		TextView tinfo=new TextView(MainActivity.this);
		tinfo.setTextColor(Color.BLUE);
		tinfo.setTextSize(11);
		
		ListView courseLv=new ListView(MainActivity.this);
		courseLv.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return detector.onTouchEvent(event);
			}
		});
		LinearLayout group=new LinearLayout(MainActivity.this);
		group.setOrientation(LinearLayout.VERTICAL);
		group.addView(tinfo);
		group.addView(courseLv);
		
		//=============防重复处理================================
			int index=-1;
			for(int i=0;i<views.size();i++){
				LinearLayout ll=(LinearLayout)views.get(i);
				TextView tv=(TextView)ll.getChildAt(0);
				if(teacher.getTeacherInfo().equals(tv.getText())){
					index=i;
					break;
				}
			}
			if(index>=0){
				views.remove(index);
			}
			views.add(group);
		//====================================================
		
		
		tinfo.setText(teacher.getTeacherInfo());
		
		// 向listView中添加数据
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		Map<String, String> courses = teacher.getCourse();
		for (String w : courses.keySet()) {
			LinkedHashMap<String, String> mp = new LinkedHashMap<String, String>();
			mp.put("title", w);
			mp.put("content", courses.get(w));
			data.add(mp);
		}

		SimpleAdapter adapter = new SimpleAdapter(MainActivity.this,
				data, R.layout.course, new String[] { "title",
						"content" }, new int[] { R.id.title,
						R.id.content });
		courseLv.setAdapter(adapter);
		
		//倒序方式加入flipper中，以保证后加的数据优先显示出来
		flipper.removeAllViews();
		for(int i=views.size()-1;i>=0;i--){
			flipper.addView(views.get(i));
		}
		
		getValidateCode();
	}
	
	
	//UI更新的全部操作
	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Bitmap validateBitmap = msg.getData().getParcelable("validate");
			String exceptionInfo = msg.getData().getString("exception");
			CourseOfTeacher teacher = (CourseOfTeacher) msg.getData()
					.getSerializable("teacher");
			if (validateBitmap != null) {
				validateIv.setImageBitmap(validateBitmap);
			}
			if (exceptionInfo != null) {
				dialog.cancel();
				popDialog(exceptionInfo);
				
			}
			if (teacher != null) {
				dialog.cancel();
				teacherInfoProcess(teacher);
			}

		}
	}

}
