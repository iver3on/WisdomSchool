package com.example.wisdomschool2;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class CourseOfTeacher implements Serializable{
	//老师的部门，名字，职称
	private String teacherInfo;
	//以星期为key的map
	private Map<String, String> course=new LinkedHashMap<String,String>();
	
	public String getTeacherInfo() {
		return teacherInfo;
	}
	public void setTeacherInfo(String teacherInfo) {
		this.teacherInfo = teacherInfo;
	}
	public Map<String, String> getCourse() {
		return course;
	}
	public void setCourse(Map<String, String> course) {
		this.course = course;
	}
	
}