package benchMarkGenerator.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import main.ArgsHandler;


public class RandomGraphs {
	
	
	public static TreeMap<Integer,TreeSet<Integer>> genBAGraph(HashMap<String, Object> params){
		
		int nbPeers = Integer.parseInt((String) params.get("nbPeers"));
		
		TreeMap<Integer,TreeSet<Integer>> g= new TreeMap<Integer,TreeSet<Integer>>();
		
		// initialization we create a link between 0 and 1 
		g.put(0, new TreeSet<Integer>());
		g.put(1, new TreeSet<Integer>());
		g.get(0).add(1);
		g.get(1).add(0);
		
		// current sum of degree
		int sd=2 ;

		for(int p=2;p<nbPeers;p++){
			g.put(p, new TreeSet<Integer>());
			boolean atLeastOneConnection = false;
			while(atLeastOneConnection == false){
				for(int pPrec=0; pPrec<p ; pPrec++){
					boolean connection = Math.random()<= (double)g.get(pPrec).size()/(double)sd;
					if(connection){
						g.get(p).add(pPrec);
						g.get(pPrec).add(p);
						sd+=2;
						atLeastOneConnection = true;
					}
						
				}
			}
		}
		
		return g;
	}
	
	public static TreeMap<Integer,TreeSet<Integer>> genGammaBAGraph(HashMap<String, Object> params){
		
		int nbPeers = Integer.parseInt((String) params.get("nbPeers"));
		double gamma=  ArgsHandler.defaultDblIfAny("gammaBA", 1.0, params);
		
		// nbOccK[i]  return the number of nodes of degree kinitial to 0
		int[] nbNodesOfDegree = new int[nbPeers];
		TreeMap<Integer,TreeSet<Integer>> g= new TreeMap<Integer,TreeSet<Integer>>();
		
		// initialization we create a link between 0 and 1 
		g.put(0, new TreeSet<Integer>());
		g.put(1, new TreeSet<Integer>());
		g.get(0).add(1);
		g.get(1).add(0);
		nbNodesOfDegree[1]+=2;

		for(int p=2;p<nbPeers;p++){
			g.put(p, new TreeSet<Integer>());
			boolean atLeastOneConnection = false;
			while(atLeastOneConnection == false){
				for(int pPrec=0; pPrec<p ; pPrec++){
					double kPowGammaGPrec = Math.pow((double)g.get(pPrec).size(),gamma);
//					*(double)nbNodesOfDegree[g.get(pPrec).size()]/(double)g.size();
					
//					System.out.println("kPowGammaGPrec " + kPowGammaGPrec);
					
					double sumkPowGammaPk =0.0;
					for(int k=1;k<g.size();k++){
						if(nbNodesOfDegree[k]>0)
						sumkPowGammaPk+= Math.pow((double)k,gamma);
//						*(double)nbNodesOfDegree[k]/(double)g.size();
					}
					
//					System.out.println("p sumkPowGammaPk " + sumkPowGammaPk);
					
//					System.out.println("p Attachement " + kPowGammaGPrec/sumkPowGammaPk);
					
					boolean connection = Math.random()<= kPowGammaGPrec/sumkPowGammaPk;
					if(connection){
						nbNodesOfDegree[g.get(p).size()]--;
						g.get(p).add(pPrec);
						nbNodesOfDegree[g.get(p).size()]++;
						
						nbNodesOfDegree[g.get(pPrec).size()]--;
						g.get(pPrec).add(p);
						nbNodesOfDegree[g.get(pPrec).size()]++;
						atLeastOneConnection = true;
						
//						for(int k=1;k<g.size();k++){
//							System.out.println(" nbNodesOfDegree["+k+"] "+nbNodesOfDegree[k]);
//						}
//						System.out.println(g+" ");
						
					}
						
				}
			}
		}
		
		return g;
	}

	
	public static TreeMap<Integer,TreeSet<Integer>> genWSGraph(HashMap<String, Object> params) {
		int nbPeers = Integer.parseInt((String) params.get("nbPeers"));
			
		int k= (int) Math.round(Math.log( (double)nbPeers));
		double neighIncr =ArgsHandler.defaultDblIfAny("neighIncr", 0.0, params);
		
		int neighDist = k+ (int)(neighIncr*(double)nbPeers);
		neighDist=(neighDist%2==1)?neighDist+1:neighDist;
		neighDist = neighDist/2;
		double rReWire =  ArgsHandler.defaultDblIfAny("rReWire", 0.2, params);

		// init Variables, graphs in the sequel are bidirectional
		TreeMap<Integer,TreeSet<Integer>> g = new TreeMap<Integer,TreeSet<Integer>>();
		TreeMap<Integer,TreeSet<Integer>> nvG= new TreeMap<Integer,TreeSet<Integer>>();
		TreeMap<Integer,TreeSet<Integer>> remG= new TreeMap<Integer,TreeSet<Integer>>();
		for(int p=0;p<nbPeers;p++){
			g.put(p, new TreeSet<Integer>());
			nvG.put(p, new TreeSet<Integer>());
			remG.put(p, new TreeSet<Integer>());
		}
		
		// create the ring
		for(int p=0;p<nbPeers;p++){	
			for(int i=1;i<=neighDist;i++){
				int neigh = (p+i)%nbPeers;
				g.get(p).add(neigh);
				g.get(neigh).add(p);
			}
		}
		
//		System.out.println(" create the ring ");
//		System.out.println(genGraph2String(g));
		
		// store old and new edges involved by the random rewiring
		for(int p=0;p<nbPeers;p++){
			for(int i=1;i<=neighDist;i++){
				double rand = Math.random();
//				System.out.println(" rem: "+rand+" "+reLinkRate);
				if(rand<rReWire){
//					System.out.println("rem process");
					// disconect a selected neighbor
					int neigh = (p+i)%nbPeers;
					remG.get(p).add(neigh);
					remG.get(neigh).add(p);
					// seek for an other available neighbor to reconnect
					
						int nbAvailableNeighs = nbPeers -1 -(2* neighDist)- nvG.get(p).size();
						if(nbAvailableNeighs<=0)
							break;
						int relativePosNeigh = (int)(Math.random()* (double)nbAvailableNeighs);
						boolean find = false;
						int avaiblePos =0;
						int nvNeigh =0;
						while(!find){
							if( (nvNeigh !=p && !nvG.get(p).contains(nvNeigh) && !g.get(p).contains(nvNeigh))
									 || remG.get(p).contains(nvNeigh)){
								if(avaiblePos==relativePosNeigh){
									nvG.get(p).add(nvNeigh);
									nvG.get(nvNeigh).add(p);
									find = true;
								}
								if( remG.get(p).contains(nvNeigh)){
									remG.get(p).remove(nvNeigh);
									remG.get(nvNeigh).remove(p);
								}
								
								avaiblePos++;
							}
							nvNeigh++;
						}
						
					}
				}
			}
			
		
		
		// update the ring
		for(Integer p :g.keySet()){	
			for(Integer neigh :remG.get(p))
				g.get(p).remove(neigh);
			for(Integer neigh :nvG.get(p))
				g.get(p).add(neigh);
		}
		
		return g;
	}
	
	
	
	// interaction density
	public static TreeMap<Integer,TreeSet<Integer>> genUDGraph(
			HashMap<String, Object> params) {
		
		int nbPeers = Integer.parseInt((String) params.get("nbPeers"));
		double density = ArgsHandler.defaultDblIfAny("density", 0.4, params);
		
		TreeMap<Integer,TreeSet<Integer>> g = new TreeMap<Integer,TreeSet<Integer>>();
		
		for(int p=0;p<nbPeers;p++){
			g.put(p, new TreeSet<Integer>());
		}
		
		for(int p1=0;p1<nbPeers-1;p1++){
			for(int p2=p1+1;p2<nbPeers;p2++){
				if(Math.random()<=density){
					if(!g.get(p1).contains(p2)){
						g.get(p1).add(p2);
						g.get(p2).add(p1);
					}
				}
			}
		}
		// System.out.println(benchMarkGenerator.graph.RandomGraphs.genGraph2String(g));
		
		return g;
		
	}
	
	
	

	public static boolean isConnected(TreeMap<Integer,TreeSet<Integer>> g){
		if (g==null || g.isEmpty())
			return false;
		Integer curNode = g.firstKey();
		ArrayList<Integer> seenNodes = new ArrayList<Integer>();
		seenNodes.add(curNode);
		return isConnected(g,curNode,seenNodes);
	}
	
	private static boolean isConnected(TreeMap<Integer,TreeSet<Integer>> g,Integer curNode, ArrayList<Integer> seenNodes){
		
		for(Integer neighbor : g.get(curNode)){
			if(!seenNodes.contains(neighbor)){
				seenNodes.add(neighbor);
			}
		}
		if(seenNodes.size()==g.keySet().size())
			return true;
		int iNextNode = seenNodes.indexOf(curNode)+1;
		if (iNextNode>= seenNodes.size())
			return false;
		
		return isConnected(g,seenNodes.get(iNextNode),seenNodes);
		
	}
	
	
	public static String genGraph2String(TreeMap<Integer,TreeSet<Integer>> g){
		String s = "";
		for(int p : g.keySet()){
			s=s+p+" -> ";
			for(int  v : g.get(p))
				s=s+v+" ";
			s=s+"\n";
		}
		return s;
	}
	
	public static int nbEdges(TreeMap<Integer,TreeSet<Integer>> g){
		int nbEdges = 0;
		for(int p : g.keySet()){
			nbEdges += g.get(p).size();
		}
		return nbEdges/2;
	}
	
	public static double density(TreeMap<Integer,TreeSet<Integer>> g){
		return (double)nbEdges(g)/ (double)((g.size())*(g.size()));
	}
	
	public static double maxDegre(TreeMap<Integer,TreeSet<Integer>> g){
		int max = 0;
		for(Integer p: g.keySet())
			if(g.get(p).size()>max)
				max = g.get(p).size();
		return max;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		for(int nbPeers =10;nbPeers<=1000;nbPeers*=10)
//		for(double eps =0.2;eps<=3.0;eps+=0.2){
//		HashMap<String,Object> params = new HashMap<String,Object>();
//		params.put("nbPeers",""+nbPeers);
//		params.put("gammaBA",""+(eps));
//		TreeMap<Integer,TreeSet<Integer>> g = genGamma2BAGraph(params);
//		System.out.println("nbPeers:"+nbPeers+"  gammaBA:"+(eps)
//				+"  density:"+density(g)+ " maxDegree:"+maxDegre(g));
//		}
		
		System.out.println(Math.log(20)+" "+0.1*20);
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("nbPeers",""+20);
		params.put("neighIncr",0.3);
		TreeMap<Integer,TreeSet<Integer>> g = genWSGraph(params);
		System.out.println(genGraph2String(g));
//		
//		params.put("nbPeers",20);
//		TreeMap<Integer,TreeSet<Integer>> g = genUDGraph(params);
//		System.out.println(genGraph2String(g));
//		
//		params.put("density", 0.7);
//		g = genUDGraph(params);
//		System.out.println(genGraph2String(g));
//		


	}

}
