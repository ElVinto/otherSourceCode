package benchMarkGenerator.peerTheory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import benchMarkGenerator.graph.RandomGraphs;

import propositionalLogic.Base;


import main.ArgsHandler;

import tools.FileTools;


import communication.MyAddress;

public class InferencePeersGraph {

	/**
	 * @param nbLocal TODO
	 * @param nbShared TODO
	 * @param args
	 */
//	public static RandomPeerTheory[] genWSInferenceGraph(TheoryParam tp,
//			WSParam swp,double globNbClsBynbVars) {
//		TreeMap<Integer, TreeSet<Integer>> g = SmallWorldGraph
//				.genWSGraph(swp);
//		//System.out.println(SmallWorldGraph.genGraph2String(g));
//		return genInferenceNetwork(tp,g,globNbClsBynbVars);
//	}
//	
//	public static RandomPeerTheory[] genBAInferenceGraph(TheoryParam tp,
//			BAParam bap,double globNbClsBynbVars){
//		TreeMap<Integer, TreeSet<Integer>> g = SmallWorldGraph
//		.genBAGraph(bap);
//		return genInferenceNetwork(tp,g,globNbClsBynbVars);
//	}
	
	public static RandomPeerTheory[] genInferenceNetwork(TreeMap<Integer, TreeSet<Integer>> g,
			int nbTargetLit, int nbLocal,int nbShared, int nbVarByCl, double locNbClsBynbVars) {
		TreeMap<Integer, RandomPeerTheory> id2Desc = new TreeMap<Integer, RandomPeerTheory>();
		for (Integer idP : g.keySet()) {
			
			HashMap<String,Object> params = new HashMap<String, Object>();
			params.put("nbShared",nbShared);
			params.put("nbTargetLit",nbTargetLit);
			params.put("nbLocal",nbLocal);
			params.put("nbClsBynbVars",locNbClsBynbVars);
			params.put("nblitInCl",nbVarByCl);

			RandomPeerTheory desc = new RandomPeerTheory("p" + idP.toString(),
				params);
			
			id2Desc.put(idP, desc);

			// link peer with neighbors by shared lit
			for (int neigh : g.get(idP)) {
				if (idP > neigh) {
					int nbSh = (id2Desc.get(neigh).get_sharedLit().size()<3)?
							3:(int)Math.floor(Math.random()*(double)3) +1;
						for(int nb=0;nb<nbSh;nb++){
							id2Desc.get(neigh).addALinkWith(desc);
						}
					}
			}
			
			
		}
		
		for (Integer idP : id2Desc.keySet()) 
			while(!id2Desc.get(idP).allShConnected){
				int randNeigh = (int) Math.floor(Math.random()*(double)id2Desc.get(idP).get_neighbors().size());
				id2Desc.get(idP).addALinkWith(id2Desc.get(randNeigh));
			}
		
		RandomPeerTheory[] a = new RandomPeerTheory[1];
		return (RandomPeerTheory[]) id2Desc.values().toArray(a);
	}
	
	
	public static RandomPeerTheory[] genInferenceNetwork2(TreeMap<Integer, TreeSet<Integer>> g,
			int nbVarsByLink, double gNbClsBynbVars,
			int nbTargetLit, int nbLocal, int nbVarByCl) {
		TreeMap<Integer, RandomPeerTheory> id2Desc = new TreeMap<Integer, RandomPeerTheory>();
		
		for (Integer idP : g.keySet()) {
			
			int nbShared = g.get(idP).size() * nbVarsByLink;
			double nbCls =  gNbClsBynbVars*((double) nbLocal+(nbShared/2)+nbTargetLit);
			double locNbClsBynbVars = nbCls/(double) (nbLocal+nbShared+nbTargetLit);
			
			
			HashMap<String,Object> params = new HashMap<String, Object>();
			params.put("nbShared",nbShared);
			params.put("nbTargetLit",nbTargetLit);
			params.put("nbLocal",nbLocal);
			params.put("nbClsBynbVars",locNbClsBynbVars);
			params.put("nblitInCl",nbVarByCl);

			RandomPeerTheory desc = new RandomPeerTheory("p" + idP.toString(),
				params);
			id2Desc.put(idP, desc);

			// link peer with neighbors by shared lit
			for (int neigh : g.get(idP)) {
				if (idP > neigh) {
					int nbSh = (id2Desc.get(neigh).get_sharedLit().size()<3)?
							3:(int)Math.floor(Math.random()*(double)3) +1;
						for(int nb=0;nb<nbSh;nb++){
							id2Desc.get(neigh).addALinkWith(desc);
						}
					}
			}
		}
		
		RandomPeerTheory[] a = new RandomPeerTheory[1];
		return (RandomPeerTheory[]) id2Desc.values().toArray(a);
	}
	
	/*
	 *  create an inference peer network s.t. each peer have a cnf of locNbClsBynbVars *(nbShared+nbLocal+nbTarget)
	 *  nbShared is dependent from nbVarsByLind and maxNbShared
	 */
	public static RandomPeerTheory[] genInferenceNetwork3(TreeMap<Integer, TreeSet<Integer>> g,
			int nbSharedByLink,
			int nbTargetLit, int nbLocal,int maxNbShared, int nbVarByCl, double locNbClsBynbVars) {
		TreeMap<Integer, RandomPeerTheory> id2Desc = new TreeMap<Integer, RandomPeerTheory>();
		for (Integer idP : g.keySet()) {
			
			int nbShared = Math.min(maxNbShared,(g.get(idP).size()*nbSharedByLink));
			
			
			HashMap<String,Object> params = new HashMap<String, Object>();
			params.put("nbShared",nbShared);
			params.put("nbTargetLit",nbTargetLit);
			params.put("nbLocal",nbLocal);
			params.put("nbClsBynbVars",locNbClsBynbVars);
			params.put("nblitInCl",nbVarByCl);

			RandomPeerTheory desc = new RandomPeerTheory("p" + idP.toString(),
				params);
			id2Desc.put(idP, desc);

			// link peer with neighbors by shared lit
			for (int neigh : g.get(idP)) {
				if (idP > neigh) {
					for(int nb=0;nb<nbSharedByLink;nb++){
						id2Desc.get(neigh).addALinkWith(desc);
					}
				}
			}
		}
		
		RandomPeerTheory[] a = new RandomPeerTheory[1];
		return (RandomPeerTheory[]) id2Desc.values().toArray(a);
	}
	
	/*
	 * generate a network of random inference peers mainly define by the network
	 * Model. The #sharedVars = #neighbour pi * #vars by link
	 * 
	 */
	public static RandomPeerTheory[] genInferenceNetwork4(TreeMap<Integer, TreeSet<Integer>> g,
			int nbSharedByLinkMin,int nbSharedByLinkMax,
			int nbTargetLit, double rLocBySh, int nbVarByCl, double rClsByVars) {
		TreeMap<Integer, RandomPeerTheory> id2Desc = new TreeMap<Integer, RandomPeerTheory>();
		
		for (Integer idP : g.keySet()) {
			
			int nbSharedByLink = nbSharedByLinkMin+(int)Math.round(Math.random()*(nbSharedByLinkMax-nbSharedByLinkMin));
			int nbShared = g.get(idP).size()*nbSharedByLink;
			int nbLocal = (int) Math.round(rLocBySh *nbShared);
			
			HashMap<String,Object> params = new HashMap<String, Object>();
			params.put("nbShared",nbShared);
			params.put("nbTargetLit",nbTargetLit);
			params.put("nbLocal",nbLocal);
			params.put("nbClsBynbVars",rClsByVars);
			params.put("nblitInCl",nbVarByCl);

			RandomPeerTheory desc = new RandomPeerTheory("p" + idP.toString(),
				params);
			id2Desc.put(idP, desc);

			// link peer with neighbors by shared lit
			for (int neigh : g.get(idP)) {
				if (idP > neigh) {
					for(int nb=0;nb<nbSharedByLink;nb++){
						id2Desc.get(neigh).addALinkWith(desc);
					}
				}
			}
		}
		
		
		RandomPeerTheory[] a = new RandomPeerTheory[1];
		return (RandomPeerTheory[]) id2Desc.values().toArray(a);
	}
	
	
	/*
	 * Generates a network of random inference peer where each peer theory size is 
	 * bounded by #VarsMax. #Vars = Min(#VarsMax, (1+rLoc/Sh)*  #neighbour pi * #vars+ nbTarget )
	 * #shared = #Vars * rLoc/sh
	 */
	public static RandomPeerTheory[] genInferenceNetwork5(TreeMap<Integer, TreeSet<Integer>> g,
			int nbSharedByLinkMin,int nbSharedByLinkMax,
			int nbTargetLit, int nbVarsMax, double rLocBySh, int nbVarByCl, double rClsByVars) {
	
		TreeMap<Integer, RandomPeerTheory> id2Desc = new TreeMap<Integer, RandomPeerTheory>();
		
		for (Integer idP : g.keySet()) {
			
			int nbSharedByLink = nbSharedByLinkMin+(int)Math.round(Math.random()*(nbSharedByLinkMax-nbSharedByLinkMin));
			int nbShared = g.get(idP).size()*nbSharedByLink;
			int nbLocal = (int) Math.round(rLocBySh *nbShared);
			
			if(nbVarsMax<nbLocal+nbShared+nbTargetLit){
				nbLocal = (int) (((double)(nbVarsMax-nbTargetLit)) * rLocBySh/((double)1+rLocBySh));
				nbShared = nbVarsMax - nbLocal- nbTargetLit;
			}
			
			HashMap<String,Object> params = new HashMap<String, Object>();
			params.put("nbShared",nbShared);
			params.put("nbTargetLit",nbTargetLit);
			params.put("nbLocal",nbLocal);
			params.put("nbClsBynbVars",rClsByVars);
			params.put("nblitInCl",nbVarByCl);

			RandomPeerTheory desc = new RandomPeerTheory("p" + idP.toString(),
				params);
			
			id2Desc.put(idP, desc);
			
			// link peer with neighbors by shared lit
			for (int neigh : g.get(idP)) {
				if (idP > neigh) {
					for(int nb=0;nb<nbSharedByLink;nb++){
						id2Desc.get(neigh).addALinkWith(desc);
					}
				}
			}
		}
		
		for (Integer idP : id2Desc.keySet()) 
			while(!id2Desc.get(idP).allShConnected){
				int randNeigh = (int) Math.floor(Math.random()*(double)id2Desc.get(idP).get_neighbors().size());
				id2Desc.get(idP).addALinkWith(id2Desc.get(randNeigh));
			}
		
		RandomPeerTheory[] a = new RandomPeerTheory[1];
		return (RandomPeerTheory[]) id2Desc.values().toArray(a);
	}
	
	
	/*
	 * Transforms the input graph g into a weighted graph gOfweight such that
	 * each edge of g is weighted between nbSharedMin and nbSharedMax  
	 */
	private static TreeMap<Integer,  TreeMap<Integer,Integer>> genWeightGraph(TreeMap<Integer, TreeSet<Integer>> g,
			int nbSharedByLinkMin, int nbSharedByLinkMax){
		
		TreeMap<Integer, TreeMap<Integer,Integer>> gOfweight = new TreeMap<Integer,  TreeMap<Integer,Integer>> ();
		// copy G in weightOfG
		for(Integer idP : g.keySet()){
			for(Integer neigh: g.get(idP)){	
				int nbSharedByLink =nbSharedByLinkMin+(int)Math.round(Math.random()*(nbSharedByLinkMax-nbSharedByLinkMin));
				
				if(!gOfweight.containsKey(idP)){
					gOfweight.put(idP,new TreeMap<Integer,Integer>());
					gOfweight.get(idP).put(neigh, nbSharedByLink);
				}else{
					if(!gOfweight.get(idP).containsKey(neigh))
						gOfweight.get(idP).put(neigh, nbSharedByLink);
				}
				if(!gOfweight.containsKey(neigh)){
					gOfweight.put(neigh,new TreeMap<Integer,Integer>());
					gOfweight.get(neigh).put(idP, nbSharedByLink);
				}else{
					if(!gOfweight.get(neigh).containsKey(idP))
						gOfweight.get(neigh).put(idP, nbSharedByLink);
				}	
			}
			
		}
		return gOfweight ;
	}
	
	
	/*
	 * Generates a network of random inference peer where each peer theory size is 
	 * bounded by #VarsMax. #Vars = Min(#VarsMax, (nbLocal+nbShared+nbTargetLit) )
	 * #shared = #Vars * rLoc/sh
	 */
	public static PeerTheory[] genInferenceNetwork6(TreeMap<Integer, TreeSet<Integer>> g,
			HashMap<String, Object> params){
			
			int nbSharedByLinkMin = ArgsHandler.defaultIntIfAny("nbSharedByLinkMin", 1, params);
			
			int nbSharedByLinkMax = ArgsHandler.defaultIntIfAny("nbSharedByLinkMax", 5, params);
					
			int nbTargetLit =	 ArgsHandler.defaultIntIfAny("nbTargetLit", 1, params);
					
			int nbVarsMax =  ArgsHandler.defaultIntIfAny("nbVarsMax", 30, params);
					
			double rLocBySh	= ArgsHandler.defaultDblIfAny("rLocBySh", 1.0, params);
					
			int nbVarByCl =	ArgsHandler.defaultIntIfAny("nbVarByCl", 3, params);
					
			double rClsByVars = ArgsHandler.defaultDblIfAny("rClsByVars", 2.0, params);
			
			
//			for(String k: params.keySet())
//				System.out.print(k+" -> "+params.get(k)+";  ");
//			System.out.println();
			
	
		TreeMap<Integer, RandomPeerTheory> id2Desc = new TreeMap<Integer, RandomPeerTheory>();
		
		
		TreeMap<Integer, TreeMap<Integer,Integer>> weightOfG = genWeightGraph(g,nbSharedByLinkMin,nbSharedByLinkMax);
		
		for (Integer idP : g.keySet()) {
			
			int nbShared = 0;
			for(Integer idNeighs : weightOfG.get(idP).keySet() ){
				nbShared+= weightOfG.get(idP).get(idNeighs);
			}
			
			int nbLocal = (int) Math.round(rLocBySh *nbShared);
			
			if(nbVarsMax<nbLocal+nbShared+nbTargetLit){
				nbLocal = (int) (((double)(nbVarsMax-nbTargetLit)) * rLocBySh/((double)1+rLocBySh));
				nbShared = nbVarsMax - nbLocal- nbTargetLit;
			}
				HashMap<String,Object> params2 = new HashMap<String, Object>();
				params2.put("nbShared",nbShared);
				params2.put("nbTargetLit",nbTargetLit);
				params2.put("nbLocal",nbLocal);
				params2.put("nbClsBynbVars",rClsByVars);
				params2.put("nblitInCl",nbVarByCl);

			RandomPeerTheory desc = new RandomPeerTheory("p" + idP.toString(),
					params2);
			id2Desc.put(idP, desc);

			// link peer with neighbors by shared lit
			for (int neigh : g.get(idP)) {
				if (idP > neigh) {
					for(int nb=0;nb<weightOfG.get(idP).get(neigh);nb++){
						id2Desc.get(neigh).addALinkWith(desc);
					}
				}
			}
		}
		
		for (Integer idP : id2Desc.keySet()) 
			while(!id2Desc.get(idP).allShConnected){
				int randNeigh = (int) Math.floor(Math.random()*(double)id2Desc.get(idP).get_neighbors().size());
				id2Desc.get(idP).addALinkWith(id2Desc.get(randNeigh));
			}
		
		RandomPeerTheory[] a = new RandomPeerTheory[1];
		return (RandomPeerTheory[]) id2Desc.values().toArray(a);
	}
	
	
	public static PeerTheory[] genInferenceNetwork7(TreeMap<Integer, TreeSet<Integer>> g,
			HashMap<String, Object> params) throws Exception {
	
		TreeMap<Integer, PeerTheory> id2Desc = new TreeMap<Integer, PeerTheory>();
		
		int nbTargetLit =  ArgsHandler.defaultIntIfAny("nbTargetLit", 1, params);
		int nbShared =  ArgsHandler.defaultIntIfAny("nbVars", 9, params);
		
		for (Integer idP : g.keySet()) {

				HashMap<String,Object> params2 = new HashMap<String, Object>();
				params2.put("nbTargetLit",nbTargetLit);
				params2.put("nbShared",nbShared);
				params2.put("nbLocal",0);
				params2.put("tightness", params.get("tightness"));
				DomainVariableTheory desc = new DomainVariableTheory("p" + idP.toString(),
					params2);
			id2Desc.put(idP, desc);

			// link peer with neighbors by shared lit
			for (int neigh : g.get(idP)) 
				if (idP > neigh) {
					id2Desc.get(neigh).addALinkWith(desc);
					//id2Desc.get(idP).addALinkWith(id2Desc.get(neigh));
				}
			
		}
		
		DomainVariableTheory[] a = new DomainVariableTheory[1];
		return (DomainVariableTheory[]) id2Desc.values().toArray(a);
	}
	
	
	@SuppressWarnings("unchecked")
	public static PeerTheory[] genInferenceNetwork8(TreeMap<Integer, TreeSet<Integer>> g,
			HashMap<String, Object> params) throws Exception {
		
		
		
		// constraints to descriptions
		TreeMap<Integer, TreeMap<Integer,BinaryConstraintTheory>> c2Desc = 
			new TreeMap<Integer, TreeMap<Integer,BinaryConstraintTheory>>();
		
		int nbTargetLit =  ArgsHandler.defaultIntIfAny("nbTargetLit", 2, params);
		int nbShared =  ArgsHandler.defaultIntIfAny("nbVars", 18, params);
		
		
		ArrayList<BinaryConstraintTheory> a = new ArrayList<BinaryConstraintTheory>();
		// build one theory per edge (constraint)
		for (Integer idP : g.keySet()) {
			for(Integer idN : g.get(idP)){
				if(idP<idN){
					
					HashMap<String,Object> params2 = new HashMap<String, Object>();
					params2.put("nbTargetLit",nbTargetLit);
					params2.put("nbShared",nbShared);
					params2.put("nbLocal",0);
					params2.put("tightness", params.get("tightness"));
					BinaryConstraintTheory desc = new BinaryConstraintTheory("p" + a.size(),
						params2);
					
					if(!c2Desc.containsKey(idP)){
						c2Desc.put(idP,new TreeMap<Integer,BinaryConstraintTheory>());
					}
					c2Desc.get(idP).put(idN, desc);
					// we also built the returned array of Peer Theory
					a.add(desc);
				}else{
					if(!c2Desc.containsKey(idP)){
						c2Desc.put(idP,new TreeMap<Integer,BinaryConstraintTheory>());
					}
					c2Desc.get(idP).put(idN,c2Desc.get(idN).get(idP));
				}
			}
		}		
		
		// connect constraint with same shared variable here idP
		for(Integer idP: c2Desc.keySet())
			// make the cartesian product of Constraints sharinf idP
			for(Integer idN : c2Desc.get(idP).keySet()){
				// save the position of idP in the first constraint
				ArrayList<String> varsIdP =null;
				if(idP< idN){
					varsIdP = (ArrayList<String>) c2Desc.get(idP).get(idN).getParam("clOfValues1");
				}else{
					varsIdP = (ArrayList<String>) c2Desc.get(idP).get(idN).getParam("clOfValues2");
				}
				for(Integer idN2 : c2Desc.get(idP).keySet()){
					if(idN<idN2){
						// save the position of idP in the second constraint
						ArrayList<String> varsIdP2 =null;
						if(idP< idN2){
							varsIdP2 = (ArrayList<String>) c2Desc.get(idP).get(idN2).getParam("clOfValues1");
						}else{
							varsIdP2 = (ArrayList<String>) c2Desc.get(idP).get(idN2).getParam("clOfValues2");
						}

						for(int iL=0;iL<varsIdP.size();iL++){
							String l= varsIdP.get(iL);
							String l2 = varsIdP2.get(iL);
							// add a mapping of equivalence between a vars of 
							// varsIdN and varsIdN2 in constraints idP,idN and 
							// idP,idN2
							ArrayList<String> clTmp = new ArrayList<String>();
							clTmp.add(l);
							clTmp.add(Base.oppLit(l2));
							c2Desc.get(idP).get(idN).get_thMapping().add(clTmp);
							c2Desc.get(idP).get(idN2).get_thMapping().add(clTmp);

							ArrayList<String> clTmp2 = new ArrayList<String>();
							clTmp2.add(Base.oppLit(l));
							clTmp2.add(l2);
							c2Desc.get(idP).get(idN).get_thMapping().add(clTmp2);
							c2Desc.get(idP).get(idN2).get_thMapping().add(clTmp2);
						}
					}
				}
			}
		
		// we modify g to represent the constraint graph
		// or the dual graph of the variables
		g.clear();
		for(Integer idP: c2Desc.keySet()){
			for(Integer idN : c2Desc.get(idP).keySet()){
				int idC =a.indexOf( c2Desc.get(idP).get(idN));
				if(!g.containsKey(idC))
					g.put(idC,new TreeSet<Integer>());
				for(Integer idN2 : c2Desc.get(idP).keySet())
					if(!idN.equals(idN2)){
						g.get(idC).add((Integer)a.indexOf( c2Desc.get(idP).get(idN2)));
					}
			}
		}
//		System.out.println("g out");
//		System.out.println(RandomGraphs.genGraph2String(g));
		return (BinaryConstraintTheory[]) a.toArray( new BinaryConstraintTheory[a.size()]);
	}
	
	
	
	public static void printInferencesPeersIn(File dir, RandomPeerTheory[] peers,
			ArrayList<ArrayList<String>> serverConnectionsSetting) {

		if (serverConnectionsSetting.size() > peers.length) {
			int diff = serverConnectionsSetting.size() - peers.length;
			System.out.println(" Too many servers for not enough peers removing "+diff+" servers.");
			for(int i=0;i<diff;i++){
				serverConnectionsSetting.remove(serverConnectionsSetting.size()-1);
			}
		}
		
			String nvDir = prepareRep(dir);
			ArrayList<File> ficRes2Write = new ArrayList<File>();
			String commonAdrBook ="";
			int nbPeersByServ = peers.length/ serverConnectionsSetting.size();
			int residu =  peers.length - (nbPeersByServ* serverConnectionsSetting.size());
			
			for (int iAd = 0; iAd < serverConnectionsSetting.size(); iAd++) {
				// a sub directory contains a set of peers hosted in a "server"
				String subDirName = serverConnectionsSetting.get(iAd).get(0)+"_"
				+serverConnectionsSetting.get(iAd).get(1);
				File subDir =composeSubNetWorkDir(nvDir,subDirName);
				subDir.mkdir();
				if(!serverConnectionsSetting.get(iAd).contains(subDir.getPath()))
					serverConnectionsSetting.get(iAd).add(subDir.getPath());
				
				File pDDIrectory = new File(subDir.getPath()+File.separator+"peers");
				pDDIrectory.mkdir();

				int lowbound = iAd * nbPeersByServ;
				double upbound = lowbound+nbPeersByServ;
				upbound+= (iAd==serverConnectionsSetting.size()-1)?residu:0;
				
				
				try {
					File fres =new File(subDir.getPath() + File.separator + "fic.res");
					ficRes2Write.add(fres);
					Writer outputRes = new BufferedWriter(new FileWriter(fres));
					outputRes.write(serverConnectionsSetting.get(iAd).get(0) + " "
							+ serverConnectionsSetting.get(iAd).get(1) + " " 
							+ serverConnectionsSetting.get(iAd).get(2) + " " 
							+ serverConnectionsSetting.get(iAd).get(3) + " " + (int)upbound + "\n");
					outputRes.close();
					
					for (int ith = lowbound; ((double) ith) < upbound; ith++) {
						RandomPeerTheory thMP = peers[ith];
					
						commonAdrBook = commonAdrBook+thMP._name+" "+serverConnectionsSetting.get(iAd).get(0) + " "
						+ serverConnectionsSetting.get(iAd).get(1) + " "+"\n";
						
						File f = new File(pDDIrectory + File.separator + thMP._name + ".fnc");
						Writer output;
						output = new BufferedWriter(new FileWriter(f));
						output.write(thMP.toString());
						output.close();
//						System.out.println(thMP._name
//								+ " file description created ");
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			for(File fres : ficRes2Write){
				Writer outputRes;
				try {
					outputRes = new BufferedWriter(new FileWriter(fres,true));
					outputRes.write(commonAdrBook);
					outputRes.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
	}
	
	public static File composeSubNetWorkDir(String dirIn,String subDirName ){
		File subDir = new File(dirIn + File.separator + subDirName);
		subDir.mkdir();
		return  subDir;
	}
	
	/*
	 *  This method checks if the current dir contains a
	 *  set of subdirs representing for a address book
	 */
	public static boolean compatibleDirInAdrBook(String dirInName, ArrayList<MyAddress> adrBook){
		File dirInFile = new File(dirInName);
		if(dirInFile.exists() && dirInFile.isDirectory()){
			for(File f :dirInFile.listFiles()){
				if(f.isDirectory()){
					boolean subdirRefer2Server=false;
					for( MyAddress adr :adrBook)
						if(f.getName().contains(adr.toString()))
							subdirRefer2Server=true;
					if(!subdirRefer2Server)
						return false;
				}
			}
		}else{
			return false;
		}
		return true;
	}

	private static String prepareRep(File rep) {
		try {
			if (rep.exists()) {
				//System.out.println(rep.getPath() + " exists");
				FileTools.recursiveDelete(rep);
			}
			
			rep.mkdirs();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return rep.getPath();
	}

	

	public static void main(String[] args) {

//		ArrayList<ArrayList<String>> adrBook = new ArrayList<ArrayList<String>>();
//		ArrayList<String >servSetting = new ArrayList<String>();
		//
//		TheoryParam tp = new TheoryParam( ((int)12), ((double)3.0), ((int)3), ((int)1));
//		WSParam smp = new WSParam(50, (int)2, 0.2);

//		servSetting.add("localhost"); servSetting.add("5000");
//		adrBook.add(servSetting);
//		RandomPeerTheory[] peers = genWSInferenceNetwork(tp, smp);
//		System.out.println("peers.length " + peers.length);
//		System.out.println("adrBook.size() " + adrBook.size());
//		File rep = new File("." + File.separator + "dataGenerate");
//		printInferencesPeersIn(rep, peers, adrBook);

	}

}
