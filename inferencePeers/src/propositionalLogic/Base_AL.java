package propositionalLogic;

import java.util.ArrayList;

public class Base_AL {
	
public static boolean addIfnotSubSubme(ArrayList<int[]> t, int[] f) {
		
		if(t.isEmpty()){
			t.add(f);
			return true;
		}
			
	
		ArrayList<int[]> toRem = new ArrayList<int[]>();
		
		for (int j = t.size() - 1; j >= 0; j--) {
			int comp = Base.subsubmeComp(f, t.get(j));
			
			if (comp == 1) {
				toRem.add(t.get(j));
			}
			if (comp == -1)
				return false;
			
		}
		
		t.removeAll(toRem);
		t.add(f);
		
		return true;
		
	}

}
