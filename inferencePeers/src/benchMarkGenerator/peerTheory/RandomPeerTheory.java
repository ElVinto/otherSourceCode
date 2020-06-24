package benchMarkGenerator.peerTheory;

import java.util.ArrayList;
import java.util.HashMap;


import propositionalLogic.Base;

public class RandomPeerTheory extends PeerTheory {

	
	int _nextShLit =0;
	
	public RandomPeerTheory(String name,HashMap<String,Object> params){
		super(name,params);
	
		int nbShared = (Integer) params.get("nbShared");
		int nbTargetLit = (Integer) params.get("nbTargetLit");
		int nbLocal = (Integer) params.get("nbLocal");
		double nbClsBynbVars = (Double) params.get("nbClsBynbVars");
		int nblitInCl =(Integer) params.get("nblitInCl");
		

		int nbVars = nbShared + nbTargetLit+nbLocal;
		int nbClauses = (int)Math.round( nbClsBynbVars*((double)nbVars ));
		
		randomlyInitTh( nbVars, nblitInCl, nbClauses);
	}
	

	
	private void randomlyInitTh(int nbVars ,int nbLitInCl,int nbClauses ) {
		
		int[][] th = new int[nbClauses][nbLitInCl];
		
		
		for (int iCl = 0; iCl < th.length; iCl++) {
			for (int iLit = 0; iLit < th[iCl].length; iLit++) {
				int randLit;
				boolean goodLit;
				do {
					goodLit = true;
					randLit = (int) Math.round(Math.random()
							* ((double) (nbVars * 2)));
					for (int i = 0; i < iLit; i++)
						if (randLit == th[iCl][i]
								|| randLit == Base.opposedLit(th[iCl][i]))
							goodLit = false;
				} while (!goodLit);
				th[iCl][iLit] = randLit;
			}
		}

		for(int iCl =0;iCl<th.length;iCl++){
			ArrayList<String> cl = new ArrayList<String>();
			for(int iLit=0;iLit<th[iCl].length;iLit++){
				cl.add(int2varString(th[iCl][iLit]));
			}
			_th.add(cl);
		}
	}

	public String nextAvaible(){
		String result = _sharedLit.get(_nextShLit);
		if(_nextShLit==_sharedLit.size()-1){
			_nextShLit= 0;
			allShConnected  =true;
		}
		else
			_nextShLit++;
		return result;
	}
	public Object getParam(String paramName){
		if(paramName.equals("nextAvaible"))
			return nextAvaible();
		
		return null;
	}
	
	public void addALinkWith(PeerTheory desc) {
		
			ArrayList<String> cl1 = new ArrayList<String>();
			ArrayList<String> cl2 = new ArrayList<String>();
			
			cl1.add(nextAvaible());
			cl2.add(nextAvaible());
			cl2.add((String)desc.getParam("nextAvaible"));
			cl1.add((String)desc.getParam("nextAvaible"));
			
			_thMapping.add(cl1);
			_thMapping.add(cl2);
			desc._thMapping.add(cl1);
			desc._thMapping.add(cl2); 
			
			if(!_neighbors.contains(desc._name))
				_neighbors.add(desc._name);
			if(!desc._neighbors.contains(_name))
				desc._neighbors.add(_name);
		
	}
	

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}








	

}
