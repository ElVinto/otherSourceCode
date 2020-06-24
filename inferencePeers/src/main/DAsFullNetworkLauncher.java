package main;

// la vie est belle, j'aime la vie 
// j'aime respirer l'air de la pluie
// sentir la chaleur des gouttes glacÃ©es piquer ma peau
// les entendre  rebondir sur le sol, 
// les voir former des cercles, des disques, des couronnes
// dont les contours disparaitront dans une onde, une mininuscule vague 
// de la flaque en Ã©quilibre.
// La vie est belle j'aime la pluie :-)
// Le 16/04/08 une flaque sur le quai du rerB Massy

import java.util.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;


import benchMarkGenerator.peerTheory.InferencePeersGraph;
import benchMarkGenerator.peerTheory.RandomPeerTheory;

import com.sun.org.apache.xml.internal.serializer.ToUnknownStream;
import communication.MyAddress;

import specificException.InvalidArgumentException;
import distributedAlgorithm.*;
import distributedAlgorithm.km3dj.KM3DJ;
import distributedAlgorithm.m2dt.M2DT;
import distributedAlgorithm.m3dj.M3DJ;

import sat4JAdapt.Sat4J;
import sat4JAdapt.Sat4J.*;
import tools.Dprint;
import tools.FileTools;
import tools.GraphMaker;
import tools.ColorDot;
import tools.HyperGraphs;
import tools.PlotMaker;
import tools.mergeTree;

import initializer.*;

// java -jar launchDAS.jar -alg M2DT -alg M3DJ -benchDir baBench
//  ps -C java -o pid,user | grep varmant| awk -F " " '{print "kill -9 "$1}'|sh

public class DAsFullNetworkLauncher {

	//  
	static String[] knownOptions = { "-alg", "-benchDir", "-dirOut", "-serv",
			"-login", "-pubKey", "-randSmallWorld", "-randPeerTheory",
			"-peerStarter", "-timeOut" };
	//
	// static MyAddress [] defaultAdr ={new MyAddress(6000, "tipi17.lri.fr")
	// , new MyAddress(7000, "tipi37.lri.fr"),new MyAddress(8000,
	// "tipi39.lri.fr"),
	// new MyAddress(9000, "tipi47.lri.fr")};

	static MyAddress[] defaultAdr = { new MyAddress(3000, "tipi02.lri.fr"),
			new MyAddress(5000, "tipi09.lri.fr"),
			new MyAddress(4000, "tipi10.lri.fr"),
			new MyAddress(2000, "tipi11.lri.fr") };

	static String defLogin = "varmant";
	// /users/iasi/varmant/.ssh/id_rsa3
	static String defPubKey = "/home/leo/armant/.ssh/id_rsa";

	static String peerStarter = "";

	static long defaultTimeOut = 10800000; // 3h

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (ArgsHandler.tabContainsVal(args, "-help")
				|| !argumentsAreCompatible(args)) {
			printUsage();
			return;
		}
		
		File dirOut = getDirOut(args);
		File benchDir = getBenchDir(args);
		peerStarter = getPeerStarter(args);
		ArrayList<ArrayList<String>> algosAndParams = getAlgosAndParams(args);

		// serverSettingline (0->servName, 1->port, 2->login, 3->pubKey)
		ArrayList<ArrayList<String>> serverConnectionsSetting =getServerConnectionsSetting(args);


		if (!dirOut.exists()) {
			dirOut.mkdirs();
		}

		long timeOut = getTimeOut(args);

		followsBenchStructureAndLaunch(benchDir, dirOut,
				serverConnectionsSetting, algosAndParams, timeOut);

	}

	private static void followsBenchStructureAndLaunch(File benchDir,
			File dirOut, ArrayList<ArrayList<String>> serverConnectionsSetting,
			ArrayList<ArrayList<String>> algosAndParams, long timeOut)
			throws Exception {

		boolean end = false; 	

		 // System.out.println("benchDir "+benchDir.getAbsoluteFile());

		File[] curBenchDirFiles = benchDir.listFiles();
		boolean chgmnt = false;
		while (!end && !chgmnt) {

			// System.out.println("Files "+curBenchDirFiles);
			for (File d : curBenchDirFiles) {

				// to remove f
				// if(d.getName().contains("nbPeers")){
				// Integer numPeers =
				// Integer.valueOf(d.getName().split(":")[1]);
				// if(numPeers%2==1)
				// continue;
				// }

				// System.out.println("curBenchDirFile : "+d.getName());
				if (d.isDirectory()) {
					File nvDir = new File(dirOut.getPath() + File.separator
							+ d.getName());
					// System.out.println("nvDir : "+nvDir.getName());
					if (!nvDir.exists())
						nvDir.mkdirs();

					followsBenchStructureAndLaunch(d, nvDir,
							serverConnectionsSetting, algosAndParams, timeOut);

				}
				
				if (d.getName().contains(".fnc")) {
					// cette verification est faite pour éviter le test d'algo
					// sur des machines #
					// if(dirOut.list().length ==0)
					end = true;
					break;
				}
			}

			File[] nvBenchDirFiles = benchDir.listFiles();
			chgmnt = nvBenchDirFiles.length > curBenchDirFiles.length;
			if (end || !chgmnt)
				break;
			File[] diffFiles = new File[nvBenchDirFiles.length
					- curBenchDirFiles.length];
			int i = 0;
			for (File nvf : nvBenchDirFiles) {
				boolean find = false;
				for (File curF : curBenchDirFiles) {
					if (nvf.getPath().equals(curF.getPath()))
						find = true;
					break;
				}
				if (!find) {
					System.out.println("nv File " + nvf.getPath());
					diffFiles[i] = nvf;
					i++;
				}
			}
			curBenchDirFiles = diffFiles;
		}

		if (end == true) {

			String[] listPeerNames = peerNamesList(benchDir);
			int nbServRequired = (serverConnectionsSetting.size() <= listPeerNames.length) ? serverConnectionsSetting
					.size()
					: listPeerNames.length;

			String resContent = createNetworkFrom(serverConnectionsSetting,
					nbServRequired, listPeerNames);
			
//			System.out.println(resContent);

			int iStarter = (int) Math.floor(Math.random()
					* listPeerNames.length);
			peerStarter = peerStarter.equals("") ? listPeerNames[iStarter]
					: peerStarter;

			// jointree with privacy building from BE algorithm and minfill heuristic
			
//			Dprint.println(" is examining:  BE algorithm and minfill heuristic "
//					+ benchDir.getPath());
//			
			ArrayList<String> be = new ArrayList<String>(Arrays.asList("BE","MinFill"));
			File nvDirOut1 = prepareTheDirAlgo( dirOut, be, resContent,benchDir) ;
			HyperGraphs.createStatsFiles(benchDir.getAbsoluteFile(),nvDirOut1);
			drawsFromStat(be, nvDirOut1);
			 
			
			
			for (ArrayList<String> aap : algosAndParams) {

				
				File nvDirOut =prepareTheDirAlgo( dirOut,aap, resContent,benchDir) ;
	
//				File minDiags = new File(benchDir.getPath() + File.separator
//						+ "minDiags.fnd");
//				File minDiagsCopy = new File(nvDirOut.getPath()
//						+ File.separator + "minDiags.fnd");
//				copy(minDiags, minDiagsCopy);

				Dprint.println(algoAndParams2DirName(aap) + " is examining: "
						+ benchDir.getPath());

				ArrayList<Thread> sessions = new ArrayList<Thread>();
				for (int iServ = 0; iServ < nbServRequired; iServ++) {
					ArrayList<String> servSetting = serverConnectionsSetting
							.get(iServ);

					String aap2string = algoAndParams2DirName(aap).replace("_",
							" ");
					String cmd = "java -Xms512m -Xmx2048m -jar experiments/runDA.jar ";
					cmd = cmd + "-alg " + aap2string;
					cmd = cmd + " -peerStarter " + peerStarter;
					cmd = cmd + " -locServ " + servSetting.get(0) + " "
							+ servSetting.get(1);
					cmd = cmd + " -dirIn " + benchDir.getAbsoluteFile();
					cmd = cmd + " -dirOut " + nvDirOut.getAbsolutePath();
					cmd = cmd + " -timeOut " + Long.toString(timeOut);
					cmd = cmd + "";
					
					System.out.println(cmd);
					
					sessions.add(execDA(cmd, servSetting));
					 
				}

				for (Thread t : sessions)
					t.join();
				
				drawsFromStat(aap,nvDirOut);
				
				// GraphMaker.drawTree(nvDirOut,peerStarter,algosAndParams.indexOf(aap)%ColorDot.values().length);

			}

		}

	}
	
	private static File prepareTheDirAlgo(File dirOut, ArrayList<String> aap, String resContent, File benchDir) 
	throws Exception {
		
//		 System.out.println("prepareTheDirAlgo  -> ");
		
		String nvDirOutName = dirOut.getPath() + File.separator + "algo="
		+ algoAndParams2DirName(aap);
		
		File nvDirOut = new File(nvDirOutName);
		if (nvDirOut.exists())
			tools.FileTools.recursiveDelete(nvDirOut);
		nvDirOut.mkdirs();

		
		File graph = new File(benchDir.getPath() + File.separator + "graph.dot");
		File graphCopy = new File(nvDirOut.getPath() + File.separator
				+ "graph.dot");
		copy(graph, graphCopy);
		
		
		
		File resFile = new File(nvDirOut.getPath() + File.separator + "fic.res");
		Writer output = new BufferedWriter(new FileWriter(resFile));
		output.write(resContent);
		output.close();
		
//		 System.out.println("prepareTheDirAlgo  -> witre fic.res at "+ nvDirOut.getPath() );
		
		return nvDirOut;
	}
	
	private static void drawsFromStat(ArrayList<String> aap, File nvDirOut){
		int iColor = (aap.get(0).equals("M3DJ") ? 0 : 1);
		try {
			File dir = new File(nvDirOut.getPath() + File.separator
					+ "trees");
			if (dir.exists())
				tools.FileTools.recursiveDelete(dir);
			dir.mkdirs();
			try {
				tools.mergeTree.drawDefaultTrees(nvDirOut, dir, iColor);
				tools.GraphMaker.drawGraphOfExpr(nvDirOut, dir,"father" , iColor);
				
//				if(aap.contains("radius")){
//					int iRad = aap.indexOf("radius")+1;
//					if(iRad<aap.size()){
//						int radMax =Integer.valueOf(aap.get(iRad));
//						// tools.GraphMaker.drawclustersOfChosenFathers (nvDirOut, dir, radMax , iColor);
//						for (int rad=0;rad <= radMax;rad++){
//							String expr =  "fatherChoseAt"+rad;
//							tools.GraphMaker.drawGraphOfExpr(nvDirOut, dir,expr , iColor);
//						}
//					}
//				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	private static void copy(File fSource, File fDest) throws Exception {
		if (fSource.exists()) {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fDest));
			BufferedReader br = new BufferedReader(new FileReader(fSource));

			String line = br.readLine();
			while (line != null) {
				bw.write(line + "\n");
				line = br.readLine();
			}

			bw.close();
			br.close();
		}
	}

	private static String[] peerNamesList(File f) {
		String[] pnl = f.list(new PeerDescFilter());
		for (int i = 0; i < pnl.length; i++) {
			pnl[i] = pnl[i].replaceAll(".fnc", "");
		}
		return pnl;
	}

	private static String createNetworkFrom(
			ArrayList<ArrayList<String>> serverConnectionsSetting,
			int nbServRequired, String[] pNames) {

		String commonAdrBook = "";
		int nbPeersByServ = pNames.length / nbServRequired;
		int residu = pNames.length - (nbPeersByServ * nbServRequired);

		ArrayList<String> peersNames = new ArrayList<String>();
		for (String pFic : pNames)
			peersNames.add(pFic.replaceFirst(".fnc", ""));

		for (int iAd = 0; iAd < nbServRequired; iAd++) {
			int lowbound = iAd * nbPeersByServ;
			double upbound = lowbound + nbPeersByServ;
			upbound += (iAd == nbServRequired - 1) ? residu : 0;

			for (int ith = lowbound; ((double) ith) < upbound; ith++) {
				int iRand = (int) Math.floor(Math.random()
						* (double) peersNames.size());
				String pName = peersNames.remove(iRand);
				commonAdrBook = commonAdrBook + pName + " "
						+ serverConnectionsSetting.get(iAd).get(0) + " "
						+ serverConnectionsSetting.get(iAd).get(1) + " " + "\n";
			}

		}

		return commonAdrBook;
	}

	private static Thread execDA(String cmd, ArrayList<String> servSetting) {
		class AlgoThread extends Thread {
			String _cmd;
			ArrayList<String> _servSetting;

			public AlgoThread(String cmd, ArrayList<String> servSetting) {
				super();
				_cmd = cmd;
				_servSetting = servSetting;
			}

			public void run() {
				if (!_servSetting.get(0).contains("localhost")) {
					SSHconnection.execCmd(_servSetting.get(0), _servSetting
							.get(2), _servSetting.get(3), "", _cmd);
				} else {
					try {
						DA_LocalNetworkLauncher.main(_cmd.split(" "));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		;
		AlgoThread aT = new AlgoThread(cmd, servSetting);
		aT.start();
		return (Thread) aT;
	}

	/* ****************************************
	 * How to use the distributed algorithm launcher
	 * ****************************************
	 */

	private static void printUsage() {
		String s = " DESCRIPTION: \n"
				+ "        runs distributed algorithms in a network of inference peers  \n"
				+ " DEFAULT USE :\n "
				+ "         runDA -alg algo1 [p1..pn] ... -alg algoN [p1..pn] -benchDir dir  \n"
				+ "         laungh thhe algorithms alg1 .. algn with respective parameter [p1..pn]\n"
				+ "		  on the benchmark directory benchDir \n"
				+ "		  As result each peer writes its measures in a proper file. \n"
				+ " OPTIONS: \n"
				+ "        [-serv serv1 port1 login cle.pub .. servn portn login cle.pub] \n"
				+ "         respectively specifies avaible servers port login publicKey location for \n"
				+ "         the connections\n" + "        [-login] \n"
				+ "         indicates a default login for connections  \n"
				+ "        [-pubKey]\n"
				+ "         indicates a public rsa public key location \n"
				+ "        [-timeOut time] \n"
				+ "         states the time out for the execution\n"
				+ "        [-dirOut directory] \n"
				+ "         points out the result directory\n"
				+ "        [-peerStarter pName]\n"
				+ "         indicates the peer starter\n" + "";
		System.out.println(s);
	}

	private static boolean argumentsAreCompatible(String[] args) {

		if ((ArgsHandler.tabContainsVal(args, "-login") && !ArgsHandler
				.tabContainsVal(args, "-pubKey"))
				|| (!ArgsHandler.tabContainsVal(args, "-login") && ArgsHandler
						.tabContainsVal(args, "-pubKey")))
			return false;

		if (!ArgsHandler.tabContainsVal(args, "-alg")
				|| !ArgsHandler.tabContainsVal(args, "-benchDir"))
			return false;

		for (int i = 0; i < args.length; i++) {
			// System.out.println(args[i]);
			if (args[i].contains("-")) {
				if (!ArgsHandler.tabContainsVal(knownOptions, args[i]))
					return false;
			}
		}

		return true;
	}

	/* ****************************************
	 * handling the distributed algorithm, option:-alg
	 * ****************************************
	 */
	public static ArrayList<ArrayList<String>> getAlgosAndParams(String[] args)
			throws InvalidArgumentException {
		ArrayList<ArrayList<String>> algosAndParams = new ArrayList<ArrayList<String>>();
		int i = 0;
		while (i < args.length) {
			if (args[i].contains("-alg")) {
				ArrayList<String> oneOptArgs = ArgsHandler.paramsFrom(args,
						i + 1);
				if (oneOptArgs.isEmpty())
					throw new InvalidArgumentException(
							"One of the -alg option is malformed");
				// getdistAlgClass(oneOptArgs.get(0));
				algosAndParams.add(oneOptArgs);
				i += oneOptArgs.size() + 1;
			} else {
				i++;
			}
		}
		if (algosAndParams.isEmpty())
			throw new InvalidArgumentException(
					"You have to specify at least a distributed" + "algorithm");

		// System.out.println(algosAndParams);

		return algosAndParams;
	}

	public static Class<? extends DistributedAlgorithm> getdistAlgClass(
			String distAlg) throws InvalidArgumentException {
		String distAlgClass = "distributedAlgorithm." + distAlg.toLowerCase()
				+ "." + distAlg.toUpperCase();

		System.out.println(distAlgClass);

		if (M2DT.class.getCanonicalName().equals(distAlgClass)) {
			return M2DT.class;
		}
		if (M3DJ.class.getCanonicalName().equals(distAlgClass)) {
			return M3DJ.class;
		}
		if (KM3DJ.class.getCanonicalName().equals(distAlgClass)) {
			return KM3DJ.class;
		}
		
		
		throw new InvalidArgumentException(distAlg
				+ " is unknown as distributed Algorithm");
	}

	/* ****************************************
	 * handling server connections****************************************
	 */

	private static ArrayList<ArrayList<String>> getServerConnectionsSetting(
			String[] args) throws Exception {
		ArrayList<ArrayList<String>> serverConnectionsSetting = new ArrayList<ArrayList<String>>();

		String login = getLogin(args);
		String pubKey = getPubKey(args);

		if (!ArgsHandler.tabContainsVal(args, "-dirIn")) {
			if (ArgsHandler.tabContainsVal(args, "-serv")) {
				int i = 0;
				while (i < args.length) {
					args[i].contains("while");
					if (args[i].contains("-serv")) {
						ArrayList<String> oneOptArgs = ArgsHandler.paramsFrom(
								args, i + 1);
						serverConnectionsSetting.add(getOneconnectionSetting(
								oneOptArgs, login, pubKey));
						i += oneOptArgs.size() + 1;
					} else {
						i++;
					}
				}
			} else {
				for (MyAddress ad : defaultAdr) {

					ArrayList<String> line = new ArrayList<String>();
					line.add(ad.host());
					line.add(Integer.toString(ad.port()));
					line.add(login);
					line.add(pubKey);
					SSHconnection.testConnection(line.get(0), line.get(2), line
							.get(3), "");
					serverConnectionsSetting.add(line);
				}
			}
				
			
			// createDirIn(args, dirIn, serverConnectionsSetting);

			// for(ArrayList<String> a: serverConnectionsSetting)
			// a.add(dirOut);
		}
		// }else{
		// File dir = new File(dirIn);
		// for(File servDir :dir.listFiles()){
		// ArrayList<String> servSetting =getOneconnectionSetting(
		// File2NetworkStructure.getLocalServerSettingFrom(servDir),
		// login,pubKey);
		// servSetting.add(servDir.getPath());
		// servSetting.add(dirOut);
		// serverConnectionsSetting.add(servSetting);
		// }
		// }

		return serverConnectionsSetting;
	}

	private static File getBenchDir(String[] args) throws Exception {

		String dirIn = "";
		int iDirIn = ArgsHandler.indexOf("-benchDir", args);
		if (iDirIn != -1) {
			dirIn = ArgsHandler.paramFrom(args, iDirIn + 1);
		} else {
			throw new InvalidArgumentException(
					"You have to specify at least the directory of benchs of peer descriptions");
		}
		return new File(dirIn);
	}

	private static File getDirOut(String[] args) throws Exception {
		String dirOut;
		int iDirOut = ArgsHandler.indexOf("-dirOut", args);
		if (iDirOut != -1) {
			dirOut = ArgsHandler.paramFrom(args, iDirOut + 1);
		} else {
			dirOut = getBenchDir(args).getName() + "_results";
		}
		return new File(dirOut);
	}

	public static String algoAndParams2DirName(ArrayList<String> aap) {
		String nvDir = aap.toString().replace(Character.toString('['), "");
		nvDir = nvDir.replace(Character.toString(']'), "");
		nvDir = nvDir.replaceAll(",", "");
		nvDir = nvDir.replaceAll(" ", "_");
		return nvDir;
	}

	private static void reproduceDirStructureFromTo(File benchDir, File dirOut) {

		for (File d : benchDir.listFiles()) {
			if (d.isDirectory()) {
				File nvDir = new File(dirOut.getPath() + File.separator
						+ d.getName());
				nvDir.mkdirs();
				reproduceDirStructureFromTo(d, nvDir);
			}

		}
	}

	private static ArrayList<String> getOneconnectionSetting(
			ArrayList<String> elmts, String login, String pubKey)
			throws Exception {
		// oneOptArgs (0->servName, 1->port, 2->login, 3->pubKey)
		ArrayList<String> oneOptArgs = new ArrayList<String>();
		oneOptArgs.addAll(elmts);
		if (oneOptArgs.size() < 2)
			throw new InvalidArgumentException(
					"One of the -serv option is malformed");
		if (!servIsReacheable(oneOptArgs.get(0)))
			throw new InvalidArgumentException(
					"One of the server is not reacheable");
		Integer.parseInt(oneOptArgs.get(1));
		if (oneOptArgs.size() == 2) {
			oneOptArgs.add(login);
			oneOptArgs.add(pubKey);
		} else if (oneOptArgs.size() != 4) {
			throw new InvalidArgumentException(
					"One of the -serv option is malformed");
		}

		Dprint.println(oneOptArgs);
		
		// if it s on linux we can connect to different computer
		if(System.getProperty("line.separator").equals("\n\r"))
			SSHconnection.testConnection(oneOptArgs.get(0), oneOptArgs.get(2),
					oneOptArgs.get(3), "");
		// if is is window we work locally
		
		return oneOptArgs;
	}

	static String getPeerStarter(String[] args) throws InvalidArgumentException {
		String peerStarter;
		int ipStart = ArgsHandler.indexOf("-peerStarter", args);
		if (ipStart != -1) {
			peerStarter = ArgsHandler.paramFrom(args, ipStart + 1);
		} else {
			peerStarter = "";
		}
		return peerStarter;
	}

	private static String getLogin(String[] args)
			throws InvalidArgumentException {
		String login;
		if (ArgsHandler.tabContainsVal(args, "-login"))
			login = ArgsHandler.paramFrom(args, ArgsHandler.indexOf("-login",
					args) + 1);
		else
			login = defLogin;
		return login;
	}

	private static String getPubKey(String[] args)
			throws InvalidArgumentException {
		String pubKey;
		if (ArgsHandler.tabContainsVal(args, "-pubKey"))
			pubKey = ArgsHandler.paramFrom(args, ArgsHandler.indexOf("-pubKey",
					args) + 1);
		else
			pubKey = defPubKey;
		return pubKey;
	}

	// private static String createDirIn(String[] args,
	// String dirIn, ArrayList<ArrayList<String>> serverConnectionsSetting)
	// throws
	// InvalidArgumentException {
	// boolean isSat = false;
	// File rep = null ;
	// while(!isSat){
	// //// TheoryParam tp = getTheoryParam(args);
	// //// WSParam smp = getSmallWorldParam(args);
	// //// RandomPeerTheory[] peers =
	// InferencePeersGraph.genWSInferenceNetwork(tp, smp);
	// // rep = new File(dirIn);
	// // System.out.println(" build a new network of theories in "+dirIn);
	// // InferencePeersGraph.printInferencesPeersIn(rep, peers,
	// serverConnectionsSetting);
	// // System.out.println(" Merge all descriptions in one dimacs file");
	// // isSat = Sat4J.isSat(Sat4J.dir2Dimacs(rep));
	// // System.out.println(" the generated problem isSat "+isSat);
	// }
	// return rep.getPath();
	// }

	private static boolean servIsReacheable(String servName) throws Exception {
		return InetAddress.getByName(servName).isReachable(3000);
	}

	/* ****************************************
	 * handling time out****************************************
	 */
	public static long getTimeOut(String[] args)
			throws InvalidArgumentException {
		long timeOut;
		int iTimeOut = ArgsHandler.indexOf("-timeOut", args);
		if (iTimeOut != -1) {
			timeOut = Long.parseLong(ArgsHandler.paramFrom(args, iTimeOut + 1));
		} else {
			timeOut = defaultTimeOut;
		}
		return timeOut;
	}

}

class PeerDescFilter implements FilenameFilter {
	public boolean accept(File dir, String name) {
		return (name.endsWith(".fnc") && !name.contains(".svn"));
	}
}
