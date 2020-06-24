package distributedAlgorithm.m2dt.MsgContent;

import java.io.Serializable;

public class MendM2DT implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int _nbRcvEndMsgChild = -1 ;
	
	public MendM2DT(int nbRcvEndMsgChild){
		_nbRcvEndMsgChild = nbRcvEndMsgChild; 
	}
	
	public int getNbRcvEndMsgChild(){
		return _nbRcvEndMsgChild ;
	}
	
	

}
