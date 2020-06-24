package distributedAlgorithm.jtdt;


import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import peers.PeerDescription;
//import propositionalLogic.DPLLIter;

import tools.DefaultComparator;
import tools.Graph;

/*
 * Ce que je veux faire dans cette classe
 *  - construitre des décompositions arborescentes à partir d'un répertoire contenant des descriptions de pbs
 *  - les décompositions arborescentes se déclinent en plusieurs heuristiques
 *  - elle prennent en compte ou pas une considération de localité des variables.
 *  
 *  
 * Comment je compte m'y prendre
 *  - Un constructeur charge les fichiers d'un répertoire.
 *  - la methode jtFrom (Objet de la décomp,algoDeLiaisonDeCluster, HeuristiqueDeClustering, UnePolitiqueDeConfidentialité),
 *  	elle retourne un jointree.
 *  
 *  
 *  Une première implémentation:
 *  	Objet de la décomp: variable
 *  	Heuristique : min-fill, 
 *  	algo de Liaison : liaison en rapport avec min-fill 
 *  		Connnexion des clusters dans l'ordre inverse de leurs création
 *  		La connexion se fait avec un cluster de plus grande instersection 
 *  		non vide et déjà connecté
 *  
 *  Ne peut on pas généraliser l'objet de la décomposition ?
 *  	litéral, variable, clauses, pairs ?
 *  
 *  La décomposition s'effectue sur des objets quelconques en relation.
 *  	Dans mon implémentation je peux considéré une couche abstraite qui construit des jt.
 *  	il faut que j'ai une façon de représenter des relations et les objets
 *  	une méthodes des {var,pairs, clauses} vers ces objet en relations
 *  
 *  	Et la confidentialité dans tout ça ?
 *		
 *		La confidentialité établit une relation particulière entre les varibles d'un même pair
 *			établissement d'une clique.
 *
 * 
 * */
public class PeerDescriptions2Jointree {
	
	TreeMap<String,PeerDescription> _peerDescriptions ;
	DefaultComparator<TreeSet<String>> _c;
	
	public PeerDescriptions2Jointree(ArrayList<PeerDescription> peerDescriptions){
		_peerDescriptions = new TreeMap<String, PeerDescription>();
		_c = new DefaultComparator<TreeSet<String>>();
		for(PeerDescription pD:peerDescriptions)
			_peerDescriptions.put(pD.get_pName(), pD);
	}
	
	public PeerDescriptions2Jointree(String dirName){
		_peerDescriptions = new TreeMap<String, PeerDescription>();
		_c = new DefaultComparator<TreeSet<String>>();
		ArrayList<String> litsString = new ArrayList<String>();
		ArrayList<String> target = new ArrayList<String>();
		ArrayList<ArrayList<String>> allTh = new ArrayList<ArrayList<String>>();
		sat4JAdapt.Sat4J.dir2OneDesc(new File(dirName), litsString, target, allTh);
	}
		

//	public TreeMap<String,MasterPeer> peers2Jointree(){
//		
//		// builds Graph of peers
//		Graph<String> gPName = new Graph<String>();
//		for(PeerDescription pD : _peerDescriptions.values()){
//			gPName.addVertex(pD.get_pName());
//			for(String neigh : pD.get_neighbors())
//				if(gPName.get_nodes().contains(neigh))
//					gPName.addEdge(pD.get_pName(), neigh);
//		}
//		
//		// taking into account the min fill heuristic
//		for( String pName : gPName.get_nodes())
//			gPName.updateMinfill(pName);
//		
//		System.out.println(gPName);
//		
//		Graph<TreeSet<String>> treeOfpNamesSet = gPName.toJoinTree();
//		
//		System.out.println(treeOfpNamesSet);
//		
//		// return a Jointree of Master Peer
//		Graph<MasterPeer> g = new Graph<MasterPeer>();
//		
//		TreeMap<TreeSet<String>,MasterPeer> nodes2MasterPeers =
//			new TreeMap<TreeSet<String>,MasterPeer>(_c);
//		
// 		for(TreeSet<String> nod : treeOfpNamesSet.get_nodes()){
//			for(String p : nod ){
//				if(!nodes2MasterPeers.containsKey(nod)){
//					nodes2MasterPeers.put(nod, new MasterPeer());
//					g.addVertex(nodes2MasterPeers.get(nod));
//				}
//				nodes2MasterPeers.get(nod).set_mPName(createName(nod));
//				nodes2MasterPeers.get(nod).get_diagLit().addAll(_peerDescriptions.get(p).get_diagLit());
//				nodes2MasterPeers.get(nod).get_peers2RImpl().put(p,_peerDescriptions.get(p).getRImplicants());
//				nodes2MasterPeers.get(nod).get_shVocabulary().addAll(_peerDescriptions.get(p).get_locShLit());
//			}
//			
//			TreeSet<String> localPeersSet = (TreeSet<String>) nod.clone();
//			for(TreeSet<String> nodNeigh :treeOfpNamesSet.get_neighbors().get(nod)){
//				if(!nodes2MasterPeers.containsKey(nodNeigh)){
//					nodes2MasterPeers.put(nodNeigh, new MasterPeer());
//					g.addVertex(nodes2MasterPeers.get(nodNeigh));
//					g.addEdge(nodes2MasterPeers.get(nod), nodes2MasterPeers.get(nodNeigh));
//				}
//				localPeersSet.removeAll(nodNeigh);
//				nodes2MasterPeers.get(nod).get_neighbors().add(createName(nodNeigh));	
//			}
//			
//			// update the Shared vocabulary of the Master Peer
//			if(!localPeersSet.isEmpty()){
//				ArrayList<String> lit2Rem = new ArrayList<String>();
//				for(String l : nodes2MasterPeers.get(nod).get_shVocabulary())
//					for(String p :localPeersSet)
//						if(l.contains(p))
//							lit2Rem.add(l);
//				nodes2MasterPeers.get(nod).get_shVocabulary().removeAll(lit2Rem);
//			}
//		}
// 		return g ;
//	}
//	
//	private String createName(TreeSet<String> listPname){
//		String s = "";
//		for(String name :listPname )
//			s+= name+" ";
//		return s; 
//	}
	
	public Graph<PeerDescription> clauses2Jointree(){
		return null ;
	}
	/*
	 * This is a centralized version of the distributed construction of jointree
	 * preserving privacy.
	 */
	public TreeSet<MasterPeer> vars2JointreeWithPrivacy(){
		
		// 1) builds a jointree based on the shared vocabulary and peers abstraction
		
		Graph<String> gShVars = new Graph<String>();
		for(PeerDescription pD :_peerDescriptions.values())
			addTh2GraphOfVars(pD.get_thMappings(), gShVars);
		
		// pName:loc substitutes the  local variables of the peer pName 
		// local connections are modeled by a clique containing  between
		// pName:loc and the shared vocabulary.
		for(String pName :_peerDescriptions.keySet()){
			ArrayList<String> f = new ArrayList<String>();
			f.add(pName+":loc");
			
//			A Revoir
//			for(String lit :_peerDescriptions.get(pName).get_locShLit()){
//				String v = getVariable(lit);
//				if(!f.contains(v))
//					f.add(v);
//			}
			ArrayList <ArrayList<String>>th = new ArrayList<ArrayList<String>>();
			th.add(f);
			addTh2GraphOfVars(th, gShVars);
		}
		
		@SuppressWarnings("unused")
		Graph<TreeSet<String>> treeOfSHVarsSet = gShVars.toJoinTree();
		// les transformer en master peer
		return null;
		
		// 2) builds locals trees
		
	}
	
	public void addTh2GraphOfVars(ArrayList<ArrayList<String>> th, Graph<String> gVars){
		for(int indCl =0; indCl<th.size();indCl++){
			for(int indL=0;indL<th.get(indCl).size();indL++){
				String v = getVariable(th.get(indCl).get(indL));
				if (!gVars.get_nodes().contains(v)) {
					gVars.addVertex(v);
				}
				for (int indNL = indL - 1; indNL >= 0; indNL--) {
					String vNeigh = getVariable(th.get(indCl).get(
							indNL));
					if (!gVars.get_neighbors().get(v).contains(vNeigh))
						gVars.addEdge(v, vNeigh);
				}
			}
		}
	}
	
	
	public TreeSet<MasterPeer> variables2Jointree(){
		
		Graph<String> gVars = new Graph<String>();
		TreeMap<String,TreeSet<Number>> var2Cls = new TreeMap<String, TreeSet<Number>>();
		
		for(String pName : _peerDescriptions.keySet()){
			_peerDescriptions.get(pName).get_th().addAll(_peerDescriptions.get(pName).get_thMappings());
			PeerDescription pD = _peerDescriptions.get(pName) ;
			for(int indCl =0; indCl<pD.get_th().size();indCl++){
				for(int indL=0;indL<pD.get_th().get(indCl).size();indL++){
					String v = getVariable(pD.get_th().get(indCl).get(indL));
					if(!gVars.get_nodes().contains(v)){
						gVars.addVertex(v);
					}
					for(int indNL=indL-1;indNL>=0;indNL--){
						String vNeigh =getVariable(pD.get_th().get(indCl).get(indNL));
						if(!gVars.get_neighbors().get(v).contains(vNeigh))
							gVars.addEdge(v, vNeigh);
					}
					if(extractPeerFromLit(v).equals(pName)){
					if(var2Cls.keySet().contains(v)){
						var2Cls.get(v).add(indCl);
					}else{
							//System.out.println("Variable "+v+" "+pName );
							TreeSet<Number> setOfIndCl = new TreeSet<Number>();
							setOfIndCl.add(indCl);
							var2Cls.put(v, setOfIndCl);
						}
					}
				}
			}
			//pD.get_th().removeAll(pD.get_thMappings());
		}
		
		for(String v: gVars.get_nodes())
			gVars.updateMinfill(v);
		
		Graph<TreeSet<String>> treeOfVarsSet = gVars.toJoinTree();
		
		System.out.println(treeOfVarsSet);

		
		// retrun a Jointree of Master Peers
		TreeSet<MasterPeer> gMasters = new TreeSet<MasterPeer>(new DefaultComparator<MasterPeer>());
		for (TreeSet<String> nod : treeOfVarsSet.get_nodes()) {
//			System.out.println("nod " + nod);
			ArrayList<ArrayList<String>> mpTh = new ArrayList<ArrayList<String>>();
			TreeSet<String> peersName = new TreeSet<String>();
			MasterPeer mP = new MasterPeer();
			
//			System.out.println(mP.rImplicants2String());
			
			mP.set_mPName(nod.toString());
			gMasters.add(mP);
			for (String v : nod) {
//				System.out.println("  var " + v);
				String pName = extractPeerFromLit(v);
				peersName.add(pName);
				for (Number indCl : var2Cls.get(v)) {
					// check if the node's vocabulary covers the clause's
					// vocabulary
					boolean clInNod = true;
					ArrayList<String> cl = _peerDescriptions.get(pName)
							.get_th().get(indCl.intValue());
					for (String lit : cl)
						if (!nod.contains(getVariable(lit))) {
							clInNod = false;
							break;
						}
					if (clInNod) {
//						System.out.println(pName + " cl:" + indCl
//								+ " is contained in " + nod);

						boolean eqCl = false;
						for (ArrayList<String> clTh : mpTh)
							if (cl.size() == clTh.size())
								if (cl.containsAll(clTh)) {
									eqCl = true;
									break;
								}
						if (!eqCl) {
							mpTh.add(cl);
							for (String dLit : _peerDescriptions.get(pName)
									.get_diagLit())
								if (nod.contains(getVariable(dLit)))
									mP.get_diagLit().add(dLit);

							for (TreeSet<String> nodNeigh : treeOfVarsSet
									.get_neighbors().get(nod)) {
								String neighName = nodNeigh.toString();
								mP.get_neighbors2Sh().put(neighName,
										new ArrayList<String>());
								for (String v2 : nod) {
									if (nodNeigh.contains(v2)) {
										mP.get_neighbors2Sh().get(neighName)
												.add(v2);
										mP.get_neighbors2Sh().get(neighName)
												.add(getOpposed(v2));
									}
								}
							}
						}
					}
				}
//				A REVOIR
//				DPLLIter dpll = new DPLLIter(mpTh);				
//				mP.reInitWeightTH(dpll.implicants()) ;
				
			}
		}
		return gMasters;
	}
	
	private String extractPeerFromLit(String l) {
		int last = l.lastIndexOf(":");
		String pName = l.substring(0, last);
		return pName.replace("!", "");

	}
	
	private String getVariable(String l){
		return l.contains("!")?l.replace("!",""):l;
	}
	
	
	
	@SuppressWarnings("unused")
	private boolean isPositive(String l){
		return l.contains("!");
	}
	
	private String getOpposed(String l){
		if(l.contains("!"))
			return l.replace("!", "");
		else
			return "!"+l;
		
	}
	
	public static void main(String args []){
		String s1 = new String("coucou");
		String s2 = "coucou";
		
		System.out.println(s1.hashCode()+" "+s2.hashCode()+s1.equals(s2));
	} 

}
