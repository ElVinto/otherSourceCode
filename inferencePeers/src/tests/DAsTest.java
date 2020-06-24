package tests;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.util.ArrayList;
import main.DAsFullNetworkLauncher;
import main.testDpll;

import distributedAlgorithm.m2dt.PeerDescription2BaseInt;

import propositionalLogic.Base;
import propositionalLogic.DPLLIter;
import sat4JAdapt.Sat4J;
import tools.Chrono;
import tools.ColorDot;
import tools.Dprint;
import tools.FileTools;
import tools.PlotMaker;
import tools.mergeTree;



public class DAsTest {
	
	
	public static void parsePeerDesc(File fin,ArrayList<ArrayList<String>> th,ArrayList<String> litNames) throws Exception{
		
		BufferedReader r = new BufferedReader(new FileReader(fin));
		// the first line is the peer name
		r.readLine();
		String line = r.readLine();
		while(!line.contains("Mappings")){
			ArrayList<String> cl = new ArrayList<String>();
			for (String lit :line.split(" ")){
				if(!litNames.contains(lit)){
					litNames.add(lit);
					litNames.add(getOpposed(lit));
				}
				cl.add(lit);
			}
			th.add(cl);
			line = r.readLine();
		}
	}
	
	public static String getOpposed(String lit){
		if (lit.contains("!"))
			return lit.replace("!", ""); 
		else
			return "!"+lit;
	}
		
	public static ArrayList<ArrayList<String>> diagMinFromSAT4J(File dir){
		ArrayList<String> litsString1 = new ArrayList<String>();
		ArrayList<String> target = new ArrayList<String>();
		ArrayList<ArrayList<String>> allTh = new ArrayList<ArrayList<String>>();
		Sat4J.dir2OneDesc(dir,	litsString1, target, allTh);
		
//		System.out.println("target "+target);
//		System.out.println("litsString1 "+litsString1);
		
		ArrayList<String> litsString = new ArrayList<String>();
		String dimacs = Sat4J.dir2Dimacs(dir,litsString);
				
//		System.out.println("litsString "+litsString);
		
		int [] targetCode = new int[target.size()];
		for(int iLit=0; iLit< target.size();iLit++){
			String lit = target.get(iLit);
			int iTarg = (litsString.indexOf(lit)/2)+1;
			iTarg = lit.contains("!")?-iTarg:iTarg;
			targetCode[iLit] = iTarg;
		}
					
		int [][]result = Sat4J.restrictModelsOn(dimacs,targetCode);
			
		ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>> ();
		for(int[] diag : result){
			ArrayList<String> line = new ArrayList<String> ();
			for(int l : diag){
				int iLit = l<0 ? -l:l;
				iLit= (iLit-1)*2;
				line.add(litsString.get(iLit));
			}
			res.add(line);
		}
		return res;	
	}
	
	public static ArrayList<ArrayList<String>> diagMinFromDpllIter(File dir){
		ArrayList<String> litsString = new ArrayList<String>();
		ArrayList<String> target = new ArrayList<String>();
		ArrayList<ArrayList<String>> allTh = new ArrayList<ArrayList<String>>();
		Sat4J.dir2OneDesc(dir,	litsString, target, allTh);
		
		int [][] thInt = PeerDescription2BaseInt.clausesOfInt(litsString, allTh);
		int[] targetLits = new int[target.size()];
		for(int iLit =0;iLit<  target.size();iLit++){
			targetLits[iLit] = litsString.indexOf(target.get(iLit));
		}
		
//		Dprint.println(" target "+target);
//		Dprint.println(" litString "+litsString);
//		Base.printSet(targetLits);
		
		
		DPLLIter dpll = new DPLLIter(thInt, litsString.size(),null);
		int[][] result=dpll.minRimplicants(targetLits);
		
		
		
		ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>> ();
		for(int[] diag : result){
			ArrayList<String> line = new ArrayList<String> ();
			for(int l : diag)
				line.add(litsString.get(l));
			res.add(line);
			
		}
		return res;
	}
	
	public static ArrayList<ArrayList<String>> diagMinFromDA(File dirRes, String [] args) throws Exception{
		
		DAsFullNetworkLauncher.main(args);
		
		ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>> ();
		
		if(dirRes.exists() && dirRes.isDirectory()){
			for(File f: dirRes.listFiles()){
				//System.out.println(" COUCOU ");
				if(PlotMaker.isStarter(f)){
					//System.out.println(" COUCOU ");
					res = PlotMaker.getMinDiags(f);
				}
			}
		}
		return res;
	}
	
	public static boolean equals(ArrayList<ArrayList<String>> t1,
			ArrayList<ArrayList<String>> t2){
		
		for(ArrayList<String> line1 :t1){
			boolean equal = false;
			for(ArrayList<String> line2 :t2){
				if(line1.containsAll(line2) && line2.containsAll(line1)){
					equal = true;
					break;
				}
			}
			if(!equal)
				return false;
		}
		
		return true;
	}
	
	public static boolean testBench(File benchDir, File outDir){
		
		if(benchDir.isDirectory()){
			
			File nvOutDir = new File(outDir.getPath()+File.separator+ benchDir.getName());
			
			nvOutDir.mkdirs();
				
			if(benchDir.listFiles()[0].isFile()){
				Dprint.println(" Testing instance : "+benchDir.getAbsolutePath());
				return testInstance(benchDir, nvOutDir);
			}
			for(File d :benchDir.listFiles()){
				if(!testBench(d, nvOutDir) )
					return false ;
			}
		}
		return true;
	}
	
	public static  boolean testInstance(File instanceDir, File nvOutDir) {

		boolean testOk = false;
		String daName = "";
		File dirRes = null;

		try{
			
//			System.out.println(" SAt4J Result ");
//			ArrayList<ArrayList<String>> dMinSAT =diagMinFromSAT4J(instanceDir);
//			System.out.println(dMinSAT);
			
			System.out.println(" DPLLIter Result " );
			ArrayList<ArrayList<String>> dMinDPLL = diagMinFromDpllIter(instanceDir);
			System.out.println(dMinDPLL);

			daName = "M2DT";
			System.out.println(" "+daName+" Result ");
			String [] dAM2DTArgs = { "-alg",daName, "-benchDir", instanceDir.getPath(),"-dirOut", nvOutDir.getPath(),"-serv","localhost","20000"};
			dirRes = new File(nvOutDir.getPath()+File.separator+"algo="+daName);
			if(dirRes.exists())
				FileTools.recursiveDelete(nvOutDir);
			dirRes.mkdirs();
			ArrayList<ArrayList<String>> dMinM2DT = diagMinFromDA( dirRes,dAM2DTArgs);
			System.out.println(dMinM2DT);

			daName ="M3DJ"; 
			System.out.println(" "+daName+" Result ");
			String [] dAM3DJArgs = { "-alg",daName, "-benchDir", instanceDir.getPath(),"-dirOut", nvOutDir.getPath(),"-serv","localhost","20000"};
			dirRes = new File(nvOutDir.getPath()+File.separator+"algo="+daName);
			if(dirRes.exists())
				FileTools.recursiveDelete(nvOutDir);
			dirRes.mkdirs();
			ArrayList<ArrayList<String>> dMinM3DJ = diagMinFromDA(dirRes, dAM3DJArgs);
			System.out.println(dMinM3DJ);

			

			testOk = (equals (dMinM3DJ,dMinM2DT) && equals (dMinM2DT,dMinDPLL) 
					/*&& equals (dMinSAT,dMinDPLL)*/  );
			
			System.out.println(" all results are equals "+ testOk );
		}catch(Exception e){
			e.printStackTrace();
			testOk = false;
		}
		return testOk;
	}
	
	public static void testOneDA(File benchDir, File dirRes, String daName, String peerStarter,int millis ){
		
		try {
			
			String [] args = { 
					"-alg",daName,
					"-timeOut",""+millis,
					"-benchDir", benchDir.getPath(),
					"-dirOut", dirRes.getPath(),
					"-peerStarter",peerStarter,
					"-serv","localhost","20000"};
			DAsFullNetworkLauncher.main(args);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
//		try {
//			
//			ColorDot color = daName.contains("M3DJ")?ColorDot.blue:ColorDot.red;
//			
//			
//			String [] args = { 
//					"-dirMerge",dirRes.getPath()+File.separator+"tree",
//					"-dirRes", dirRes.getPath()};
//			mergeTree.main(args);
//			
////			PlotMaker.main(args);
//			
//			
//			
//		} catch (Exception e) {
//
//			e.printStackTrace();
//		}
		
	}
	
	public static void testM2DTM3DJ(File benchDir, File dirRes, int milis, String serv, boolean justJT){
		
		try {
			if(justJT){
				String [] args = {
					"-alg","M2DT", "joinTree",
					"-alg","M3DJ","joinTree",
					"-alg","KM3DJ","joinTree","radius",""+0,
//					"-alg","KM3DJ","joinTree","radius",""+1,
//					"-alg","KM3DJ","joinTree","radius",""+2,
//					"-alg","KM3DJ","joinTree","radius",""+3,
//					"-alg","KM3DJ","joinTree","radius",""+4,
//					"-alg","KM3DJ","joinTree","radius",""+5,
					"-timeOut",""+milis,
					"-benchDir", benchDir.getPath(),
					"-dirOut", dirRes.getPath(),
					"-serv",serv,"20000"};
				DAsFullNetworkLauncher.main(args);
				
				
			}else{
				String [] args = { 
						"-alg","M2DT",
						"-alg","M3DJ",
						"-timeOut",""+milis,
						"-benchDir", benchDir.getPath(),
						"-dirOut", dirRes.getPath(),
						"-serv",serv,"20000"};
				DAsFullNetworkLauncher.main(args);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// demander l'affichage de courbes
//		try {
//			
//			String [] args = {
////					"-alg","BE","MinFill",
//					"-alg","M2DT","joinTree",
//					"-alg","M3DJ","joinTree",
//					"-alg","KM3DJ","joinTree","radius",""+0,
////					"-alg","KM3DJ","joinTree","radius",""+1,
////					"-alg","KM3DJ","joinTree","radius",""+2,
////					"-alg","KM3DJ","joinTree","radius",""+3,
////					"-alg","KM3DJ","joinTree","radius",""+4,
////					"-alg","KM3DJ","joinTree","radius",""+5,
//					"-defaultView",
//					"-dirRes", dirRes.getPath()};
//			
//			PlotMaker.main(args);
//		} catch (Exception e) {e.printStackTrace();}
		
//		
		
	}
	
	public static void countDownTest(){
	try {
			
			String [] args = { 
					"-alg","M3DJ",
					"-timeOut",""+5000,
					"-benchDir", "baBench/nbPeers:160/nbClsBynbVars:3.0/instance:1" ,
					"-dirOut", "baBenchRes",
					"-serv","localhost","5000"};
			DAsFullNetworkLauncher.main(args);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		
		

		
//		int [] t1 = {1,2,3};
//		int [] t2 = {2,4,5};
//		int [] t3 = {2,4,5};
//		
//		ArrayList<ArrayList<int[]>> aati = new ArrayList<ArrayList<int[]>>(2);
//		aati.add(new ArrayList<int[]>());
//		aati.get(0).add(t1);
//		aati.get(0).add(t2);
//		aati.add(new ArrayList<int[]>());
//		aati.get(1).add(t3);
//		System.out.println(""+aati);
//		
//		if(true)
//			return;
		
		File benchDir;
		File outDir ;
		// ba/nbPeers:10/nbClsBynbVars:1.3/instance:5
		// ws_12_20:110/nbPeers:110
		//baTest6_16_3:4, baVeryEasy6_20_100:500
		//KM3DJ_joinTree is examining: baLitle4_6/nbPeers:5/nbClsBynbVars:1.5/rLocBySh:1.0
		// baLitle4_20/nbPeers:8/nbClsBynbVars:1.0/rLocBySh:1.0/instance:1
		
		benchDir= new File("C:/Users/VinTo/workspace/expe08062012/benchDebug");
		
		outDir = new File(benchDir.getPath()+"Res");
		testM2DTM3DJ(benchDir, outDir,3600000, "localhost",true);

//		benchDir= new File("baDebug5_305");
//		outDir = new File(benchDir.getName()+"Res");
//		testM2DTM3DJ(benchDir, outDir,3600000, "localhost",true);
//		testOneDA(benchDir, outDir, "M3DJ","p0",300000);
		
//		String [] serv = {"tipi08.lri.fr","tipi09.lri.fr","tipi10.lri.fr","tipi11.lri.fr","tipi12.lri.fr","tipi13.lri.fr"};
//		
//		// ba/nbPeers:10
//		File setBenchDir = new File("baBench5.2/nbPeers:50");
//		int nbSubBenchs = setBenchDir.listFiles().length;
//		for(int iF =0; iF <nbSubBenchs;iF++){
//			File b = setBenchDir.listFiles()[iF];
//
//			outDir = new File(b.getName()+"Res");
//			if(outDir.exists())
//				tools.FileTools.recursiveDelete(outDir);
//			testM2DTM3DJ(b, outDir,2, serv[iF]);
//		}
		
		
//		wsBench4/nbPeers:210/nbClsBynbVars:1.0/pReLink:0.1/instance:1		
//		ArrayList<ArrayList<String>> dMinDPLL = diagMinFromDpllIter(benchDir);
//		System.out.println(dMinDPLL);
//		 testBench(benchDir, outDir);
		
		
		
		//countDownTest();
		
		
//		ArrayList<String> cl1 = new ArrayList<String>();
//		cl1.add("ab");cl1.add("e1");cl1.add("e2");cl1.add("!s");
//		
//		ArrayList<String> cl2 = new ArrayList<String>();
//		cl2.add("ab");cl2.add("!e1");cl2.add("s");
//		
//		ArrayList<String> cl3 = new ArrayList<String>();
//		cl3.add("ab");cl3.add("e2");cl3.add("s");
//		
////		ArrayList<String> cl4 = new ArrayList<String>();
////		cl4.add("ab");cl4.add("!e1");cl4.add("!e2");cl4.add("!s");
//		
//		ArrayList<ArrayList<String>> th = new ArrayList<ArrayList<String>>();
//		th.add(cl1);th.add(cl2);th.add(cl3);//th.add(cl4);
//		
//		ArrayList<String> litNames = new ArrayList<String>();
//		litNames.add("ab");litNames.add("!ab");litNames.add("e1");litNames.add("!e1");
//		litNames.add("e2");litNames.add("!e2");litNames.add("s");litNames.add("!s");
//		
		//int [][] thInt = PeerDescription2BaseInt.clausesOfInt(litNames, th);
//		ArrayList<ArrayList<String>> th = new ArrayList<ArrayList<String>> ();
//		ArrayList<String> litNames = new ArrayList<String> ();
//		File desc = new File("/users/iasi/varmant/experiments/");
//		parsePeerDesc(desc,th,litNames);
//		int [][] thInt = PeerDescription2BaseInt.clausesOfInt(litNames, th);
//		System.out.println(" FNC ");
//		Base.printBase(thInt);
//		DPLLIter dpll = new DPLLIter(thInt, litNames.size(),null);
//		System.out.println(" FND ");
//		Base.printBase(dpll.implicants());
//		int [] targetLits = {0};
//		DPLLIter dpllMin = new DPLLIter(thInt, litNames.size(),null);
//		System.out.println(" min Diags");
//		Base.printBase(dpllMin.minRimplicants(targetLits));
//		
//		String allTh = sat4JAdapt.Sat4J.dir2Dimacs(new File("/users/iasi/varmant/experiments/baBench/Dir_nbPeers:100/Dir_NbClsBynbVarsFic:3.0/Dir_numInstance:1"));
//		System.out.println(sat4JAdapt.Sat4J.isSat(allTh));
		
		
		
		// test de la correction des résultats sur un benchmark :
		

//		ArrayList<String> litsString = new ArrayList<String>();
//		ArrayList<String> target = new ArrayList<String>();
//		ArrayList<ArrayList<String>> allTh = new ArrayList<ArrayList<String>>();
//		Sat4J.dir2OneDesc(dir,	litsString, target, allTh);
		
//		System.out.println(" Vocabulary ");
//		System.out.println(litsString);
//		System.out.println(" Production ");
//		System.out.println(target);
//		System.out.println(" Description ");
//		System.out.println(allTh);
		
		
//		int [][] thInt = PeerDescription2BaseInt.clausesOfInt(litsString, allTh);
//		int[] targetLits = new int[target.size()];
//		for(int iLit =0;iLit<  target.size();iLit++)
//			targetLits[iLit] = litsString.indexOf(target.get(iLit));
		
//		DPLLIter dpllMin = new DPLLIter(thInt, litsString.size(),null);
//		int[][] result=dpllMin.minRimplicants(targetLits);
//		
//		for(int[] diag : result){
//			String s = "";
//			for(int l : diag)
//				s=litsString.get(l)+" ";
//			System.out.println("MINIMAL DIAGNOSIS: "+s);
//		}
		
//		DPLLIter dpll = new DPLLIter(thInt, litsString.size(),null);
//		 result =dpll.implicants();
//		
//		for(int[] diag : result){-
//			String s = "";
//			for(int l : diag)
//				s=litsString.get(l)+" ";
//			System.out.println("DIAGNOSIS: "+s);
//			
//		}
		
		
		
//		0 2 5 6 
//		0 3 4 6 
//		0 2 4 7 
//		0 3 5 7

	}

}
