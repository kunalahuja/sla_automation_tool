package com.example.data;

import java.util.ArrayList;
import java.util.HashMap;

public class SlaInfo {

	private String programName;
	private String availableDate;
	private String programNumber;
	private String link;
	private ArrayList<String> programNames;
	private HashMap<String,String> programNumbers;
	private String supportedPrograms;
	
	public SlaInfo(){
		programNames = new ArrayList<String>();
		programNumbers = new HashMap<String, String>();
	}
	
	public void setAvailableDate(String availableDate) {
		this.availableDate = availableDate;
	}
	
	public String getAvailableDate() {
		return availableDate;
	}
	
	public void setProgramName(String programName) {
		this.programName = programName;
	}
	
	public String getProgramNames() {
		String result = "";
		for(int i=0;i<programNames.size();i++){
			result = result+ programNames.get(i)+"\n";
		}
		return result;
	}
	
	public String getProgramName(int i){
		return programNames.get(i);
	}
	
	public ArrayList<String> getProgramNameList(){
		return programNames;
	}
	
	public void setProgramNumber(String programNumber) {
		this.programNumber = programNumber;
	}
	
	public String getProgramNumber() {
		return programNumber;
	}
	
	public void setLink(String link) {
		this.link = "http://www-03.ibm.com"+link;
	}
	
	public String getLink() {
		return link;
	}
	
	public void adddNameToList(String p_name){
		
		programNames.add(p_name);
		
	}

	public void setSupportedPrograms(String programs) {
		if(programs.contains("<br>")){
			programs =programs.replaceFirst("<br>","");
			programs = programs.replace("<br>", ";");
			programs= programs.replaceAll("&nbsp;", " ");
			programs = programs.trim();
		}
		supportedPrograms = programs;
		System.out.println("Programs set to:: "+programs);
	}
	
	public String getSupportedPrograms() {
		return supportedPrograms;
	}
	
	public void addProgramNumber(String program, String number){
		programNumbers.put(program, number);
	}
	
	public String getProgramNumber(String programName){
		return programNumbers.get(programName);
	}
}
