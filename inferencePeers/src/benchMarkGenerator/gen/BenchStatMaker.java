package benchMarkGenerator.gen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import main.ArgsHandler;

import propositionalLogic.DPLLIter;
import benchMarkGenerator.graph.RandomGraphs;
import benchMarkGenerator.peerTheory.PeerTheory;
import benchMarkGenerator.peerTheory.RandomPeerTheory;
import distributedAlgorithm.m2dt.PeerDescription2BaseInt;

import sat4JAdapt.Sat4J;
import tools.Chrono;
import tools.Dprint;
import tools.FileTools;
import tools.HyperGraphs;
import tools.PlotMaker;
import tools.ThreadWrapper;

public class BenchStatMaker {
	
	
	public static void examine(File benchDir, File statDir,  long timeOut)
			throws Exception {

		if(!benchDir.isDirectory())
			return;
		
		// concerne internal node in the benchdir Hierarchy 
		if(!FileTools.dirContainsFilesWithExt(benchDir,".fnc")){
			for (File d :  benchDir.listFiles()) {
				if (d.isDirectory()) {
					File nvDir = new File(statDir.getPath() + File.separator
							+ d.getName());
					// System.out.println("nvDir : "+nvDir.getName());
					if (!nvDir.exists())
						nvDir.mkdirs();
					examine(d, nvDir, timeOut);
				}
			}
			// concerne leaves directories that contains .fnc descriptions
		}else{
			
		}

	}
	
	
	public static boolean  makeStat(RandomP2PIS rP2PIS, HashMap<String, Object> params) throws Exception{
		
		String curBenchDirName= (String) params.get("curBenchDir");
		String statBenchDir = curBenchDirName.replace((String) params.get("benchDir"),(String) params.get("benchStat"));
		File benchDirStat = new File(statBenchDir);
		if(benchDirStat.exists())
			tools.FileTools.recursiveDelete(benchDirStat);
		benchDirStat.mkdirs();
		
				
		long timeOutSat = ArgsHandler.defaultLongIfAny("timeOutSat", (long)300000, params); // 5 min
		long timeOutGen = ArgsHandler.defaultLongIfAny("timeOutGen", (long)300000, params); // 5 min
		long nbTemptMax = ArgsHandler.defaultIntIfAny("nbTemptMax", 10, params);

		
		int satStatus = -3;
		int nbTempt = 0;
		long timeGen = timeOutGen;
		long sumTimeGen = 0;
		long timeSat = timeOutSat;
		long sumTimeSat = 0;
		int isConnected =0;
		
		do{
			nbTempt ++;
			
			ThreadWrapper tw = new ThreadWrapper(rP2PIS);
			tw.start();
			timeGen = System.currentTimeMillis();
			tw.join(timeOutGen);
			timeGen = System.currentTimeMillis() - timeGen;
			sumTimeGen+=timeGen;
			isConnected = RandomGraphs.isConnected(rP2PIS.getPeersGraph())?1:0;
			
			
//			System.out.println("rP2PIS.getPeersGraph()");
//			System.out.println(RandomGraphs.genGraph2String(rP2PIS.getPeersGraph()));
			
			
			try{tw.interrupt();}
			catch(Exception e){e.printStackTrace();}

			if(timeOutGen>timeGen){
				String dimacs = benchDirStat.getPath()+File.separator+"th.dimacs";
				Sat4J.graph2dimacs(rP2PIS.getPeersTheory(),dimacs);
				timeSat = System.currentTimeMillis();
				satStatus =Sat4J.isSat(dimacs,(int)timeOutSat);
				timeSat = System.currentTimeMillis() - timeSat;
				sumTimeSat+= timeSat;
			}else
				satStatus = -3;

			File fTempt = new File(benchDirStat.getPath());
			fTempt.mkdirs();
			
			if(params.get("mustBeSat").equals((Boolean)false))
				break;

			
		}while(satStatus != 1 && nbTemptMax> nbTempt);
		
		int nbLinks = 0;
		for(Integer idP :rP2PIS.getPeersGraph().keySet())
			nbLinks+= rP2PIS.getPeersGraph().get(idP).size();
		nbLinks = nbLinks/2;
			
		 BufferedWriter w = new BufferedWriter(new FileWriter(benchDirStat.getPath()+File.separator+"gen.stat"));
		 w.write("father: "+"gen"+"\n");
		 w.write("nbTemptatives= "+nbTempt+"\n");
		 w.write("timeSat= "+timeSat+"\n");
		 w.write("timeGen= "+timeGen+"\n");
		 w.write("avgTimeSat= "+sumTimeSat/nbTempt+"\n");
		 w.write("avgTimeGen= "+sumTimeGen/nbTempt+"\n");
		 w.write("satStatus= "+satStatus+"\n");
		 w.write("isConnected= "+isConnected+"\n");
		 w.write("nbLinks= "+nbLinks+"\n");
		 
		 
		 // créer mesures par pair,
		 // nbModel local
		 // nb model partagée
		 // nb impliquant locaux
		 // nbImpliquant shared
		 // tighness des impliquants locaux
		 
		 // gérer la jointure de table avec les bases de données, ajout de clé primaires
		 // ajouter le nb de models par pair en un certain temps et si fini tous le modèle ou non
		 // ajouter le nombre de min Diag par pair en un certain temps et si tous les min diag
		 // ajouter calcul tw par min filled et maw card centralisé et décentralisé 
		 
		 
		 w.close();
		
		return satStatus==1 ;
	}
	
	
	public static void makeStatFromPeers(RandomP2PIS rP2PIS, HashMap<String, Object> params){
		
		for (PeerTheory pt :rP2PIS.getPeersTheory()){
			int [][] clauses = PeerDescription2BaseInt.clausesOfInt(pt.get_allLits(), pt.get_th());
			
			// the number of models with a timeOut
			
			// the number of implicant 
			
			// the number of 
			
			
		}
	}
	
	
	

	public static void writeMinDiags(
			RandomPeerTheory[] peers, String dir, String[]args) throws Exception {
		ArrayList<String> litsString = new ArrayList<String>();
		ArrayList<String> targString = new ArrayList<String>();
		ArrayList<ArrayList<String>> allTh = new ArrayList<ArrayList<String>>();

		//	System.out.println("writeMinDiags");

		Sat4J.graph2OneDesc(peers, litsString, targString ,allTh);

		//	System.out.println("allth : \n"+allTh.toString());
		//	System.out.println("litstString : "+litsString.toString());
		//	System.out.println("targtString : "+targString.toString());

		int [][] clauses = PeerDescription2BaseInt.clausesOfInt(litsString, allTh);


		int [] targInt = new int[targString.size()];
		for(int iLit=0;iLit<targString.size();iLit++)
			targInt[iLit]=litsString.indexOf(targString.get(iLit));

		DPLLIter dpll = new DPLLIter(clauses, litsString.size(), null);

		long timeOut = 120000;
		long end = System.currentTimeMillis()+timeOut;
		dpll.setTimeOut(end);

		int [][] diagMinInt = dpll.minRimplicants(targInt);
		String finishMode = System.currentTimeMillis()<=end?"END":"TIMEOUT";

		File f = new File(dir + File.separator + "minDiags.fnd");
		Writer output;
		try {
			output = new BufferedWriter(new FileWriter(f));
			output.write("finishMode: "+finishMode+"\n");
			for(int [] dmin : diagMinInt)
				output.write("MINIMAL DIAGNOSIS: "+PeerDescription2BaseInt.litsToString(litsString,dmin )+"\n");

			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {


	}
	
	

}
