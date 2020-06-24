package distributedAlgorithm.km3dj;

import java.io.Serializable;

public class KM3DJMsgType implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int INFO = 29;
	public static final int DEPTH = 30;
	public static final int CHILD = 31; 
	public static final int ANCESTOR_STATE = 32;
	public static final int UPDATE = 33;
	public static final int SUBTREE_WIRED = 34;
	public static final int SHARED_KNOWLEDGE = 35;
	
	public static String typeToString(int type){
		switch(type){
		case(INFO): return "INFO";
		case(DEPTH): return "DEPTH";
		case(CHILD): return "CHILD";
		case(ANCESTOR_STATE): return "ANCESTOR_STATE";
		case(UPDATE): return "UPDATE";
		case(SUBTREE_WIRED): return "SUBTREE_WIRED";
		case(SHARED_KNOWLEDGE): return "SHARED_KNOWLEDGE";
		default:return "";
		}
	}
	

}
