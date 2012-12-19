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

import com.example.data.LicenseInfo;
import com.example.data.SlaInfo;

public class PLCXmlParser {

		String data;
		LicenseInfo licenseInfoObject;
		ArrayList<LicenseInfo> listOfLicenseInfo = new ArrayList<LicenseInfo>();
		
		DefaultHandler handler = new DefaultHandler() {
			 
			boolean blink = false;
			boolean blname = false;
			boolean bnname = false;
			boolean bsalary = false;
		 
			public void startElement(String uri, String localName,String qName, 
		                Attributes attributes) throws SAXException {
		 
//				System.out.println("Start Element :" + qName);
		 
				if (qName.equalsIgnoreCase("SWTitle")) {
					licenseInfoObject = new LicenseInfo();
					licenseInfoObject.setNumber(attributes.getValue("synKey"));
					blink = true;
				}
		 
				if (qName.equalsIgnoreCase("TH")) {
		
				}
		 
				if (qName.equalsIgnoreCase("A")) {
					
				}
		 
				if (qName.equalsIgnoreCase("TD")) {
					
				}
		 
			}
		 
			public void endElement(String uri, String localName,
				String qName) throws SAXException {
		 
//				System.out.println("End Element :" + qName);
				if(qName.equals("SWTitle")){
					listOfLicenseInfo.add(licenseInfoObject);
				}
			}
		 
			public void characters(char ch[], int start, int length) throws SAXException {
		 
				if (blink) {
					licenseInfoObject.setName(new String(ch, start, length));
					blink = false;
				}
		 
				if (blname) {
					System.out.println("Last Name : " + new String(ch, start, length));
					blname = false;
				}
		 
				if (bnname) {
					System.out.println("Nick Name : " + new String(ch, start, length));
					bnname = false;
				}
		 
				if (bsalary) {
					System.out.println("Salary : " + new String(ch, start, length));
					bsalary = false;
				}
		 
			}
		 
		     };
		
		
		
		public ArrayList<LicenseInfo> parseDocument(String p_data) {

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
			return listOfLicenseInfo;	
		}
		

	}




