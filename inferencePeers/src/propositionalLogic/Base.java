package propositionalLogic;

import tools.Dprint;



/* 
 * A base is a set of set of literal
 * 
 * if b is a CNF 
 *  b is Empty : b is True
 *  b contains an empty set (clause) : b is False
 *  
 * if b is a DNF
 *  b is empty : b is False
 *  b contains an empty set (product) : b is True
 * 
 * */
public class Base {
	
	
	/*
	 *  Static members
	 */
	public static final int NOVALUE = -1;
	
	
	public static boolean isEmpty(int [][]b){
		return b.length==0;
	}
	
	public static boolean containsEmptySet(int [][]b){
		for(int i=0;i<b.length;i++)
			if(b[i].length ==0)
				return true;
		return false;
	}
	
	public static int[][] distribution(int[][] b1, int[][] b2){
		int[][] bTmp = new int[b1.length*b2.length][];
		int curLength = 0;
		for(int i1=0; i1<b1.length;i1++)
			for(int i2=0;i2<b2.length;i2++){
				int[] lRes =union(b1[i1],b2[i2]);
				if(lRes.length !=0){
					bTmp[curLength]= lRes;
					curLength ++;
				}
			}
		int[][] bRes = new int[curLength][];
		System.arraycopy(bTmp, 0, bRes, 0, curLength);
		return bRes;
	}
	
	public static int[] union(int[] lits1, int[] lits2){
		if(isConsistent(lits1,lits2)){
			int curLength = lits1.length;
			for(int i = lits1.length-1; i>=0;i--){
				if(contains(lits2,lits1[i])){
					swap(lits1,i,curLength-1);
					curLength --;
				}
			}
			int[] lRes = new int[curLength+lits2.length];
			System.arraycopy(lits1, 0, lRes, 0, curLength);
			System.arraycopy(lits2, 0, lRes, curLength, lits2.length);
			return lRes;
		}	
		return new int[0];		
	}
	
	public static int[][]union(int[] f1,int[][]b2){
		int [][] tmp =  {f1};
		return union(b2,tmp);
	}
	
	/*
	 * Return the union of non empty bases b1, b2
	 */
	public static int[][] union(int[][] b1, int[][] b2){	
		int[][] bRes = new int[b1.length+b2.length][];
		for(int i=0;i<b1.length;i++){
			bRes[i]= new int[b1[i].length];
			System.arraycopy(b1[i], 0, bRes[i], 0, b1[i].length);
		}

		for(int i=0;i<b2.length;i++){
			bRes[i+b1.length]= new int[b2[i].length];
			System.arraycopy(b2[i], 0, bRes[i+b1.length], 0, b2[i].length);
		}
		
		return bRes;
	}
	
	public static int[][] remove(int[][] b, int[] lits){
		int[][] bRes = new int[b.length][];
		for(int i=0;i<b.length;i++){
			bRes[i] =remove(b[i],lits);
		}
		return bRes ;
	}
	
	public static int[] remove(int []impl,int[]lits){
		int curLength = impl.length;
		for(int j=impl.length-1;j>=0;j--){
			if(contains(lits,impl[j])){
				swap(impl,j,curLength-1);
				curLength--;
			}
		}
		int[] implRes = new int[curLength];
		System.arraycopy(impl, 0, implRes, 0, curLength);
		return implRes;
	}
	

	
	public static int[] restrictOn (int[]impl, int[] target){
		
		if(target==null)
			return impl;
		if(target.length ==0)
			return impl;
		
		int curLength = impl.length;
		for(int j=impl.length-1;j>=0;j--){
			if(!contains(target,impl[j])){
				swap(impl,j,curLength-1);
				curLength--;
			}
		}
		int[] implRes = new int[curLength];
		System.arraycopy(impl, 0, implRes, 0, curLength);
		
		return implRes;
		
	}
	
	
	public static boolean addIfnotSubSubme(int[][][] tab,int iSender, int[] f) {
		
		// verifier iSender =0 selon appel
		int nbSubSubmeByf = 0;
		int[][]t= tab[iSender];
		
		for (int j = t.length - 1; j >= 0; j--) {
			int comp = Base.subsubmeComp(f, t[j]);
			
			if (comp == 1) {
				nbSubSubmeByf++;
				if(nbSubSubmeByf>=t.length)
					break;
				int[] temp = t[t.length - nbSubSubmeByf];
				t[t.length - nbSubSubmeByf] = t[j];
				t[j] = temp;
				
			}
			if (comp == -1)
				return false;
		}
		if(nbSubSubmeByf>=t.length){
			int[][]nvT = {f};
			tab[iSender]=nvT;
			return true;
		}
		if(nbSubSubmeByf>0 && nbSubSubmeByf< t.length){
			int[][]nvT = new int[t.length - nbSubSubmeByf][0];
			System.arraycopy(t, 0, nvT, 0, t.length - nbSubSubmeByf);
			tab[iSender]=Base.union(f,nvT);
		}else
			tab[iSender]=Base.union(f,t);
		return true;
	}

	
	
	
	public static int[] removeDuplicateLit(int[] impl){
		// put at the beginning of the table the duplicate occurence
		// of a literal
		//printSet(impl);
		int nvDeb =0;
		int i=0;
		while(i<impl.length-1){
			for(int j=i+1;j<impl.length;j++){
				if(impl[i]==impl[j]){
					swap(impl,nvDeb,j);
					nvDeb++;
				}
			}
			i=(i<nvDeb)?nvDeb:i+1;
		}
		int nvLength = impl.length-nvDeb;
		int [] result = new int[nvLength];
		System.arraycopy(impl, nvDeb, result, 0, nvLength);
		return result;
	}
	
	public static void main(String [] args){
		int[] impl = {};
		printSet(removeDuplicateLit(impl));
	}
	
	
	public static void swap(int []a, int pos1,int pos2){
		int temp = a[pos1];
		a[pos1]= a[pos2];
		a[pos2]=temp;
	}
	
	
	/*
	 * if lits1 is in lits2 return 1
	 * if lits2 is in or equals lits1 return -1
	 * otherwise return 0
	 */
	public static int subsubmeComp(int[] lits1, int[] lits2){
		
		if(lits1.length==0 && lits2.length>0)
			return 1;
		if(lits2.length==0 && lits1.length>=0)
			return -1;
		
		int nbCommonLits = 0;
		for(int i=0;i<lits1.length;i++)
			if(contains(lits2,lits1[i]))
				nbCommonLits ++;
		return (nbCommonLits==lits2.length)? -1:
			(nbCommonLits==lits1.length)? 1: 0;
	}
	
	public static boolean eq(int[] lits1, int[] lits2){
		for(int i=0;i<lits1.length;i++)
			if(!contains(lits2,lits1[i]))
				return false;
		return true;
	}
	
	public static int opposedLit(int i){
		return (i%2==0)? i+1 : i-1;
	}
	
	public static boolean contains (int[] lits, int lit){
		for(int i=0; i<lits.length;i++)
			if(lits[i]== lit)
				return true;
		return false;
	}
	
	public static boolean isConsistent(int[] lits1, int []lits2){
		for(int i=0;i< lits1.length; i++)
			if (contains(lits2,opposedLit(lits1[i])))
					return false;
		return true;
	}
	
	public static void printBase(int[][] b){
		for(int i=0;i<b.length;i++){
			for(int j=0;j<b[i].length;j++)
				Dprint.print(b[i][j]+" ");
			Dprint.println("");
		}
	}
	
	public static String base2String(int[][]b){
		String s ="";
		for(int i=0;i<b.length;i++){
			for(int j=0;j<b[i].length;j++)
				s= s+b[i][j]+" ";
			s=s+"\n";
			}
		return s;
	}
	
	public static void printSet(int[] a){
		for(int i=0;i<a.length;i++){
			Dprint.print(a[i]+" ");
		}
		Dprint.println("");
	}
	
	public static String set2String(int[] a){
		String s ="";
		for(int i=0;i<a.length;i++){
			s=s+ String.valueOf(a[i])+" ";
		}
		return s;
	}
	
	// other String operations
	
	public static String extractPeerFromLit(String l) {
		int last = l.lastIndexOf(":");
		String pName = l.substring(0, last);
		return pName.replace("!", "");
	}

	public static String oppLit(String lit) {
		return lit.contains("!") ? lit.substring(1) : "!" + lit;
	}
	
	public static String varOf(String lit){
		return (lit.contains("!"))? lit.substring(1) : lit;
		}
	
	

}
