package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class DBViewer {

	public static ResultSet selectXYFromTab(String x, String y, String tabName,
			String cond, String yfnct, DBConnector dbc) {

		ResultSet rs = null;
		try {

			String selectExpr = "";
			String yAxe = (yfnct.isEmpty()) ? y : DBConnector.mysqlFunct(yfnct,
					y);
			selectExpr = "SELECT " + x + ", " + yAxe;

			String whereExpr = "";
			if (!cond.isEmpty())
				whereExpr = "WHERE " + cond;

			String groupByExpr = "";
			if ((yfnct != null))
				groupByExpr = "GROUP BY " + x;

			System.out.println(selectExpr + " FROM " + tabName + " "
					+ whereExpr + " " + groupByExpr);
			rs = dbc.select(selectExpr + " FROM " + tabName + " " + whereExpr
					+ " " + groupByExpr);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return rs;
	}

	// SELECT tightness, gammaBA, avg(timeSat) FROM `benchstat20`.`benchstat20`
	// WHERE bench='GBAGDVT' Group By tightness, gammaBA;
	public static ResultSet selectXYZFromTab(String x, String y, String z,
			String tabName, String cond, String zfnct, DBConnector dbc) {

		ResultSet rs = null;
		try {

			String selectExpr = "";
			String zAxe = (zfnct.isEmpty()) ? z : DBConnector.mysqlFunct(zfnct,
					z);
			selectExpr = "SELECT " + x + ", " + y + ", " + zAxe;

			String whereExpr = "";
			if (!cond.isEmpty())
				whereExpr = "WHERE " + cond;

			String groupByExpr = "";
			groupByExpr = "GROUP BY " + x + ", " + y;

			System.out.println(selectExpr + " FROM " + tabName + " "
					+ whereExpr + " " + groupByExpr);
			rs = dbc.select(selectExpr + " FROM " + tabName + " " + whereExpr
					+ " " + groupByExpr);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return rs;
	}

	public static File save3DResultSet(ResultSet rs, ArrayList<String> colNames,
			String outFileName) throws Exception {
		File res = new File(outFileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(res));
		boolean firstValHasChanged = false;
		Object firstVal = null;
		while (rs.next()) {
			for (int iCol=0; iCol< colNames.size(); iCol++ ){
				if(firstVal ==null)
					firstVal = rs.getObject(colNames.get(iCol));
				if(iCol==0){
					if(!firstVal.equals(rs.getObject(colNames.get(iCol)))){
						firstVal = rs.getObject(colNames.get(iCol));
						firstValHasChanged = true;
					}
				}
				if(firstValHasChanged){
					bw.write("\n");
					firstValHasChanged = false;
				}
				bw.write(rs.getObject(colNames.get(iCol)) + " ");
			}
			
			bw.write("\n");
		}
		bw.close();
		return res;
	}
	
	
	public static File saveResultSet(ResultSet rs, ArrayList<String> colNames,
			String outFileName) throws Exception {
		File res = new File(outFileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(res));
		while (rs.next()) {
			for (int iCol=0; iCol< colNames.size(); iCol++ ){
				bw.write(rs.getObject(colNames.get(iCol)) + " ");
			}
			bw.write("\n");
		}
		bw.close();
		return res;
	}

	public static String remApostrophe(String s) {
		return s.replaceAll("'", "");
	}

	public static void writeDefault2DLinesPoints(String x, String y,
			String cond, String yfunc, HashMap<String, File> curveFiles,
			File outFolder) throws Exception {

		x = remApostrophe(x);
		y = remApostrophe(y);
		cond = remApostrophe(cond);
		yfunc = remApostrophe(yfunc);

		String title = "(" + yfunc + " " + y + " by " + x 
			+ ")"+ (cond.isEmpty()?cond:(" with " + cond));
		String drawType = yfunc.isEmpty() ? "points" : "lines";

		String cmd = "reset \n" + "set terminal post eps \n" + "set output \'"
				+ outFolder.getPath() + File.separator + "draw.eps\' \n"
				+ "set title \'" + title + "\' \n" 
				+ "set autoscale \n"
				+ "set style data " + drawType + " \n" 
				+ "set key below \n"
				+ "set xlabel \'" + x + "\' \n" + "set ylabel \'" + yfunc + " "
				+ y + "\' \n" + "plot ";

		for (String curveName : curveFiles.keySet()) {
			cmd = cmd + "\'" + curveFiles.get(curveName).getPath() + "\' "
					+ " using 1:2 " + " title \'"
					+ curveName.replaceAll("'", "") + "\'" + ", ";
		}
		cmd = cmd.substring(0, cmd.lastIndexOf(","));

		System.out.println(cmd);

		File fPlot = new File(outFolder.getPath() + File.separator
				+ "linePoints.plot");
		BufferedWriter bw = new BufferedWriter(new FileWriter(fPlot));
		bw.write(cmd);
		bw.write("\n");
		bw.close();

		Runtime.getRuntime().exec(
				"C:/Program Files/gnuplot/binary/wgnuplot.exe "
						+ fPlot.getPath());
	}

	public static void writeDefault3DPlots(String x, String y, String z,
			String cond, String zfunc, HashMap<String, File> curveFiles,
			File outFolder) throws Exception {

		x = remApostrophe(x);
		y = remApostrophe(y);
		z = remApostrophe(z);
		cond = remApostrophe(cond);
		zfunc = remApostrophe(zfunc);

		String title = "(" + zfunc + " " + z + " by " + y + " by " + x
				+ ")"+ (cond.isEmpty()?cond:(" with " + cond));
		String drawType = zfunc.isEmpty() ? "points" : "lines";

		String cmd = "reset \n" + "set terminal post eps \n" + "set output \'"
				+ outFolder.getPath() + File.separator + "draw.eps\' \n"
				+ "set title \'" + title + "\' \n"
				+ "set style data "+ drawType + " \n" 
				+ "set autoscale \n" 
				+ "set key below \n"
				+ "set xlabel \'" + x + "\' \n" 
				+ "set ylabel \'" + y + "\' \n"
				+ "set zlabel \'" + zfunc + " " + z + "\' \n" + "splot ";

		for (String curveName : curveFiles.keySet()) {
			cmd = cmd + "\'" + curveFiles.get(curveName).getPath() + "\' "
					+ " using 1:2:3 " + " title \'"
					+ curveName.replaceAll("'", "") + "\'" + ", ";
		}
		cmd = cmd.substring(0, cmd.lastIndexOf(","));

		System.out.println(cmd);

		File fPlot = new File(outFolder.getPath() + File.separator
				+ "linePoints.plot");
		BufferedWriter bw = new BufferedWriter(new FileWriter(fPlot));
		bw.write(cmd);
		bw.write("\n");
		bw.close();

		Runtime.getRuntime().exec(
				"C:/Program Files/gnuplot/binary/wgnuplot.exe "
						+ fPlot.getPath());

	}

	/*
	 * From a hierarchy of var=val folders witch contains a set of .stat files
	 * creates a database named by the root folder, and a table witch colums are
	 * named by var and contain values val.
	 */
	public static void createDBFromFolder(String folderName) throws Exception {
		File d = new File(folderName);
		if (!d.isDirectory())
			return;
		DBConnector dbc = new DBConnector(d.getName());
		for (File sd : d.listFiles())
			createTable(sd, new HashMap<String, Object>(), dbc, d.getName());
	}

	public static void createTable(File statDir, HashMap<String, Object> row,
			DBConnector dbc, String tabName) throws Exception {

		// concerne internal node in the statDir Hierarchy
		if (statDir.isDirectory()) {
			// System.out.println(" dir level "+statDir.getName());
			String var = setVarValueIn(statDir.getName(), row);
			if (var == null)
				System.out.println(" The folder named " + statDir
						+ "has not been taked into account for the view");
			for (File d : statDir.listFiles()) {
				createTable(d, row, dbc, tabName);
			}
			if (var != null)
				row.remove(var);

		} else {
			// System.out.println(" File level "+statDir.getName());
			if (statDir.getName().contains(".stat")
					&& !statDir.getName().contains(".svn")) {
				// concerne internal node in the stat file

				ArrayList<String> insertedVars = new ArrayList<String>();
				BufferedReader r = new BufferedReader(new FileReader(statDir));
				String line = r.readLine();
				while (line != null) {
					String var = setVarValueIn(line, row);
					if (var != null)
						insertedVars.add(var);
					line = r.readLine();
				}
				r.close();

				// System.out.println(" Insert a row in  tab");
				dbc.insertRow(tabName, row);
				for (String var : insertedVars)
					row.remove(var);

			}

		}

	}

	public static String setVarValueIn(String varVal,
			HashMap<String, Object> row) {

		if (!varVal.contains("="))
			return null;

		String var = varVal.split("=")[0].trim();
		String val = varVal.split("=")[1].trim();

		if (val.matches("[-+]?\\d+"))
			row.put(var, Integer.parseInt(val));
		else if (val.matches("[-+]?\\d*\\.\\d+"))
			row.put(var, Double.parseDouble(val));
		else
			row.put(var, val);
		return var;
	}

	private static ArrayList<String> getSubConds(String curveCond,
			DBConnector dbc, String tabName) throws Exception {

		ArrayList<String> result = new ArrayList<String>();

		if (curveCond.contains("all")) {
			curveCond = curveCond.replaceAll("=all", "").trim();
			System.out.println("SELECT " + curveCond + " FROM " + tabName
					+ " where .... ");
			ResultSet rs = dbc.select("SELECT " + curveCond + " FROM "
					+ tabName + " WHERE " + curveCond
					+ " IS NOT NULL GROUP BY " + curveCond);
			while (rs.next()) {
				if (rs.getObject(curveCond) instanceof String) {
					if (!rs.getObject(curveCond).equals(null))
						result.add(curveCond + "='" + rs.getString(curveCond)
								+ "'");
					System.out.println(rs.getObject(curveCond));
				} else {
					if (!rs.getObject(curveCond).equals(null))
						result.add(curveCond + "=" + rs.getString(curveCond));
					System.out.println(rs.getObject(curveCond));
				}
			}
		} else {
			for (String subCond : curveCond.split(" "))
				result.add(subCond);
		}
		return result;
	}

	// drawCurves "bench=all" nbPeers, timeSat, avg, density=0.7 ,db,tab
	public static void drawCurves(String curveScript, String x, String y,
			String yfnct, String otherCond, String dbName, String tabName,
			String dirViewName) throws Exception {

		int ind = 0;
		File outFolder = new File(dirViewName + File.separator + x + "BY" + y
				+ "-v" + ind);
		while (outFolder.exists()) {
			ind++;
			outFolder = new File(dirViewName + File.separator + x + "BY" + y
					+ "-v" + ind);
		}
		if (!outFolder.mkdirs())
			throw new Exception("Does not succeed to create outFolder");

		DBConnector dbc = new DBConnector(dbName);

		ArrayList<String> axes = new ArrayList<String>();
		axes.add(x);
		axes.add(DBConnector.mysqlFunct(yfnct, y));

		HashMap<String, File> curveFiles = new HashMap<String, File>();

		int indCurve = 0;
		ArrayList<String> curveSubConds = new ArrayList<String>();
		for (String curveCond : curveScript.split(",")) {
			if (curveSubConds.isEmpty()) {
				curveSubConds.addAll(getSubConds(curveCond, dbc, tabName));
				continue;
			}
			ArrayList<String> temp = new ArrayList<String>();
			for (String subCond1 : getSubConds(curveCond, dbc, tabName))
				for (String subCond2 : curveSubConds)
					temp.add(subCond1 + " and " + subCond2);
			curveSubConds.clear();
			curveSubConds.addAll(temp);
		}

		for (String curveSubCond : curveSubConds) {
			String cond = curveSubCond;
			cond += otherCond.isEmpty() ? "" : " AND " + otherCond;
			ResultSet rs = selectXYFromTab(x, y, tabName, cond, yfnct, dbc);
			File dat = saveResultSet(rs, axes, outFolder.getPath()
					+ File.separator + "curve" + indCurve + ".dat");
			curveFiles.put(curveSubCond, dat);
			indCurve++;
		}

		writeDefault2DLinesPoints(x, y, otherCond, yfnct, curveFiles, outFolder);

	}

	// drawCurves "bench=all" nbPeers, timeSat, avg, density=0.7 ,db,tab
	public static void draw3DCurves(String curveScript, String x, String y,
			String z, String zfnct, String otherCond, String dbName,
			String tabName, String dirViewName) throws Exception {

		int ind = 0;
		File outFolder = new File(dirViewName + File.separator + x + "BY" + y
				+ "BY" + z + "-v" + ind);
		while (outFolder.exists()) {
			ind++;
			outFolder = new File(dirViewName + File.separator + x + "BY" + y
					+ "BY" + z + "-v" + ind);
		}
		if (!outFolder.mkdirs())
			throw new Exception("Does not succeed to create outFolder");

		DBConnector dbc = new DBConnector(dbName);

		ArrayList<String> axes = new ArrayList<String>();
		axes.add(x);
		axes.add(y);
		axes.add(DBConnector.mysqlFunct(zfnct, z));

		HashMap<String, File> curveFiles = new HashMap<String, File>();

		int indCurve = 0;
		ArrayList<String> curveSubConds = new ArrayList<String>();
		for (String curveCond : curveScript.split(",")) {
			if (curveSubConds.isEmpty()) {
				curveSubConds.addAll(getSubConds(curveCond, dbc, tabName));
				continue;
			}
			ArrayList<String> temp = new ArrayList<String>();
			for (String subCond1 : getSubConds(curveCond, dbc, tabName))
				for (String subCond2 : curveSubConds)
					temp.add(subCond1 + " and " + subCond2);
			curveSubConds.clear();
			curveSubConds.addAll(temp);
		}

		for (String curveSubCond : curveSubConds) {
			String cond = curveSubCond;
			cond += otherCond.isEmpty() ? "" : " AND " + otherCond;
			ResultSet rs = selectXYZFromTab(x, y, z, tabName, cond, zfnct, dbc);
			File dat = save3DResultSet(rs, axes, outFolder.getPath()
					+ File.separator + "curve" + indCurve + ".dat");
			curveFiles.put(curveSubCond, dat);
			indCurve++;
		}

		writeDefault3DPlots(x, y, z, otherCond, zfnct, curveFiles, outFolder);

	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		  createDBFromFolder("C:/Users/VinTo/workspace/expe150411/benchStat20v2");

		// Runtime.getRuntime().exec("C:/Program Files/gnuplot/binary/wgnuplot.exe "+
		// "C:/Users/VinTo/workspace/benchStatView/viewBenchNbPeersNbTemp/curves.plot");

		 drawCurves("bench='GBAGDVT',gammaBA=all","tightness", "satStatus",
		 "avg","",
		 "benchStat20v2", "benchStat20v2",
		 "C:/Users/VinTo/workspace/expe150411/benchStatView");
		
		 drawCurves("bench='GBAGDVT',gammaBA=all","tightness", "timeSat",
		 "avg","",
		 "benchStat20v2", "benchStat20v2",
		 "C:/Users/VinTo/workspace/expe150411/benchStatView");
		
		
//		 drawCurves("bench='GBAGDVT',gammaBA=all","tightness", "timeGen",
//		 "avg","",
//		 "benchStat20v0", "benchStat20v0",
//		 "C:/Users/VinTo/workspace/expe150411/benchStatView");
		
		//
		 drawCurves("bench='UDGDVT',density=all","tightness", "satStatus",
		 "avg","",
		 "benchStat20v0", "benchStat20v0",
		 "C:/Users/VinTo/workspace/expe150411/benchStatView");
		
		 drawCurves("bench='UDGDVT',density=all","tightness", "timeSat",
		 "avg","",
		 "benchStat20v0", "benchStat20v0",
		 "C:/Users/VinTo/workspace/expe150411/benchStatView");
		//
		// drawCurves("bench='UDGDVT',density=all","tightness", "timeGen",
		// "avg","",
		// "benchStat20", "benchStat20",
		// "C:/Users/VinTo/workspace/expe150411/benchStatView");
		//
		//
		 drawCurves("bench='WSGDVT',neighIncr=all","tightness", "satStatus",
		 "avg","",
		 "benchStat20v2", "benchStat20v2",
		 "C:/Users/VinTo/workspace/expe150411/benchStatView");
		
		 drawCurves("bench='WSGDVT',neighIncr=all","tightness", "timeSat",
		 "avg","",
		 "benchStat20v2", "benchStat20v2",
		 "C:/Users/VinTo/workspace/expe150411/benchStatView");
		
//		 drawCurves("bench='WSGDVT',neighIncr=all","tightness", "timeGen",
//		 "avg","",
//		 "benchStat20v1", "benchStat20v1",
//		 "C:/Users/VinTo/workspace/expe150411/benchStatView");
		//

		
		
		draw3DCurves("bench='GBAGDVT'", "tightness", "gammaBA","satStatus",
				"avg", "", 
				"benchStat20v2", "benchStat20v2",
				"C:/Users/VinTo/workspace/expe150411/benchStatView");

		draw3DCurves("bench='GBAGDVT'", "tightness","gammaBA", "timeSat",
				"avg", "",
				"benchStat20v2", "benchStat20v2",
				"C:/Users/VinTo/workspace/expe150411/benchStatView");

//		draw3DCurves("bench='GBAGDVT'", "tightness","gammaBA","timeGen",
//				"avg", "", "benchStat20", "benchStat20",
//				"C:/Users/VinTo/workspace/expe150411/benchStatView");
//
		draw3DCurves("bench='UDGDVT'", "tightness","density", "satStatus",
				"avg", "", 
				"benchStat20v0", "benchStat20v0",
				"C:/Users/VinTo/workspace/expe150411/benchStatView");

		draw3DCurves("bench='UDGDVT'", "tightness","density", "timeSat", "avg",
				"", 
				"benchStat20v0", "benchStat20v0",
				"C:/Users/VinTo/workspace/expe150411/benchStatView");
//
//		draw3DCurves("bench='UDGDVT'", "tightness","density", "timeGen", "avg",
//				"", "benchStat20", "benchStat20",
//				"C:/Users/VinTo/workspace/expe150411/benchStatView");
//
		draw3DCurves("bench='WSGDVT'", "tightness","neighIncr", "satStatus",
				"avg", "",
				"benchStat20v2", "benchStat20v2",
				"C:/Users/VinTo/workspace/expe150411/benchStatView");

		draw3DCurves("bench='WSGDVT'", "tightness","neighIncr", "timeSat",
				"avg", "",
				"benchStat20v2", "benchStat20v2",
				"C:/Users/VinTo/workspace/expe150411/benchStatView");
//
//		draw3DCurves("bench='WSGDVT'", "tightness","neighIncr", "timeGen",
//				"avg", "", "benchStat20", "benchStat20",
//				"C:/Users/VinTo/workspace/expe150411/benchStatView");

	}

}
