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
	
	//��֤���������
	public boolean validateError(){
		 boolean flag=false;
		
		//�����Ϊ�쳣���
		if(body.text().trim().length()==0){
			//��֤�����
			if(script.data().startsWith("alert")){
				flag=true;
			}
		}
		
		return flag;
	}
	
	//û�пγ̵����
	public boolean noCourse(){
		
		 boolean flag=false;
		//�����Ϊ�쳣���
		if(body.text().trim().length()==0){
			//��֤�����
			if(!(script.data().startsWith("alert"))){
				flag=true;
			}
			
		}
		return flag;
	}
	
	//�пε����
	public CourseOfTeacher getCourse(){
		CourseOfTeacher teacher=new CourseOfTeacher();
		//��ʦ��Ϣ
		String teacherInfo=body.getElementsByTag("table").get(2).text();
		teacher.setTeacherInfo(teacherInfo);
		
		//��ȡ�α���Ϣ
		Elements trs=body.getElementsByTag("table").get(3).child(0).children();
		Element weekTr=trs.get(0);//����һ����..���ڵ���
		
		//�ӵ�2�п�ʼ������
		for(int i=1;i<trs.size();i++){
			
			Element tr=trs.get(i);
			Elements tds=tr.children();
			int tdCount=tds.size();
			
			//��Ϊ�ǲ�����������������Ĵ����ֶΣ���������еĿ�ʼ��
			int j=tdCount-7;
			//������,����7��
			for(;j<tdCount;j++){
				//weekTr�ж�λ�����ڼ�
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
