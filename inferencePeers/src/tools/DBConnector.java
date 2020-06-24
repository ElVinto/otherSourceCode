package tools;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import main.ArgsHandler;
public class DBConnector {

	/**
	 * @param args
	 */
	
	String _dbRoot = "mysql";
	String _userRoot = "root";
	String _passRoot = "arm_vins";
	
	String _dbName = "db";
	String _dbUser = "arm_vins";
	String _dbPassword = "arm_vins";
	String _dbServerName = "localhost";
	int _dbServerPort = 3306;
	
	
	public DBConnector (String db) throws Exception{
		_dbName = db;
		init();
	}
	
	
	public DBConnector(String db, String user, String password, String server, int serverPort )throws Exception{
		_dbName = db;
		_dbUser= user;
		_dbPassword = password;
		_dbServerName = server;
		_dbServerPort = serverPort;
		init();
	}
	
	
	public DBConnector(HashMap<String,Object> dbParams) throws Exception{
		_dbName = (String) (dbParams.get("dbName")!=null?dbParams.get("dbName"):_dbName);
		_dbUser=(String) (dbParams.get("dbUser")!=null?dbParams.get("dbUser"):_dbUser);
		_dbPassword =(String) (dbParams.get("dbPassword")!=null?dbParams.get("dbPassword"):_dbPassword);
		_dbServerName = (String) (dbParams.get("dbServerName")!=null?dbParams.get("dbServerName"):_dbServerName);
		_dbServerPort =(Integer) (dbParams.get("dbServerPort")!=null?dbParams.get("dbServerPort"):_dbServerPort);
		init();
	}
	
	private void init() throws Exception{
		if(!dbExists(_dbName)){
			createDataBase(_dbName);
			createUser(_dbName, _dbUser, _dbPassword);
		}else
			if(!userExists(_dbUser))
				createUser(_dbName, _dbUser, _dbPassword);
	}
	
	public  boolean dbExists(String dbName){
		try{
			
			Connection con = createConnection(_dbRoot,_userRoot,_passRoot);
			
			Statement stmt = con.createStatement();
			
			// This statement return -1 if the db contains the tab, 0 if not or the number of rows
			int result =stmt.executeUpdate("SHOW DATABASES"
					+" LIKE '"+dbName+"'");
			
			con.close();
			return result==-1;
			}catch(Exception e){
				e.printStackTrace();
			}
			return false;
	}
	
	public  boolean userExists(String userName){
		try{
			
			Connection con = createConnection(_dbRoot,_userRoot,_passRoot);
			
			Statement stmt = con.createStatement();
			
			// This statement return -1 if the db contains the tab, 0 if not or the number of rows
			ResultSet result =stmt.executeQuery("SELECT * FROM mysql.user where user ='"+userName+"'");
			
			boolean resultIsnotEmpty =  result.next();
			con.close();
			return resultIsnotEmpty;
			}catch(Exception e){
				e.printStackTrace();
			}
			return false;
	}
	
	public  boolean tabExists(String tabName){
		
		try{
		
		Connection con = createConnection(_dbName,_dbUser,_dbPassword);
		Statement stmt = con.createStatement();
		
		// This statement return -1 if the db contains the tab, 0 if not or the number of rows
		int result =stmt.executeUpdate("SHOW TABLES FROM "+_dbName
				+" LIKE '"+tabName+"'");
		
		con.close();
		return result==-1;
		
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	
	private  Connection createConnection(String db, String user, String password) throws Exception{
	
		//Register the JDBC driver for MySQL.
		Class.forName("com.mysql.jdbc.Driver");

		//Define URL of database server for
		// database named mysql on the localhost
		// with the default port number 3306.
		String url =
			"jdbc:mysql://"+_dbServerName+":"+_dbServerPort+"/"+db;

		//Get a connection to the database for a
		// user named root with a blank password.
		// This user is the default administrator
		// having full privileges to do anything.
		Connection con =
			DriverManager.getConnection(
					url,user, password);
		//Get a Statement object
		
		return con;
	}
	
	private Connection defaultDBConnection() throws Exception{
		return createConnection(_dbName, _dbUser, _dbPassword);
	}
	
	public  void createDataBase(String db){
		try {
			Connection con = createConnection(_dbRoot,_userRoot,_passRoot);
			Statement stmt = con.createStatement();

		      //Create the new database
		      stmt.executeUpdate(
		                       "CREATE DATABASE "+db+";");
		    
		      
		      
		      con.close();
		      
		    }catch( Exception e ) {
		      e.printStackTrace();
		    }//end catch
	}
	
	public  void createUser(String db, String user, String password){
		try {
			Connection con = createConnection(_dbRoot,_userRoot,_passRoot);
			Statement stmt = con.createStatement();

		      //Register a new user named user on the
		      // database named JunkDB with a password
		      // drowssap enabling several different
		      // privileges.
		      stmt.executeUpdate(
		          "GRANT SELECT,INSERT,UPDATE,DELETE," +
		          "CREATE,ALTER,DROP " +
		          "ON "+db+".* TO '"+user+"'@'localhost' " +
		          "IDENTIFIED BY '"+password+"';");
		      con.close();
		      
		    }catch( Exception e ) {
		      e.printStackTrace();
		    }//end catch
	}
	
	public  void deleteDatabase(String db, String user){
		try {
			 Connection con = createConnection(_dbRoot,_userRoot,_passRoot);
			Statement stmt = con.createStatement();

		      //Remove the user named auser
		      stmt.executeUpdate(
		          "REVOKE ALL PRIVILEGES ON *.* " +
		          "FROM '"+user+"'@'localhost'");
		      stmt.executeUpdate(
		          "REVOKE GRANT OPTION ON *.* " +
		          "FROM '"+user+"'@'localhost'");
		      stmt.executeUpdate(
		          "DELETE FROM mysql.user WHERE " +
		          "User='"+user+"' and Host='localhost'");
		      stmt.executeUpdate("FLUSH PRIVILEGES");

		      //Delete the database
		      stmt.executeUpdate(
		                       "DROP DATABASE "+db);

		      con.close();
		    }catch( Exception e ) {
		      e.printStackTrace();
		    }//end catch

	}
	
	public static String obj2DBType(Object o){
		if( o instanceof Integer || o instanceof Boolean)
			return "INT";
		if(o instanceof Double ||o instanceof Long || o instanceof Float )
			return "DECIMAL(20,3)";
		if(o instanceof String  )
			return "VARCHAR(200)";
		
		return null;
	}
	
	public static Object DBType2Obj(Object o){
		if(o instanceof BigDecimal)
			return (BigDecimal)o;
		if(o instanceof Integer)
			return (Integer)o;
		if(o instanceof String)
			return (String)o;
		
		return null;
		
	}
	
	
	public static String  mysqlFunct(String fnct, String par){
		if(fnct.equals("avg"))
			return "AVG("+par+")";
		if(fnct.equals("min"))
			return "MIN("+par+")";
		if(fnct.equals("max"))
			return "MAX("+par+")";
		if(fnct.equals("sum"))
			return "SUM("+par+")";
		if(fnct.equals("count"))
			return "COUNT("+par+")";
		if(fnct.equals("all"))
			return par;
		return null;
	}
	

	public  boolean colExists(String colName, String colType, String tabName){
		
		try{
		
		Connection con = defaultDBConnection();
		Statement stmt = con.createStatement();

		
		// This statement return -1 if the db contains the tab, 0 if not or the number of rows
		int result =stmt.executeUpdate("SHOW COLUMNS FROM "+tabName
				+ " FROM "+_dbName
				+" WHERE Field ='"+colName+"'"
				+" AND Type LIKE '%"+colType+"%'");
		
		
		con.close();
		return result==-1;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	public  ArrayList<String> colsFromTab( String tabName){
		
		ArrayList<String> cols = new ArrayList<String>();
		try{
		
		Connection con = defaultDBConnection();
		Statement stmt = con.createStatement();

		
		// This statement return -1 if the db contains the tab, 0 if not or the number of rows
		int queryOk =stmt.executeUpdate("SHOW COLUMNS FROM "+tabName
				+ " FROM "+_dbName);
		
		if(queryOk>0){
			ResultSet rs = stmt.getResultSet();
			while (rs.next())
				cols.add(rs.getString("Field"));
		}
		
		
		
		con.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return cols;
	}
	
	
	public  void createTable(String tableName,
			HashMap<String,Object> nameValRow){
		try{
		
		Connection con = defaultDBConnection();
		Statement stmt = con.createStatement();
		
		String colScript = "CREATE  TABLE `"+_dbName
		+"`.`"+tableName+"` ( id INTEGER NOT NULL AUTO_INCREMENT,";
		for(String colName : nameValRow.keySet()){
			colScript+= colName+ " "+obj2DBType(nameValRow.get(colName))+",";
		}
		//colScript = (String)colScript.subSequence(0, colScript.length()-1);
		colScript+="PRIMARY KEY (id))";		
		
		System.out.println(colScript);
		
		stmt.executeUpdate(colScript);
		
		con.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public  void addColIfNeeded(String tabName,
			HashMap<String,Object> nameValRow){
		try{

	
		Connection con = defaultDBConnection();
		Statement stmt = con.createStatement();
		
		for(String colName : nameValRow.keySet()){
			if(!colExists(colName,
					obj2DBType(nameValRow.get(colName)),tabName)){
				String colScript = "  ALTER  TABLE "+_dbName
				+"."+tabName+" ADD "+colName+ " "
				+ obj2DBType(nameValRow.get(colName));
				
				//System.out.println(colScript);
				
				stmt.executeUpdate(colScript);
			}
		}
		con.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	// modification à apporter pour la prise en compte de tables annexes
	// règle le shema de la base de donnée est une forêt d'arbres enracinés
	// le schema de la base est se retrouve la relation tabSchema(Father,Son, CommonCols).
	
	public boolean insertLinkBetween(String parent, String son){
		boolean inserted = false;
		HashMap<String, Object> nameValRow = new HashMap<String, Object>();
		nameValRow.put("parent",parent);
		nameValRow.put("son",son);
		if(!tabExists("schemaTab")){
			createTable("schemaTab", nameValRow);
		}
		
		String query = "SELECT id FROM schemaTab WHERE parent='"+parent+"' AND son='"+son+"'";
//		System.out.println(query);
		try {
			
			Connection con = defaultDBConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if(rs.next())
				inserted= false;
			else{
				insertRow("schemaTab",nameValRow);
				inserted= true;
			}
		}catch( Exception e ) {
		      e.printStackTrace();
		}//
		return inserted;
	}
	
	/*
	 * To insert a row in a table tab has a father fat,
	 * dbSchemaTab should contain (fat,son, CommonCols)
	 */
	public void insertRefRow(String tabName,HashMap<String,Object> nameValRow, String tabParent)
	{
		try {
			
			if(!tabExists(tabParent))
				return;
			
			insertLinkBetween(tabParent,tabName);
			
			ArrayList<String> colsInCommon = colsFromTab(tabParent);
			colsInCommon.retainAll(nameValRow.keySet());
			
			String idQuery = "SELECT id FROM "+tabParent+" WHERE";
			
			for(String col :colsInCommon ){
				if(nameValRow.get(col) instanceof String)
					idQuery += " AND "+col+"='"+nameValRow.get(col)+"'";
				else
					idQuery += " AND "+col+"="+nameValRow.get(col);
			}
			idQuery=idQuery.replaceFirst(" AND", "");
			
//			System.out.println(idQuery);
			
			Connection con = defaultDBConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(idQuery);
			ArrayList<Integer> idsRef = new ArrayList<Integer>();
			while(rs.next())
				idsRef.add(rs.getInt("id"));
			
			for(Integer idRef : idsRef){
				nameValRow.put("parent", idRef);
				insertRow(tabName,nameValRow);
			}
			
		
		}catch( Exception e ) {
		      e.printStackTrace();
		}//end catch
	}
	
	public  void insertRow(String tabName,
			HashMap<String,Object> nameValRow){
		try {

			if(!tabExists(tabName)){
				createTable(tabName, nameValRow);
			}else{
				addColIfNeeded(tabName, nameValRow);
			}
			
			Connection con = defaultDBConnection();
			Statement stmt = con.createStatement();
			String colsNames = "(";
			String colsValues = "(";
			for(String colName : nameValRow.keySet()){
				colsNames+=colName+",";
				if(obj2DBType(nameValRow.get(colName)).contains("VARCHAR"))
					colsValues+="'"+ nameValRow.get(colName)+"',";
				else
					colsValues+= nameValRow.get(colName)+",";
			}
			colsNames = (String)colsNames.subSequence(0, colsNames.length()-1);
			colsNames+=")";
			colsValues = (String)colsValues.subSequence(0, colsValues.length()-1);
			colsValues+=")";	
			
			String insertScript ="INSERT INTO "+_dbName
			+"."+tabName+" "+ colsNames
			+ " VALUES "+ colsValues;
			
			// System.out.println(insertScript);
			
			stmt.executeUpdate(insertScript);
		
			con.close();
		}catch( Exception e ) {
		      e.printStackTrace();
		}//end catch
	}
	
	public  ResultSet select(String query)throws Exception{

				Connection con = defaultDBConnection();
				Statement stmt = con.createStatement();
				
				// This statement return -1 if the db contains the tab, 0 if not or the number of rows
				return stmt.executeQuery(query);				
				
	}
	
	
	
	
	
	
	public static void main(String args[]) throws Exception{
		
		HashMap<String, Object> dbParams = new HashMap<String, Object>();
		dbParams.put("dbName", "dbT");
		dbParams.put("dbUser",  "arm_vins");
		dbParams.put("dbPassword", "arm_vins");

		
		DBConnector dbc = new DBConnector (dbParams);
		
		// dbc.deleteDatabase("dbTest", "arm_vins");
		
		//System.out.println(dbc.colsFromTab( "benchStat"));
		
		
//		dbc.createUser("db2", "arm_vins", "arm_vins");
		
//		System.out.println( "test dbExist "+ (dbc.dbExists("db")==true) ) ;
//		System.out.println( "test !dbExist "+ (dbc.dbExists("db1")==false) ) ;
//		System.out.println( "test userExist "+ (dbc.userExists("arm_vins")==true) ) ;
//		System.out.println( "test !userExist "+ (dbc.userExists("arm_vins1")==false) ) ;
//		System.out.println( "test tabExist "+ (dbc.tabExists("table3")==true) ) ;
//		System.out.println( "test !tabExist "+ (dbc.tabExists("notAtable")==false) ) ;
//		
		HashMap<String,Object> row = new HashMap<String, Object>();
		row.put("col1", 1);	row.put("col2", "un");
		dbc.insertRow("tablePrinc", row);
		row.put("col1", 2);	row.put("col2", "deux");
		dbc.insertRow("tablePrinc", row);
		row.put("col1", 2);	row.put("col2", "two");
		dbc.insertRow("tablePrinc", row);
		
		row.put("col1", 1);	row.put("col2", "un"); row.put("col3", "a");
		dbc.insertRefRow("tableRef1", row,"tablePrinc");
		row.put("col1", 2);	row.put("col2", "deux");row.put("col3", "b");
		dbc.insertRefRow("tableRef1", row,"tablePrinc");
		
		
//		
//		ResultSet rs = dbc.select(" Select * From db2.table1 where col1=2");
//		
//		while(rs.next()){
//			System.out.println("col1->"+ rs.getInt(1)+" col2->"+ rs.getString(2));
//		}
		
		
		
		
	 }


}
