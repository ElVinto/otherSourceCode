package distributedAlgorithm;

import java.io.Serializable;
import java.util.ArrayList;

import peers.LocalPeer;
import peers.PeerDescription;
import communication.Msg;

public interface DistributedAlgorithm {
	
	/*
	 * return the name of the distributed algorithm Type of 
	 */
	public String getDAName();
	/*
	 * return the name of the peer
	 */
	public String getName();
	
	/*
	 * adds the list of parameters
	 */
	public void addParams(ArrayList<String> listParams);
	
	/*
	 * replace the constructor of the distributed algorithm
	 */
	public void initWith(String name,PeerDescription pD);
	
	/*
	 * associate  a local Peer to the distributed algorithm
	 */
	public void setLocalPeer( LocalPeer lp);
	
	/*
	 * return the associated local peer 
	 */
	public LocalPeer getLocalPeer();
	
	/*
	 *  Return true if all statements related to the intermediate result  
	 */
	public  <Impl> boolean middleResult(Impl imp);
	
	public void ends(String pName);
	
	/*
	 * return true if the algorithm has ever begun
	 */
	public  boolean isAwaked();

	/*
	 * First statements execute by a peer, returns true
	 * if all the instructions are well executed. 
	 */
	public boolean wakeUp();
	
	/*
	 * Unwrap and a Message m and executes the associated 
	 * procedure
	 */
	public <MContent extends Serializable>  boolean  receiveMsg(Msg<MContent> m);
	
	
	/*
	 * A general method to allow other tasks to be executed
	 */
	public void otherTask();
	
	/*
	 * return true if the distributed algorithm has finished  
	 */
	public  boolean isTerminated();
	
	/*
	 * return true if all the instructions of the termination
	 * succeed
	 */
	public  void terminate();
	
	public void  notifyEndFor(String obName);
	
	public void writeStats();
	public boolean remainingTask();
	
}
