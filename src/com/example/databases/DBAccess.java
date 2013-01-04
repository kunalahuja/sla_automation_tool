package com.example.databases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.example.data.SlaInfo;

public class DBAccess {

	static int pk =1;
	
	static  String url = "jdbc:mysql://localhost:3306/";
	static  String dbName = "SlaAutomation";
	static  String driver = "com.mysql.jdbc.Driver";
	static  String userName = "root"; 
	static  String password = "zaq12wsx";
	static  String tableName = "sla_information";
	
	public DBAccess(){
		 System.out.println("MySQL Connect Example.");
      	       	        	 
      	  try {
			Class.forName(driver).newInstance();
			
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      	  
	}
	public void createTables(){
		String createTable = "CREATE TABLE IF NOT EXISTS "+tableName+" (" + "id INT, "
				+ "name TEXT, " + "program_number TINYTEXT, "
				+ "supported TEXT)";
		try {
			Connection conn = DriverManager.getConnection(url + dbName,
					userName, password);
			System.out.println("Connected to the database");
			PreparedStatement s = conn.prepareStatement(createTable);
			s.execute();
			conn.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public void updatePK(){
		String selectMax="SELECT max(id) from "+tableName;
		try {
			Connection conn = DriverManager.getConnection(url + dbName,
					userName, password);
			System.out.println("Connected to the database");
			PreparedStatement s = conn.prepareStatement(selectMax);
			ResultSet rs = s.executeQuery();
			if(rs.next()){
				pk=rs.getInt(1)+1;
			}
			s.close();
			rs.close();
			conn.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	public void saveResults(ArrayList<SlaInfo> p_list) {

		try{
			Connection conn = DriverManager.getConnection(url+dbName+"?useServerPrepStmts=false&rewriteBatchedStatements=true",userName,password);
       	  System.out.println("Connected to the database");
       	  PreparedStatement s = conn.prepareStatement("Insert into "+ tableName + " VALUES(?,?,?,?)");

	
			for (int i = 0; i < p_list.size(); i++) {
				for (int j = 0; j < p_list.get(i).getProgramNameList().size(); j++) {
					String name = p_list.get(i).getProgramNameList().get(j).replace("'", "''");
			
					String supportedPrograms = p_list.get(i)
							.getSupportedPrograms().replace("'", "''");
					System.out.println("supportedPrograms:: "
							+ supportedPrograms);
					String[] supportedList = supportedPrograms.split(";");

					for (int supportCount = 0; supportCount < supportedList.length; supportCount++) {
						System.out.println("pk is:: " + pk);
						System.out.println("supported "
								+ supportedList[supportCount]);
						s.setInt(1, pk);
						s.setString(2, name);
						s.setString(3, p_list.get(i).getProgramNumber(name));
						s.setString(4, supportedList[supportCount]);

						s.addBatch();
						pk++;
					}
				}
			}
			s.executeBatch();

			s.close();
			conn.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
}
