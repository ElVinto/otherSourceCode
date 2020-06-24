package benchMarkGenerator.gen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import benchMarkGenerator.graph.RandomGraphs;
import benchMarkGenerator.peerTheory.InferencePeersGraph;
import benchMarkGenerator.peerTheory.PeerTheory;
import benchMarkGenerator.peerTheory.RandomPeerTheory;

import propositionalLogic.DPLLIter;
import sat4JAdapt.Sat4J;
import tools.Chrono;
import distributedAlgorithm.m2dt.PeerDescription2BaseInt;

import main.ArgsHandler;

public class P2PISBenchGenerator {
	
	/*
	 * _att is a map from String to a non empty list of values Ex:
	 * _att.get("nbPeers") returns an Object that can be cast to an ArrayList
	 */
	HashMap<String, Object> _attSimple = new HashMap<String, Object>();
	HashMap<String, ArrayList<Object>> _attList = new HashMap<String, ArrayList<Object>>();
	
	

	public P2PISBenchGenerator() throws Exception {
		initDefautParameters(null);
	}

	public P2PISBenchGenerator(String[] args) throws Exception {
		initDefautParameters(args);
	}

	protected void initDefautParameters(String[] args) throws Exception {

		/*
		 * Default Values for the automated benchmarks generator
		 * parameterization
		 */
		
		
		// Important Ajouter les parametre mis dans Args et ne figurant pas en dessous
		
		ArgsHandler.setDefaultFileNameTo("benchDir", "./../benchDir", args,
				_attSimple);
		ArgsHandler.setDefaultFileNameTo("benchStat", "./../benchStat", args,
				_attSimple);
		
		ArgsHandler.setDefaultBoolTo("makeStat", false, args, _attSimple);
		ArgsHandler.setDefaultBoolTo("mustBeSat", false, args, _attSimple);
		ArgsHandler.setDefaultBoolTo("connectedGraph", true, args, _attSimple);
		
		

		ArrayList<Object> defNBInstances = new ArrayList<Object>();
		defNBInstances.add("1");
		defNBInstances.add("10");
		defNBInstances.add("1");
		ArgsHandler.setDefaultIntervalTo("nbInstances", defNBInstances, args,
				_attList);

		/* Default Values for the network parameterization */
		
		ArrayList<Object> defNBPeers = new ArrayList<Object>();
		defNBPeers.add("20");
		defNBPeers.add("80");
		defNBPeers.add("20");
		ArgsHandler.setDefaultIntervalTo("nbPeers", defNBPeers, args,
				_attList);
		
		ArgsHandler.setDefaultParamTo("randomGraph","WSGraph",args,_attSimple);
		
		if(_attSimple.get("randomGraph").equals("WSGraph")){
			ArrayList<Object> defReWire = new ArrayList<Object>();
			defReWire.add("0.2"); // 0.1
			defReWire.add("0.2"); // 0.4
			defReWire.add("0.1");
			ArgsHandler.setDefaultIntervalDbleTo("rReWire", defReWire, args,
				_attList);	
			
			ArrayList<Object> defNeighIncr = new ArrayList<Object>();
			// The neighbor increment represent the proportion of peer known by each peer 
			// in addition to its minimal neighborhood.
			defNeighIncr.add("0.1");
			defNeighIncr.add("0.4"); // 0.4
			defNeighIncr.add("0.05");
			ArgsHandler.setDefaultIntervalDbleTo("neighIncr", defNeighIncr, args,
				_attList);
		}
		
		if(_attSimple.get("randomGraph").equals("UDGraph")){
			ArrayList<Object> defDensity = new ArrayList<Object>();
			defDensity.add("0.1");
			defDensity.add("0.9");
			defDensity.add("0.1");
			ArgsHandler.setDefaultIntervalDbleTo("density", defDensity, args, _attList);	
		}
		
		if(_attSimple.get("randomGraph").equals("GBAGraph")){
			ArrayList<Object> gammaBA = new ArrayList<Object>();
			gammaBA.add("1.0");
			gammaBA.add("2.5");
			gammaBA.add("0.5");
			ArgsHandler.setDefaultIntervalDbleTo("gammaBA", gammaBA, args, _attList);	
		}
		

		/* Default values for random inference peer parameterization */
		ArgsHandler.setDefaultParamTo("peerTheory","DomaineVariableTheory",args,_attSimple);
		
		if(_attSimple.get("peerTheory").equals("RandomPeerTheory")){
			ArgsHandler.setDefaultParamTo("nbLocal", "1", args, _attSimple);
			ArrayList<Object> defRCls = new ArrayList<Object>();
			defRCls.add("1.0");
			defRCls.add("3.0");
			defRCls.add("1.0");
			ArgsHandler.setDefaultIntervalDbleTo("rClsVars", defRCls, args, _attList);

			ArrayList<Object> defLocBySh = new ArrayList<Object>();
			defLocBySh.add("0.5");
			defLocBySh.add("1.5");
			defLocBySh.add("0.5");
			ArgsHandler.setDefaultIntervalDbleTo("rLocSh", defLocBySh, args, _attList);
		}
		
		if(_attSimple.get("peerTheory").equals("DomaineVariableTheory") || 
				_attSimple.get("peerTheory").equals("BinaryConstraintTheory") ){
			ArrayList<Object> defTightness = new ArrayList<Object>();
			defTightness.add("0.1"); // 0.1
			defTightness.add("0.9"); // 0.9
			defTightness.add("0.1");
			ArgsHandler.setDefaultIntervalDbleTo("tightness", defTightness, args, _attList);
			
		}
		


	}
	
	
	
	public final void genBenchDir() throws Exception {
		File dir = new File((String) _attSimple.get("benchDir"));
		genBenchDir(0, new int[_attList.keySet().size()], _attList,
		_attSimple, dir);
	}
	
	
	protected  void genBenchDir(
			int curK, int []curInK ,
			HashMap<String,ArrayList<Object>> paramList,
			HashMap<String,Object> paramSimple,File dir) throws Exception{

		
		File curDir = new File(dir.getPath());
		if(curDir.exists()){
			tools.FileTools.recursiveDelete(curDir);
		}
		curDir.mkdirs();
		
		Object [] keys = paramList.keySet().toArray();
		
		
		
		if(curK >= keys.length){
			
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.putAll(paramSimple);
			for(int i=0;i<keys.length;i++)
				params.put((String)keys[i],paramList.get(keys[i]).get(curInK[i]));
			
			// System.out.println(" params "+params.keySet());
			
			RandomP2PIS rP2PIS = new RandomP2PIS(params);
			if(params.get("makeStat").equals(true)){
				params.put("curBenchDir",dir.getPath());
				BenchStatMaker.makeStat(rP2PIS,params);
			}else
				rP2PIS.genInstance();
			
			if(rP2PIS.getPeersTheory()!=null){
				writeGraphOfPeers (rP2PIS.getPeersTheory(),curDir.getAbsolutePath());
				writeNetworkGraph(curDir.getAbsolutePath(), rP2PIS.getPeersGraph());
			}
			
		}else{
			
			for(int i =0;i<paramList.get(keys[curK]).size();i++){
				curInK[curK]=i;
				File nvDir = new File(curDir.getPath()+File.separator+
						keys[curK]+"="+ paramList.get(keys[curK]).get(i)+"");
				genBenchDir(curK+1,curInK,paramList,paramSimple,nvDir);

			}
		}
	}
		
	
	
	private  void writeGraphOfPeers(PeerTheory[] peers, String dir){
		for (PeerTheory thP : peers) {
			
			File dirF = new File(dir);
			if(!dirF.exists())
				dirF.mkdirs();
			
			File f = new File(dir + File.separator + thP.get_name() + ".fnc");
			Writer output;
			try {
				output = new BufferedWriter(new FileWriter(f));
				output.write(thP.toString());
				output.close();
				//                                System.out.println(thMP._name
				//                                                + " file description created ");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private static void writeNetworkGraph(String dir,TreeMap<Integer, TreeSet<Integer>> g ){
		File f = new File(dir + File.separator + "graph.dot");
		Writer output;
		try {
			output = new BufferedWriter(new FileWriter(f));
			output.write(RandomGraphs.genGraph2String(g));
			output.close();
		                                      
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		//		AutomaticInferencePeersGenenerator aipg = new AutomaticInferencePeersGenenerator(args);
		//		aipg.genBenchDir();

	}
	
	/* ****************************************
	 * How to use the automated benchmark generator 
	 ******************************************/

//	private static void printUsage(){
//		String s =
//			" DESCRIPTION: \n"+
//			"        creates a benchmark of inference peer graphes following two models.  \n"+
//			"			A WS instance defines a Watts and Stogatz graph for the inference peers network. \n"+
//			"			WS small wordl is characterize by is high clustering coefficient but \n"+ 
//			"			Unfortunately the degree distribution does not follow a power low \n"+
//			"			A BA model specifies a Barbarasi Albert Graph for the inference peers network. \n"+
//			"			BA small worl model is characterize by is power law distribution of the degree\n"+
//			"\n"+
//			" USAGE :\n "+
//			"        java -jar makeBench.jar [OPTIONS]  \n"+
//			"        creates a benchmark of inference peer graphes parameterized by default values\n"+
//			"\n"+
//			" GENERATOR OPTIONS:\n"+
//			"        [-nbInstances  nb] \n" +
//			"			nb indicates the number of instances for each values taken by the options \n"+
//			"			by default nb="+nbInstances+"\n"+
//			"\n"+
//			"       [-benchDir dir]\n"+
//			"       	creates the benchmark in dir. If directory exists then all files that it \n"+"" +
//			"			contains will be deleted. by default dir="+benchDir+"\n"+
//			"\n"+
//			"       [-timeOut time] \n"+
//			"       	sets the duration for generating one instance to time.\n"+
//			"			by default time="+timeOut+"\n"+
//			"\n"+
//			" NETWORK OPTIONS:\n"+
//			"		 [-netModel nModel]\n"+
//			"			nModel takes value between BA and WS.\n" +
//			"			by default nModel="+nModel+"\n"+
//			"\n"+
//			"        [-nbPeers  nbPeersMin nbPeersMax stepNbPeers] \n" +
//			"        	builts instances from nbPeersMin to nbPeersMax by step of stepNbPeers  \n" +
//			"			the number nbPeers specifies the number of peers in the networks \n"+
//			"			by default nbPeersMin="+nbPeersMin+", nbPeersMax="+nbPeersMax+", stepNbPeers="+stepNbPeers+" \n"+
//			"\n"+
//			"		 [-rReWire rReWireMin rReWireMax stepRRewire]\n"+
//			"			indicates the rewiring pourcentage of the edges. ONLY FOR WS network models\n"+
//			"			by default rRewireMin="+rReWireMin+", rReWireMax="+rReWireMax+" stepRRewire="+stepRReWire+"\n"+
//			"\n"+
//			" INFERENCE PEER OPTIONS:\n"+
//			"        [-rClsVars rClsVarsMin rClsVarsMin stepRClsVars ] \n" +
//			"        	builts instances from rClsVarsMin to rClsVarsMax by step of stepRClsVars  \n" +
//			"		 	the ratio   rClsVars denates #clauses/#vars for an inference peer \n"+
//			"        	by default rClsVarsMin="+rClsVarsMin+", rClsVarsMax="+rClsVarsMax+" ,stepRClsVars="+stepRClsVars+" \n" +
//			"\n"+
//			"        [-nbLocal  nb] \n" +
//			"			nb indicates the number of local variables for each inference peer \n"+
//			"			by default nblocal="+nbLocal+"\n"+
//			"\n"+
//			"        [-nbShared  nb] \n" +
//			"			nb indicates the number of shared variables for each inference peer \n"+
//			"			by default nbShared="+nbShared+"\n"+
//			"\n"+
//			"        [-nbSharedBylinkMin  nb] \n" +
//			"			nb indicates the minimum number of shared variables for each link between peers \n"+
//			"			by default 	-nbSharedBylink="+nbSharedBylinkMin+"\n"+
//			"\n"+
//			"        [-nbTargetLit  nb] \n" +
//			"			nb indicates the number of target literals for each inference peer \n"+
//			"			by default nbTargetLit="+nbTargetLit+"\n"+
//			"\n"+
//			"        [-nbVarByCl  nb] \n" +
//			"			nb indicates the number of literals by clauses peer \n"+
//			"			by default nbVarByCl="+nbVarByCl+"\n"+
//			"";
//		System.out.println(s);
//	}
	

}
