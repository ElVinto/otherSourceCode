package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import main.DAsFullNetworkLauncher;


import distributedAlgorithm.m2dt.PeerDescription2BaseInt;

import peers.PeerDescription;
import propositionalLogic.Base;
import propositionalLogic.DPLLIter;
import sat4JAdapt.Sat4J;
import tools.PlotMaker;



public class testDpll {
	
	
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
		
		ArrayList<String> litsString = new ArrayList<String>();
		String dimacs = Sat4J.dir2Dimacs(dir,litsString);
				
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
	
	
	public static ArrayList<ArrayList<String>> diagMinFromDA(String daName, File dir) throws Exception{
		
		File dirOut = new File( "tmpDirOut");
			
		String [] args = { "-alg",daName, "-benchDir", dir.getPath(),"-dirOut",dirOut.getPath(),"-serv","localhost","5000"};
		DAsFullNetworkLauncher.main(args);
		
		ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>> ();
		
		File dirRes = new File(dirOut.getPath()+File.separator+"algo:"+daName);
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
	
	
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
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
		
		
		File dir = new File("baTest6_20_5:10/nbPeers:10/nbClsBynbVars:1.0/rLocBySh:3.0/instance:1");
		
		
		System.out.println(" DPLLIter Result " );
		ArrayList<ArrayList<String>> dMinDPLL = diagMinFromDpllIter(dir);
		System.out.println(dMinDPLL);
		
		System.out.println(" SAt4J Result ");
		ArrayList<ArrayList<String>> dMinSAT =diagMinFromSAT4J(dir);
		System.out.println(dMinSAT);
		
		
		System.out.println(" M2DT Result ");
		ArrayList<ArrayList<String>> dMinM2DT = diagMinFromDA("M2DT", dir);
		System.out.println(dMinM2DT);
		
		
		System.out.println(" M3DJ Result ");
		ArrayList<ArrayList<String>> dMinM3DJ = diagMinFromDA("M3DJ", dir);
		System.out.println(dMinM3DJ);
		
		
		System.out.println(" all results are equals "+(contains (dMinM3DJ,dMinM2DT) && contains (dMinM2DT,dMinSAT) 
				&& contains (dMinSAT,dMinDPLL) && contains (dMinDPLL,dMinM3DJ) ) );
		
		
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
