package tools;



public class RingNum {
	int _nbPeer =0;
	
	public RingNum(int nbPeer){
		_nbPeer = nbPeer;
	}
	
	public int pos(int x){
		return x>=0 ?x%_nbPeer:_nbPeer+(x%_nbPeer);
	}
	
	public int add(int x,int y){
		return 	pos((x+y));
	}
	
	public int minus(int x,int y){
		return pos(x-y);
	}
	
	public boolean between(int val, int bot,int top){
		int pos1 = pos(bot);
		int pos2 = pos(top);
		int p = pos(val);
		return (pos1<=pos2)?
				(p>=pos1 && p<=pos2) :
				!between(p, pos2+1, pos1-1);
	}
	


}
