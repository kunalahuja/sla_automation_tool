package com.example.base;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.example.data.LicenseInfo;
import com.example.data.SlaInfo;
import com.example.databases.DBAccess;
import com.thoughtworks.selenium.DefaultSelenium;

public class SlaAutomationTool {

	static ArrayList<String> resultPages = new ArrayList<String>();
	String urlString = "http://www-03.ibm.com/software/sla/sladb.nsf/searchlis/?searchview&searchorder=4&searchmax=0&query=";
	static ArrayList<SlaInfo> list = new ArrayList<SlaInfo>();

	public static void main(String args[]) {

		System.out.println("---------This is SLA Automation Tool-----------");
		String toolUsage= "Proper tool usage: java -jar SlaAutomationTool.jar [GenNewSearchList] | [ProductStartIndex=<Integer>]";

		final String sServerHost = "localhost";
		final int iServerPort = 4444;
		final String sBrowserType = "*firefox";//"*firefox*iexplore"; // For Firefox, use *firefox
		String sBaseUrl = "http://www-03.ibm.com/software/sla/sladb.nsf/displaylis/F5CFEC4EF1EDB47E85257A13007A60B1?OpenDocument";
		String resultsFileName = "listOfResults";
		int pageStartIndex =0;
		///generate search list to file
		if(args.length ==1){
			if(args[0].equalsIgnoreCase("GenNewSearchList")){
				generateSearchList(resultsFileName);
			}else if(args[0].startsWith("ProductStartIndex=")){
				String[] params =args[0].split("=");
				if(params.length==2){
					pageStartIndex = Integer.parseInt(params[1]);
					System.out.println("Starting from page index = "+ pageStartIndex);
				}else{
					System.out.println(toolUsage);
					return;
				}
			}
		}
		//read search list results from file 
		getListOfPagesFromFile(resultsFileName);
		
		//start selenium instance
		DefaultSelenium oDefaultSelenium = new DefaultSelenium(
				sServerHost, iServerPort, sBrowserType, sBaseUrl);
		oDefaultSelenium.start(); // Start Selenium.
		
		//initiate DB access
		DBAccess databaseHelper = new DBAccess();
		
		databaseHelper.createTables();
		
		Date timestart= new Date();

		for (int pageCounter = pageStartIndex; pageCounter < resultPages.size(); pageCounter++) {
			System.out.println("Start Time: "+timestart +" Current Time: "+new Date());
			System.out
					.println("--------------------------------------------SearchPageCounter: "
							+ pageCounter+"  out of "+resultPages.size());
			String result = resultPages.get(pageCounter);
			// System.out.println("Page Is: " + result);

			SLAXmlParser parser = new SLAXmlParser();
			// String result = resultPages.get(0);
		//	System.out.println("result:: " + result);
			if (result.indexOf("<TBODY") != -1) {
				//Get list of links for particular SLAs
				list = parser
						.parseDocument(result.substring(
								result.indexOf("<TBODY"),
								result.indexOf("/TBODY>") + 7));
				System.out.println("Total number of results:: " + list.size());
				
				for (int programCounter = 0; programCounter < list.size(); programCounter++) {
					System.out.println("Name: " + programCounter + 1 + " "
							+ list.get(programCounter).getProgramNames());
					System.out.println("Link: " + list.get(programCounter).getLink());

					sBaseUrl = list.get(programCounter).getLink();
					String value = "";
					
					//Navigate through license search
					try {
						try {
							openLicensePage(sBaseUrl, oDefaultSelenium);

						} catch (Exception ex) {
							// in case timeout exception first time, repeat open
							// sequence
							openLicensePage(sBaseUrl, oDefaultSelenium);

						}
					} catch (Exception ex) {
						System.out.println("Unable to oprn URL..skipping to next one");
						continue;
					}

					//Get the result HTML
					value = oDefaultSelenium.getHtmlSource();
					System.out.println(new Date());	
					//Find Program numbers for each product
					for (String name : list.get(programCounter).getProgramNameList()) {
						
						String pnValue = findProgramNumber(value, name);
						
						list.get(programCounter).addProgramNumber(name, pnValue);
					}

					int startIndex = value
							.indexOf("The following are Supporting Programs licensed with the Program");
					// System.out.println("value is:: "+value);
					if (startIndex > 0) {

						String value1 = value.substring(startIndex);
						// System.out.println("value is------------------ :"+value1);
						String programs = value1.substring(0,
								value1.indexOf("<br><br>"));
						// System.out.println("Programs:: "+programs);
						list.get(programCounter).setSupportedPrograms(programs);
					} else {
						System.out.println("No Supporting Programs!!! ");
						list.get(programCounter).setSupportedPrograms(
								"No Supporting Programs!!! ");
						
						saveLeftover(list.get(programCounter).getProgramName(0), value);
						
					}
				}
				
				databaseHelper.saveResults(list);
			} else {
				System.out.println("no licenses");
			}
		}
		oDefaultSelenium.stop();
	}

	private static String findProgramNumber(String pageSource,
			String productName) {
		String pnValue ="";
		//create pattern to search for program Numbers plus clean the pattern to avoid compile errors
		String pattern = productName.replace("*", "").replace(";", "") + "<br>Program Number:\\s*.{4}-.{3}<br>";
		try {
			Pattern pnPattern = Pattern.compile(pattern);

			Matcher matcher = pnPattern.matcher(pageSource);

			if (matcher.find()) {
				String[] splitedMatch = matcher.group()
						.split("Program Number:");
				if (splitedMatch.length > 0) {
					pnValue = splitedMatch[1].replaceAll("<br>", "")
							.replaceAll("\\s", "");
				}
				
			}
		} catch (PatternSyntaxException ex) {
			System.out
					.println("Pattern problem... skipping this produt number");
		}
		return pnValue;
	}

	private static void generateSearchList(String fileName) {
		ArrayList<LicenseInfo> searchList = null;
		PLCXmlParser xpb = new PLCXmlParser();
		FileInputStream stream = null;
		System.out.println("----------------------------Generating new search list--------------------");
		try {
			stream = new FileInputStream(new File("PLCWeeklyXMLDownload.xml"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
					fc.size()); /* Instead of using default, pass in a decoder. */
			String result = Charset.defaultCharset().decode(bb).toString();

			searchList = xpb.parseDocument(result);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		 * for loop to get html of search list
		 * 
		 */

		for (int count = 0; count < searchList.size(); count++) {
			try {
				String urlString = "http://www-03.ibm.com/software/sla/sladb.nsf/searchlis/?searchview&searchorder=4&searchmax=0&query=";
				String key = searchList.get(count).getName()
						.replace(" ", "%20");
				 urlString = urlString.concat("(" + key + ")");
			

				System.out.println("key:: " + key);
				System.out.println("++++++++++++++++" + urlString);
				String result = getResponseForUrl(urlString);
				resultPages.add(result);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)));
			for(String result : resultPages){
				writer.write(result);
				writer.newLine();
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void getListOfPagesFromFile(String fileName) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line =null;
			while((line =reader.readLine())!=null){
				 resultPages.add(line);
			}

			System.out.println("-----------------Finished reading search list------------------");
			reader.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void saveLeftover(String name, String fileContent) {
		try {
			
			String fileName=name.replaceAll(" ", "_").replaceAll("\\*", "").replaceAll("\\\\","_").replaceAll("/", "_").replaceAll(",","_").replaceAll(";","_");
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File("leftovers/"+ fileName + ".html")));
			writer.write(fileContent);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void openLicensePage(String sBaseUrl,
			DefaultSelenium oDefaultSelenium) {
		oDefaultSelenium.open(sBaseUrl);
		oDefaultSelenium.waitForPageToLoad("30000");

		oDefaultSelenium.click("ibm-submit");
		
		oDefaultSelenium.waitForPageToLoad("30000");
	}

	public static void doPost() {
		URL url;
		try {
			url = new URL(
					"http://www-03.ibm.com/software/sla/sladb.nsf/displaylis/F5CFEC4EF1EDB47E85257A13007A60B1?OpenDocument&amp;Seq=1");
			HttpURLConnection httpCon = (HttpURLConnection) url
					.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("POST");
			OutputStreamWriter out = new OutputStreamWriter(
					httpCon.getOutputStream());
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					httpCon.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			rd.close();

			httpCon.disconnect();
			out.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String getResponseForUrl(String p_urlString) {
		String result = "";
		try {
			URL url = new URL(p_urlString);
			// System.out.println("url is:: "+url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			if (conn.getResponseCode() != 200) {
				// System.out.println("response:: "+conn.getResponseMessage());
				throw new IOException(conn.getResponseMessage());
			}

			// Buffer the result into a string
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			rd.close();

			conn.disconnect();
			result = sb.toString();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}
