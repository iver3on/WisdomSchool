package com.example.wisdomschool2;

import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ByTeacherParser {
	private Document doc;
	private Element body;
	private Element script;
	public ByTeacherParser(Document doc){
		this.doc=doc;
		body=doc.getElementsByTag("body").get(0);
		script=doc.getElementsByTag("script").get(0);
	}
	
	//验证码错误的情况
	public boolean validateError(){
		 boolean flag=false;
		
		//此情况为异常情况
		if(body.text().trim().length()==0){
			//验证码错误
			if(script.data().startsWith("alert")){
				flag=true;
			}
		}
		
		return flag;
	}
	
	//没有课程的情况
	public boolean noCourse(){
		
		 boolean flag=false;
		//此情况为异常情况
		if(body.text().trim().length()==0){
			//验证码错误
			if(!(script.data().startsWith("alert"))){
				flag=true;
			}
			
		}
		return flag;
	}
	
	//有课的情况
	public CourseOfTeacher getCourse(){
		CourseOfTeacher teacher=new CourseOfTeacher();
		//老师信息
		String teacherInfo=body.getElementsByTag("table").get(2).text();
		teacher.setTeacherInfo(teacherInfo);
		
		//获取课表信息
		Elements trs=body.getElementsByTag("table").get(3).child(0).children();
		Element weekTr=trs.get(0);//星期一，二..所在的行
		
		//从第2行开始处理行
		for(int i=1;i<trs.size();i++){
			
			Element tr=trs.get(i);
			Elements tds=tr.children();
			int tdCount=tds.size();
			
			//因为是不规则表格，所以有特殊的处理手段，来处理各行的开始列
			int j=tdCount-7;
			//处理列,都是7列
			for(;j<tdCount;j++){
				//weekTr中定位是星期几
				int dayOfWeek=j-tdCount+7+1;
				
				String ss=tr.child(j).text().trim();
				
				if(ss.length()!=0){
					String week=weekTr.child(dayOfWeek).text();
					String temp=teacher.getCourse().get(week);
					if(temp!=null){
						temp=temp+"\n";
						teacher.getCourse().put(week, temp+ss);
					}
					else{
						teacher.getCourse().put(week,ss);
					}
				}
			}
			
		}
		
		
		return  teacher;
	}
	
}
