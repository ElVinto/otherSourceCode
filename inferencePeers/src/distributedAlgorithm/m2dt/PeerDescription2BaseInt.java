package distributedAlgorithm.m2dt;


import java.util.ArrayList;
import java.util.TreeSet;

import tools.Dprint;


public class PeerDescription2BaseInt {

	public static ArrayList<String> arrayOfLit(ArrayList<ArrayList<String>> th) {
		ArrayList<String> _litNames = new ArrayList<String>();
		TreeSet<String> t = new TreeSet<String>();
		for (ArrayList<String> cl : th)
			for (String l : cl) {
				if (l.contains("!"))
					l = l.replace("!", "");
				if (!t.contains(l))
					t.add(l);
			}
		for (String l : t) {
			_litNames.add(l);
			_litNames.add("!" + l);
		}
		return _litNames;
	}

	public static int[][] clausesOfInt(ArrayList<String> litNames,
			ArrayList<ArrayList<String>> th) {
		int[][] clauses = new int[th.size()][];
		for (int cl = 0; cl < th.size(); cl++) {
			clauses[cl] = new int[th.get(cl).size()];
			for (int l = 0; l < clauses[cl].length; l++) {
				int ind = litNames.indexOf(th.get(cl).get(l));
				clauses[cl][l] = ind;
			}
		}
		return clauses;
	}

	public static int[] litsToInt(ArrayList<String> litNames,
			ArrayList<String> lits) {
		int[] result = new int[lits.size()];
		for (int i = 0; i < lits.size(); i++)
			result[i] = litNames.indexOf(lits.get(i));
		return result;
	}

	public static ArrayList<String> litsToString(ArrayList<String> litNames,
			int[] lits) {
		
//		Dprint.print( litNames.get(0)+"Implicant size : "+ lits.length+ " ");
//		for(int i=0;i<lits.length;i++)
//			Dprint.print(lits[i]+" ");
//		Dprint.println("");
//		Dprint.println("litNames "+litNames);
		
		ArrayList<String> res = new ArrayList<String>(lits.length);
		for (int i = 0; i < lits.length; i++)
			res.add(litNames.get(lits[i]));
		return res;
	}

}
