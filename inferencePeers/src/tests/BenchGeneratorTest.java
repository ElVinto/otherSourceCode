package tests;


import java.util.ArrayList;

import benchMarkGenerator.gen.P2PISBenchGenerator;
import tools.Chrono;
import tools.PlotMaker;

public class BenchGeneratorTest {

	/**
	 * @param args
	 */
	// A REFAIRE
	public static String [] genDefaultArgs(String model, String dirBench){
		String [] args = {
				"-nbInstances",""+5,
				"-timeOut",""+Chrono.min2Millis(2),
				"-benchDir",dirBench,
				"-netModel",model,
				"-nbPeers",""+500,""+500,""+30,
//				"-nbLocal",""+5,
//				"-nbShared",""+6,
				"-nbVarsMax",""+12,
				"-nbSharedBylinkMin",""+1,
				"-nbSharedBylinkMax",""+3,
				"-rLocBySh",""+0.2,""+2.0,""+2.0,
				"-rClsVars",""+1.0,""+3.0,""+0.5, 
				"-nbTargetLit",""+1,
				"-nbVarByCl",""+3,
				"-rReWire",""+0.3,""+0.3,""+0.2
				};
		return args;
	}
	
	public static String [] genLitletArgs(String model, String dirBench){
		String [] args = {
				"-nbInstances",""+10,
				"-timeOut",""+Chrono.min2Millis(2),
				"-benchDir",dirBench,
				"-netModel",model,
				"-nbPeers",""+5,""+305,""+50,
//				"-nbLocal",""+1,
//				"-nbShared",""+4,
				"-nbVarsMax",""+30,
				"-nbSharedBylinkMin",""+1,
				"-nbSharedBylinkMax",""+5,
				"-rLocBySh",""+1.0,""+1.0,""+1.0,
				"-nbTargetLit",""+1,
				"-nbVarByCl",""+3,
				"-rReWire",""+0.2,""+0.2,""+0.2,
				"-rClsVars",""+1.0,""+1.0,""+1.0 
				};
		return args;
	}
	
	/*
	 * args for generating a bench of
	 * inferences peers network where
	 * the structure is  a WS (Wattz and Strogatz)small world
	 * and the inference peer represents the domaine of a variable
	 */
	public static String [] gen(String rGraph, String pTheory){
		String [] args1 = {
				"-nbInstances",""+1,""+10,""+1,
				"-benchDir","C:/Users/VinTo/workspace/expe08062012/bench="+ getUpper(rGraph)+getUpper(pTheory),
//				"/home/leo/armant/experiments/expe150411/benchDir20/bench="+ getUpper(rGraph)+getUpper(pTheory),
				"-benchStat", "C:/Users/VinTo/workspace/expe08062012/bench="+ getUpper(rGraph)+getUpper(pTheory),
//				"-benchStat", "/home/leo/armant/experiments/expe150411/benchStat20/bench="+ getUpper(rGraph)+getUpper(pTheory),
				"-randomGraph",rGraph,
				"-peerTheory",pTheory,
				"-makeStat","true",
				"-mustBeSat","false",
				"-timeOutSat",""+1200000, // 20min
				"-nbPeers",""+20,""+60,""+20 //  20 
				};
		return args1;
	}
	
	public static String getUpper(String s){
		String result = "";
		for(char ch :s.toCharArray())
			if(Character.isUpperCase(ch)){
				result+=ch;
			 }
		return result;
	}
	
	public static String [] union(String [] tab1,String[] tab2){
		String [] res = new String [tab1.length+tab2.length];
		System.arraycopy(tab1, 0,res,0,tab1.length);
		System.arraycopy(tab2, 0, res, tab1.length,tab2.length);
		return res;
	}
	
	
	public static String[] genView(String param,ArrayList<String> rGraphs, ArrayList<String> pTheories){
		String [] args1 = {
				"-dirRes", "./../benchStat",
				"-dirView", "C:/Users/VinTo/workspace/benchStatView", 
				"-benchDir", "./../benchDir",
				"-title", param, 
				"-axes", "nbPeers", param,
				"-gContraints",  "satStatus>=0",
				"-select",param ,"all",
				"-networkMetric", param, "moy"
				};
		
		
		
		for(String rGraph :rGraphs)
			for(String pTheorie : pTheories){
			String benchName = getUpper(rGraph)+getUpper(pTheorie);
			String []nvArgs ={"-pConstraints","bench="+benchName};
			args1 = union(args1,nvArgs);
		}
		
		
		return args1;
		
		
	}
	
	
	
	public static void main(String[] args) throws Exception {
		
		ArrayList<String> rGraphs = new ArrayList<String>();
		rGraphs.add("GBAGraph");
//		rGraphs.add("UDGraph");
//		rGraphs.add("WSGraph");
		
		ArrayList<String> pTheories = new ArrayList<String>();
		// pTheories.add("RandomPeerTheory");
		 pTheories.add("DomaineVariableTheory");
		// pTheories.add("BinaryConstraintTheory");
		
		P2PISBenchGenerator p2pisB ;
		for(String rGraph :rGraphs)
			for(String pTheorie : pTheories){
				p2pisB= new P2PISBenchGenerator(gen(rGraph,pTheorie));
				p2pisB.genBenchDir();
			}
		
		//System.out.println("Generation des vues");
		// PlotMaker.main(genView("timeSat", rGraphs, pTheories));
		//PlotMaker.main(genView("nbTemptatives", rGraphs, pTheories));
		// PlotMaker.main(genView("density", rGraphs, pTheories));
		// PlotMaker.main(genView("tightness", rGraphs, pTheories));
	}

}
