package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import propositionalLogic.DPLLIter;
import sat4JAdapt.Sat4J;
import specificException.InvalidArgumentException;
import benchMarkGenerator.peerTheory.RandomPeerTheory;
import distributedAlgorithm.m2dt.PeerDescription2BaseInt;

public class MinDiag {

	static long defaultTimeOut = 3600000; // 1 hour
	
	private static void writeMinDiags( File dirIn,File dirOut, String[]args) throws Exception {
		
		if(dirOut.isDirectory()){
			if(!ArgsHandler.tabContainsExpr(dirOut.list(), ".res")){
				for(File nvD: dirOut.listFiles())
					writeMinDiags(dirIn,nvD,args);
				return;
			}
		}
		
		ArrayList<String> litsString = new ArrayList<String>();
		ArrayList<String> targString = new ArrayList<String>();
		ArrayList<ArrayList<String>> allTh = new ArrayList<ArrayList<String>>();
		Sat4J.dir2OneDesc(dirIn, litsString, targString ,allTh);
		
		int [][] clauses = PeerDescription2BaseInt.clausesOfInt(litsString, allTh);
		
		int [] targInt = new int[targString.size()];
		for(int iLit=0;iLit<targString.size();iLit++)
			targInt[iLit]=litsString.indexOf(targString.get(iLit));
		
		DPLLIter dpll = new DPLLIter(clauses, litsString.size(), null);
		long startAt = System.currentTimeMillis();
		long end = System.currentTimeMillis()+ getTimeOut(args);
		dpll.setTimeOut(end);
		
		int [][] diagMinInt = dpll.minRimplicants(targInt);
		String finishMode = System.currentTimeMillis()<=end?"END":"TIMEOUT";
		
		File f = new File(dirOut + File.separator + "minDiags.fnd");
		Writer output;
		try {
			output = new BufferedWriter(new FileWriter(f));
			output.write("finishMode: "+finishMode+"\n");
			output.write("time: "+(System.currentTimeMillis()-startAt)+"\n");
			for(int [] dmin : diagMinInt)
				output.write("MINIMAL DIAGNOSIS: "+PeerDescription2BaseInt.litsToString(litsString,dmin )+"\n");
			
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void createMinDiagsFiles(File benchDir, File resDir,String [] args) throws Exception{
		if(benchDir.isDirectory())
			if(!ArgsHandler.tabContainsExpr(benchDir.list(), ".fnc"))
				for(File f: benchDir.listFiles()){
					String fName = f.getName();
					File nvResDir = new File(resDir.getAbsoluteFile()+File.separator+fName);
					if(!nvResDir.exists())
						continue;
					createMinDiagsFiles(f,nvResDir, args);
				}
			else
				writeMinDiags(benchDir, resDir,args);
	}
	
	public static long  getTimeOut(String[] args)throws InvalidArgumentException{
		long timeOut;
		int iTimeOut = ArgsHandler.indexOf("-timeOut",args);
		if(iTimeOut != -1){
			timeOut = Long.parseLong(ArgsHandler.paramFrom(args,iTimeOut+1));
		}else{
			timeOut = defaultTimeOut;
		}
		return timeOut;
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
	
	private static String getExprContainsInFiles2Del (String [] args)throws Exception{
		String expr = "";
		int iExpr = ArgsHandler.indexOf("-del",args);
		if(iExpr != -1){
			expr = ArgsHandler.paramFrom(args,iExpr+1);
		}
		return expr;
	}
	
	private static void delFiles(File d, String expr){
		if(d.isDirectory()){
			for(File f : d.listFiles()){
				delFiles(f, expr);
			}
		}else if(d.getName().contains(expr)){
			d.delete();
		}
	}
	
	private static File getBenchDir(String[] args) throws Exception {
		String dirIn = "";
		int iDirIn = ArgsHandler.indexOf("-benchDir",args);
		if(iDirIn != -1){
			dirIn = ArgsHandler.paramFrom(args,iDirIn+1);
		}else{
			throw new InvalidArgumentException("You have to specify at least the directory of benchs of peer descriptions");
		}
		return new File(dirIn);
	}
	
	/**
	 * @param args
	 * 
	 * cmd : makeMinDiags -benchDir bdir -dirRes dRes -del minDiags.fnd
	 * 
	 */
	public static void main(String[] args) throws Exception {
		File dirRes = getDirResult(args);
		File benchDir = getBenchDir(args);
		String exprContainsInFiles2Del = getExprContainsInFiles2Del(args);
		if(exprContainsInFiles2Del==""){
			delFiles(dirRes,exprContainsInFiles2Del);
		}
		createMinDiagsFiles(benchDir,dirRes,args);
	}

}
