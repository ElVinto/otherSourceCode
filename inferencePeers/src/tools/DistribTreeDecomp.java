package tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.text.StyledEditorKit.UnderlineAction;
import javax.xml.stream.events.StartDocument;

import sun.swing.MenuItemLayoutHelper.ColumnAlignment;

import com.sun.org.apache.bcel.internal.generic.NEWARRAY;

public class DistribTreeDecomp {

	
	@SuppressWarnings("unchecked")
	public static<E,W extends Comparable<W>>void updateNodeCWIdHG(
			HashMap<CWId<E,W>, HashMap<CWId<E,W>,Comparable<?>>> vwn2pp,
			HyperGraph<CWId<E,W>> remainingWHG,
			ArrayList<ArrayList<E>> bucks,
			HashMap<E,ArrayList<Integer>> node2iBucks,
			HashMap<Integer,E> iBuck2Node
			) {


		for (CWId<E,W> cwidEnd : vwn2pp.keySet()) {
			COList<Integer> min = null;
			for(CWId<E,W> cwidStart : vwn2pp.get(cwidEnd).keySet()){
				if(vwn2pp.get(cwidEnd).get(cwidStart)!=null){
					if(min == null){
						min = (COList<Integer>) vwn2pp.get(cwidEnd).get(cwidStart);
					}else{
						if(min.compareTo((COList<Integer>)vwn2pp.get(cwidEnd).get(cwidStart))>0){
							min  =  (COList<Integer>)vwn2pp.get(cwidEnd).get(cwidStart);
						}
					}
				}
			}
			cwidEnd._weight = (W) min;
		}
		
	}
	
	
	private static <E> void min_predsCur_Bucks_Ancs(E eStart,int ipred,
			ArrayList<ArrayList<ArrayList<Integer>>> predsCur_Bucks_iBuckAncs,
			int[] cur_predsCur_Bucks_Ancs,
			int[] min_predsCur_Bucks_Ancs,
			int[] val_Min,
			HashSet<Integer> hsPropagationMin,
			ArrayList<ArrayList<E>> bucks) {
		
		//	System.out.println("In min_predsCur_Bucks_Ancs ");
		if(ipred < predsCur_Bucks_iBuckAncs.size()){
			for(int k=0;k< predsCur_Bucks_iBuckAncs.get(ipred).size();k++){
				cur_predsCur_Bucks_Ancs[ipred]=k;
				min_predsCur_Bucks_Ancs(eStart,ipred+1,predsCur_Bucks_iBuckAncs
						, cur_predsCur_Bucks_Ancs, min_predsCur_Bucks_Ancs,val_Min, hsPropagationMin,bucks);
			}
		}else{
			// Now I have to select the min
			int nbReachableRootBucks=0;
			HashSet<Integer> reachableRootBucks = new HashSet<Integer>(); 
			for(int ip=0; ip<predsCur_Bucks_iBuckAncs.size();ip++){
				int iLast = predsCur_Bucks_iBuckAncs.get(ip).get(cur_predsCur_Bucks_Ancs[ip]).size()-1;
				reachableRootBucks.add(predsCur_Bucks_iBuckAncs.get(ip).get(cur_predsCur_Bucks_Ancs[ip]).get(iLast));
			}
			nbReachableRootBucks = reachableRootBucks.size();
			
			// compute the set of buckets reachable till probable elimination
			HashSet<Integer> hsPropagation = new HashSet<Integer>();
			Integer  iBEliminator = -1;
			if(nbReachableRootBucks>1){
				//	System.out.println(" nbReachableRootBucks: "+ nbReachableRootBucks);
				for(int ip=0; ip<predsCur_Bucks_iBuckAncs.size();ip++){
					hsPropagation.addAll(predsCur_Bucks_iBuckAncs.get(ip).get(cur_predsCur_Bucks_Ancs[ip]));
				}
			}else{
				ArrayList<Integer> alIntersBucks = new ArrayList<Integer>();
				for(int ip=0; ip<predsCur_Bucks_iBuckAncs.size();ip++){
					if(alIntersBucks.isEmpty()){
						alIntersBucks.addAll(predsCur_Bucks_iBuckAncs.get(ip).get(cur_predsCur_Bucks_Ancs[ip]));
					}else{
						alIntersBucks.retainAll(predsCur_Bucks_iBuckAncs.get(ip).get(cur_predsCur_Bucks_Ancs[ip]));
					}
				}
				
				iBEliminator = alIntersBucks.get(alIntersBucks.size()-1);
//				System.out.println(" iBEliminator: "+ iBEliminator);
				for(int ip=0; ip<predsCur_Bucks_iBuckAncs.size();ip++){
					for(int iiB = predsCur_Bucks_iBuckAncs.get(ip).get(cur_predsCur_Bucks_Ancs[ip]).indexOf(iBEliminator);
						iiB<predsCur_Bucks_iBuckAncs.get(ip).get(cur_predsCur_Bucks_Ancs[ip]).size();
						iiB++){
						hsPropagation.add(predsCur_Bucks_iBuckAncs.get(ip).get(cur_predsCur_Bucks_Ancs[ip]).get(iiB));
					}
				}
			}
			
			int nbBucksProp = hsPropagation.size();
			for(Integer iB :hsPropagation){
				if(bucks.get(iB).contains(eStart))
					nbBucksProp --;
			}
//			System.out.println(" nbBucksProp: "+ nbBucksProp);
			
			
			// compute a local view of the treeWidth
			int localTw = 0;
			for(Integer ib : hsPropagation){
				if( bucks.get(ib).size()>localTw )
					localTw = bucks.get(ib).size();
			}
//			System.out.println(" localTw: "+ localTw);
			
			//compute the distance to the elimination bucket if any
			// local distances + nb SHared Roots is a lower bound in centralized context ?
			int distToElim = 0;
			if(nbReachableRootBucks>1){
				// This distance is not a real one is to penalize 
				// variable that can be eliminated
				distToElim = hsPropagation.size();
			}else{
				// iBEliminator represents the closer common ancestor of all predecessor
				distToElim = predsCur_Bucks_iBuckAncs.get(0).get(cur_predsCur_Bucks_Ancs[0]).size();
				distToElim -= predsCur_Bucks_iBuckAncs.get(0).get(cur_predsCur_Bucks_Ancs[0]).indexOf(iBEliminator);
				
//				for(int ip=0; ip<predsCur_Bucks_iBuckAncs.size();ip++){
//					int iiB = predsCur_Bucks_iBuckAncs.get(ip).get(cur_predsCur_Bucks_Ancs[ip]).indexOf(iBEliminator);
//					if(iiB+1>distToElim)
//						distToElim = iiB+1;
//				}
			}
//			System.out.println(" distToElim: "+ distToElim);
			
			COList<Integer> colMin = new COList<Integer>();
			colMin.add(val_Min[0]);
			colMin.add(val_Min[1]);
			colMin.add(val_Min[2]);
			
			COList<Integer> colCur = new COList<Integer>();
			colCur.add(localTw);
			colCur.add(nbBucksProp);
			colCur.add(distToElim);
			
			if(colMin.compareTo(colCur)>=0){
				val_Min[0] = localTw;
				val_Min[1] = nbBucksProp;
				val_Min[2] = distToElim;
				for(int k=0;k < cur_predsCur_Bucks_Ancs.length; k++)
					cur_predsCur_Bucks_Ancs[k] = 
						min_predsCur_Bucks_Ancs[k];
				hsPropagationMin.clear();
				hsPropagationMin.addAll(hsPropagation);
			}
			

			
		}
	}
	
	
//	
//	public static  <E,W extends Comparable<W>> void addArcWeight(CWId<E,W> start,
//			CWId<E,W> end,
//			HashMap<CWId<E,W>, HashMap<CWId<E,W>,Comparable<?>>> vwn2pp,
//			ArrayList<ArrayList<E>> bucks,
//			HashMap<E,ArrayList<Integer>> node2iBucks,
//			HashMap<Integer,E> iBuck2Node,
//			HashMap<Integer,Integer> iBuck2iFather,
//			HashMap<Integer,ArrayList<Integer>> iBuck2iBuckAncestors,
//			HashMap<E, ArrayList<E>> successorsOf
//			){
//		
//		System.out.println("In addArcWeight ");
//		
//		
//		
//		
//		ArrayList<ArrayList<ArrayList<Integer>>> sucsCur_Bucks_iBuckAncs =
//			new ArrayList<ArrayList<ArrayList<Integer>>>();
//		// add the ancestor of the potentiel predecessor
//		sucsCur_Bucks_iBuckAncs.add(new ArrayList<ArrayList<Integer>>());
//		for(int iB :node2iBucks.get(end))
//			sucsCur_Bucks_iBuckAncs.get(0).add(iBuck2iBuckAncestors.get(iB));
//		
//		if( successorsOf.containsKey(end._elmt)){
//			// add the ancestors of the previously choose predecessors
//			for(E predCur : successorsOf.get(end)){
//				sucsCur_Bucks_iBuckAncs.add(new ArrayList<ArrayList<Integer>>());
//				for(int iB :node2iBucks.get(predCur))
//					sucsCur_Bucks_iBuckAncs.get(sucsCur_Bucks_iBuckAncs.size()-1).add(iBuck2iBuckAncestors.get(iB));
//			}
//		}
//		
//		// Select path of propagation that minimize the tree width measures
//		int[] cur_predsCur_Bucks_Ancs = new int[sucsCur_Bucks_iBuckAncs.size()];
//		int[] min_predsCur_Bucks_Ancs = new int[sucsCur_Bucks_iBuckAncs.size()];
//		HashSet<Integer> hsPropagationMin = new HashSet<Integer>();
//		int[] val_Min = new int[3];
//		for(int i=0;i<val_Min.length;i++)
//			val_Min[i]= Integer.MAX_VALUE;
//			min_predsCur_Bucks_Ancs(end._elmt,0,
//					sucsCur_Bucks_iBuckAncs,
//					cur_predsCur_Bucks_Ancs,
//					min_predsCur_Bucks_Ancs,
//					val_Min,
//					hsPropagationMin,
//					bucks);
//		
//			
//		System.out.println("hsPropagationMin: "+hsPropagationMin);
//		for(Integer iB: hsPropagationMin){
//			if(!bucks.get(iB).contains(iB))
//				bucks.get(iB).add(end._elmt);
//		}
//		
//		
//		ArrayList<E> b =new ArrayList<E>();
//		b.add(end._elmt);
//		bucks.add(b);
//		
//		node2iBucks.get(end._elmt).add(bucks.size()-1);
//		
//		iBuck2Node.put(bucks.size()-1,end._elmt);
//		
//		Integer iBFather = -1 ;
//		for(Integer iBPred :min_predsCur_Bucks_Ancs)
//			if(node2iBucks.containsKey(start._elmt)){
//				iBuck2iFather.put(bucks.size()-1, iBPred);
//				break;
//			}
//		
//		iBuck2iBuckAncestors.put(bucks.size()-1, new ArrayList<Integer>());
//		iBuck2iBuckAncestors.get(bucks.size()-1).addAll(iBuck2iBuckAncestors.get(iBFather));
//		iBuck2iBuckAncestors.get(bucks.size()-1).add(iBFather);
//		
//		if(!successorsOf.containsKey(end._elmt))
//			successorsOf.put(end._elmt, new ArrayList<E>());
//		successorsOf.get(end._elmt).add(start._elmt);
//		
//	}
//	
	
	
	
	/**
	 * 
	 * @param <E>
	 * @param start
	 * @param end
	 * @param vwn2ps
	 */
	public static  <E,W extends Comparable<W>> void updateArcWeight(CWId<E,W> start,
			CWId<E,W> end,
			HashMap<CWId<E,W>, HashMap<CWId<E,W>,Comparable<?>>> vwn2ps,
			ArrayList<ArrayList<E>> bucks,
			HashMap<E,ArrayList<Integer>> node2iBucks,
			HashMap<E, ArrayList<E>> successorsOf,
			HashMap<Integer,ArrayList<Integer>> iBuck2iBuckAncestors
			){
		
		
//		System.out.println("IN updateArcWeight "+ start._elmt+" -> "+ end._elmt);
//		System.out.println("vwn2ps ");
//		for(CWId<E,W> st : vwn2ps.keySet()){
//			System.out.print(st+" succs: ");
//			for(CWId<E,W> nd : vwn2ps.get(st).keySet())
//				System.out.print(" "+nd+" "+ 
//						vwn2ps.get(st).get(nd)+",");
//			System.out.println();
//		}
		
		// Arc directed to new node are penalized by Max value
		if(!successorsOf.containsKey(end._elmt)){
			
//			System.out.println(" udpdate "+ start._elmt+" -> "+ end._elmt+" !successorsOf.containsKey "+end._elmt );
			
			COList<Integer> colMax = new COList<Integer>();
			colMax.add(Integer.MAX_VALUE);
			colMax.add(Integer.MAX_VALUE);
			colMax.add(Integer.MAX_VALUE);
			colMax.add(Integer.MAX_VALUE);
			vwn2ps.get(start).put(end,colMax);
//			System.out.println("QUIT 1st updateArcWeight "+ start._elmt+" -> "+ end._elmt+ " weight "+ colMax);
			return ;
		}
		
		
		
//		System.out.println("start: "+start+" "+"end: "+end +" to weight ");
		
		ArrayList<ArrayList<ArrayList<Integer>>> succsCur_Bucks_iBuckDescs = new ArrayList<ArrayList<ArrayList<Integer>>>();
		
		
//		System.out.println("compute succsCur_Bucks_iBuckDescs");
		
		// add the descendants of the potentiel successor
		succsCur_Bucks_iBuckDescs.add(new ArrayList<ArrayList<Integer>>());
		for(int iiB=0; iiB< node2iBucks.get(end._elmt).size();iiB++){
			int iB =node2iBucks.get(end._elmt).get(iiB);
			
//			System.out.println(" potential Ibuckets of end "+end+" IB "+iB);
			ArrayList<Integer> tmp = new ArrayList<Integer>(iBuck2iBuckAncestors.get(iB));
			tmp.add(iB);
			
			succsCur_Bucks_iBuckDescs.get(0).add(tmp);
		}
		
		
		// add the descendants of the previously chosen successors
		if(node2iBucks.containsKey(start._elmt)){
			for(int iiB=0; iiB< node2iBucks.get(start._elmt).size();iiB++){
				int iB =node2iBucks.get(start._elmt).get(iiB);
				
				if(node2iBucks.containsKey(start._elmt))
					if (node2iBucks.get(start._elmt).contains(iB))
						continue;
				
//				System.out.println(" chosen Ibuckets of start "+start+" IB "+iB);
				
				succsCur_Bucks_iBuckDescs.add(new ArrayList<ArrayList<Integer>>());
				succsCur_Bucks_iBuckDescs.get(succsCur_Bucks_iBuckDescs.size()-1).add(iBuck2iBuckAncestors.get(iB));
			}
		}
		
		// add the descendants of the connected neighbors
		for(CWId<E,W> pSucc :vwn2ps.get(start).keySet() ){
			if(node2iBucks.containsKey(pSucc._elmt) && !pSucc.equals(end)){
				succsCur_Bucks_iBuckDescs.add(new ArrayList<ArrayList<Integer>>());
				int iLast = succsCur_Bucks_iBuckDescs.size()-1;
				for(int iiB=0; iiB< node2iBucks.get(pSucc._elmt).size();iiB++){
					int iB =node2iBucks.get(pSucc._elmt).get(iiB);

//					System.out.println(" potential Ibuckets of succ of start "+pSucc+" IB "+iB);
					
					ArrayList<Integer> tmp = new ArrayList<Integer>(iBuck2iBuckAncestors.get(iB));
					tmp.add(iB);
					succsCur_Bucks_iBuckDescs.get(iLast).add(tmp);
				}
				
				if (succsCur_Bucks_iBuckDescs.get(iLast).isEmpty())
					succsCur_Bucks_iBuckDescs.remove(iLast);
			}
		}
		

		
		
//		System.out.println(" succsCur_Bucks_iBuckDescs "+succsCur_Bucks_iBuckDescs);
//		for(ArrayList<ArrayList<Integer>> Buck_iBuckDescs :succsCur_Bucks_iBuckDescs){
//			for(ArrayList<Integer> iBuckDescs :Buck_iBuckDescs){
//				System.out.println(" iBuckDescs "+iBuckDescs);
//			}
//		}
		
		// Select path of propagation that minimize the tree width measures
		int[] cur_predsCur_Bucks_Succs = new int[succsCur_Bucks_iBuckDescs.size()];
		int[] min_predsCur_Bucks_Succs = new int[succsCur_Bucks_iBuckDescs.size()];
		HashSet<Integer> hsPropagationMin = new HashSet<Integer>();
		int[] val_Min = new int[3];
		for(int i=0;i<val_Min.length;i++)
			val_Min[i]= Integer.MAX_VALUE;
			min_predsCur_Bucks_Ancs(start._elmt,0,
					succsCur_Bucks_iBuckDescs,
					cur_predsCur_Bucks_Succs,
					min_predsCur_Bucks_Succs,
					val_Min,
					hsPropagationMin,
					bucks);
			
//		System.out.print(" val_Min ");
//		for(int i=0;i<val_Min.length;i++)
//			System.out.print( val_Min[i]);
//		System.out.println();
		
		COList<Integer> colMin = new COList<Integer>();
		for(int i =0 ;i<val_Min.length; i++)
			colMin.add(val_Min[i]);
		// we add the chosen iB of the end node at the end
		
		colMin.add(node2iBucks.get(end._elmt).get(min_predsCur_Bucks_Succs[0]));
		vwn2ps.get(start).put(end, colMin);
		
//		System.out.println("QUIT 2nd updateArcWeight "+ start._elmt+" -> "+ end._elmt+ " weight "+ colMin);
//		HashMap<E,Integer> node2BestBuck = new HashMap<E,Integer>(); 
//		node2BestBuck.put(end._elmt, node2iBucks.get(end._elmt).get(min_predsCur_Bucks_Succs[0]));
//		if(successorsOf.containsKey(start._elmt)){
//			for(int iSucCur = 0; iSucCur< successorsOf.get(start._elmt).size();iSucCur++){
//				E sucCur = successorsOf.get(start._elmt).get(iSucCur);
//				node2BestBuck.put(sucCur, node2iBucks.get(sucCur).get(min_predsCur_Bucks_Succs[iSucCur+1]));
//				succsCur_Bucks_iBuckDescs.add(new ArrayList<ArrayList<Integer>>());
//			}
//		}
//		
//		vn2psbb.get(start._elmt).put(end._elmt, node2BestBuck);
	}
	
	
//	
//	/**
//	 * add valid weighted arcs between node of nvWNodes and
//	 * existing in remainingWHG 
//	 * @param <E>
//	 * @param nvWNodes represents the list of fresh added successors
//	 * @param remainingWHG is the remaining weihgted graph  to oriented
//	 * @param vwn2pp is the of relevent nodes and edges under process
//	 */
//	public static <E,W extends Comparable<W>> void addNvNode2vwn2pp(
//			ArrayList<CWId<E,W>> nvWNodes ,
//			HyperGraph<CWId<E,W>> remainingWHG,
//			HashMap<CWId<E,W>, HashMap<CWId<E,W>,Comparable<?>>> vwn2pp,
//			ArrayList<ArrayList<E>> bucks,
//			HashMap<E,ArrayList<Integer>> node2iBucks,
//			HashMap<E, ArrayList<E>> predecessorsOf,
//			HashMap<Integer,ArrayList<Integer>> iBuck2iBuckAncestors){
//		
//		System.out.println("IN addNvNode2vwn2pp");
//		
//		for(CWId<E,W> wn : nvWNodes){
//			if(!vwn2pp.containsKey(wn))
//				vwn2pp.put(wn, new HashMap<CWId<E,W>,Comparable<?>>());
//			// we only add the neighbors of the nodes that has not been completly treated
//			for(CWId<E,W> neighOfwn : remainingWHG.getNeighbors(wn)){
//				if(! vwn2pp.containsKey(neighOfwn))
//					vwn2pp.put(neighOfwn, new HashMap<CWId<E,W>,Comparable<?>>());
//				
//				if(!vwn2pp.get(wn).containsKey(neighOfwn)){
//					vwn2pp.get(wn).put(neighOfwn, null);
//					updateArcWeight(wn, neighOfwn,vwn2pp, 
//						bucks, node2iBucks,predecessorsOf,iBuck2iBuckAncestors);
//				}
//				
//				if(! vwn2pp.get(neighOfwn).containsKey(wn)){
//					vwn2pp.get(neighOfwn).put(wn,null);
//					updateArcWeight(neighOfwn, wn ,vwn2pp, 
//							bucks, node2iBucks,predecessorsOf,iBuck2iBuckAncestors);
//				}
//			}
//		}
//	}
//		
		/**
		 * link valid weighted arcs between node of nvWNodes and
		 * existing in remainingWHG 
		 * @param <E>
		 * @param nvWNodes represents the list of fresh added successors
		 * @param remainingWHG is the remaining weihgted graph  to oriented
		 * @param vwn2ps is the of relevent nodes and edges under process
		 */
		public static <E,W extends Comparable<W>> void linkNvNode2vwn2pp(
				ArrayList<CWId<E,W>> nvWNodes ,
				HyperGraph<CWId<E,W>> remainingWHG,
				HashMap<CWId<E,W>, HashMap<CWId<E,W>,Comparable<?>>> vwn2ps,
				ArrayList<ArrayList<E>> bucks,
				HashMap<E,ArrayList<Integer>> node2iBucks,
				HashMap<E, ArrayList<E>> predecessorsOf,
				HashMap<Integer,ArrayList<Integer>> iBuck2iBuckAncestors){
			
//			System.out.println("IN linkNvNode2vwn2pp");
			
			for(CWId<E,W> wn : nvWNodes){
				if(!vwn2ps.containsKey(wn)){
					vwn2ps.put(wn, new HashMap<CWId<E,W>,Comparable<?>>());
				}
				// we only add the neighbors of the nodes that has not been completly treated
				for(CWId<E,W> neighOfwn : remainingWHG.getNeighbors(wn)){
					if( vwn2ps.containsKey(neighOfwn)){
						if(!vwn2ps.get(wn).containsKey(neighOfwn)){
							vwn2ps.get(wn).put(neighOfwn, null);
						}
//						updateArcWeight(wn, neighOfwn,vwn2ps,
//							bucks, node2iBucks,predecessorsOf,iBuck2iBuckAncestors);
						if(! vwn2ps.get(neighOfwn).containsKey(wn)){
							vwn2ps.get(neighOfwn).put(wn,null);
						}
//						updateArcWeight(neighOfwn, wn ,vwn2ps, 
//								bucks, node2iBucks,predecessorsOf,iBuck2iBuckAncestors);
					}
				}
			}
		
	}
	
	public static  <E,W extends Comparable<W>> void addArcElmt2IBuck(
			CWId<E,W> cwidStart,
			Integer iBend,
			HyperGraph<CWId<E,W>> remainCWIHG,
			HashMap<CWId<E,W>, HashMap<CWId<E,W>,Comparable<?>>> vwn2ps,
			ArrayList<ArrayList<E>> bucks,
			HashMap<E,ArrayList<Integer>> node2iBucksBuilt,
			HashMap<Integer,E> iBuckBuilt2Node,
			HashMap<E, ArrayList<E>> successorsOf,
			HashMap<Integer,Integer> iBuck2iFather,
			HashMap<Integer,ArrayList<Integer>> iBuck2iBuckAncestors,
			ArrayList<CWId<E, W>> treatedNode2Invest
	){
		
		
		
		E end = iBuckBuilt2Node.get(iBend);
//		System.out.println("\n\nADDING arc eStart "+cwidStart +
//				" -> eEnd "+ end );
		
		// create a buckets at start containing the elements of the edges
		ArrayList<E> b =new ArrayList<E>();
		b.add(cwidStart._elmt);
		b.add(end);
		bucks.add(b);
		Integer iBStart = bucks.size()-1;
		
		// associate the bucket to its creator node : Start
		if(!node2iBucksBuilt.containsKey(cwidStart._elmt))
			node2iBucksBuilt.put(cwidStart._elmt,new ArrayList<Integer>());
		node2iBucksBuilt.get(cwidStart._elmt).add(iBStart);
		
		iBuckBuilt2Node.put(iBStart,cwidStart._elmt);
		
		// link the bucket to its successor
		iBuck2iFather.put(iBStart, iBend);
		
		// add the Ancestor of end and end to the ancestor
		iBuck2iBuckAncestors.put(iBStart, new ArrayList<Integer>());
		iBuck2iBuckAncestors.get(iBStart).addAll(iBuck2iBuckAncestors.get(iBend));
		if(!iBuck2iBuckAncestors.get(iBStart).contains(iBend))
			iBuck2iBuckAncestors.get(iBStart).add(iBend);
		
		
		// search for a join bucket to propagate start._elmt
		
		ArrayList<ArrayList<Integer>> ancsBuck = new ArrayList<ArrayList<Integer>>();
//		ancsBuck.add(iBuck2iBuckAncestors.get(iBend));
//		ancsBuck.get(ancsBuck.size()-1).add(iBend);
		if( successorsOf.containsKey(cwidStart._elmt)){
			// add the ancestors of previously chosen successors
			for(int iBs  : node2iBucksBuilt.get(cwidStart._elmt)){
				ancsBuck.add(new ArrayList<Integer>());
				ancsBuck.get(ancsBuck.size()-1).addAll(iBuck2iBuckAncestors.get(iBs));
				ancsBuck.get(ancsBuck.size()-1).add(iBs);
			}
		}
		
//		System.out.print("\nancsBuck ");
//		for(ArrayList<Integer> ans: ancsBuck)
//			System.out.print(" "+ans);
		
		ArrayList<Integer> alIntersBucks = new ArrayList<Integer>();
		for(int iBS=0; iBS<ancsBuck.size();iBS++){
			if(alIntersBucks.isEmpty()){
				alIntersBucks.addAll(ancsBuck.get(iBS));
			}else{
				alIntersBucks.retainAll(ancsBuck.get(iBS));
			}
		}
		
		
		
		HashSet<Integer> hsPropagation = new HashSet<Integer>();
		Integer iBJoin = -1;
		if(alIntersBucks.isEmpty()){
			// If it does not exist a join bucket start._elmt is propagated 
			// till join roots buckets
			for(int iBs=0; iBs<ancsBuck.size();iBs++){
				hsPropagation.addAll(ancsBuck.get(iBs));
			}
		}else{
			// If it exists a join bucket start._elmt is propagated to this bucket
			if(ancsBuck.size()>1){
			iBJoin = alIntersBucks.get(alIntersBucks.size()-1);
			for(int iBs=0; iBs<ancsBuck.size();iBs++){
				for(int iiB2Prop = ancsBuck.get(iBs).indexOf(iBJoin);
					iiB2Prop<ancsBuck.get(iBs).size();
					iiB2Prop++){
					hsPropagation.add(ancsBuck.get(iBs).get(iiB2Prop));
				}
			}
			}
		}
		
		
		
//		System.out.println("\nhsPropagation ");
//		for(Integer iB : hsPropagation){
//			System.out.print(" "+iB);	
//		}
//		System.out.println();
//		
//		System.out.println("\niBuck2iBuckAncestors ");
//		for(Integer iB : iBuck2iBuckAncestors.keySet()){
//			System.out.print(" "+iB+" has as Ancestors "+iBuck2iBuckAncestors.get(iB));
//			System.out.println();
//		}
		
		
		// create an elimination bucket where will converge the path 
		// from the started element.
		if( iBJoin!= -1){
			
			
			// save ibuckets involved in the propagation of start._elmt  directed to iBjoin
			ArrayList<Integer> propBucksBeforeBjoin = new ArrayList<Integer>();
			for(int iBs=0; iBs<ancsBuck.size();iBs++){
				int iiB2Prop = ancsBuck.get(iBs).indexOf(iBJoin);
				// if the join bucket is not the last one
				if(iiB2Prop < ancsBuck.get(iBs).size()-1 ){
					int iBPredInPath = ancsBuck.get(iBs).get(iiB2Prop+1);
					propBucksBeforeBjoin.add(iBPredInPath);
				}
			}
			
			// create the elimination bucket
			ArrayList<E> bE =new ArrayList<E>();
			
			// add the shared elements between iBjoin and some of its predecessors
			//
			for(int iBs : propBucksBeforeBjoin){
				for(E e :bucks.get(iBs))
					if(bucks.get(iBJoin).contains(e))
						if(!bE.contains(e))
							bE.add(e);
			}
			if(!bE.contains(cwidStart._elmt))
				bE.add(cwidStart._elmt);
			bucks.add(bE);
			Integer iBElim = bucks.size()-1;
			
			// get the node corresponding to the join bucket			
			E nodeJoin = iBuckBuilt2Node.get(iBJoin);
			// put the elimination bucket as it creator
			if(!node2iBucksBuilt.containsKey(nodeJoin))
				node2iBucksBuilt.put(nodeJoin,new ArrayList<Integer>());
			node2iBucksBuilt.get(nodeJoin).add(iBElim);
			
			iBuckBuilt2Node.put(iBElim,nodeJoin);
			
			// turn iBElim 2 iBJoin
			iBuck2iFather.put(iBElim, iBJoin);
			// turn some preds of iBJoin 2 iBElim
			for(int iBs : propBucksBeforeBjoin){
				iBuck2iFather.put(iBs, iBElim);
			}
			
			// add the ancestors of iBElim
			iBuck2iBuckAncestors.put(iBElim, new ArrayList<Integer>());
			iBuck2iBuckAncestors.get(iBElim).addAll(iBuck2iBuckAncestors.get(iBJoin));
			if(!iBuck2iBuckAncestors.get(iBElim).contains(iBJoin))
				iBuck2iBuckAncestors.get(iBElim).add(iBJoin);
			
			
			// propagate the element in the tree where leaves are
			// buckets from start._elmt
			hsPropagation.remove(iBJoin);
			for(Integer iB: hsPropagation){
				if(!bucks.get(iB).contains(cwidStart._elmt)){
					bucks.get(iB).add(cwidStart._elmt);
				}
			}
			
			// Update the ancestors of 
			ArrayList<Integer> toVisit = new ArrayList<Integer>();
			ArrayList<Integer> visited = new ArrayList<Integer>(); 
			toVisit.addAll(propBucksBeforeBjoin);
			while(!toVisit.isEmpty()){
				Integer iB = toVisit.remove((int)0);
				visited.add(iB);
//				System.out.println("cur "+ iB+" -> toVisit "+toVisit);
				
				int iiBJoin = iBuck2iBuckAncestors.get(iB).indexOf(iBJoin);
				if(!iBuck2iBuckAncestors.get(iB).contains(iBElim))
					iBuck2iBuckAncestors.get(iB).add(iiBJoin+1,iBElim);
				
				// visit the predecessor
				for(Integer iP: iBuck2iFather.keySet()){
					if(iBuck2iFather.get(iP).equals(iB)&& !iP.equals(iB)&&
							!visited.contains(iP)){
						toVisit.add(iP);
					}
				}
				
			}
			
		}else{ // here the element just have 1 successor
			
			
		}
		
		
		
		
//		System.out.println(" hsPropagation "+hsPropagation);
//		System.out.print("\nadded "+cwidStart._elmt+ " to Bucks:");
//		for(Integer iB: hsPropagation)
//			System.out.print(" "+iB);
		
		
		
//		System.out.println("\nUpdate the other structure ");
		
		
		// update the other structure 
		
		if(!successorsOf.containsKey(cwidStart._elmt))
			successorsOf.put(cwidStart._elmt, new ArrayList<E>());
		successorsOf.get(cwidStart._elmt).add(end);
		
		CWId<E, W> cwidEnd = null;
		ArrayList<CWId<E, W>> he2Rem = null;
		 for( ArrayList<CWId<E, W>> he : remainCWIHG.getHEdges(cwidStart)){
			for( CWId<E, W> cwidNode : he){
				if(cwidNode._elmt.equals(end)){
					cwidEnd = cwidNode;
					he2Rem =he;
					 break;
				}
			}
			if(cwidEnd != null)
				break;
		 }
		 
//		 CWId<E, W> tmpEnd ;
//		 CWId<E, W> tmpStart;
//		 remainCWIHG.getNeighbors(cwidStart)
		 
		 
//		 System.out.println(" has removed edge "+cwidStart+" <-> "+cwidEnd);
		 
		 vwn2ps.get(cwidStart).remove(cwidEnd);
		 vwn2ps.get(cwidEnd).remove(cwidStart);
		 
		 HashSet<CWId<E, W>> vToRem = ( HashSet<CWId<E, W>>)
		 	remainCWIHG.removeHEdge(he2Rem);
		 
//		 System.out.println("vwn2ps: ");
//			for(CWId<E,W> st : vwn2ps.keySet()){
//				System.out.print(st+" succs: ");
//				for(CWId<E,W> nd : vwn2ps.get(st).keySet())
//					System.out.print(" "+nd+" "+ 
//							vwn2ps.get(st).get(nd)+",");
//				System.out.println();
//			}
//			
//		System.out.println( " AFTER removed remainCWIHG " +remainCWIHG);
			
		 
//		 if(vwn2ps.get(cwidStart).isEmpty() && 
//				 remainCWIHG.getNeighbors(cwidStart).size()>0)
//			 if(!treatedNode2Invest.contains(cwidStart))
//				 treatedNode2Invest.add(cwidStart);
//		 if(vwn2ps.get(cwidEnd).isEmpty() && 
//				 remainCWIHG.getNeighbors(cwidEnd).size()>0)
//			 if(!treatedNode2Invest.contains(cwidEnd))
//				 treatedNode2Invest.add(cwidEnd);

		if(vToRem != null)
			for(CWId<E, W> cwid : vToRem ){
				if(remainCWIHG.getVertices().contains(cwid))
					remainCWIHG.removeVertex(cwid);	 	 
				vwn2ps.remove(cwid);
				while(treatedNode2Invest.contains(cwid))
					treatedNode2Invest.remove(cwid);
			}
		 
		 
//		 System.out.println("\nsuccessors ");
//			for(E elmt : successorsOf.keySet()){
//				System.out.print(" "+elmt+" sucs ");
//				for(E pred : successorsOf.get(elmt))
//					System.out.print(" "+pred);
//				System.out.println();
//			}
//		 
//		 System.out.println("\nvwn2ps ");
//			for(CWId<E,W> st : vwn2ps.keySet()){
//				System.out.print(st+" succs: ");
//				for(CWId<E,W> nd : vwn2ps.get(st).keySet())
//					System.out.print(" "+nd+" "+ 
//							vwn2ps.get(st).get(nd)+",");
//				System.out.println();
//			}
//		 
//		 
//		 System.out.println("\nnode2iBucks ");
//			for(E elmt : node2iBucksBuilt.keySet()){
//				System.out.print(" "+elmt+" has built IBucks");
//				for(Integer iB : node2iBucksBuilt.get(elmt))
//					System.out.print(" "+iB+",");
//				System.out.println();
//			}
//			
//			System.out.println("\niBuckBuilt2Node ");
//			for(Integer iB : iBuckBuilt2Node.keySet()){
//				System.out.print(" "+iB+" has been built by Node "+iBuckBuilt2Node.get(iB));
//				System.out.println();
//			}
//			
//			System.out.println("\nbucks ");
//			for(int iB=0;iB<bucks.size();iB++){
//				System.out.print(" buck:"+iB+" contains "+bucks.get(iB));
//				System.out.println();
//			}
//			
//			System.out.println("\niBuck2iFather ");
//			for(int iB=0;iB<iBuck2iFather.size();iB++){
//				System.out.print(" buck:"+iB+" has as father "+iBuck2iFather.get(iB));
//				System.out.println();
//			}
//			
//			System.out.println("\niBuck2iBuckAncestors ");
//			for(Integer iB : iBuck2iBuckAncestors.keySet()){
//				System.out.print(" "+iB+" has as Ancestors "+iBuck2iBuckAncestors.get(iB));
//				System.out.println();
//			}
	}
	
	
	/**
	 * returns the set of new nodes to investigate
	 * @param lastTreatedWNodes represents the last treated nodes
	 * @param remainCWIHG is the remaining hyper graph under process and to be process
	 * @param vwn2ps represents nodes under process some of them do not have successor yet
	 */
	public static <E,W extends Comparable<W>> ArrayList<CWId<E,W>> nvWNoteToInvestigate(
			ArrayList<CWId<E, W>> lastTreatedWNodes,
			HyperGraph<CWId<E, W>> remainCWIHG,
			HashMap<CWId<E, W>, HashMap<CWId<E, W>, Comparable<?>>> vwn2ps ){
		
//		System.out.println("IN nvWNoteToInvestigate ");
//		System.out.println( "\nremainCWIHG " +remainCWIHG);
//		 System.out.println("\nvwn2ps ");
//			for(CWId<E,W> st : vwn2ps.keySet()){
//				System.out.print(st+" succs: ");
//				for(CWId<E,W> nd : vwn2ps.get(st).keySet())
//					System.out.print(" "+nd+" "+ 
//							vwn2ps.get(st).get(nd)+",");
//				System.out.println();
//			}
		
		
		ArrayList<CWId<E,W>> nvNodes = new ArrayList<CWId<E,W>> ();
		// returns empty if all edges between reached neighbors of nvwn are not directed
		for(CWId<E,W> ltwn : lastTreatedWNodes){
			boolean isUnderProcess = false;
			ArrayList<CWId<E,W>> nvCurNodes = new ArrayList<CWId<E,W>> ();
			for(CWId<E,W> neighOfwn : remainCWIHG.getNeighbors(ltwn)){
				if(vwn2ps.containsKey(neighOfwn)){
					// if the neighbor is under process we quit 
					// -> we have to finish to process its neighbors first
					if( !vwn2ps.get(neighOfwn).containsKey(ltwn) && 
						 !vwn2ps.get(ltwn).containsKey(neighOfwn)){
							isUnderProcess = true;
							break;
					}
				}else{
					if(!nvCurNodes.contains(neighOfwn))
						nvCurNodes.add(neighOfwn);
				}
			}
			if(!isUnderProcess)
				for(CWId<E,W> n: nvCurNodes)
					if(!nvNodes.contains(n))
						nvNodes.add(n);
		}

		return nvNodes ;
	}
	
	public static <E> void updateFillInWeights(
			Set<WeightedElmt<E>> v2Update, HyperGraph<WeightedElmt<E>> whg) {

		// System.out.println("Le graphe Ã  pondÃ©rer par Min fill "+whg);

		for (WeightedElmt<E> wv : v2Update) {

			HashSet<WeightedElmt<E>> seenWVertices = new HashSet<WeightedElmt<E>>();
			for (Collection<WeightedElmt<E>> whe : whg.getHEdges(wv)) {
				seenWVertices.addAll(whe);
			}

			int nbEdges = whg.nbNeighbors(wv);
			for (int i = 0; i < whg.nbNeighbors(wv) - 1; i++) {
				for (int j = i + 1; j < whg.nbNeighbors(wv); j++) {
					if (whg.getNeighbors(whg.getNeighbors(wv).get(i)).contains(
							whg.getNeighbors(wv).get(j))) {
						nbEdges++;
					}
				}
			}

			// System.out.println(wv+" is in "+ whg.getHEdges(wv));
			// Warning here wv is not in whg.nbNeighbors(wv)
			int wvCliqueWidth = (whg.nbNeighbors(wv) * (whg.nbNeighbors(wv) + 1)) / 2;
			wv.setWeight(wvCliqueWidth - nbEdges);
		}
	}
	

	public static  <E extends Comparable<E>> E chooseRoot(String minMax,
			HyperGraph<E> hg){
		// transforms the hyper graph hg in a weighted hyper graph whg
		HyperGraph<WeightedElmt<E>> whg = new HyperGraph<WeightedElmt<E>>();
		for (Collection<E> he : hg.getHEdges()) {
			HashSet<WeightedElmt<E>> whedge = new HashSet<WeightedElmt<E>>();
			for (E v : he)
				whedge.add(new WeightedElmt<E>(Integer.MAX_VALUE,v));
			whg.addHedge(whedge);
		}
		HashSet<WeightedElmt<E>> v2Update = new HashSet<WeightedElmt<E>>(
				whg.getVertices());
		
		updateFillInWeights(v2Update, whg);
		

		WeightedElmt<E> root = null;
		if(minMax.equals("min")){
			root = Collections.min(whg.getVertices());
		}
		if(minMax.equals("max")){
			root = Collections.max(whg.getVertices());
		}
		return root._elmt;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static <E extends Comparable<E>,W extends Comparable<W>> HyperGraph<E> distribJT5(
			HyperGraph<E> hg, HyperGraph<AbstCompArrayList<E>> dualHjt, W hversion) {

		// transforms the hyper graph hg in a weighted hyper graph whg
		// remainWHG contains weighted node and edges not treated
		HyperGraph<CWId<E,W>> remainCWIHG = new HyperGraph<CWId<E,W>>();
		for (Collection<E> he : hg.getHEdges()) {
			HashSet<CWId<E,W>> whedge = new HashSet<CWId<E,W>>();
			for (E v : he){
				
				whedge.add(new CWId<E,W>(v));
			}
			remainCWIHG.addHedge(whedge);
		}
		
		// CHOOSE A ROOT NODE
		CWId<E,W> root = new CWId<E,W> (chooseRoot("min",hg));
		ArrayList<CWId<E,W>> neighOfRoots = 
			new ArrayList<CWId<E,W>>(remainCWIHG.getNeighbors(root));

//		System.out.println(" root "+ root);
//		System.out.println(" neighOfRoots "+ neighOfRoots);
		
		// INITIALIZE STRUCTURES FOR THE JOINTREE
		
		// bucks.get(i), return the bucket of id i
		ArrayList<ArrayList<E>> bucks = new ArrayList<ArrayList<E>>();
		
		// node2iBucks.get(e) returns the list of buckets created by the node
		HashMap<E,ArrayList<Integer>> node2iBucksBuilt = new HashMap<E, ArrayList<Integer>>();
		
		
		// iBuck2Node.get(i) returns the node where iBuck was created
		HashMap<Integer,E> iBuckBuilt2Node = new HashMap<Integer,E>();
		
		// iBuck2iFather.get(i) returns the father's bucket of i
		HashMap<Integer,Integer> iBuck2iFather = new HashMap<Integer, Integer>();
		
		// iBuck2iBuckAncestors.get(i) returns the ancestors ' buckets of i
		HashMap<Integer,ArrayList<Integer>> iBuck2iBuckAncestors = new HashMap<Integer, ArrayList<Integer>>();
		
		// Adding the root to the jointree structure
		ArrayList<E> b =new ArrayList<E>();
		b.add(root._elmt);
		bucks.add(0,b);
		node2iBucksBuilt.put(root._elmt,new ArrayList<Integer>());
		node2iBucksBuilt.get(root._elmt).add(0);
		iBuckBuilt2Node.put(0,root._elmt);
		iBuck2iFather.put(0, 0);
		iBuck2iBuckAncestors.put(0, new ArrayList<Integer>());
		iBuck2iBuckAncestors.get(0).add(0);
		
//		System.out.println("\nnode2iBucks ");
//		for(E elmt : node2iBucksBuilt.keySet()){
//			System.out.print(" "+elmt+" has built IBucks");
//			for(Integer iB : node2iBucksBuilt.get(elmt))
//				System.out.print(" "+iB+",");
//			System.out.println();
//		}
//		
//		System.out.println("\niBuckBuilt2Node ");
//		for(Integer iB : iBuckBuilt2Node.keySet()){
//			System.out.print(" "+iB+" has been built by Node "+iBuckBuilt2Node.get(iB));
//			System.out.println();
//		}
//		
//		System.out.println("\nbucks ");
//		for(int iB=0;iB<bucks.size();iB++){
//			System.out.print(" buck:"+iB+" contains "+bucks.get(iB));
//			System.out.println();
//		}
//		
//		System.out.println("\niBuck2iFather ");
//		for(int iB=0;iB<iBuck2iFather.size();iB++){
//			System.out.print(" buck:"+iB+" has as father "+iBuck2iFather.get(iB));
//			System.out.println();
//		}
//		
//		System.out.println("\niBuck2iBuckAncestors ");
//		for(Integer iB : iBuck2iBuckAncestors.keySet()){
//			System.out.print(" "+iB+" has as Ancestors "+iBuck2iBuckAncestors.get(iB));
//			System.out.println();
//		}
		
		
		
		// INITIALIZE STUCTURE FOR THE LOOP
		
		// successorsOf.get(i) returns the successors of th node  i
		HashMap<E, ArrayList<E>> successorsOf = new HashMap<E, ArrayList<E>>();
		successorsOf.put(root._elmt,  new ArrayList<E>());
		successorsOf.get(root._elmt).add(root._elmt);
		
		
//		System.out.println("\nsuccessors ");
//		for(E elmt : successorsOf.keySet()){
//			System.out.print(" "+elmt+" sucs ");
//			for(E pred : successorsOf.get(elmt))
//				System.out.print(" "+pred);
//			System.out.println();
//		}
		
	
		
		// valid weighted Node 2 potential successors
		// vwn2pp.get(wn1).get(wn2) represents a comparable for the edge (n1, n2) 
		HashMap<CWId<E,W>, HashMap<CWId<E,W>,Comparable<?>>> vwn2ps = new
		HashMap<CWId<E,W>, HashMap<CWId<E,W>,Comparable<?>>>();
		// valid node 2 potential successors best buckets 
		
		
		vwn2ps.put(root, new HashMap<CWId<E,W>, Comparable<?>>());
		linkNvNode2vwn2pp(neighOfRoots, remainCWIHG, vwn2ps,
				bucks,node2iBucksBuilt, successorsOf, iBuck2iBuckAncestors);
		
		for(CWId<E,W> cwidST : vwn2ps.keySet()){
			for(CWId<E,W> cwidND : vwn2ps.get(cwidST).keySet()){
				updateArcWeight(cwidST, cwidND, vwn2ps, bucks, node2iBucksBuilt, successorsOf, iBuck2iBuckAncestors);
			}
		}
		
//		 System.out.println("\nvwn2ps ");
//			for(CWId<E,W> st : vwn2ps.keySet()){
//				System.out.print(st+" succs: ");
//				for(CWId<E,W> nd : vwn2ps.get(st).keySet())
//					System.out.print(" "+nd+" "+ 
//							vwn2ps.get(st).get(nd)+",");
//				System.out.println();
//			}
		
		
		
		
		// a chosen arc is an element at start and a bucket at end
		ArrayList<CWId<E,W>> chosenStarts = new ArrayList<CWId<E,W>>();
		ArrayList<Integer> chosenIBEnds  = new ArrayList<Integer>();
		
		for(CWId<E,W> neigh : remainCWIHG.getNeighbors(root)){
			chosenStarts.add(neigh);
			chosenIBEnds.add(0);
		}
		
//		System.out.println("\nchosen arc");
//		for(int i=0;i<chosenStarts.size();i++ ){
//			System.out.println(chosenStarts.get(i)+ " -> " + chosenIBEnds.get(i));
//		}
		
		
		while(!chosenStarts.isEmpty() && !chosenIBEnds.isEmpty()){
			
			// Here is the procedure for adding an arc between an element and a bucket
			
			 ArrayList<CWId<E, W>> treatedNode2Invest = new ArrayList<CWId<E,W>>();
			// we create a the bucket for each start of a chosen arc and link it
			// to the bucket iBend
			for(int iArc =0; iArc<chosenStarts.size(); iArc++){
				
				
				CWId<E,W> cwidStart = chosenStarts.get(iArc);
				Integer iBend = chosenIBEnds.get(iArc);
				
				addArcElmt2IBuck(
						 cwidStart,
						 iBend,
						 remainCWIHG,
						 vwn2ps,
						 bucks,
						 node2iBucksBuilt,
						 iBuckBuilt2Node,
						 successorsOf,
						 iBuck2iFather,
						 iBuck2iBuckAncestors,
						 treatedNode2Invest
				);
			}
			
			
			for(CWId<E,W> cwidST : vwn2ps.keySet()){
				if(vwn2ps.get(cwidST).isEmpty() && 
						 remainCWIHG.getNeighbors(cwidST).size()>0)
					if(!treatedNode2Invest.contains(cwidST))
						treatedNode2Invest.add(cwidST);
			}
			
//			System.out.println("\n\nTREATED Node2Invest "+treatedNode2Invest);
			
			
			
			if(!treatedNode2Invest.isEmpty()){
				 ArrayList<CWId<E,W>> nvNeighs = nvWNoteToInvestigate(treatedNode2Invest, remainCWIHG, vwn2ps);
				 
//				 System.out.println("nvWNoteToInvestigate "+nvNeighs);
				 
				 linkNvNode2vwn2pp(nvNeighs, remainCWIHG, vwn2ps, 
							bucks,node2iBucksBuilt, successorsOf, iBuck2iBuckAncestors);
				
				
			 }
			
			for(CWId<E,W> cwidST : vwn2ps.keySet()){
				for(CWId<E,W> cwidND : vwn2ps.get(cwidST).keySet()){
					updateArcWeight(cwidST, cwidND, vwn2ps, bucks, node2iBucksBuilt, successorsOf, iBuck2iBuckAncestors);
				}
			}
			
			 updateNodeCWIdHG(vwn2ps,remainCWIHG, bucks,node2iBucksBuilt,iBuckBuilt2Node);
			 
			 
			 
			 
//			 System.out.println("\nsuccessors ");
//				for(E elmt : successorsOf.keySet()){
//					System.out.print(" "+elmt+" sucs ");
//					for(E pred : successorsOf.get(elmt))
//						System.out.print(" "+pred);
//					System.out.println();
//				}
			 
//			 System.out.println("\nvwn2ps ");
//				for(CWId<E,W> st : vwn2ps.keySet()){
//					System.out.print(st+" succs: ");
//					for(CWId<E,W> nd : vwn2ps.get(st).keySet())
//						System.out.print(" "+nd+" "+ 
//								vwn2ps.get(st).get(nd)+",");
//					System.out.println();
//				}
//				
//			 
//			 System.out.println("\nnode2iBucks ");
//				for(E elmt : node2iBucksBuilt.keySet()){
//					System.out.print(" "+elmt+" has built IBucks");
//					for(Integer iB : node2iBucksBuilt.get(elmt))
//						System.out.print(" "+iB+",");
//					System.out.println();
//				}
				
//				System.out.println("\niBuckBuilt2Node ");
//				for(Integer iB : iBuckBuilt2Node.keySet()){
//					System.out.print(" "+iB+" has been built by Node "+iBuckBuilt2Node.get(iB));
//					System.out.println();
//				}
				
//				System.out.println("\nbucks ");
//				for(int iB=0;iB<bucks.size();iB++){
//					System.out.print(" buck:"+iB+" contains "+bucks.get(iB));
//					System.out.println();
//				}
//				
//				System.out.println("\niBuck2iFather ");
//				for(int iB=0;iB<iBuck2iFather.size();iB++){
//					System.out.print(" buck:"+iB+" has as father "+iBuck2iFather.get(iB));
//					System.out.println();
//				}
//				
//				System.out.println("\niBuck2iBuckAncestors ");
//				for(Integer iB : iBuck2iBuckAncestors.keySet()){
//					System.out.print(" "+iB+" has as Ancestors "+iBuck2iBuckAncestors.get(iB));
//					System.out.println();
//				}
				
				
			
			// we select the Min Edges of the graph in a distributed setting should be modified
				CWId<E,W> startMin = null;
				for(CWId<E,W> start :vwn2ps.keySet())
					if(start._weight != null){
						if(startMin == null)
							startMin=start;
						else
							if(startMin.compareTo(start)>0)
								startMin=start;
					}
					
//				System.out.println(" \n \n startMin "+startMin);
				
				chosenStarts.clear();
				chosenIBEnds.clear();
				
				if(startMin != null){
					chosenStarts.add(startMin);
					chosenIBEnds.add(((COList<Integer>)startMin._weight).
						get(((COList<Integer>)startMin._weight).size()-1));
				}
		}
		
		// at end
		
//		System.out.println(" root "+root);
//		
//		
//		System.out.println("\niBuckBuilt2Node ");
//		for(Integer iB : iBuckBuilt2Node.keySet()){
//			System.out.print(" "+iB+" has been built by Node "+iBuckBuilt2Node.get(iB));
//			System.out.println();
//		}
//		
//		System.out.println("\niBuck2iFather ");
//		for(int iB=0;iB<iBuck2iFather.size();iB++){
//			System.out.print(" buck:"+iB+" has as father "+iBuck2iFather.get(iB));
//			System.out.println();
//		}
//		
//		
//		
//		System.out.println("\nnode2iBucks ");
//		for(E elmt : node2iBucksBuilt.keySet()){
//			System.out.print(" "+elmt+" has built IBucks");
//			for(Integer iB : node2iBucksBuilt.get(elmt))
//				System.out.print(" "+iB+",");
//			System.out.println();
//		}
//		
//		
//		System.out.println("\nbucks ");
//		for(int iB=0;iB<bucks.size();iB++){
//			System.out.print(" buck:"+iB+" contains "+bucks.get(iB));
//			System.out.println();
//		}
		
		
		
		
		
		
		// add the vertices to the dualBucket
		for(Integer iB : iBuck2iFather.keySet()){
			dualHjt.addVertex(new AbstCompArrayList<E>(bucks.get(iB),iB));
		}
		// add the edges to the dualucket
		for(Integer iB : iBuck2iFather.keySet()){
			if(!iBuck2iFather.get(iB).equals(iB) ){
				AbstCompArrayList<E>  alc1 = new AbstCompArrayList<E>(bucks.get(iB),iB);
				
				AbstCompArrayList<E>  alc2 = new AbstCompArrayList<E>( bucks.get(iBuck2iFather.get(iB)),iBuck2iFather.get(iB)); 
				
				dualHjt.addHedge(alc1,alc2);
					
			}
		}
		
		
		HyperGraph<E> hjt = new HyperGraph<E>();
		// add the edge to hjt
		for(Integer iB : iBuck2iFather.keySet()){
			hjt.addHedge((bucks.get(iB)));
		}
		return hjt;
		
	}
		
	public static <E extends Comparable<E>,W extends Comparable<W>> HyperGraph<E> distribJT6(
			HyperGraph<E> hg, HyperGraph<AbstCompArrayList<E>> dualHjt, W hversion) {

		// transforms the hyper graph hg in a weighted hyper graph whg
		// remainWHG contains weighted node and edges not treated
		HyperGraph<CWId<E,W>> remainCWIHG = new HyperGraph<CWId<E,W>>();
		for (Collection<E> he : hg.getHEdges()) {
			HashSet<CWId<E,W>> whedge = new HashSet<CWId<E,W>>();
			for (E v : he){
				
				whedge.add(new CWId<E,W>(v));
			}
			remainCWIHG.addHedge(whedge);
		}
		
		// CHOOSE A ROOT NODE
		CWId<E,W> root = new CWId<E,W> (chooseRoot("max",hg));
		ArrayList<CWId<E,W>> neighOfRoots = 
			new ArrayList<CWId<E,W>>(remainCWIHG.getNeighbors(root));
		// 

		

		return null;
		
	}
	
	
	public static HyperGraph<Integer> grid(int nbL, int nbC){
		HyperGraph<Integer> hg = new HyperGraph<Integer>();
		for(int l=0;l<nbL;l++){
			for(int c=0;c<nbC;c++){
				//int ligne =nbC*l;
				if(c<nbC-1)
					hg.addHedge(l*nbC+c,l*nbC+c+1);
				if(l<nbL-1)
					hg.addHedge(l*nbC+c,(l*nbC)+nbC+c);
			}
		}
		
		return hg;
	}
	
	public static void exemple2(){
		ArrayList<String> row1 = new ArrayList<String>(Arrays.asList("1", "2"));
		ArrayList<String> row2 = new ArrayList<String>(Arrays.asList("2", "3"));
		ArrayList<String> row3 = new ArrayList<String>(Arrays.asList("3", "4"));
		ArrayList<String> row4 = new ArrayList<String>(Arrays.asList("4", "1"));
		ArrayList<String> row5 = new ArrayList<String>(Arrays.asList("2", "4"));
		
		ArrayList<String> row6 = new ArrayList<String>(Arrays.asList("5", "6"));
		ArrayList<String> row7 = new ArrayList<String>(Arrays.asList("6", "7"));
		ArrayList<String> row8 = new ArrayList<String>(Arrays.asList("7", "8"));
		ArrayList<String> row9 = new ArrayList<String>(Arrays.asList("8", "5"));
		ArrayList<String> row10 = new ArrayList<String>(Arrays.asList("6", "8"));
		
		ArrayList<String> row11 = new ArrayList<String>(Arrays.asList("9", "10"));
		ArrayList<String> row12 = new ArrayList<String>(Arrays.asList("10", "11"));
		ArrayList<String> row13 = new ArrayList<String>(Arrays.asList("11", "12"));
		ArrayList<String> row14 = new ArrayList<String>(Arrays.asList("12", "9"));
		ArrayList<String> row15 = new ArrayList<String>(Arrays.asList("10", "12"));
		
		ArrayList<String> row16 = new ArrayList<String>(Arrays.asList("13", "14"));
		ArrayList<String> row17 = new ArrayList<String>(Arrays.asList("14", "15"));
		ArrayList<String> row18 = new ArrayList<String>(Arrays.asList("15", "16"));
		ArrayList<String> row19 = new ArrayList<String>(Arrays.asList("16", "13"));
		ArrayList<String> row20 = new ArrayList<String>(Arrays.asList("14", "16"));

		ArrayList<String> row21 = new ArrayList<String>(Arrays.asList("1", "5"));
		ArrayList<String> row22 = new ArrayList<String>(Arrays.asList("5", "9"));
		ArrayList<String> row23 = new ArrayList<String>(Arrays.asList("9", "13"));
		ArrayList<String> row24 = new ArrayList<String>(Arrays.asList("13", "1"));
		
		HyperGraph<String> hg = new HyperGraph<String>();
		hg.addHedge(row1);
		hg.addHedge(row2);
		hg.addHedge(row3);
		hg.addHedge(row4);
		hg.addHedge(row5);
		hg.addHedge(row6);
		hg.addHedge(row7);
		hg.addHedge(row8);
		hg.addHedge(row9);
		hg.addHedge(row10);
		hg.addHedge(row11);
		hg.addHedge(row12);
		hg.addHedge(row13);
		hg.addHedge(row14);
		hg.addHedge(row15);
		hg.addHedge(row16);
		hg.addHedge(row17);
		hg.addHedge(row18);
		hg.addHedge(row19);
		hg.addHedge(row20);
		hg.addHedge(row21);
		hg.addHedge(row22);
		hg.addHedge(row23);
		hg.addHedge(row24);
		
		System.out.println(" Le graphe " + hg);
//		 ArrayList<String> order = HyperGraphs.minFillInvOrder(hg);
//		
//		 ArrayList<ArrayList<String>> buckets = new
//		 ArrayList<ArrayList<String>>();
//		 String[] varFather = new String[order.size()];
//		
//		 System.out.println(" les noeuds ordonnés "+order+"\n");
//		 HyperGraph<String> jt = HyperGraphs.bucketElimination(hg,order,
//		 buckets,
//		 varFather);
//		 System.out.println(" jointree par BE Min Fill"+jt);
		
		 
		HyperGraph<AbstCompArrayList<String>> dualHjt = new HyperGraph<AbstCompArrayList<String>>();
		Integer hVersion = 0;
		HyperGraph<String> jt5 = distribJT5(hg, dualHjt,hVersion);
		
		
		System.out.println("\n jointree par distribJT5" + jt5);
		System.out.println( " \n check the jointree par distribJT5 "+HyperGraphs.isAjointreeOf(dualHjt, hg));
		System.out.println(" \n Dual jointree par distribJT5 " + dualHjt);
	}
	
	public static void exemple1(){
		
		ArrayList<String> row1 = new ArrayList<String>(Arrays.asList("a", "b"));
		ArrayList<String> row2 = new ArrayList<String>(Arrays.asList("b", "c"));
		ArrayList<String> row3 = new ArrayList<String>(Arrays.asList("c", "d"));
		ArrayList<String> row4 = new ArrayList<String>(Arrays.asList("d", "a"));

		ArrayList<String> row5 = new ArrayList<String>(Arrays.asList("a", "e"));
		ArrayList<String> row6 = new ArrayList<String>(Arrays.asList("e", "f"));
		ArrayList<String> row7 = new ArrayList<String>(Arrays.asList("f", "g"));
		ArrayList<String> row8 = new ArrayList<String>(Arrays.asList("g", "a"));

		ArrayList<String> row9 = new ArrayList<String>(Arrays.asList("a", "h"));
		ArrayList<String> row10 = new ArrayList<String>(Arrays.asList("h", "i"));
		ArrayList<String> row11 = new ArrayList<String>(Arrays.asList("i", "a"));

		ArrayList<String> row12 = new ArrayList<String>(Arrays.asList("a", "j"));
		ArrayList<String> row13 = new ArrayList<String>(Arrays.asList("j", "k"));
		ArrayList<String> row14 = new ArrayList<String>(Arrays.asList("k", "a"));
		ArrayList<String> row15 = new ArrayList<String>(Arrays.asList("j", "l"));
		ArrayList<String> row16 = new ArrayList<String>(Arrays.asList("l", "k"));
		// ArrayList<String> row17 = new
		// ArrayList<String>(Arrays.asList("k","l"));

		HyperGraph<String> hg = new HyperGraph<String>();
		hg.addHedge(row1);
		hg.addHedge(row2);
		hg.addHedge(row3);
		hg.addHedge(row4);
		hg.addHedge(row5);
		hg.addHedge(row6);
		hg.addHedge(row7);
		hg.addHedge(row8);
		hg.addHedge(row9);
		hg.addHedge(row10);
		hg.addHedge(row11);
		hg.addHedge(row12);
		hg.addHedge(row13);
		hg.addHedge(row14);
		hg.addHedge(row15);
		hg.addHedge(row16);

		System.out.println("\n Le graphe " + hg);
//		 ArrayList<String> order = HyperGraphs.minFillInvOrder(hg);
//		
//		 ArrayList<ArrayList<String>> buckets = new
//		 ArrayList<ArrayList<String>>();
//		 String[] varFather = new String[order.size()];
//		
//		 System.out.println(" les noeuds ordonnés "+order+"\n");
//		 HyperGraph<String> jt = HyperGraphs.bucketElimination(hg,order,
//		 buckets,
//		 varFather);
//		 System.out.println(" jointree par BE Min Fill"+jt);
		
		 
		HyperGraph<AbstCompArrayList<String>> dualHjt = new HyperGraph<AbstCompArrayList<String>>();
		Integer hVersion = 0;
		HyperGraph<String> jt5 = distribJT5(hg, dualHjt,hVersion);
		
		
		System.out.println("\n jointree par distribJT5 " + jt5);
		System.out.println( " \n check the jointree par distribJT5 "+HyperGraphs.isAjointreeOf(dualHjt, hg));
		System.out.println(" \n Dual jointree par distribJT5 " + dualHjt);
	}
	
	public static void testGrids(){
		HyperGraph<Integer> grid = grid(5,3);
		
		System.out.println(" grid" + grid);
		
		HyperGraph<AbstCompArrayList<Integer>> dualHjt = new HyperGraph<AbstCompArrayList<Integer>>();
		Integer hVersion = 0;
		HyperGraph<Integer> jt2 = distribJT5 (grid, dualHjt,hVersion);
		
		
		System.out.println(" jointree par DistBotUp 2" + jt2);
		
		System.out.println(" dual HJT" + dualHjt);
		System.out.println( " \n check the jointree par DistBotUp "+HyperGraphs.isAjointreeOf(dualHjt, grid));
		// System.out.println(" \n Dual jointree par DistBotUp 2 " + dualHjt);
		
		
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		exemple1();
//		exemple2();
		testGrids();

	}

}
