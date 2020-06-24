package main;

import java.io.Serializable;
import java.util.HashMap;

public class MsgerResult implements Serializable{

	public HashMap<String,Number>results;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String location;
	public MsgerResult (String location){
		this.location = location;
		results = new HashMap<String,Number>();
		results.put("nbOutMsg",0);
		results.put("nbLocalMsg",0);
	
	}

}
