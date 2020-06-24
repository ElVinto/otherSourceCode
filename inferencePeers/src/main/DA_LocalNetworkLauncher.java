package main;

/*
 * ce qui a de bien avec la nature c'est qu'elle est naturellement
 * belle. Quel que soit l'endroit,  l'humeur ou le moment il suffit juste 
 * de se laisser emporter par ses formes, ses mouvements à la fois
 * son équilibre plein de déséquilibres la diversité de sa complémentarité.
 */

import java.io.File;
import java.util.ArrayList;

import specificException.InvalidArgumentException;

import benchMarkGenerator.*;
import communication.MyAddress;
import distributedAlgorithm.DistributedAlgorithm;
import distributedAlgorithm.m2dt.M2DT;
import distributedAlgorithm.m3dj.M3DJ;

import initializer.LocalNetworkBuilder;
import initializer.SSHconnection;

public class DA_LocalNetworkLauncher {
	
	static String defaultPeerStarter="p0";
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception  {
		
		// java -jar runDA.jar -alg M2DT -dirIn dirIn/localhost_5000 -peerStarter p0 -dirOut dirOut -timeOut 10000
		// awk '$0 ~/tipi43.lri.fr/ {print} $2 !="tipi43.lri.fr" && NF!=3 {$2="tipi43.lri.fr";$3 ="5000";print)}' fic.res > fic.new.res
		
		String peerStarter = getPeerStarter(args);
		String dirIn = getDirIn(args);
		File dirOut = getDirOut(args);
		//createDirOut(dirIn,dirOut.getPath());
		ArrayList<String> algoAndParams = getAlgoAndParams(args);
		
		ArrayList<String> locServ  = getLocalServerSetting(args);
		long timeOut = DAsFullNetworkLauncher.getTimeOut(args);
		Class<? extends DistributedAlgorithm> dA= 	DAsFullNetworkLauncher.getdistAlgClass(algoAndParams.get(0));
		algoAndParams.remove(0);
		LocalNetworkBuilder ln1 =
			new LocalNetworkBuilder(peerStarter,locServ,dirIn,dirOut.getPath(),dA,algoAndParams,timeOut);
		ln1.start();
		ln1.join();
	}
	
	/* ****************************************
	 * Methods for handling  peerStarter dirOut option
	 ******************************************/

	static String getPeerStarter(String[] args) throws InvalidArgumentException {
		String peerStarter;
		int ipStart = ArgsHandler.indexOf("-peerStarter",args);
		if(ipStart != -1){
			peerStarter = ArgsHandler.paramFrom(args,ipStart+1);
		}else{
			peerStarter = defaultPeerStarter;
		}
		return peerStarter;
	}
	
	private static ArrayList<String> getLocalServerSetting (
			String[] args) throws Exception{
		ArrayList<String> locServSetting =null;
		if(ArgsHandler.tabContainsVal(args, "-locServ")){
			int i = ArgsHandler.indexOf("-locServ", args);
			locServSetting = ArgsHandler.paramsFrom(args,i+1);
			if(locServSetting.isEmpty())
				throw new InvalidArgumentException("One of the -locServ option is malformed");     
		}else{
			throw new InvalidArgumentException("You have to specify at least a local server name and a port");
		}
		return locServSetting;
	}
	
	private static File getDirOut(String[] args)throws Exception{
		String dirOut;
		int iDirOut = ArgsHandler.indexOf("-dirOut",args);
		if(iDirOut != -1){
			dirOut = ArgsHandler.paramFrom(args,iDirOut+1);
		}else{
			throw new InvalidArgumentException("You have to specify at least an out dir");
		}
		return new File(dirOut);
	}
	
	private static void createDirOut(String dirIn, String dirOut){
		for(String pName : getPeersName(dirIn)){
			try{
				File stat = new File(dirOut+File.separatorChar+pName+".stat");
				if(stat.exists())
					stat.delete();
				stat.createNewFile();
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private static ArrayList<String> getPeersName(String dirIn) {
		ArrayList<String> FNCfiles = new ArrayList<String>();
		File dir = new File(dirIn);
		try {
			for (File f : dir.listFiles()) {
				if(f.isDirectory())
					FNCfiles.addAll(getPeersName(f.getPath()));
				else
					if (f.getName().contains(".fnc")) {
						FNCfiles.add(f.getName().replaceAll(".fnc", ""));
					}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return FNCfiles;
	}
	
	private static ArrayList<String> getAlgoAndParams(String[] args)
	throws InvalidArgumentException{
		ArrayList<String> algoAndParams =null;
		if(ArgsHandler.tabContainsVal(args, "-alg")){
			int i = ArgsHandler.indexOf("-alg", args);
			algoAndParams = ArgsHandler.paramsFrom(args,i+1);
			if(algoAndParams.isEmpty())
				throw new InvalidArgumentException("One of the -alg option is malformed");     
		}else{
			throw new InvalidArgumentException("You have to specify at least a distributed algorithm");
		}
		return algoAndParams;
	}
	
	public static String getDirIn(String[] args) throws Exception {

		String dirIn = "";
		int iDirIn = ArgsHandler.indexOf("-dirIn",args);
		if(iDirIn != -1){
			dirIn = ArgsHandler.paramFrom(args,iDirIn+1);
		}else{
			throw new InvalidArgumentException("You have to specify at least the directory of peer description");
		}
		return dirIn;
	}

	
	

}