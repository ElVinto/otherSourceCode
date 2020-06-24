package distributedAlgorithm.m2dt.MsgContent;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import distributedAlgorithm.m2dt.PeerDescription2BaseInt;

import initializer.*;



public class MImpl implements Serializable{

	/**
	 * Describe the message content of a formula 
	 */ 
	private static final long serialVersionUID = 1L;

	public int[][] _formula = null;
	public MImpl(int[][] f) {
		_formula = f;
	}

	public int[][] getFormula(){
		return _formula ;
	}
	

}
