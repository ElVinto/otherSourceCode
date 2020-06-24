package distributedAlgorithm.m2dt.MsgContent;

import java.io.Serializable;
import java.util.HashMap;

public class MAckVocOfInterest implements Serializable {
	
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public HashMap<Number,Number> _intToint = null;
	public MAckVocOfInterest(HashMap<Number,Number> intToint){
		_intToint = intToint ;
	}

}
