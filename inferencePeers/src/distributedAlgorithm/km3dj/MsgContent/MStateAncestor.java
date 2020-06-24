package distributedAlgorithm.km3dj.MsgContent;

import java.io.Serializable;
import java.util.ArrayList;

public class MStateAncestor implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public  boolean isInCycle ;
	public ArrayList<String> vVal;

	public MStateAncestor(boolean isInCycle_arg, ArrayList<String> vVal_arg) {
		isInCycle = isInCycle_arg;
		vVal = vVal_arg;
	}
	
	public String toString(){
		return " isInCycle: "+isInCycle+" vVal:"+vVal+" ";
	}
}
