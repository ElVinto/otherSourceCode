package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import main.ArgsHandler;
import main.DAsFullNetworkLauncher;

import propositionalLogic.Base;
import specificException.InvalidArgumentException;
import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;



public class PlotMaker {

	
	/*
	 * 
	 * 
	 * -dirRes /users/iasi/varmant/experiments/baBench_results/  
-benchDir  /users/iasi/varmant/experiments/baBench
-title "Time performance" 
-axes nbPeers totalTime
-gContraints  nbClsByVars:3.1
-pConstraints M3DJ algo=M3DJ 
-pConstraints M2DT algo=M2DT  
-select totalTime  med
-networkMetric totalTime med

	 * 
	  	java -jar makeView.jar -dirRes /users/iasi/varmant/experiments/ba_10_results/  
		-benchDir  /users/iasi/varmant/experiments/baBench
		-title "Time performance" 
		-axes nbPeers totalTime
		-gContraints  nbClsByVars:3.1
		-pConstraints M3DJ algo=M3DJ 
		-pConstraints M2DT algo=M2DT  
		-select totalTime  med
		-networkMetric totalTime med
	 */

	 

	public static V_Constraint PRECISION = new V_Constraint("precision","");
	public static V_Constraint RECALL = new V_Constraint("recall","");
	public static V_Constraint FMEASURE = new V_Constraint("fmeasure","");
	public static V_Constraint FirstDIAGTIME = new V_Constraint("firstDiagTime","");
	public static  V_Constraint []SPE_CONST = {PRECISION,RECALL,FMEASURE, FirstDIAGTIME};
	
	
	

	public static void main(String[] args) throws Exception {
		
//		String [] nvArgs = {"-summary","/users/iasi/varmant/workspace/inferencePeers/wsDebug5_305.gen",
//				"-dirView","/users/iasi/varmant/workspace/inferencePeers", 
//					"-title","genWSDebug5_305",};
//		args = nvArgs;
		
		
		for(String a : args)
			System.out.print(a+" ");
		System.out.println();
		
		
		if(ArgsHandler.tabContainsExpr(args,"-defaultView")){
				ArrayList<ArrayList<String>> algosAndParams = DAsFullNetworkLauncher.getAlgosAndParams(args);
				ArrayList<String> algos = new ArrayList<String>();
				for(ArrayList<String> algoParams : algosAndParams){
					algos.add(DAsFullNetworkLauncher.algoAndParams2DirName(algoParams));
				}
				makeDefaultView(algos,args);
				return;
		}
		
		if(ArgsHandler.tabContainsExpr(args, "-summary")){
			makeSummaryView(args);
			return ;
		}
		
		File dirResult = getDirResult(args);
		File dirView = getDirView(args);

		ArrayList<V_Constraint> axes = getConstraints("axes",args);

		ArrayList<V_Constraint> gConstraints = getConstraints("gConstraints",args);
		
		ArrayList<V_Select> selectMeasures  = getSelectMeasure(args);

		String selectedAxis = getSelectedAxis(args);
		
		String title = getTitle(args);

		TreeMap<String, ArrayList<V_Constraint>> plotConstraints = getPlotConstraints(args);
		TreeMap<V_Constraint, ArrayList<V_Select>> networkM = getNetWorkMetric(args);
		TreeMap<String,File> plotFile = new TreeMap<String, File>();
		
		ArrayList<String> plotAxes = new ArrayList<String>();
		String [] refStyle = new String[1];
		
		for(String cName: plotConstraints.keySet()){
			
			ArrayList<String>  nvAxesOrder = new ArrayList<String>();
			plotConstraints.get(cName).addAll(gConstraints);
			axes = getConstraints("axes",args);
			ArrayList<ArrayList<String>> tab = 
				builtArrayFrom(dirResult, axes, plotConstraints.get(cName), nvAxesOrder, networkM);
			
			
		
			
//			System.out.println("Tab from instances ");
//			for(ArrayList<String>line : tab)
//				System.out.println(line);
//			System.out.println(" nvAxesOrder: "+nvAxesOrder);
			
			ArrayList<ArrayList<String>> tabRes =restrictTabWith(tab,nvAxesOrder, selectMeasures,selectedAxis,refStyle);

			plotAxes = tabRes.get(0);
//			System.out.println("Tab with Metrics");
//			for(ArrayList<String>line : tabRes){
//				System.out.println(line);
//			}

			File data = new File(dirView.getAbsolutePath()+File.separator+cName+".dat");
			printTable(tabRes,data);
			plotFile.put(cName, data);

		}
		if(plotAxes.size()==2 && selectMeasures.size()==1){
			boolean inv =plotAxes.get(1).contains(axes.get(0).dim);
			
			File fPlot = new File(dirView.getAbsolutePath()+File.separator+"linePoints.plot");
			printDefault2DLinesPoints(plotAxes,gConstraints,fPlot,plotFile,inv,refStyle[0]);
		}
	}
	
	

	private static void makeSummaryView(String [] args) throws Exception{
		
		File s = getSummaryFile(args);
		File dirView = getDirView(args);
		
		
		BufferedReader br = new BufferedReader(new FileReader(s));
		String line = br.readLine();
		//TreeMap<String,ArrayList<ArrayList<String>>> ratio2Tab = new TreeMap<String,ArrayList<ArrayList<String>>>();
		TreeMap<String,TreeMap<Integer,ArrayList<Double>>> ratio2Tab = 
			new TreeMap<String,TreeMap<Integer,ArrayList<Double>>>();
		
		while(line!= null){
			String [] sp1 = line.split(" ");
			if(sp1.length != 3)
				break;
			
			// store time
			double time = Double.valueOf(sp1[2].trim());
			double sign = time <=0? -1:1;
			time = (time<=0 )?180000:time;
			time= time*sign;
			// store nbattemps
			double nbAttempts =sign* Double.valueOf(sp1[1]);
			//System.out.println("sp1[1] "+sp1[1].trim()+" "+nbAttempts );
			
			String [] sp2 = sp1[0].split("/");
			// store ratio
			String ratio = sp2[2];
			if(!ratio2Tab.containsKey(ratio))
				ratio2Tab.put(ratio, new TreeMap<Integer,ArrayList<Double>>());
			// store nbPeers
			int nbPeers = Integer.valueOf(sp2[1].split("=")[1]);
			if(!ratio2Tab.get(ratio).containsKey(nbPeers))
				ratio2Tab.get(ratio).put(nbPeers, new ArrayList<Double> ());
			
			ratio2Tab.get(ratio).get(nbPeers).add(nbAttempts);
			
			ratio2Tab.get(ratio).get(nbPeers).add(time);
			
			
			line = br.readLine();
		}
		br.close();
		
		// "ba_100/nbPeers:31/nbClsBynbVars:4.9/instance:2 1392 -1";
		
		TreeMap<String,String> ratio2FName = new TreeMap<String,String>();
		for(String r : ratio2Tab.keySet()){
			File fRatio = new File(dirView.getPath()+File.separator+r+".dat");
			ratio2FName.put(r, fRatio.getPath());
			BufferedWriter bw = new BufferedWriter(new FileWriter(fRatio));
			
			for( Integer nbPeers : ratio2Tab.get(r).keySet()){
				for(int i = 0;i<ratio2Tab.get(r).get(nbPeers).size()/2;i+=2){
					String l = ""+nbPeers+ " "+ratio2Tab.get(r).get(nbPeers).get(i)+" "
					+ratio2Tab.get(r).get(nbPeers).get(i+1);
					bw.write(l+"\n");
					}
			}
			bw.close();
		}
		
		// plotFile.get();
		
		printSummary(dirView,s.getName(),ratio2FName,"time");
		printSummary(dirView,s.getName(),ratio2FName,"nbAttempts");
		
	}
	
	private static void printSummary(File dirView,
			String fName,TreeMap<String,String> plotFile,String y) throws Exception{
		// ecrire le tab dans dir View
		String cmd = 
			"reset \n"+ 
			"set terminal png \n"+
			"set output \""+ dirView.getPath()+File.separator+"sum"+fName+y+".png\" \n"+
			"set title "+"\'"+y+" for building an instance\' "+"\n"+
			"set autoscale \n"+
			"set style data lines \n"+
			"set key below \n" +
			"set xlabel \'nbPeers\' \n"+
			"set ylabel \'"+y+"\' \n"+
			"plot ";
		
		String colOrder = y.equals("time")?"1:3":"1:2";
		for(String curveName : plotFile.keySet()){
			cmd= cmd+"\'"+plotFile.get(curveName)+"\'"+" using "+colOrder+" title \'"+curveName+"\'"+", ";
		}
		String res =cmd.substring(0,cmd.lastIndexOf(","));
		
		
		File plot = new File(dirView.getAbsolutePath()+File.separator+fName+y+".plt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(plot));
		bw.write(res);
		bw.write("\n");
		bw.close();
		
		Runtime.getRuntime().exec("gnuplot "+plot.getAbsolutePath());
		
	}
	
	private static File getSummaryFile(String[]args)throws Exception{
		String summary="";
		int iSummary = ArgsHandler.indexOf("-summary",args);
		if(iSummary != -1){
			summary = ArgsHandler.paramFrom(args,iSummary+1);
		}
		File res = new File(summary);
		if(!res.exists())
			throw new Exception("The summary file does not exists");
		return res;
	}
	
	public static void makeDefaultView(ArrayList<String> values, String []args) throws Exception{
		
		ArrayList<String> defCmds = new ArrayList<String>();
		
		
		
		defCmds.add(defCmd("nbPeers","nbVarsAtEnd","max","med","algo", values));
		defCmds.add(defCmd("nbPeers","nbVarsAtEnd","max","moy","algo", values));
		// defCmds.add(defCmd("nbPeers","nbVarsAtEnd","max","all","algo", values));

		defCmds.add(defCmd("nbPeers","nbVarsAtEnd","moy","med","algo", values));
		defCmds.add(defCmd("nbPeers","nbVarsAtEnd","moy","moy","algo", values));
		// defCmds.add(defCmd("nbPeers","nbVarsAtEnd","moy","all","algo", values));
		
		
		defCmds.add(defCmd("nbPeers","nbSharedAtEnd","max","med","algo", values));
		defCmds.add(defCmd("nbPeers","nbSharedAtEnd","max","moy","algo", values));
		// defCmds.add(defCmd("nbPeers","nbSharedAtEnd","max","all","algo", values));
		
		defCmds.add(defCmd("nbPeers","nbSharedAtEnd","moy","med","algo", values));
		defCmds.add(defCmd("nbPeers","nbSharedAtEnd","moy","moy","algo", values));
		// defCmds.add(defCmd("nbPeers","nbSharedAtEnd","max","all","algo", values));
		
		
		defCmds.add(defCmd("nbPeers","nbSentMsg","max","med","algo", values));
		defCmds.add(defCmd("nbPeers","nbSentMsg","moy","med","algo", values));
//		defCmds.add(defCmd("nbPeers","nbSentMsg","max","all","algo", values));
		
		
		defCmds.add(defCmd("nbPeers","treeTime","max","med","algo", values));
		defCmds.add(defCmd("nbPeers","treeTime","moy","med","algo", values));
//		defCmds.add(defCmd("nbPeers","treeTime","max","all","algo", values));
		
//		defCmds.add(defCmd("nbPeers","firstDiagTime","","moy","algo",values));
//		defCmds.add(defCmd("nbPeers","firstDiagTime","","med","algo",values));
//		defCmds.add(defCmd("nbPeers","firstDiagTime","","all","algo",values));
//		
//		defCmds.add(defCmd("nbPeers","moyTime2SSent","max","med","algo", values));
//		defCmds.add(defCmd("nbPeers","moyTime2SSent","moy","med","algo", values));
//		defCmds.add(defCmd("nbPeers","moyTime2SSent","max","all","algo", values));
//		
//		defCmds.add(defCmd("nbPeers","maxTime2SSent","max","med","algo", values));
//		defCmds.add(defCmd("nbPeers","maxNbRImplicantsStored","moy","med","algo", values));
//		
//		defCmds.add(defCmd("nbPeers","maxNbRImplicantsStored","max","med","algo", values));
//		defCmds.add(defCmd("nbPeers","maxNbRImplicantsStored","moy","med","algo", values));
//		defCmds.add(defCmd("nbPeers","maxNbRImplicantsStored","all","all","algo", values));
//		
//		defCmds.add(defCmd("nbPeers","nbNbRImplicantsStoredAtEnd","max","med","algo", values));
//		defCmds.add(defCmd("nbPeers","nbNbRImplicantsStoredAtEnd","moy","med","algo", values));
//		defCmds.add(defCmd("nbPeers","nbNbRImplicantsStoredAtEnd","all","all","algo", values));
//
//		
//		defCmds.add(defCmd("nbPeers","moyTime2SSent","moy","moy","algo", values));
//		defCmds.add(defCmd("nbPeers","maxTime2SSent","max","med","algo", values));
//		
//		defCmds.add(defCmd("nbPeers","dpllTime","max","med","algo", values));
//		defCmds.add(defCmd("nbPeers","dpllTime","moy","med","algo", values));
//		defCmds.add(defCmd("nbPeers","dpllTime","max","all","algo", values));
//		
//		
//		defCmds.add(defCmd("nbPeers","waitingTime","max","med","algo", values));
//		defCmds.add(defCmd("nbPeers","waitingTime","moy","med","algo", values));
//		defCmds.add(defCmd("nbPeers","waitingTime","max","all","algo", values));
//		
		defCmds.add(defCmd("nbPeers","workingTime","max","med","algo", values));
		defCmds.add(defCmd("nbPeers","workingTime","moy","med","algo", values));
//		defCmds.add(defCmd("nbPeers","workingTime","max","all","algo", values));
//		
		defCmds.add(defCmd("nbPeers","liveTime","max","med","algo", values));
		defCmds.add(defCmd("nbPeers","liveTime","moy","med","algo", values));
//		
//		defCmds.add(defCmd("nbPeers","workByLiveTime","max","med","algo", values));
//		defCmds.add(defCmd("nbPeers","workByLiveTime","moy","med","algo", values));
//		defCmds.add(defCmd("nbPeers","workByLiveTime","max","all","algo", values));
//		
//		defCmds.add(defCmd("nbPeers","waitByLiveTime","max","med","algo", values));
//		defCmds.add(defCmd("nbPeers","waitByLiveTime","moy","med","algo", values));
//		defCmds.add(defCmd("nbPeers","waitByLiveTime","max","all","algo", values));
//		
//		
//		defCmds.add(defCmd("nbPeers","sumProductTime","max","med","algo", values));
//		defCmds.add(defCmd("nbPeers","sumProductTime","moy","med","algo", values));
//		
//		defCmds.add(defCmd("nbPeers","maxProductTime","max","med","algo", values));
//		defCmds.add(defCmd("nbPeers","maxProductTime","moy","med","algo", values));
//		defCmds.add(defCmd("nbPeers","maxProductTime","max","all","algo", values));
//		
//		defCmds.add(defCmd("nbPeers","maxProductTime","max","med","algo", values));
//		defCmds.add(defCmd("nbPeers","maxProductTime","moy","med","algo", values));
//		
//		defCmds.add(defCmd("nbPeers","productivity","max","med","algo", values));
//		defCmds.add(defCmd("nbPeers","productivity","moy","med","algo", values));
//		defCmds.add(defCmd("nbPeers","productivity","max","all","algo", values));
//		
//		defCmds.add(defCmd("nbPeers","avProductTime","max","med","algo", values));
//		defCmds.add(defCmd("nbPeers","avProductTime","moy","med","algo", values));
//		defCmds.add(defCmd("nbPeers","avProductTime","max","all","algo", values));
		
		
		

		
		//		defCmds.add(defCmd("totalTime","","all",args));

				

//		defCmds.add(defCmd("precision","","med"));
//		defCmds.add(defCmd("recall","","med"));
//		defCmds.add(defCmd("fmeasure","","med"));

		String [] args2 = remExprFrom("-defaultView",args);
		for(String cmd : defCmds){
			String[] nvArgs = union(args2,cmd.split(" "));
			main(nvArgs);
		}
	}
	
	private static String createPConstr(String constraintName,ArrayList<String> values ){
		String res = "";
		for(String value: values){
			res+="-pConstraints "+value+" "+constraintName+"="+value+" ";
		}
		return res;
	}
	
	private static String defCmd(String x,String y, String netMetric, String globMetric,
			String pConstr,ArrayList<String> values) throws Exception{
		
		String netOpt = netMetric.equals("")? "":"-networkMetric "+y+" "+netMetric+" ";
		
		String res =
			"-axes "+x+" "+y+" "+
			"-select "+y+" "+globMetric+" "+
			netOpt;
			
		res+=createPConstr(pConstr, values);
		
		res+= "-title "+y+globMetric+netMetric;
		
		return res;
	}
	
	private static String[] remExprFrom  (String expr,String [] args){
		String [] args2 = new String [args.length-1];
		if(ArgsHandler.tabContainsExpr(args,expr)){
			int step =0;
			for(int i=0; i< args.length; i++)
				if(!args[i].equals(expr))
					args2[i-step]=args[i];
				else
					step=1;
		}
		return args2;
	}
	
	
	public static String [] union(String [] tab1,String[] tab2){
		String [] res = new String [tab1.length+tab2.length];
		System.arraycopy(tab1, 0,res,0,tab1.length);
		System.arraycopy(tab2, 0, res, tab1.length,tab2.length);
		return res;
	}
	
	
	private static void printDefault2DLinesPoints  (
			ArrayList<String> axes,
			ArrayList<V_Constraint> gConstraints,
			File plot, TreeMap<String, File> plotFile, 
			boolean inv, String type)throws Exception {
		
		int x = inv? 1:0;
		int y = inv? 0:1;
		
		String title =axes.get(y)+"/"+ axes.get(x);
		title+="(";
		for(V_Constraint gC :gConstraints)
			title+=gC+" ";
		title+=")";
		title ="\'"+title+"\'";


		String cmd = 
			"reset \n"+ 
			"set terminal gif \n"+
			"set output \'"+ (plot.getParent()).replace("."+File.separator+".."+File.separator, "")
				+File.separator+"draw.gif\' \n"+
			"set title "+title +" \n"+
			"set autoscale \n"+
			"set style data "+type+" \n"+
			"set key below \n" +
			"set xlabel \'"+axes.get(x)+"\' \n"+
			"set ylabel \'"+axes.get(y)+"\' \n"+
			"plot ";
		
		
		 System.out.println("wgnuplot "+plot.getAbsolutePath().replace("."+File.separator+".."+File.separator, ""));
		
		String colOrder = ""+(x+1)+":"+(y+1);
		for(String curveName : plotFile.keySet()){
			 
			cmd= cmd+"\'"+plotFile.get(curveName).getAbsolutePath().replace("."+File.separator+".."+File.separator, "")
				+"\' "+" using "+colOrder+" "+" title \'"+curveName+"\'"+", ";
		}
		String res =cmd.substring(0,cmd.lastIndexOf(","));
		
		
		System.out.println(res);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(plot.getAbsolutePath().replace("."+File.separator+".."+File.separator, "")));
		bw.write(res);
		bw.write("\n");
		bw.close();
		
		if(System.getProperty("line.separator").equals("\r\n"))
			Runtime.getRuntime().exec("C:/Program Files/gnuplot/binary/wgnuplot_pipes.exe "+plot.getAbsolutePath().replace("."+File.separator+".."+File.separator, ""));
		else
			Runtime.getRuntime().exec("gnuplot "+plot.getAbsolutePath().replace("."+File.separator+".."+File.separator, ""));

	}


	private static ArrayList<ArrayList<String>> restrictTabWith(ArrayList<ArrayList<String>> tab,
			ArrayList<String> nvAxesOrder,
			ArrayList<V_Select> selectMeasure,
			String selectedAxis, String[] refStyle){

		
		ArrayList<ArrayList<String>> tabRes;
		int iSelect = 0;
		for(int i= 0;i< nvAxesOrder.size();i++){
			iSelect=i;
			if(nvAxesOrder.get(i).contains(selectedAxis))
				break;
		}
		
//		System.out.println(" iSelect: "+iSelect);
		
		if(!selectMeasure.contains(V_Select.ALL)){
			refStyle[0] ="lines";

			//				System.out.println("nvAxesOrder "+nvAxesOrder);
			//				for(ArrayList<String> line :tab)
			//					System.out.println( line);
			//		System.out.println(" iselct "+iSelect+" name " +selectedAxis + " ");

			tabRes = removeSelectFrom(tab,iSelect);
			//		System.out.println(" tab  "+selectedAxis+tabRes);
			tabRes= sortArray(tabRes);

			for(ArrayList<String> lRes : tabRes ){
				ArrayList<String> selectedVal = getSelectValues(tab,iSelect,lRes);
				//			System.out.println(" selectedVal for "+ lRes + " -> "+selectedVal);
				lRes.addAll(getMeasuresFrom(selectMeasure,selectedVal));
			}

		}else{
			refStyle[0] ="points";
			tabRes = tab;
		}
		
		
		tabRes.add(0,headColumn(nvAxesOrder, iSelect, selectMeasure));
		return tabRes;
	}
	
	private static ArrayList<ArrayList<String>> sortArray(ArrayList<ArrayList<String>> tab){
		SortedMap <Double,Integer> tmSorted= new TreeMap<Double,Integer>();
	
		for( ArrayList<String> lTab : tab){
			double d=-1;
			try{ d=Double.valueOf(lTab.get(0));
			}catch(Exception ex){return tab; }
			tmSorted.put(d, tab.indexOf(lTab));
		}
		
		ArrayList<ArrayList<String>> aSorted = new ArrayList<ArrayList<String>>();
		for(Integer ind :tmSorted.values())
			aSorted.add(tab.get(ind));
		return aSorted;
		
	}

	private static ArrayList<String> headColumn (ArrayList<String> nvAxesOrder,int  iselectedAxis, ArrayList<V_Select>  selectMeasure ){
		ArrayList<String> headColumn = new ArrayList<String>();
		headColumn.addAll(nvAxesOrder);
		
		String selectedAxis = headColumn.remove(iselectedAxis);
		for(V_Select v_s: selectMeasure ){
			String nv = selectedAxis+"_"+v_s.toString();
			if(!headColumn.contains(nv))
				if(!selectMeasure.contains(V_Select.ALL)){
					headColumn.add(nv);
				}else{
					headColumn.add(iselectedAxis,nv);
				}
		}
		return headColumn;
	}


	private static ArrayList<String>  getMeasuresFrom(ArrayList<V_Select> selectMeasure,ArrayList<String> values){
		ArrayList<String> res = new ArrayList<String>();
		V_Measure m = new V_Measure(values);
		for(V_Select v_s : selectMeasure){
			String s = "";
			for(double d : m.getSelected(v_s))
				s=s+d+" ";
			res.add(s);
		}

		return res;
	}


	@SuppressWarnings("unchecked")
	private static ArrayList<ArrayList<String>> removeSelectFrom(ArrayList<ArrayList<String>> tab,int i){
		ArrayList<ArrayList<String>> tRes = new ArrayList<ArrayList<String>> ();
		for(ArrayList<String> line: tab){
			ArrayList<String> line2 = (ArrayList<String>) line.clone();
			line2.remove(i);

			if(!arrayContainsList(tRes,line2))
				tRes.add(line2);
		}
		return tRes;
	}


	private static boolean arrayContainsList(ArrayList<ArrayList<String>> tab, ArrayList<String> line){
		for(ArrayList<String> lTab : tab){
			boolean contains = true ;
			for(String elmt : line){
				contains &= lTab.contains(elmt); 
				if(!contains)
					break;
			}
			if(contains)
				return true;
			else
				continue;
		}
		return  false; 
	}

	private static ArrayList<String> getSelectValues(ArrayList<ArrayList<String>> tab,
			int iSelect,ArrayList<String> line ){
		ArrayList<String> res = new ArrayList<String>();
		for(ArrayList<String> lTab : tab){
			if(lTab.containsAll(line)){
				res.add(lTab.get(iSelect));
			}
		}
		return res;
	}

	private static void printTable(ArrayList<ArrayList<String>> tab, File f) throws Exception{
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		for(ArrayList<String> line : tab){
			for(String elmt : line)
				bw.write(elmt+" ");
			bw.write("\n");
		}
		bw.close();


	}
	

	private static String getSelectedAxis(String args[]) throws Exception {
		String axis;
		int iAxis = ArgsHandler.indexOf("-select",args);
		if(iAxis != -1){
			axis = ArgsHandler.paramFrom(args,iAxis+1);
		}else{
			axis = getConstraints("axes",args).get(0).dim;
		}
		return axis;
	}

	private static ArrayList<V_Select> getSelectMeasure(String args[]) throws Exception{
		ArrayList<V_Select> aRes = new ArrayList<V_Select>();

		int iSelect =  ArgsHandler.indexOf("-select",args);
		if(args[iSelect].contains("-select")){
			ArrayList<String> oneOptArgs = ArgsHandler.paramsFrom(args,iSelect+1);
			// remove the selected axis.
			oneOptArgs.remove(0);
			if(oneOptArgs.isEmpty())
				throw new InvalidArgumentException("One of the -select option is malformed");
			for(String select : oneOptArgs ){
//				System.out.println( " select opt "+ select);
				V_Select v_s = V_Measure.string2V_Select(select);
				aRes.add(v_s);

			}
		}else
			aRes.add(V_Select.MOY);
		return aRes;
	}
	
	private static TreeMap<V_Constraint,ArrayList<V_Select>> getNetWorkMetric(String []args) throws Exception{
		TreeMap<V_Constraint,ArrayList<V_Select>> networkM = new TreeMap<V_Constraint,ArrayList<V_Select>> ();
		int i=0;
		while(i<args.length){
			
			if(args[i].contains("-networkMetric")){
				ArrayList<String> oneOptArgs = ArgsHandler.paramsFrom(args,i+1);
				if(oneOptArgs.isEmpty())
					throw new InvalidArgumentException("One of the -networkMetric option is malformed");
				V_Constraint cmetric = new V_Constraint(oneOptArgs.remove(0));
				networkM.put(cmetric,new ArrayList<V_Select>());
				
				for(String select : oneOptArgs ){
					networkM.get(cmetric).add(V_Measure.string2V_Select(select));
				}
				i += oneOptArgs.size()+1;
			}else{
				i++;
			}
		}
		
		return networkM;
	}
	
	
	

	private static String getTitle(String args[]) throws Exception{
		String title ="";
		int iTitle = ArgsHandler.indexOf("-title",args);
		if(iTitle != -1){
			title = ArgsHandler.paramFrom(args,iTitle+1);
		}
		else
			throw new Exception(" indidcate a title");
		return title;
	}


	private static ArrayList<V_Constraint> getConstraints( String cType,String args[]){
		ArrayList<String> constraintNames = new ArrayList<String> ();

		int iConstr = ArgsHandler.indexOf("-"+cType,args) ;
		if(iConstr != -1){
			constraintNames = ArgsHandler.paramsFrom(args, iConstr+1);
		}

		ArrayList<V_Constraint> constraints = new ArrayList<V_Constraint>();
		for(String c : constraintNames)
			constraints.add(new V_Constraint(c));

		//System.out.println("iConstr "+iConstr+" constraints "+constraints);
		
		return constraints;
	}


	private static TreeMap<String,ArrayList<V_Constraint>> getPlotConstraints(String args[]) throws Exception{
		TreeMap<String,ArrayList<V_Constraint>> constraints = new TreeMap<String,ArrayList<V_Constraint>> ();
		int i=0;
		while(i<args.length){
			if(args[i].contains("-pConstraints")){
				ArrayList<String> oneOptArgs = ArgsHandler.paramsFrom(args,i+1);
				if(oneOptArgs.isEmpty())
					throw new InvalidArgumentException("One of the -constr option is malformed");
				String cname = oneOptArgs.remove(0);
				constraints.put(cname,new ArrayList<V_Constraint>());
				for(String cString : oneOptArgs ){
					constraints.get(cname).add(new V_Constraint(cString));
				}
				i += oneOptArgs.size()+1;
			}else{
				i++;
			}
		}
		if(constraints.isEmpty())
			throw new InvalidArgumentException("You have to specify at least a plot constraint");
		return constraints;
	}


	private static File getDirResult(String[] args)throws Exception{
		String dirRes;
		int iDirRes = ArgsHandler.indexOf("-dirRes",args);
		if(iDirRes != -1){
			dirRes = ArgsHandler.paramFrom(args,iDirRes+1);
		}else{
			throw new Exception("You forget to indicate the input result directory");
		}
		return new File(dirRes);
	}

	

	private static File getDirView(String[] args) throws Exception{
		String dirView;
		int iDirView = ArgsHandler.indexOf("-dirView",args);
		if(iDirView != -1){
			dirView = ArgsHandler.paramFrom(args,iDirView+1)+
			File.separator+getTitle(args);
		}else{
			dirView = getDirResult(args).getAbsolutePath()+"_view"
			+File.separator+getTitle(args);
		}
		File dir = new File(dirView);
		if(dir.exists())
			tools.FileTools.recursiveDelete(dir);
		dir.mkdirs();
		return dir;
	}


	private static ArrayList<ArrayList<String>> builtArrayFrom(File dirResult,
			ArrayList<V_Constraint> axis,
			ArrayList<V_Constraint>  constraints,
			ArrayList<String> nvAxesOrder, TreeMap<V_Constraint,ArrayList<V_Select>> networkM) throws Exception{

		ArrayList<ArrayList<String>> tabRes = new ArrayList<ArrayList<String>>();
		for(File d: dirResult.listFiles()){
			if(d.isDirectory()){
				
				V_Constraint cDir = new V_Constraint(d.getName());

				if( isMoreSpecificIn(cDir,constraints)==0){
					
					continue;

				}
				
//				System.out.println(" Examining "+d.getAbsolutePath() );
//				for(V_Constraint c : constraints)
//					System.out.println(c.dim+" "+c.val);

				ArrayList<ArrayList<String>> tmp = builtArrayFrom(d,axis,constraints, nvAxesOrder, networkM);
				if(isMoreSpecificIn(cDir,axis)==1){
					if(!tmp.isEmpty()){
						if(!nvAxesOrder.contains(cDir.dim))
							nvAxesOrder.add(cDir.dim);
						for(ArrayList<String> line :tmp)
							line.add(cDir.val);
						tabRes.addAll(tmp);
					}else{
//						ArrayList<String> tmp2 = new ArrayList<String>() ;
//						tmp2.add(cDir.val);
//						tabRes.add(tmp2);
					}
				}else{
					if(!tmp.isEmpty()){
						tabRes.addAll(tmp);
					}
				}
			}else{
				if(isStarter(d)){
					ArrayList<String> res = composeLine(d,axis,constraints,nvAxesOrder,networkM);
					if(!res.isEmpty()){
						tabRes.add(res);
						//	System.out.println(" Extracting " +res+" from "+d.getAbsolutePath() );
					}
					break;
				}
			}

		}
		return tabRes;
	}

	/*
	 * return -1 if no Constraint Domain in col match domain of v_c
	 * return 0 if domain v_c is in col but v_c is not moreSpecific than others compatible constraints in col
	 * return 1 if v_c is more specific than all other compatible constraint of col
	 */
	private static int isMoreSpecificIn(V_Constraint v_c, Collection<V_Constraint> col){
		boolean sameAs = false ;
		boolean isValid =true;

		for(V_Constraint c: col ){
			if(v_c.sameDimAs(c)){
				sameAs =true;
				isValid = isValid && v_c.isMoreSpecifThan(c);
				//System.out.println(v_c +" v_c.isMoreSpecifThan "+ c+ " : "+isValid);
			}
		}

		if(!sameAs)
			return -1;
		return(isValid)? 1:0;
	}

	private static boolean isIn(V_Constraint v_c, Collection<V_Constraint> col){
		for(V_Constraint c: col)
			if(v_c.sameDimAs(c))
				return true;
		return false;
	}

	private static ArrayList<String> composeLine(File fStarter,
			ArrayList<V_Constraint> axis,
			ArrayList<V_Constraint> constraints,
			ArrayList<String> nvAxisOrder, TreeMap<V_Constraint,ArrayList<V_Select>> networkM) throws Exception{
		
		
//		System.out.println("fstarter "+fStarter.getName());

		ArrayList<String> res = new ArrayList<String>();

		//		System.out.println("Constraints ");
		//		for(V_Constraint c : constraints)
		//			System.out.println(c.dim+" "+c.val);
		//		
		//		System.out.println("Axis ");
		//		for(V_Constraint c : axis)
		//			System.out.println(c.dim+" "+c.val);

		//  Metric already presents in the peer starter
		BufferedReader r = new BufferedReader(new FileReader(fStarter));
		String line = r.readLine();
		while(line !=null){
			V_Constraint v_c = new V_Constraint(line);
			if(!isIn(v_c,networkM.keySet())){
				if(!addValIfNeeded(v_c,axis,constraints,nvAxisOrder,res))
					return  new ArrayList<String>();
			}
			line = r.readLine();
		}
		r.close();
		
//		System.out.println("res ->" +res);
		
		// Metric of an instance  set of peer
		if(!networkM.isEmpty()){
			networkMetric(fStarter.getParentFile(), nvAxisOrder, networkM,res);
		}

		// composed metric
		if(isIn(PRECISION,axis)||isIn(PRECISION,constraints)){
			V_Constraint v_prec = new V_Constraint(PRECISION.dim+"="+getPrecision(fStarter));
			if(!addValIfNeeded(v_prec,axis,constraints,nvAxisOrder,res))
				return  new ArrayList<String>();
		}

		if(isIn(RECALL,axis)||isIn(RECALL,constraints)){
			V_Constraint v_prec = new V_Constraint(RECALL.dim+"="+getRecall(fStarter));
			if(!addValIfNeeded(v_prec,axis,constraints,nvAxisOrder,res))
				return  new ArrayList<String>();
		}

		if(isIn(FMEASURE,axis)||isIn(FMEASURE,constraints)){
			V_Constraint v_prec = new V_Constraint(FMEASURE.dim+"="+getFMeasure(fStarter));
			if(!addValIfNeeded(v_prec,axis,constraints,nvAxisOrder,res))
				return  new ArrayList<String>();
		}
		
		if(isIn(FirstDIAGTIME,axis)||isIn(FirstDIAGTIME,constraints)){
			V_Constraint v_prec = new V_Constraint(FirstDIAGTIME.dim+"="+getFirstDiagTime(fStarter));
			if(!addValIfNeeded(v_prec,axis,constraints,nvAxisOrder,res))
				return  new ArrayList<String>();
		}

		return res ;
	}

	


	private static boolean networkMetric(File dir,
			 ArrayList<String> nvAxisOrder,
			TreeMap<V_Constraint,ArrayList<V_Select>> networkM,
			ArrayList<String> res
			)
	throws Exception{
		
		TreeMap<V_Constraint,ArrayList<String>> values = new TreeMap<V_Constraint, ArrayList<String>>();
		for(V_Constraint v_c : networkM.keySet())
			values.put(v_c, new ArrayList<String>());
		
//		System.out.println("Extracting network metric from"+ dir.getPath());
		for(File f : dir.listFiles()){
			if(f.getName().contains(".stat")){
				BufferedReader r = new BufferedReader(new FileReader(f));
				String line = r.readLine();
				while(line !=null){
					V_Constraint v_c = new V_Constraint(line);
					for(V_Constraint netC : networkM.keySet() )
						if(v_c.sameDimAs(netC) ){
							values.get(netC).add(v_c.val);
//							System.out.println(networkM.firstKey() +" 1 -> "+values.get(networkM.firstKey()));
						}
					line = r.readLine();
			}
				r.close();	
			}
		}
		
//		System.out.println(networkM.firstKey() +" 2 -> "+values.get(networkM.firstKey()));
		//System.out.println(" networkM.size() "+networkM.size());
		for(V_Constraint netC: networkM.keySet()){
			ArrayList<String> meas = getMeasuresFrom(networkM.get(netC),values.get(netC));
//			System.out.println(netC +" 3 -> "+values.get(netC));
//			System.out.println(netC +" -> "+meas);
			res.addAll(meas);
			ArrayList<String> tmp = new ArrayList<String>();
			tmp.add(netC.dim);
			ArrayList<String> extraColumns = headColumn(tmp,0, networkM.get(netC));
			if(!nvAxisOrder.containsAll(extraColumns)){
				nvAxisOrder.addAll(extraColumns);
			}
		}
//		System.out.println();
		return true;
		
	}

	private static boolean addValIfNeeded(V_Constraint v_c,
			ArrayList<V_Constraint> axis, ArrayList<V_Constraint> constraints,
			ArrayList<String> nvAxisOrder, ArrayList<String> res) throws Exception{

		if( isMoreSpecificIn(v_c, constraints)==0){
			System.out.println(" Not Add ");
			return false;
		}

		if(isMoreSpecificIn(v_c, axis)==1){
			res.add(v_c.val);
			if(!nvAxisOrder.contains(v_c.dim))
				nvAxisOrder.add(v_c.dim);
		}
		return true;

	}

	private static long getFirstDiagTime(File fstarter) throws Exception{
		long time = 10800000;
			BufferedReader r = new BufferedReader(new FileReader(fstarter));
			String line = r.readLine();
			while(line !=null){	
				if(line.contains("DIAGNOSIS")&& line.contains("after:") ){
					time = Long.parseLong(line.split("after:")[1].trim());
					break;
				}		
				line = r.readLine();
			}
			r.close();
		
		return time;
	}

	private static double getPrecision(File fstarter) throws Exception {

		double res = 0;

		int nbDiags =0;
		try{
			BufferedReader r = new BufferedReader(new FileReader(fstarter));
			String line = r.readLine();
			while(line !=null){	
				if(line.contains("DIAGNOSIS")&& !line.contains("MINIMAL") ){
					nbDiags++;
				}		
				line = r.readLine();
			}
			r.close();
		}catch(Exception e){
			e.printStackTrace();
		}

		int nbDiagMins = 0;
		try{
			//File mindiag = new File(fstarter.getParentFile().getPath()+File.separator+"minDiags.fnd");	
			BufferedReader r = new BufferedReader(new FileReader(fstarter));
			String line = r.readLine();
			while(line !=null){
				if(line.contains("MINIMAL DIAGNOSIS") ){
					nbDiagMins++;
				}
				line = r.readLine();
			}
			r.close();
		}catch(Exception e){
			e.printStackTrace();
		}

		if(nbDiags>0)
			res = ((double)nbDiagMins/(double)nbDiags);
		else
			res =(((double)nbDiagMins/(double)nbDiags));

		return res;

	}


	private static double getRecall(File fstarter) {

		double res = 0;
		ArrayList<ArrayList<String>> diags = new ArrayList<ArrayList<String>>();
		try{
			BufferedReader r = new BufferedReader(new FileReader(fstarter));
			String line = r.readLine();
			while(line !=null){	
				if(line.contains("DIAGNOSIS")&& !line.contains("MINIMAL")){
					diags.add(extractDiag(line));
				}		
				line = r.readLine();
			}
			r.close();
		}catch(Exception e){
			e.printStackTrace();

		}

		ArrayList<ArrayList<String>> minDiags = new ArrayList<ArrayList<String>>();
		try{
			//File mindiag = new File(fstarter.getParentFile().getPath()+"minDiags.stat");	
			BufferedReader r = new BufferedReader(new FileReader(fstarter));
			String line = r.readLine();
			while(line !=null){
				if(line.contains("MINIMAL DIAGNOSIS") ){
					minDiags.add(extractDiag(line));
				}
				line = r.readLine();
			}
			r.close();
		}catch(Exception e){
			e.printStackTrace();
		}

		int nb =0;
		for(ArrayList<String> line1 : minDiags){
			for(ArrayList<String> line2 :diags){
				if(line1.containsAll(line2));
				nb++;
				break;
			}
		}

		if(minDiags.isEmpty())
			res = -1;
		else{
			res =(((double)nb/(double)minDiags.size()));
		}
		return res;
	}

	private static ArrayList<String> extractDiag(String line) {
		ArrayList<String> res = new ArrayList<String> ();

		if(line.contains("DIAGNOSIS")){
			String tmp = line.substring(line.indexOf('[')+1, line.lastIndexOf(']'));
			for(String l : tmp.split(",")){
				res.add(l);
			}
		}
		return res;
	}


	private static double getFMeasure(File starter) throws Exception {
		double precision = getPrecision(starter);
		double recall = getRecall(starter);
		return ((2* precision*recall)/(precision+recall));
	}

	public static boolean contains(ArrayList<ArrayList<String>> t1,
			ArrayList<ArrayList<String>> t2){

		for(ArrayList<String> line1 :t1){
			boolean contain = false;
			for(ArrayList<String> line2 :t2){
				if(line1.containsAll(line2));
				contain = true;
				break;
			}
			if(!contain)
				return false;
		}

		return true;
	}

	public static boolean isStarter(File f) throws Exception{

		boolean isStarter = false;
		
		if (f.isFile() && f.getName().contains(".stat")){
			String pName = f.getName().replaceFirst(".stat", "").trim();
			
			BufferedReader r = new BufferedReader(new FileReader(f));
			String line = r.readLine();
			while(line !=null){
				if(line.contains("father")){
					String father = line.split(":")[1].trim();
					if(father.equals(pName))
						isStarter = true;
					break;
				}
				line = r.readLine();
			}
			r.close();
		}
		return isStarter;

	}


	public static ArrayList<String> getParamFromFile(ArrayList<String> dimensions,File f) throws Exception{

		ArrayList<String> stats= new ArrayList<String>();
		BufferedReader r = new BufferedReader(new FileReader(f));

		String line = r.readLine();
		while(line !=null){
			for(String dim : dimensions)
				if(line.contains(dim)){
					stats.add(line.split(" ")[1]);
				}
			line = r.readLine();
		}
		r.close();

		if(dimensions.contains("precision"))
			stats.add(getPrecisionFromFile(f));
		return stats;

	}


	public static String getPrecisionFromFile(File f)throws Exception{

		int nbDiags =0;
		int nbDiagMins = 0;

		BufferedReader r = new BufferedReader(new FileReader(f));
		String line = r.readLine();
		while(line !=null){

			if(line.contains("DIAGNOSIS")&& !line.contains("MINIMAL") ){
				nbDiags++;
			}
			if(line.contains("MINIMAL DIAGNOSIS") ){
				nbDiagMins++;
			}

			line = r.readLine();
		}
		r.close();

		if(nbDiags>0)
			return ""+((double)nbDiagMins/(double)nbDiags);
		else
			return "0";

	}

	public static ArrayList<ArrayList<String>> getMinDiags(File f) throws Exception{
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();

		BufferedReader r = new BufferedReader(new FileReader(f));
		String line = r.readLine();
		while(line !=null){
			if(line.contains("MINIMAL DIAGNOSIS:") ){
				//System.out.println(" COUCOU ");
				line = line.replaceAll("MINIMAL DIAGNOSIS:","");
				line = line.replace(Character.toString('['),"");
				line = line.replace(Character.toString(']'),"");
				line = line.replace(",","");
				ArrayList<String> lineLits = new ArrayList<String> (); 
				for (String lit :line.split(" ")){
					if(!lit.contains(" ") && !lit.equals(""))
						lineLits.add(lit);
				}
				result.add(lineLits);
			}

			line = r.readLine();
		}
		r.close();

		return result;
	}



	public static void  columns(File dirResult, ArrayList<String> col){
		if(dirResult.isDirectory()){
			File d = dirResult.listFiles()[0];
			String dName = d.getName();
			if(dName.contains("=")){
				String tmp = dName.split("=")[0];
				tmp.replace("Dir_", "");
				col.add(tmp);
			}else{
				col.add("algo");
			}
		}else{
			col.add("time");
		}
	}
	
	

}

enum V_Select {MIN,MAX,MOY,MED,UPMED,LOWMED,SUM,ALL};

class V_Measure{

	ArrayList<Double> _tabD;
	
	public V_Measure(){}
	
	public V_Measure(ArrayList<String> line){

		_tabD= new ArrayList<Double>();
		for(int i=0;i<line.size(); i++){
			Double curD = Double.parseDouble(line.get(i));
			boolean added = false;
			for(int pos = 0; pos < _tabD.size(); pos ++){
				if(_tabD.get(pos)>curD){
					_tabD.add(pos,curD);
					added =true;
					break;
				}
			}
			if(!added)
				_tabD.add(curD);
		}

		//		System.out.println( "_Vmeasure "+_tabD);
	}
	
	public void add(double d){
		_tabD.add(d);
	}
	
	public ArrayList<Double> getSelected(V_Select v_s){
		
		ArrayList<Double> res= new ArrayList<Double>();
		switch (v_s) {
		case MIN: 
			res.add(min());	break;
		case MAX: 
			res.add(max());	break;
		case MOY: 
			res.add(average());	break;
		case MED: 
			res.add(med());	break;
		case UPMED: 
			res.add(upmed());	break;
		case LOWMED: 
			res.add(lowmed());	break;
		case SUM: 
			res.add(sum());	break;
		case ALL:
			res.addAll(_tabD);
		default:
			break;
		}
		return res;
	}
	
	public static V_Select string2V_Select(String select) throws Exception{
		V_Select v_s = select.equals("moy") ?  V_Select.MOY:
			select.equals("min")?  V_Select.MIN:
				select.equals("max")?   V_Select.MAX:
					select.equals("med")? V_Select.MED:
						select.equals("upMed")? V_Select.UPMED:
							select.equals("lowMed")? V_Select.LOWMED:
								select.equals("sum")?  V_Select.SUM:
									select.equals("all")?  V_Select.ALL:
									null ;
		if(v_s==null)
			throw new Exception(" the select option is unknown: "+select);
		return v_s;
	}

	public double min(){
		double min = _tabD.get(0);
		for(double val :_tabD)
			if(val<min)
				min=val;
		return min;
	}

	public double max(){
		double max = _tabD.get(0);
		for(double val :_tabD)
			if(val>max)
				max=val;
		return max;
	}

	public double average(){
		double average = 0;
		for(double val :_tabD)
			average+=val;
		return average/(double)_tabD.size();
	}
	
	public double sum(){
		double sum = 0;
		for(double val :_tabD)
			sum+=val;
		return sum;
	}

	public double med(){
		return _tabD.get(_tabD.size()/2);
	}

	public double upmed(){
		return _tabD.get(_tabD.size()*3/4);
	}

	public double lowmed(){
		return _tabD.get(_tabD.size()/4);
	}

}

enum V_Condition {ALL,EQ,LEQ,GEQ};

class V_Constraint implements Comparable{

	public String dim;
	public V_Condition cond = V_Condition.ALL;
	public String val;

	public V_Constraint( String dim, String value){
		this.dim = dim;
		this.val = value;
	}

	// expr can come from directory name or a constraint in the request
	// expr have the form  name comp value
	public V_Constraint( String expr){
		String sep = getSeparator(expr);
		if(!sep.equals("")){
			String [] words = expr.split(sep);
			dim = words[0].trim();
			val = words[1].trim();
			cond = getCondString(sep);
		}else{
			dim = expr;
			cond = V_Condition.ALL;
			val="";
		}
	}

	private String getSeparator(String expr){
		if(expr.contains("<="))
			return "<=";
		if(expr.contains(">="))
			return ">=";
		if(expr.contains("=") )
			return "=";
		if(expr.contains(":"))
			return ":";
		return "";
	}

	private V_Condition getCondString(String sep){
		if(sep.equals("=")||sep.equals(":"))
			return V_Condition.EQ;
		if(sep.equals("<="))
			return V_Condition.LEQ;
		if(sep.equals(">="))
			return V_Condition.GEQ;
		return V_Condition.ALL;

	}

	public boolean sameDimAs(V_Constraint v){
		return v.dim.equals(dim);
	}

	public static boolean compare(String v1, V_Condition c,String v2){
		switch(c){
		case ALL : return true;
		case EQ : return v1.equals(v2);
		case LEQ : return Double.valueOf(v1)<= Double.valueOf(v2);
		case GEQ : return Double.valueOf(v1) >= Double.valueOf(v2);	
		}
		return true;
	}

	public boolean isMoreSpecifThan(V_Constraint cons){

		if(cons.dim.equals(dim)){

			if(cons.cond.equals(V_Condition.ALL))
				return true;

			if(cond.equals(V_Condition.ALL))
				return false;

			if(cond.equals(V_Condition.EQ) )
				return compare(val,cons.cond,cons.val);
			

			if(!cond.equals(cons.cond))
				return false;

			return compare(val,cond,cons.val);

		}
		return false;
	}

	public String toString(){
		String sCond = "";
		switch(cond){
		case ALL : sCond="All"; break;
		case EQ : sCond="="; break;
		case LEQ : sCond="<="; break;
		case GEQ : sCond=">="; break;	
		}
		
		return dim+sCond+val;
	}

	public boolean isMoreGeneralThan(V_Constraint cons){

		if(cons.dim.equals(dim)){

			if(cond.equals(V_Condition.ALL))
				return true;

			if(cons.cond.equals(V_Condition.ALL) )
				return false;

			if(cons.cond.equals(V_Condition.EQ) )
				return compare(val,cond,cons.val);

			if(!cond.equals(cons.cond))
				return false;

			return compare(val,cond,cons.val);

		}
		return false;
	}

	@Override
	public int compareTo(Object arg0) {
		V_Constraint v_c = (V_Constraint) arg0;
		return dim.equals(v_c.dim) ? val.equals(v_c.val)? 0:1:-1;
	}

}

class  V_Request{

	public ArrayList<V_Constraint> allDimensions;

	public V_Request(){

	}
	String name;

}






