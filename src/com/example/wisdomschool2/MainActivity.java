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
	private ProgressDialog dialog;// �������ѯʱ�������ĶԻ���
	
	private Handler handler;
	private GestureDetector detector;

	private String currentGh;//��ǰ�Զ���ɿ�ѡ��Ĺ���
	
	private List<View> views=new ArrayList<View>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		client = new DefaultHttpClient();// ��֤��Ψһ�ĳ�ʼ������

		validateCodeAc = (AutoCompleteTextView) findViewById(R.id.validateCodeAc);
		validateIv = (ImageView) findViewById(R.id.validateCodeIv);
		validateEt = (EditText) findViewById(R.id.validateCodeEt);
		flipper=(ViewFlipper) findViewById(R.id.flipper);
		dialog=new ProgressDialog(this);
		
		// ��ʼ���Զ���ɿ�
		validateCodeAc.setOnItemClickListener(this);
		initDataForValidateAc();

		handler = new MyHandler();// ���½����ϵ����
		getValidateCode();// ��ȡ��֤��
		
		detector=new GestureDetector(this,new MyGestureListener(flipper));//���Ƽ����
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return detector.onTouchEvent(event);
	}

	// �Զ���ɿ�ĵ���¼�������ҳ���ѡ����ʦ�Ĺ���
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

	// Ϊ�Զ���ɿ��������
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

	// ��ѯ��ť���¼�����
	public void query(View v) {
		// ��ѯ������׼��

		String validateCode = validateEt.getEditableText().toString();
		String type = "1";
		String term = "20141";
		dialog.show();
		
		queryCourseByTeacher(currentGh, validateCode, term, type);

	}

	public void changeValidateCode(View v) {
		getValidateCode();
	}
	//����֤��Ĳ�ѯ
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

	// ������ݵ�post��ѯ
	private void queryCourseByTeacher(String gh, String validateCode,
			String term, String type) {
		final HttpPost request = new HttpPost(Constants.QUERY_BY_TEACHER_URL);

		// ==================��ѯ������׼��=========================================

		List<NameValuePair> list = new ArrayList<NameValuePair>();
		NameValuePair XNXQPair = new BasicNameValuePair("Sel_XNXQ", term);// ѧ��
		NameValuePair JSPair = new BasicNameValuePair("Sel_JS", gh);// ��ʦ����
		NameValuePair typePair = new BasicNameValuePair("type", type);
		NameValuePair yzmPair = new BasicNameValuePair("txt_yzm", validateCode);
		list.add(yzmPair);
		list.add(XNXQPair);
		list.add(JSPair);
		list.add(typePair);

		// ==================��ʼ��post����=========================================

		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list);
			request.setEntity(entity);
			request.setHeader("referer",
					"http://gl.sycm.com.cn/Jwweb/ZNPK/TeacherKBFB.aspx");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		//ִ�в�ѯ
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
						msg.getData().putString("exception", "У�������");
					} else if (parser.noCourse()) {
						msg.getData().putString("exception", "����ʦ�޿γ̣�");
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
	
	//������Ϣ�ĵ�����
	private void popDialog(String info){
		AlertDialog.Builder builder = new AlertDialog.Builder(
				MainActivity.this);
		builder.setTitle("��Ҫ��ʾ");
		builder.setMessage(info);
		builder.setPositiveButton("ȷ��", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getValidateCode();
			}
		});
		builder.create().show();
	}
	
	
	
	
	//�Է��� �Ŀγ̱���д���
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
		
		//=============���ظ�����================================
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
		
		// ��listView���������
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
		
		//����ʽ����flipper�У��Ա�֤��ӵ�����������ʾ����
		flipper.removeAllViews();
		for(int i=views.size()-1;i>=0;i--){
			flipper.addView(views.get(i));
		}
		
		getValidateCode();
	}
	
	
	//UI���µ�ȫ������
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
