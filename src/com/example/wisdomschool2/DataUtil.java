package com.example.wisdomschool2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DataUtil {
	private static List<String> ghs=new ArrayList<String>();
	private static List<String> names=new ArrayList<String>();
	
	public static void init(InputStream in){
		Document doc=null;
		try {
			doc=Jsoup.parse(in, "gbk","");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Elements options=doc.getElementsByTag("option");
		System.out.println("parse.....");
		for(int i=0;i<options.size();i++){
			Element option=options.get(i);
			String gh=option.attr("value");
			String name=option.text().trim();
			ghs.add(gh);names.add(name);
		}
		
	}
	
	public static List<String> getGhs() {
		return ghs;
	}

	public static List<String> getNames() {
		return names;
	}

}


















