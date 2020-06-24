package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

/*
 * A member of this class parses data csv file having the following form:
 *
 * # optional comment lines starting with the symbol #
 * # yUnit=Unit
 * # ...other optional info
 * xUnit,SerieA,SerieB,SerieC,...
 * 0,34.5,10.9,5.5,
 * 1,3,9,100.0.45,6
 * 2,3.1,0.3,8.3
 * 3,...
 * ...
 */
public class CandleSticksPlotter {

	/*
	 * reorganisation:
	 * 	list of series
	 * 		Each serie has a name a vector of dimensions
	 */



	String xName;
	TreeMap<String,String> serieName2CsvFileName;

	String outPlotFolderName;

	String fileSep=System.getProperty("file.separator");

	public CandleSticksPlotter(String outPlotFolderName){
		serieName2CsvFileName = new TreeMap<String, String>();
		setOutPlotFolder(outPlotFolderName);
	}


	private void setOutPlotFolder(String outPlotFolderName){
		FileManager.createFolderIfNeeded(outPlotFolderName);
		this.outPlotFolderName = outPlotFolderName;
	}

	public void addUnikCandleSerie(
			String xName, String serieName
			,TreeMap<Double,ArrayList<Double>> xVal2Quartiles
			, double xCoeff, double yCoeff,
			 String plotName) throws IOException{

		StringBuffer key2QuartilesTxt = new StringBuffer();
		for(Double k : xVal2Quartiles.keySet() ){
			key2QuartilesTxt.append(""+(k*xCoeff));
			for(Double q : xVal2Quartiles.get(k))
				key2QuartilesTxt.append(","+(q*yCoeff));
			key2QuartilesTxt.append("\n");

		}
		System.out.println(key2QuartilesTxt);

		this.xName = xName;
		String csvFileName = outPlotFolderName+plotName+".csv";
		serieName2CsvFileName.put(serieName, csvFileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(csvFileName));
		bw.write(key2QuartilesTxt.toString());
		bw.flush();
        bw.close();
	}

	public void addCandlesSeries(
			String xName, String yName
			,TreeMap<String,TreeMap<Double,ArrayList<Double>>> serie2XVal2YQuartiles
			, double xCoeff, double yCoeff
			) throws IOException{

		this.xName = xName;

		StringBuffer key2QuartilesTxt = new StringBuffer();
		for(String serieName: serie2XVal2YQuartiles.keySet()){

			for(double k : serie2XVal2YQuartiles.get(serieName).keySet() ){
				key2QuartilesTxt.append(""+(k*xCoeff));
				for(double q : serie2XVal2YQuartiles.get(serieName).get(k))
					key2QuartilesTxt.append(","+(q*yCoeff));
				key2QuartilesTxt.append("\n");

			}
			String plotName = serieName+"_"+xName+"_vs_"+yName+"Quartiles";

			String csvFileName = outPlotFolderName+plotName+".csv";
			serieName2CsvFileName.put(serieName, csvFileName);
			BufferedWriter bw = new BufferedWriter(new FileWriter(csvFileName));
			bw.write(key2QuartilesTxt.toString());
			bw.flush();
	        bw.close();
		}
		System.out.println(key2QuartilesTxt);

	}


	public void plot(
			 String plotName
			, String xUnit
			, String yUnit
			) throws Exception {


        String title =  plotName.replace("_", "-") ;

        String terminal =  (System.getProperty("file.separator").equals("/")) ? "postscript color enhanced": "pdf";
        String extension = (System.getProperty("file.separator").equals("/")) ? "eps": "pdf";
        String outfileName = outPlotFolderName  +plotName+"."+extension;

        String cmd = "reset \n" + "set terminal "+terminal+" \n" // postscript enhanced color
                + "set output \'"
                +  relative2AbsolutePath(new File(outfileName))+"\' \n"
                + "set title \'" + title + "\' \n"
//			+ "set size square \n"
                + "set autoscale \n"
                + "set key left top \n"
                + "set xlabel \'" + xUnit + "\' \n" + "set ylabel \'" + yUnit + "\' \n"
//                + "set grid \n"
                + "set boxwidth 50 \n"
//			+ "set style line 1 lt 1 lw 3.0 \n"
//			+ "set style line 3 lt 3 lw 3.0 \n"
//			+ "set pointsize 0.5 \n"
				+ "set datafile sep ',' \n"
        		;



        cmd = cmd+ "plot ";

        int i=0;
        for(String serieName: serieName2CsvFileName.keySet()){
        	String csvFileName = serieName2CsvFileName.get(serieName);
          cmd = cmd + "\'" + relative2AbsolutePath(new File(csvFileName)) + "\' "
                    + " using 1:3:2:6:5 with candlesticks lt "+(1+i)+" lc "+(3+i)+" lw 2"
                    + " title \'" + serieName.replace("_", "-") + "\' whiskerbars" ;
          cmd = cmd + ",\'" + relative2AbsolutePath(new File(csvFileName)) + "\' "
                    + " using 1:4:4:4:4 with candlesticks lt -1 lc "+(3+i)+" lw 2 notitle";
          cmd = cmd+ ((i<serieName2CsvFileName.size()-1)?", ":"");
          i++;
        }


        // System.out.println(cmd);
        File fPlot = new File(outPlotFolderName  +plotName+ "_candles.plt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(fPlot));
        bw.write(cmd);
        bw.write(System.getProperty("line.separator"));
        bw.close();

        System.out.println("write file: " + fPlot.getPath());
        if (System.getProperty("file.separator").equals("/")){
            Process p =Runtime.getRuntime().exec("/usr/local/bin/gnuplot " + fPlot.getAbsolutePath());
            if(p.waitFor()==0){
            	Runtime.getRuntime().exec("/usr/bin/pstopdf "+ outfileName);
            }
        }else{
            Runtime.getRuntime().exec("gnuplot.exe "
                    + fPlot.getPath());
        }
	}

	private String relative2AbsolutePath(File relativeFileLoc){
 		String absolute = relativeFileLoc.getAbsolutePath();
 		String relative = relativeFileLoc.getPath();
 		absolute = absolute.replace(relative, "");
 		relative =relative.replace("."+File.separator,"" );
 		absolute = absolute+relative;
 		return absolute;
 	}


}
