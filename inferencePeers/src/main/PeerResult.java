package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

// Just a structure to handle the peer result 
public class PeerResult implements Serializable{
	/*
	 firstMsg: 0
DIAGNOSIS: [!p5:v0, !p3:v0, !p8:v0, !p6:v0, !p2:v0] after: 2111
DIAGNOSIS: [!p2:v0, !p3:v0, !p8:v0, !p6:v0, !p9:v0] after: 2111
DIAGNOSIS: [!p2:v0, !p10:v0, !p5:v0, !p6:v0, !p8:v0] after: 2111
DIAGNOSIS: [!p5:v0, !p11:v0, !p2:v0, !p8:v0] after: 2111
DIAGNOSIS: [!p9:v0, !p11:v0, !p2:v0, !p8:v0] after: 2111
-DPLLTime: 2
finishMode: END
finishat: 2112
nbLocalImplicants: 3
nbImplicantsAtEnd: 5
nbClauses: 14
nbVars: 16
nbNeighbors: 1
nbRcvdMsg: 3
nbSentMsg: 2
MINIMAL DIAGNOSIS: [!p5:v0, !p3:v0, !p8:v0, !p6:v0, !p2:v0]
MINIMAL DIAGNOSIS: [!p2:v0, !p3:v0, !p8:v0, !p6:v0, !p9:v0]
MINIMAL DIAGNOSIS: [!p2:v0, !p10:v0, !p5:v0, !p6:v0, !p8:v0]
MINIMAL DIAGNOSIS: [!p5:v0, !p11:v0, !p2:v0, !p8:v0]
MINIMAL DIAGNOSIS: [!p9:v0, !p11:v0, !p2:v0, !p8:v0]
WorkingTime: 4
TotalTime: 2112
	 */
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public HashMap<String,Number>results;

	
	public enum PeerType {Root,Internal,Leaf}; 
	public PeerType pType ;
	public String location;
	public String father;
	public boolean terminate = false;
	public ArrayList<String> neighborNames;
	
	public ArrayList<String> diags;
	public ArrayList<Boolean> minDiags;
	public ArrayList<Number> diagsTime;
	
	
	public PeerResult(PeerType type, String location){
		pType =type;
		this.location = location;
		results = new HashMap<String,Number>();
			
		results.put("nbClauses", 0);
		results.put("nbVarsAtStart", 0);
		results.put("nbVarsAtEnd", 0);
		results.put("nbLocalImplicants", 0);
		results.put("nbImplicantsAtEnd", 0);
		
		results.put("nbRcvdMsg", 0);
		results.put("nbSentMsg", 0);
		
		results.put("workingTime", 0);
		results.put("dpllTime", 0);
		results.put("totalTime", 0);
		results.put("firstMsgTime", 0);
		results.put("knownTreeTime", 0);
	}
	
	public PeerResult( File location){
		
			
		
	}
	
	public void finalize(){
		
		
	}
	
	
	

}
