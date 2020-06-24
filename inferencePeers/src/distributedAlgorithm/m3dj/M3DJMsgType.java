package distributedAlgorithm.m3dj;

import java.io.Serializable;

public class M3DJMsgType implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int NEIGHBORS = 20;
	public static final int MINFILL = 21;
	public static final int VOTE = 22;
	//public static final int DIAGREQ = 33;
	public static final int MERGE = 24;
	public static final int CHANGE_FATHER = 25;

}
