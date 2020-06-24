package tools;

public class ArrayInt {
	
	public static int indexOf(int[] a,int e){
		for(int i=0;i<a.length;i++){
			if(a[i]==e)
				return i;
		}
		return -1;
	}
	
	public static int indexOf(String[] a,String e){
		for(int i=0;i<a.length;i++){
			if(a[i].equals(e))
				return i;
		}
		return -1;
	}

}
