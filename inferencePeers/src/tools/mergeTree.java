package tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import main.ArgsHandler;

public class mergeTree {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		File dirRes = getDirResult(args);
		File dirMerge = getDirMerge(args);

		// File dirRes = new File("baBenchRes3");
		// File dirMerge = new File("baBenchRes3_mergeTree");

		if (!dirMerge.exists()) {
			dirMerge.mkdirs();
		}
		String fName = dirMerge.getPath() + File.separator + dirRes.getName()
				+ "_";
		recSearch(dirRes, dirMerge, fName);
	}

	private static void recSearch(File dirRes, File dirMerge, String fName)
			throws Exception {
		boolean end = false;
		for (File d : dirRes.listFiles()) {
			end = false;
			if (d.isDirectory()) {
				for (File subd : d.listFiles()) {
					if (subd.isFile() && subd.getName().contains(".stat")) {
						end = true;
						break;
					}
				}
				if (end) {

					TreeMap<String, TreeMap<String, ArrayList<String>>> g = new TreeMap<String, TreeMap<String, ArrayList<String>>>();

					String[] pStarter = new String[1];

					// located at the algo level

					for (File d1 : dirRes.listFiles()) {
						fName += "-" + d1.getName();
					}
					File[] svgdDirRes = dirRes.listFiles();

					File rep = new File(fName);
					if (!rep.exists())
						rep.mkdirs();

					int iF = 0;
					for (File d1 : svgdDirRes) {

						drawDefaultTrees(d1, rep, iF % ColorDot.values().length);
						// File gF = new
						// File(d.getPath()+File.separator+"tree.dot");

						// merge(g,gF, pStarter);
						iF++;
					}

					if (true)
						return;

					File fout = new File(rep + File.separator + "compTree.dot");

					print(g, fout, pStarter[0]);

					Runtime.getRuntime().exec(
							"dot -Tps " + fout.getPath() + " -o "
									+ fout.getPath() + ".ps");
					Runtime.getRuntime().exec("dot -Tgif " +fout.getPath() + " -o " + fout.getPath() + ".gif");

				} else
					recSearch(d, dirMerge, fName + d.getName() + "_");
			}
		}

	}

	private static void merge(
			TreeMap<String, TreeMap<String, ArrayList<String>>> g, File gf,
			String[] pStarter) throws Exception {

		BufferedReader br = new BufferedReader(new FileReader(gf));
		String line = br.readLine();
		// escape the first 2 lines
		line = br.readLine();
		pStarter[0] = line;
		while (line != null) {
			// parse line
			String[] sp1 = line.split(" ");
			if (sp1.length == 3) {
				String node = sp1[0].substring(0, sp1[0].length() - 2);
				String neigh = sp1[1];
				String style = sp1[2];

				if (style.contains("color")) {

					if (g.containsKey(neigh))
						if (g.get(neigh).containsKey(node)) {
							ArrayList<String> toRem = new ArrayList<String>();
							for (String sty : g.get(neigh).get(node))
								if (!sty.contains("color"))
									toRem.add(sty);
							g.get(neigh).get(node).removeAll(toRem);
						}

					if (!g.containsKey(node)) {
						g.put(node, new TreeMap<String, ArrayList<String>>());
						g.get(node).put(neigh, new ArrayList<String>());
					} else {
						if (g.get(node).containsKey(neigh)) {
							ArrayList<String> toRem = new ArrayList<String>();
							for (String sty : g.get(node).get(neigh))
								if (!sty.contains("color"))
									toRem.add(sty);
							g.get(node).get(neigh).removeAll(toRem);
						} else {
							g.get(node).put(neigh, new ArrayList<String>());
						}
					}
					g.get(node).get(neigh).add(style);
				} else {
					boolean add = true;
					if (g.containsKey(neigh))
						if (g.get(neigh).containsKey(node))
							add = false;
					if (g.containsKey(node))
						if (g.get(node).containsKey(neigh))
							add = false;

					if (add == true) {
						if (!g.containsKey(node)) {
							g.put(node,
									new TreeMap<String, ArrayList<String>>());
							g.get(node).put(neigh, new ArrayList<String>());
						} else {
							if (!g.get(node).containsKey(neigh))
								g.get(node).put(neigh, new ArrayList<String>());
						}
						g.get(node).get(neigh).add(style);
					}
				}
			}
			line = br.readLine();
		}
		br.close();
	}

	private static void print(
			TreeMap<String, TreeMap<String, ArrayList<String>>> g, File f,
			String pStarter) throws Exception {
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));

		bw.write("digraph \"G\"{\n");
		bw.write(pStarter + "\n");
		for (String node : g.keySet()) {
			for (String neigh : g.get(node).keySet()) {
				if (g.get(node).get(neigh).size() > 1) {
					bw.write(node + "-> " + neigh
							+ " [style=bold,color=green];\n");
					// p15-> p1 [style=bold,color=blue];
				}
				if (g.get(node).get(neigh).size() == 1) {
					bw.write(node + "-> " + neigh + " "
							+ g.get(node).get(neigh).get(0) + "\n");
				}
				/*
				 * for(String style : g.get(node).get(neigh))
				 * bw.write(node+"-> "+neigh+" "+style+"\n");
				 */
			}
		}
		bw.write("}\n");
		bw.close();

	}

	private static File getDirResult(String[] args) throws Exception {
		String dirRes;
		int iDirRes = ArgsHandler.indexOf("-dirRes", args);
		if (iDirRes != -1) {
			dirRes = ArgsHandler.paramFrom(args, iDirRes + 1);
		} else {
			throw new Exception(
					"You forget to indicate the input result directory");
		}
		return new File(dirRes);
	}

	private static File getDirMerge(String[] args) throws Exception {
		String dirMerge;
		int iDirMerge = ArgsHandler.indexOf("-dirMerge", args);
		if (iDirMerge != -1) {
			dirMerge = ArgsHandler.paramFrom(args, iDirMerge + 1);
		} else {
			dirMerge = getDirResult(args).getAbsolutePath() + "_merge";
		}
		File dir = new File(dirMerge);
		if (!dir.exists())
			dir.mkdirs();
		return dir;
	}

	public static boolean isNumber(String s) {
		for (Character c : s.toCharArray()) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}

	public static void drawSpecificTree(File dirRes, File rep,
			String nodeLabel, String nodeColor, String edgeLabel,
			String edgeColor) throws Exception {
		TreeMap<String, String> tree = new TreeMap<String, String>();
		TreeMap<String, String> wNodeLabel = new TreeMap<String, String>();
		TreeMap<String, String> wNodeColor = new TreeMap<String, String>();
		TreeMap<String, String> wEdgeLabel = new TreeMap<String, String>();
		TreeMap<String, String> wEdgeColor = new TreeMap<String, String>();

		long minNodeLabel = Long.MAX_VALUE;
		long maxNodeLabel = Long.MIN_VALUE;
		long minNodeColor = Long.MAX_VALUE;
		long maxNodeColor = Long.MIN_VALUE;
		long minEdgeLabel = Long.MAX_VALUE;
		long maxEdgeLabel = Long.MIN_VALUE;
		long minEdgeColor = Long.MAX_VALUE;
		long maxEdgeColor = Long.MIN_VALUE;

		String[] peersStat = dirRes.list(new FileFilter(".stat"));

		if (peersStat.length == 0)
			return;

		for (String pStat : peersStat) {

			// System.out.println(" pStat "+ pStat);

			String pName = pStat.replaceAll(".stat", "").trim();

			File fstat = new File(dirRes.getPath() + File.separator + pStat);
			FileReader fr = new FileReader(fstat);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();

			while (line != null) {
				if (line.contains("father:")) {
					String father = line.split(":")[1].trim();
					tree.put(pName, father);
				}

				if (line.contains(nodeLabel)) {
					String value = line.split(":")[1].trim();
					wNodeLabel.put(pName, value);
					if (isNumber(value)) {
						Long l = Long.parseLong(value);
						minNodeLabel = Math.min(minNodeLabel, l);
						maxNodeLabel = Math.max(maxNodeLabel, l);
					}
				}

				if (line.contains(nodeColor)) {
					String value = line.split(":")[1].trim();
					wNodeColor.put(pName, value);
					if (isNumber(value)) {
						Long l = Long.parseLong(value);
						minNodeColor = Math.min(minNodeLabel, l);
						maxNodeColor = Math.max(maxNodeLabel, l);
					}
				}

				if (line.contains(edgeLabel)) {
					String value = line.split(":")[1].trim();
					wEdgeLabel.put(pName, value);
					if (isNumber(value)) {
						Long l = Long.parseLong(value);
						minEdgeLabel = Math.min(minEdgeLabel, l);
						maxEdgeLabel = Math.max(maxEdgeLabel, l);
					}
				}

				if (line.contains(edgeColor)) {
					String value = line.split(":")[1].trim();
					wEdgeColor.put(pName, value);
					if (isNumber(value)) {
						Long l = Long.parseLong(value);
						minEdgeColor = Math.min(minEdgeLabel, l);
						maxEdgeColor = Math.max(maxEdgeLabel, l);
					}
				}

				line = br.readLine();
			}
			try {
				br.close();
				fr.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		String dot = "digraph \"G\"{ \n";

		dot += "node[color=blue,width=.25,height=.375,fontsize=9] ;\n";

		// First we draw the tree
		ArrayList<String> bfsTree = bfs(tree, new ArrayList<String>(), 0);

		for (String p : bfsTree) {
			int iEdgeColor = 0;
			double d = 0;
			if (isNumber(wEdgeColor.get(p))) {

				double logL = Math.log1p(Long.parseLong(wEdgeColor.get(p)));
				double logMax = Math.log1p(maxEdgeColor);
				double logMin = Math.log1p(minEdgeColor);
				// iEdgeColor = 100-( 30+ (int)Math.floor((logL/(logMax
				// -logMin))* (double)60 ));
				d = ((double) 0.1 + ((logL - logMin) / (logMax - logMin))
						* (double) 0.8);
				if (d > 0.9 || d < 0.1) {
					// Dprint.println("p:"+p+" edgeColor:"+edgeColor+ " dAV:"+d
					// +" logL:"+logL +" logMax:"+logMax +" logMin:"+logMin );
				}

				d = Math.floor(d * (double) 10) / (double) 10;

				if (d > 0.9 || d < 0.1) {
					// Dprint.println("p:"+p+" edgeColor:"+edgeColor+ "dAP:"+d
					// +" logL:"+logL +" logMax:"+logMax +" logMin:"+logMin );
				}
			}
			dot += p + "-> " + tree.get(p) + " [style=bold,color=\"0.0 " + d
					+ " 1.0\",label=" + wEdgeLabel.get(p) + "]; \n";
		}

		for (String p : bfsTree) {
			int iNodeColor = 0;
			double d = 0;
			if (isNumber(wNodeColor.get(p))) {
				double logL = Math.log1p(Long.parseLong(wNodeColor.get(p)));
				double logMax = Math.log1p(maxNodeColor);
				double logMin = Math.log1p(minNodeColor);
				// iNodeColor = 100-( 30+ (int) Math.floor((logL/(logMax
				// -logMin))* (double)60 ));
				d = ((double) 0.1 + ((logL - logMin) / (logMax - logMin))
						* (double) 0.8);
				if (d > 0.9 || d < 0.1) {
					// Dprint.println("p:"+p+" nodeColor:"+nodeColor+ " dAV:"+d
					// +" logL:"+logL +" logMax:"+logMax +" logMin:"+logMin );
				}
				d = Math.floor(d * (double) 10) / (double) 10;

				// if(d>0.9 || d<0.1){
				// Dprint.println("p:"+p+" nodeColor:"+nodeColor+ " dAP:"+d
				// +" logL:"+logL +" logMax:"+logMax +" logMin:"+logMin );
				// }
			}
			dot += p + " " + "[style=filled,color=\"0.0 " + d + " 1.0\",label="
					+ wNodeLabel.get(p) + "]; \n";
		}

		dot += nodeLabel + "_" + nodeColor + "_" + edgeLabel + "_" + edgeColor
				+ ";\n";
		dot += " }\n ";

		File f = new File(rep.getPath() + File.separator + "tree_" + nodeLabel
				+ "_" + nodeColor + "_" + edgeLabel + "_" + edgeColor + ".dot");
		Writer output;
		try {
			output = new BufferedWriter(new FileWriter(f));
			output.write(dot);
			output.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		Runtime.getRuntime().exec(
				"dot -Tps " + f.getPath() + " -o " + f.getPath() + ".ps");
		Runtime.getRuntime().exec("dot -Tgif " +f.getPath() + " -o " + f.getPath() + ".gif");

	}

	public static void drawDefaultTrees(File dirRes, File rep, int icolor)
			throws Exception {

		// // How Do I organize my time
		 drawSpecificTree(dirRes,rep,"workingTime","workingTime","liveTime","liveTime");
		// drawSpecificTree(dirRes,rep,"waitingTime","waitingTime","liveTime","liveTime");
		// drawSpecificTree(dirRes,rep,"workingTime","workingTime","waitingTime","waitingTime");
		// drawSpecificTree(dirRes,rep,"workByLiveTime","workByLiveTime","liveTime","liveTime");
		//		
		// // what am I doing when I work
		 drawSpecificTree(dirRes,rep,"treeTime","treeTime","workingTime","workingTime");
		// drawSpecificTree(dirRes,rep,"dpllTime","dpllTime","workingTime","workingTime");
		// drawSpecificTree(dirRes,rep,"sumProductTime","sumProductTime","workingTime","workingTime");
		// drawSpecificTree(dirRes,rep,"sumAddRImpltTime","sumAddRImpltTime","workingTime","workingTime");
		//		
		// drawSpecificTree(dirRes,rep,"sumAddResultTime","sumAddResultTime","sumProductTime","sumProductTime");
		// drawSpecificTree(dirRes,rep,"maxAddResultTime","maxAddResultTime","maxProductTime","maxProductTime");
		//		
		// drawSpecificTree(dirRes,rep,"maxAddRImplTime","maxAddRImplTime","maxTime2SSent","maxTime2SSent");
		// drawSpecificTree(dirRes,rep,"maxProductTime","maxProductTime","maxTime2SSent","maxTime2SSent");
		//		
		//		
		// // What am I produce when I am working
		// drawSpecificTree(dirRes,rep,"dpllTime","dpllTime","nbLocalImplicants","nbLocalImplicants");
		// drawSpecificTree(dirRes,rep,"nbSharedAtEnd","nbSharedAtEnd","maxNbRImplicantsStored","maxNbRImplicantsStored");
		// drawSpecificTree(dirRes,rep,"workingTime","workingTime","nbResultSent","nbResultSent");
		// drawSpecificTree(dirRes,rep,"avProductTime","avProductTime","productivity","productivity");

		// Jointree quality
		drawSpecificTree(dirRes, rep, "nbVarsAtStart", "nbVarsAtStart",
				"nbVarsAtEnd", "nbVarsAtEnd");
		drawSpecificTree(dirRes, rep, "nbVarsAtEnd", "nbVarsAtEnd", "depth",
				"depth");
		drawSpecificTree(dirRes, rep, "nbSharedAtEnd", "nbSharedAtEnd",
				"treeTime", "treeTime");

		// Others
		drawSpecificTree(dirRes, rep, "nbNeighbors", "nbNeighbors", "depth",
				"depth");

		// drawTree(dirRes,rep, icolor);
		drawGraph(dirRes, rep, icolor);

	}
	
	
	public static void drawGraphOfFatherLink(File dirRes, File rep, int icolor)
		throws Exception {
			TreeMap<String, Integer> maxNbResultStore = new TreeMap<String, Integer>();
			TreeMap<String, Boolean> timeOut = new TreeMap<String, Boolean>();
			TreeMap<String, Boolean> slowDpll = new TreeMap<String, Boolean>();
			// TreeMap <String,String> tree = new TreeMap<String,String>();
			TreeMap<String, ArrayList<String>> graph = new TreeMap<String, ArrayList<String>>();

			// System.out.println(" dirRes "+ dirRes);
			// System.out.println(" dirRes  children");
			// for(String f :dirRes.list())
			// System.out.println(f);

			String[] peersStat = dirRes.list(new FileFilter(".stat"));

			// System.out.println(" selected  children");
			// for(String f :peersStat)
			// System.out.println("coucou "+f);
			if (peersStat.length == 0)
				return;

			for (String pStat : peersStat) {

				// System.out.println(" pStat "+ pStat);

				String pName = pStat.replaceAll(".stat", "").trim();

				File fstat = new File(dirRes.getPath() + File.separator + pStat);
				BufferedReader br = new BufferedReader(new FileReader(fstat));
				String line = br.readLine();

				double totalTime = 0;
				double dpllTime = 0;
				while (line != null) {
					// if(line.contains("father")){
					// String father = line.split(":")[1].trim();
					// tree.put(pName,father);
					// // System.out.println(" father of "+ pName+ " "+ father);
					//
					// }
					if (line.contains("finishMode")) {
						String mode = line.split(":")[1].trim();
						boolean outOfTime = !mode.equals("END");
						timeOut.put(pName, outOfTime);
					}
					if (line.contains("dpllTime")) {
						String time = line.split(":")[1].trim();
						dpllTime = Double.parseDouble(time);
					}
					if (line.contains("totalTime")) {
						String time = line.split(":")[1].trim();
						totalTime = Double.parseDouble(time);
					}

					if (line.contains("maxNbResultStore")) {
						String nb = line.split(":")[1].trim();
						maxNbResultStore.put(pName, Integer.parseInt(nb));
					}

					if (line.contains("neighbors")) {
						String neighs = line.split(":")[1].trim();

						// System.out.println(" neighborhood of "+ pName+ " "+
						// neighs);

						neighs = neighs.replace(Character.toString('['), "");
						neighs = neighs.replace(Character.toString(']'), "");

						graph.put(pName, new ArrayList<String>());
						for (String neigh : neighs.split(",")) {
							if (!graph.keySet().contains(neigh.trim()))
								graph.get(pName).add(neigh.trim());
						}
					}
					line = br.readLine();
				}

				if ((dpllTime / totalTime) > 0.8) {
					slowDpll.put(pName, (dpllTime / totalTime) > 0.8);
				}

				br.close();
			}

			// Removing the parent peer from the neighbors
			// for(String p : tree.keySet()){
			// graph.get(p).remove(tree.get(p));
			// graph.get(tree.get(p)).remove(p);
			// }

			// tree.remove(pStarter);

			String dot = "digraph \"G\"{ \n";

			// First we draw the tree
			// ArrayList<String> bfsTree = bfs(tree, new ArrayList<String>() ,0);
			// dot+="node[width=.25,height=.375,fontsize=9] ;\n";

			// for(String p : bfsTree){
			// dot += p+"-> "+
			// tree.get(p)+" [style=bold,color="+ColorDot.values()[icolor]+",label="+maxNbResultStore.get(p)+"]; \n";
			// }
			//		
			// Then we shape the vertex
			// dot+= pStarter+" [shape=Mdiamond]; \n";
			// for(String p : bfsTree){
			// String nodeShape = "";
			// String nodeBgColor = "";
			// String style="";
			// if(timeOut.get(p)){
			// style = ",style=filled";
			// nodeBgColor =",color=lightgrey";
			// if(slowDpll.keySet().contains(p)){
			// nodeShape=",shape=diamond";
			// }
			// dot += p+" [label="+p+style+nodeBgColor+nodeShape+"]; \n";
			// }
			//			
			// }

			// Afterwards we add the other edges
			for (String p : graph.keySet()) {
				for (String n : graph.get(p))
					dot += p + "-> " + n + " [arrowhead=none]; \n";
			}

			dot += " }\n ";

			// System.out.println(rep.getPath());

			File f = new File(rep.getPath() + File.separator + "graphBis" + icolor
					+ ".dot");
			Writer output;
			try {
				output = new BufferedWriter(new FileWriter(f));
				output.write(dot);
				output.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

			Runtime.getRuntime().exec(
					"dot -Tps " + f.getPath() + " -o " + f.getPath() + ".ps");
			Runtime.getRuntime().exec("dot -Tgif " +f.getPath() + " -o " + f.getPath() + ".gif");
		
	}
	
	

	public static void drawGraph(File dirRes, File rep, int icolor)
			throws Exception {
		TreeMap<String, Integer> maxNbResultStore = new TreeMap<String, Integer>();
		TreeMap<String, Boolean> timeOut = new TreeMap<String, Boolean>();
		TreeMap<String, Boolean> slowDpll = new TreeMap<String, Boolean>();
		// TreeMap <String,String> tree = new TreeMap<String,String>();
		TreeMap<String, ArrayList<String>> graph = new TreeMap<String, ArrayList<String>>();

		// System.out.println(" dirRes "+ dirRes);
		// System.out.println(" dirRes  children");
		// for(String f :dirRes.list())
		// System.out.println(f);

		String[] peersStat = dirRes.list(new FileFilter(".stat"));

		// System.out.println(" selected  children");
		// for(String f :peersStat)
		// System.out.println("coucou "+f);
		if (peersStat.length == 0)
			return;

		for (String pStat : peersStat) {

			// System.out.println(" pStat "+ pStat);

			String pName = pStat.replaceAll(".stat", "").trim();

			File fstat = new File(dirRes.getPath() + File.separator + pStat);
			BufferedReader br = new BufferedReader(new FileReader(fstat));
			String line = br.readLine();

			double totalTime = 0;
			double dpllTime = 0;
			while (line != null) {
				// if(line.contains("father")){
				// String father = line.split(":")[1].trim();
				// tree.put(pName,father);
				// // System.out.println(" father of "+ pName+ " "+ father);
				//
				// }
				if (line.contains("finishMode")) {
					String mode = line.split(":")[1].trim();
					boolean outOfTime = !mode.equals("END");
					timeOut.put(pName, outOfTime);
				}
				if (line.contains("dpllTime")) {
					String time = line.split(":")[1].trim();
					dpllTime = Double.parseDouble(time);
				}
				if (line.contains("totalTime")) {
					String time = line.split(":")[1].trim();
					totalTime = Double.parseDouble(time);
				}

				if (line.contains("maxNbResultStore")) {
					String nb = line.split(":")[1].trim();
					maxNbResultStore.put(pName, Integer.parseInt(nb));
				}

				if (line.contains("neighbors")) {
					String neighs = line.split(":")[1].trim();

					// System.out.println(" neighborhood of "+ pName+ " "+
					// neighs);

					neighs = neighs.replace(Character.toString('['), "");
					neighs = neighs.replace(Character.toString(']'), "");

					graph.put(pName, new ArrayList<String>());
					for (String neigh : neighs.split(",")) {
						if (!graph.keySet().contains(neigh.trim()))
							graph.get(pName).add(neigh.trim());
					}
				}
				line = br.readLine();
			}

			if ((dpllTime / totalTime) > 0.8) {
				slowDpll.put(pName, (dpllTime / totalTime) > 0.8);
			}

			br.close();
		}

		// Removing the parent peer from the neighbors
		// for(String p : tree.keySet()){
		// graph.get(p).remove(tree.get(p));
		// graph.get(tree.get(p)).remove(p);
		// }

		// tree.remove(pStarter);

		String dot = "digraph \"G\"{ \n";

		// First we draw the tree
		// ArrayList<String> bfsTree = bfs(tree, new ArrayList<String>() ,0);
		// dot+="node[width=.25,height=.375,fontsize=9] ;\n";

		// for(String p : bfsTree){
		// dot += p+"-> "+
		// tree.get(p)+" [style=bold,color="+ColorDot.values()[icolor]+",label="+maxNbResultStore.get(p)+"]; \n";
		// }
		//		
		// Then we shape the vertex
		// dot+= pStarter+" [shape=Mdiamond]; \n";
		// for(String p : bfsTree){
		// String nodeShape = "";
		// String nodeBgColor = "";
		// String style="";
		// if(timeOut.get(p)){
		// style = ",style=filled";
		// nodeBgColor =",color=lightgrey";
		// if(slowDpll.keySet().contains(p)){
		// nodeShape=",shape=diamond";
		// }
		// dot += p+" [label="+p+style+nodeBgColor+nodeShape+"]; \n";
		// }
		//			
		// }

		// Afterwards we add the other edges
		for (String p : graph.keySet()) {
			for (String n : graph.get(p))
				dot += p + "-> " + n + " [arrowhead=none]; \n";
		}

		dot += " }\n ";

		// System.out.println(rep.getPath());

		File f = new File(rep.getPath() + File.separator + "graphBis" + icolor
				+ ".dot");
		Writer output;
		try {
			output = new BufferedWriter(new FileWriter(f));
			output.write(dot);
			output.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		Runtime.getRuntime().exec(
				"dot -Tps " + f.getPath() + " -o " + f.getPath() + ".ps");
		Runtime.getRuntime().exec("dot -Tgif " + f.getPath() + " -o " + f.getPath() + ".gif");
		
	}

	public static void drawTree(File dirRes, File rep, int icolor)
			throws Exception {

		TreeMap<String, Integer> maxNbResultStore = new TreeMap<String, Integer>();
		TreeMap<String, Boolean> timeOut = new TreeMap<String, Boolean>();
		TreeMap<String, Boolean> slowDpll = new TreeMap<String, Boolean>();
		TreeMap<String, String> tree = new TreeMap<String, String>();
		TreeMap<String, ArrayList<String>> graph = new TreeMap<String, ArrayList<String>>();

		// System.out.println(" dirRes "+ dirRes);
		// System.out.println(" dirRes  children");
		// for(String f :dirRes.list())
		// System.out.println(f);

		String[] peersStat = dirRes.list(new FileFilter(".stat"));

		// System.out.println(" selected  children");
		// for(String f :peersStat)
		// System.out.println("coucou "+f);
		if (peersStat.length == 0)
			return;

		for (String pStat : peersStat) {

			// System.out.println(" pStat "+ pStat);

			String pName = pStat.replaceAll(".stat", "").trim();

			File fstat = new File(dirRes.getPath() + File.separator + pStat);
			BufferedReader br = new BufferedReader(new FileReader(fstat));
			String line = br.readLine();

			double totalTime = 0;
			double dpllTime = 0;
			while (line != null) {
				if (line.contains("father:")) {
					String father = line.split(":")[1].trim();
					tree.put(pName, father);
					// System.out.println(" father of "+ pName+ " "+ father);

				}
				if (line.contains("finishMode")) {
					String mode = line.split(":")[1].trim();
					boolean outOfTime = !mode.equals("END");
					timeOut.put(pName, outOfTime);
				}
				if (line.contains("dpllTime")) {
					String time = line.split(":")[1].trim();
					dpllTime = Double.parseDouble(time);
				}
				if (line.contains("totalTime")) {
					String time = line.split(":")[1].trim();
					totalTime = Double.parseDouble(time);
				}

				if (line.contains("maxNbResultStore")) {
					String nb = line.split(":")[1].trim();
					maxNbResultStore.put(pName, Integer.parseInt(nb));
				}

				if (line.contains("neighbors")) {
					String neighs = line.split(":")[1].trim();

					// System.out.println(" neighborhood of "+ pName+ " "+
					// neighs);

					neighs = neighs.replace(Character.toString('['), "");
					neighs = neighs.replace(Character.toString(']'), "");

					// graph.put(pName, new ArrayList<String>());
					// for(String neigh :neighs.split(",")){
					// if(!graph.keySet().contains(neigh.trim()))
					// graph.get(pName).add(neigh.trim());
					// }
				}
				line = br.readLine();
			}

			if ((dpllTime / totalTime) > 0.8) {
				slowDpll.put(pName, (dpllTime / totalTime) > 0.8);
			}

			br.close();
		}

		// Removing the parent peer from the neighbors
		// for(String p : tree.keySet()){
		// graph.get(p).remove(tree.get(p));
		// graph.get(tree.get(p)).remove(p);
		// }

		// tree.remove(pStarter);

		String dot = "digraph \"G\"{ \n";

		// First we draw the tree
		ArrayList<String> bfsTree = bfs(tree, new ArrayList<String>(), 0);
		dot += "node[width=.25,height=.375,fontsize=9] ;\n";

		for (String p : bfsTree) {
			dot += p + "-> " + tree.get(p) + " [style=bold,color="
					+ ColorDot.values()[icolor] + ",label="
					+ maxNbResultStore.get(p) + "]; \n";
		}

		// Then we shape the vertex
		// dot+= pStarter+" [shape=Mdiamond]; \n";
		for (String p : bfsTree) {
			String nodeShape = "";
			String nodeBgColor = "";
			String style = "";
			if (timeOut.get(p)) {
				style = ",style=filled";
				nodeBgColor = ",color=lightgrey";
				if (slowDpll.keySet().contains(p)) {
					nodeShape = ",shape=diamond";
				}
				dot += p + " [label=" + p + style + nodeBgColor + nodeShape
						+ "]; \n";
			}

		}

		// Afterwards we add the other edges
		// for(String p : tree.keySet()){
		// for(String n: graph.get(p))
		// dot+= p+"-> "+n+" [arrowhead=none,style=dashed]; \n";
		// }

		dot += " }\n ";

		// System.out.println(rep.getPath());

		File f = new File(rep.getPath() + File.separator + "treeBis" + icolor
				+ ".dot");
		Writer output;
		try {
			output = new BufferedWriter(new FileWriter(f));
			output.write(dot);
			output.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		Runtime.getRuntime().exec(
				"dot -Tps " + f.getPath() + " -o " + f.getPath() + ".ps");
		Runtime.getRuntime().exec("dot -Tgif " + f.getPath() + " -o " + f.getPath() + ".gif");
	}

	/*
	 * return the breadth first search of the tree
	 */
	private static ArrayList<String> bfs(TreeMap<String, String> tree,
			ArrayList<String> bfsList, int curI) {

		if (bfsList.size() >= tree.size() || (curI + 1) >= tree.size()
				|| tree.isEmpty()){
			return bfsList;
		}
		if (bfsList.isEmpty()) {
			// search of the root
			for (String k : tree.keySet())
				if (k.equals(tree.get(k))){
					bfsList.add(k);
					
				}
			if(bfsList.isEmpty()){
//				Dprint.println("The root has not been found");
				for (String k : tree.keySet())
					Dprint.println(k+ " -> "+tree.get(k));
				return bfsList;
			}else{
//				Dprint.println("The root has been found "+bfsList.get(0));
			}
				
			return bfs(tree, bfsList, 0);
		} else {
			int prevlast = bfsList.size();
			for (int i = curI; i < prevlast; i++) {
				// search node that are directed to bfsList.get(i)
				for (String k : tree.keySet()) {
					if (bfsList.get(i).equals(tree.get(k))) {
						if (!bfsList.contains(k))
							bfsList.add(k);
					}
				}
			}
			if (prevlast >= bfsList.size())
				return bfsList;
			else 
				return bfs(tree, bfsList, prevlast);
		}

	}

}
