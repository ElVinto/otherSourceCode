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
public class LinePointsPlotter {

	String csvFileName ;
	// serieNames.get(0) -> xName, serieNames.get(1) serie 1
	ArrayList<String> serieNames;

	String outPlotFolderName;

	String fileSep=System.getProperty("file.separator");

	TreeMap<Double, ArrayList<Double>> raws;

	Double a = null; // y = ax

	Double cnstY = null; // y = cnst

	Double cnstX = null; // x = cnst
	Double fromY = null; // x= cnst from y
	Double toY = null; // x = cnst to y

	boolean plotLinearReg = false;

	String keyLocation =null;


	public LinePointsPlotter(String csvFileName, String outPlotFolderName) throws Exception {
		initSerieNames(csvFileName);
		setOutPlotFolder( outPlotFolderName);
	}

	public LinePointsPlotter(String outPlotFolderName){
		serieNames = new ArrayList<String>();
		setOutPlotFolder(outPlotFolderName);

	}

	private void initSerieNames(String csvFileName) throws IOException{

		BufferedReader reader = new BufferedReader(new FileReader(csvFileName));
		String line = reader.readLine(); // we pass the culumn names
		serieNames = new ArrayList<String>();
		serieNames.addAll(Arrays.asList(line.split(",")));
		reader.close();
		this.csvFileName = csvFileName;
	}

	private void setOutPlotFolder(String outPlotFolderName){
		FileManager.createFolderIfNeeded(outPlotFolderName);
		this.outPlotFolderName = outPlotFolderName;
		raws = new TreeMap<Double, ArrayList<Double>>();
	}

	public void addUnikPointsSerie(
			TreeMap<Double, ArrayList<Double>> points
			,String xName, String serieName, String plotName) throws IOException{


		serieNames.add(xName);
		serieNames.add(serieName);
		csvFileName = outPlotFolderName+plotName+".csv";
		BufferedWriter bw = new BufferedWriter(new FileWriter(csvFileName));

		for(Number x :points.keySet()){
			for(Number y :points.get(x)){
				 bw.write(""+x+","+y+"\n");
			}
		}
		bw.flush();
        bw.close();
	}

	public void addPointSerie(TreeMap<Double, ArrayList<Double>> points
			,String xName, String serieName){

		// TODO rethink about it

	}

	public void plotAxFnct(double a){
		this.a = a;
	}

	public void plotLinRegFnct( boolean plotLinReg){
		plotLinearReg = plotLinReg;
	}

	public void plotCnstYFnct(double cnst){
		this.cnstY = cnst;
	}

	public void plotCnstXFnct(double cnst, double fromY, double toY){
		this.cnstX = cnst;
		this.fromY = fromY;
		this.toY = toY;
	}

	public void setKeyLocation(String loc){
		keyLocation =loc; // e.g.: graph 0.4, graph 1 <=> 0.4*width from left border, top
	}


	public void plot(
			 String plotName
			, String xUnit
			, String yUnit
			, String style
			) throws Exception {


        String title =  plotName.replace("_", " ") ;
        String drawType = style ;

        String terminal =  (System.getProperty("file.separator").equals("/")) ? "postscript color enhanced": "pdf";
        String extension = (System.getProperty("file.separator").equals("/")) ? "eps": "pdf";
        String outfileName = outPlotFolderName  +plotName+"."+extension;

        String cmd = "reset \n" + "set terminal "+terminal+" \n" // postscript enhanced color
                + "set output \'"
                +  relative2AbsolutePath(new File(outfileName))+"\' \n"
//                + "set title \'" + title + "\' \n"
//			+ "set size square \n"
                + "set font  \',20\' \n"
                + "set xtics font \',20\' \n"
                + "set ytics font \',20\' \n"
//                + "set size .8,.8 \n"
                + "set autoscale \n"
                + "set style data " + drawType + " \n"
                + "set key at "+(keyLocation==null?"graph 0.45, graph 1":keyLocation)+" \n"
                + "set key spacing 1.2 font \',22\'\n"
                + "set xlabel \'" + xUnit.replace("Nb", "") + "\' font \',20\' \n"
                + "set ylabel \'" + yUnit + "\' font \',20\' \n"
                + "set grid \n"
                + "set lmargin 5.0 ; \n" //2.7
                + "set rmargin 0; \n"
                + "set bmargin 0; \n"
                + "set tmargin 0; \n"

//			+ "set style line lw 2.0 \n"
//			+ "set style line 3 lt 3 lw 3.0 \n"
			+ "set pointsize 2.0 \n"
				+ "set datafile sep ',' \n"
        		+ ((cnstX!=null)?"set arrow from "+cnstX+","+fromY+" to "+cnstX+","+toY+" nohead lc -1 lw 2 \n":"") ;


        if(plotLinearReg){
        	for(int sIdx=1;sIdx<serieNames.size();sIdx++){
        		cmd=cmd+ "f"+sIdx+"(x) = m*x + b \n"
        				+ "fit f"+sIdx+"(x) \'" + relative2AbsolutePath(new File(csvFileName)) + "\'  using 1:"+(sIdx+1)+" via m,b \n";
        	}
        }

        cmd = cmd+ "plot ";

//        System.out.println(serieNames);


        for(int sIdx=1;sIdx<serieNames.size();sIdx++){

        	int ptStartI= serieNames.contains("SolutionsBenchO")?3:2;
        	int lcStartI= serieNames.contains("SolutionsBenchO")?11:0;

            cmd = cmd + "\'" + relative2AbsolutePath(new File(csvFileName)) + "\' "
                    + " using 1:"+(sIdx+1)
                    +tmpSerieName2Pt(serieNames.get(sIdx))
                    +tmpSerieName2Lt(serieNames.get(sIdx))
                    +" pt " + (ptStartI+2*sIdx)+" lt 1 lw 4 lc "+(lcStartI+(sIdx==2?-1:sIdx))
                 //  + " pt " + (3+sIdx) // point type: 1=+, 2=X, 3=*, 4=square, 5=filled square, 6=circle,  7=filled circle, 8=triangle, 9=filled triangle, etc.
                    + " title \'" + serieName2PretyName(serieNames.get(sIdx)) + "\'" ;

            cmd=cmd+(plotLinearReg? ", f"+sIdx+"(x) lt 1 lw 3 lc "+(3+sIdx)+" ":"");
            if(sIdx<serieNames.size()-1)
            	cmd = cmd + ",  ";
        }

        cmd=cmd+(a!=null? ", "+a+"*x lt 1 lc 3 ":"");
        cmd=cmd+(cnstY!=null? ", "+cnstY+" lt 1 lc 3 lw 2 ":"");



//        cmd = cmd + "\'" + relative2AbsolutePath(new File(csvFileName)) + "\' "
//                + " using 1:"+2
//             //  + " pt " + (3+sIdx) // point type: 1=+, 2=X, 3=*, 4=square, 5=filled square, 6=circle,  7=filled circle, 8=triangle, 9=filled triangle, etc.
//                + " title \'" + plotName + "\'" ;


        // System.out.println(cmd);
        File fPlot = new File(outPlotFolderName  +plotName+ "_"+drawType+".plt");
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

	private String serieName2PretyName(String serieName){
		return serieName.replace("_", "-");
		
	}

	// point type: 1=+, 2=X, 3=*, 4=square, 5=filled square, 6=circle,  7=filled circle, 8=triangle, 9=filled triangle, etc.
	private String tmpSerieName2Pt(String serieName){
		
//		case "M1" :{return " pt 12 ";}
//		case "M2" :{return " pt 6 ";}
//		case "M3" :{return " pt 8 ";}
//		case "M4" :{return " pt 10 ";}
//		case "R_M4" :{return " pt 4 ";}
		
		return "";
	}

	private String tmpSerieName2Lt(String serieName){
		
//		case "M1" :{return " lt 1 lc rgb \'violet\' lw 2.0 ";}
//		case "M2" :{return " lt 1 lc rgb \'black\' lw 2.0 ";}
//		case "M3" :{return " lt 1 lc rgb \'red\' lw 2.0 ";}
//		case "M4" :{return " lt 1 lc rgb \'blue\' lw 2.0 ";}
//		case "R_M4" :{return " lt 1 lc rgb \'green\' lw 2.0 ";}
		
		return "";
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
