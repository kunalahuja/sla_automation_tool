package com.example.base;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.example.data.SlaInfo;

public class SLAXmlParser{

	String data;
	SlaInfo slaObject;
	ArrayList<SlaInfo> listOfSlas = new ArrayList<SlaInfo>();
	
	DefaultHandler handler = new DefaultHandler() {
		 
		boolean blink = false;
		boolean blname = false;
		boolean bnname = false;
		boolean bsalary = false;
		boolean bProgamNum = false;
		boolean blinkEnd = false;
		public void startElement(String uri, String localName,String qName, 
	                Attributes attributes) throws SAXException {
	 
//			System.out.println("Start Element :" + qName);
	 
			if (qName.equalsIgnoreCase("TR")) {
				slaObject = new SlaInfo();
			}
	 
			if (qName.equalsIgnoreCase("TH")) {
	
			}
	 
			if (qName.equalsIgnoreCase("A")) {
				
				slaObject.setLink(attributes.getValue("href"));
				blink = true;
			}
	 
			if (qName.equalsIgnoreCase("TD")) {
				if(blinkEnd){
					bProgamNum = true;
				}
			}
	 
		}
	 
		public void endElement(String uri, String localName,
			String qName) throws SAXException {
	 
//			System.out.println("End Element :" + qName);
			if(qName.equals("TR")){
				listOfSlas.add(slaObject);
			}
			if(qName.equals("TH")){
				blinkEnd = true;
			}
		}
	 
		public void characters(char ch[], int start, int length) throws SAXException {
	 
			if (blink) {
				slaObject.adddNameToList(new String(ch, start, length));
				blink = false;
			}
	 
			if(bProgamNum){
				
				String[] progNumbers = new String(ch,start,length).split("<BR />");
				blinkEnd= false;
				bProgamNum = false;
			}
	 
		}
	 
	     };
	
	
	
	public ArrayList<SlaInfo> parseDocument(String p_data) {

		//get a factory
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {

			//get a new instance of parser
			SAXParser sp = spf.newSAXParser();
			
			InputStream stream = new ByteArrayInputStream(p_data.getBytes("UTF-8"));

			//parse the file and also register this class for call backs
			sp.parse(stream,handler);
			
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch (IOException ie) {
			ie.printStackTrace();
		}
		return listOfSlas;	
	}
	

}


