package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.TreeMap;



public class GraphMaker {
	
	public static  int nGDraw =0;

	/*
	 * This method draws edges corresponding to expr
	 * expr should be present in stat file of the peer.
	 */
	public static void drawclustersOfChosenFathers(File dirRes, File rep,int radMax, int icolor)
	throws Exception {
		
		
		ArrayList<String> layersRad = new ArrayList<String>();
		
		
		String dot = "digraph allGraphesFatherChoseAt { \n";
		
		nGDraw =0;
		
		for (int rad=0;rad <= 5;rad++){
			 dot+="subgraph fatherChoseAt"+rad+" {\n";
			 dot+="color=lightgrey  ;\n";
			 dot += graphOfExpr2Dot(dirRes, true,  "fatherChoseAt"+rad,"style=bold color="+ColorDot.values()[icolor]);
			 dot += graphOfExpr2Dot(dirRes, false,  "neighbors","arrowhead=none");
			  
			 nGDraw++;
//			 dot += graphOfExpr2Dot(dirRes, rep,  "fatherChoseAt"+rad,"style=bold color="+ColorDot.values()[icolor]);
//			 nGDraw++;
			 dot+=" }\n ";
		}
		dot+=" }\n ";
		 
		 File f = tools.FileTools.writeContentInFile(dot,rep + File.separator + "allGraphesFatherChoseAt"+".dot");
			Runtime.getRuntime().exec(
					"dot -Tps " + f.getPath() + " -o " + f.getPath() + ".ps");
			Runtime.getRuntime().exec(
					"neato -Tps " + f.getPath() + " -o " + f.getPath() + "v2.ps");
			
//		 dot = "digraph \"allGrapheOfClustersFatherChoseAt\"{ \n";
//			for (int rad=0;rad <= 5;rad++){
//				 dot+="subgraph fatherChoseAt"+rad+" {\n";
//				 dot+="color=lightgrey ; \n";
//				 dot += graphOfExpr2Dot(dirRes, rep,  "fatherChoseAt"+rad, icolor);
//				 dot+=" }\n ";
//				 
//			}
//			 dot+=" }\n ";
//			 
//			  f = tools.FileTools.writeContentInFile(dot,rep + File.separator + "allGrapheOfClustersFatherChoseAt"+".dot");
//				Runtime.getRuntime().exec(
//						"dot -Tps " + f.getPath() + " -o " + f.getPath() + ".ps");

	}
	
	
	/*
	 * This method draws edges corresponding to expr
	 * expr should be present in stat file of the peer.
	 */
	public static void drawGraphOfExpr(File dirRes, File rep, String expr,int icolor)
	throws Exception {
	
		
		String dot = "digraph \"G\"{ \n";
		 dot += graphOfExpr2Dot(dirRes, true,  expr,"style=bold,color="+ColorDot.values()[icolor]);
		 dot += graphOfExpr2Dot(dirRes, false,  "neighbors","arrowhead=none");
		 dot+=" }\n ";
		File f = tools.FileTools.writeContentInFile(dot,rep + File.separator + expr+".dot");
		Runtime.getRuntime().exec(
				"dot -Tps " + f.getPath() + " -o " + f.getPath() + ".ps");
		
	}
	
	public static String graphOfExpr2Dot(File dirRes, boolean digraph, String expr,
			String edgeOpt)
	throws Exception {

		TreeMap <String,ArrayList<String>> goe = diGraphOfExpr(dirRes, expr);

		String dot = "";

		ArrayList<String> vistedKeys = new ArrayList<String>();
		for(String k : goe.keySet()){
			for(String p: goe.get(k)){
				if(! digraph)
					if(vistedKeys.contains(p))
						if(goe.get(p).contains(k))
							continue;
				dot += "g"+nGDraw+k+" -> "+ "g"+nGDraw+p+
				"["+edgeOpt+"]"+";\n";
			}
			vistedKeys.add(k);
		}
		return dot;
	}
	
	public static TreeMap <String,ArrayList<String>> diGraphOfExpr(File dirRes,String expr)
	throws Exception {
		TreeMap <String,ArrayList<String>> goe = new TreeMap<String,ArrayList<String>>();
		String [] peersStat = dirRes.list(new FileFilter(".stat"));

		for(String pStat : peersStat){
			String pName =pStat.replaceAll(".stat", "").trim();

			File fstat = new File(dirRes.getPath()+File.separator+pStat);
			BufferedReader br = new BufferedReader(new FileReader(fstat));
			String line = br.readLine();
			while(line!=null){
		
				if(line.contains(expr)){
					String value = line.split(":")[1].trim();
					if(!goe.containsKey(pName))
						goe.put(pName,new ArrayList<String>());
					
					if(expr.contains("neighbors")){
						String neighs = line.split(":")[1].trim();
						neighs = neighs.replace(Character.toString('['),"");
						neighs = neighs.replace(Character.toString(']'),"");
						for(String neigh :neighs.split(",")){
							goe.get(pName).add(neigh.trim());
						}
					}else
						goe.get(pName).add(value);
					
				}
				
				
				
				line = br.readLine();
			}
			br.close();
		}
		
//		for(String k : goe.keySet()){
//			System.out.println(k+" "+goe.get(k));
//		}
	
		return goe;
	}
	
	
	

	
	public static void drawTree(File dirRes,String pStarter,int icolor)throws Exception{

		TreeMap <String,String> tree = new TreeMap<String,String>();
		TreeMap <String,ArrayList<String>> graph = new TreeMap<String, ArrayList<String>>();

//		System.out.println(" dirRes "+ dirRes);
//		System.out.println(" dirRes  children");
//		for(String f :dirRes.list())
//			System.out.println(f);

		
		String [] peersStat = dirRes.list(new FileFilter(".stat"));

//		System.out.println(" selected  children");
//		for(String f :peersStat)
//			System.out.println(f);



		for(String pStat : peersStat){

//			System.out.println(" pStat "+ pStat);

			String pName =pStat.replaceAll(".stat", "").trim();

			File fstat = new File(dirRes.getPath()+File.separator+pStat);
			BufferedReader br = new BufferedReader(new FileReader(fstat));
			String line = br.readLine();
			while(line!=null){
				if(line.contains("father")){
					String father = line.split(":")[1].trim();
					tree.put(pName,father);

//					System.out.println(" father of "+ pName+ " "+ father);

				}
				if(line.contains("neighbors")){
					String neighs = line.split(":")[1].trim();

//					System.out.println(" neighborhood of "+ pName+ " "+ neighs);

					neighs = neighs.replace(Character.toString('['),"");
					neighs = neighs.replace(Character.toString(']'),"");

					graph.put(pName, new ArrayList<String>());
					for(String neigh :neighs.split(",")){
						if(!graph.keySet().contains(neigh.trim()))
							graph.get(pName).add(neigh.trim());
					}
				}
				line = br.readLine();
			}
			br.close();
		}
		
		

		for(String p : tree.keySet()){
			Dprint.println(p+" father\'s : "+tree.get(p));
		}
		for(String p : graph.keySet()){
			Dprint.println(p+" neighbours"+graph.get(p));
		}
		
		
		// Removing the tree link! from the graph; 
		for(String p : tree.keySet()){
			graph.get(p).remove(tree.get(p));
			graph.get(tree.get(p)).remove(p);
		}
		
		tree.remove(pStarter);

		String dot = "digraph \"G\"{ \n";
		dot+= pStarter+" [shape=Mdiamond]; \n";
		
		// First we draw the tree
		for(String p : bfs(tree, new ArrayList<String>() ,0)){
			dot += p+"-> "+ tree.get(p)+" [style=bold,color="+ColorDot.values()[icolor]+"]; \n";
		}
		
		// Afterwards we add the other edges 
		for(String p : tree.keySet()){
			for(String n: graph.get(p))
				dot+= p+"-> "+n+" [arrowhead=none,style=dashed]; \n";
		}
		dot+=" }\n ";
		File f = new File(dirRes + File.separator + "tree.dot");
		Writer output;
		try {
			output = new BufferedWriter(new FileWriter(f));
			output.write(dot);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/*
	 * return the breadth first search of the tree
	 */
	private static  ArrayList<String> bfs(TreeMap <String,String> tree,
			ArrayList<String> bfsList, int curI ){
		
		if(bfsList.size()== tree.size())
			return bfsList;
		
		for(String k: tree.keySet()){
			return bfsList;
		}
		
		if(bfsList.isEmpty()){
			// search of the root
			for(String k: tree.keySet())
				if(k.equals(tree.get(k)))
					bfsList.add(k);
			curI = 0;
		}else{
			int last = bfsList.size();
			for(int i = curI;i<last;i++){
				for(String k:tree.keySet()){
					if(tree.get(bfsList.get(i)).equals(k)){
						bfsList.add(k);
					}
				}
			}
			curI ++;
		}
		return bfs(tree,bfsList,curI);
	}

	
}
