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

import tools.ThreadWrappable;

import main.ArgsHandler;

import benchMarkGenerator.graph.RandomGraphs;
import benchMarkGenerator.peerTheory.InferencePeersGraph;
import benchMarkGenerator.peerTheory.PeerTheory;

/*
 * This class takes as input attributes that are parameters and values for
 * automatic generation of specific instances of inference network.
 * Inside attributes is specified an instanceGenerator. 
 */
public  class RandomP2PIS implements ThreadWrappable{

	HashMap<String,Object> _attSimple = new HashMap<String, Object>();
	PeerTheory[] _peerTheories = null;
	TreeMap<Integer, TreeSet<Integer>> _peersGraph= null;
	
	public RandomP2PIS(HashMap<String,Object> params) {
		_attSimple = params;
	}
	
	public void init(HashMap<String,Object> params) {
		_attSimple = params;
	}

	public void setParam(String paramName, Object o) {
		try {
			ArgsHandler.setDefaultParamTo(paramName, o, (String []) null, _attSimple);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	public void genInstance() throws Exception {

		genPeersInteractionGraph(_attSimple);
		
		if(_attSimple.get("peerTheory").equals("RandomPeerTheory"))
			_peerTheories = InferencePeersGraph
				.genInferenceNetwork6(_peersGraph, _attSimple );
		if (_attSimple.get("peerTheory").equals("DomaineVariableTheory"))
			_peerTheories = InferencePeersGraph.genInferenceNetwork7( _peersGraph,_attSimple);
		if (_attSimple.get("peerTheory").equals("BinaryConstraintTheory")){
			_peerTheories = InferencePeersGraph.genInferenceNetwork8( _peersGraph,_attSimple);
//			System.out.println("_peerTheories "+_peerTheories.length);
//			for(PeerTheory pt: _peerTheories){
//				System.out.print(pt.get_name());
//			}
//			System.out.println();
		}

	}
	
	@Override
	public void runInThread() {
		try {
			genInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void genPeersInteractionGraph(HashMap<String, Object> params) {
		
		if(params.get("randomGraph").equals("WSGraph")){
			_peersGraph= RandomGraphs.genWSGraph(params);
			if(params.get("connectedGraph").equals(true))
				while(! RandomGraphs.isConnected(_peersGraph))
					_peersGraph= RandomGraphs.genWSGraph(params);
		}
		
		if(params.get("randomGraph").equals("BAGraph")){
				_peersGraph= RandomGraphs.genBAGraph(params);
		}
		
		if(params.get("randomGraph").equals("GBAGraph")){
			_peersGraph= RandomGraphs.genGammaBAGraph(params);
		}
		
		if(params.get("randomGraph").equals("UDGraph")){
			_peersGraph= RandomGraphs.genUDGraph(params);
			if(params.get("connectedGraph").equals(true))
				while(! RandomGraphs.isConnected(_peersGraph))
					_peersGraph= RandomGraphs.genUDGraph(params);
		};
	}



	public PeerTheory[] getPeersTheory() {
		return _peerTheories;
	}



	public TreeMap<Integer, TreeSet<Integer>> getPeersGraph() {
		return _peersGraph;
	}



	

}
