package benchMarkGenerator.peerTheory;

import java.util.ArrayList;
import java.util.HashMap;

import propositionalLogic.Base;

public abstract class PeerTheory {
	

		String _name;
		int _nbVars;
		
		ArrayList<String> _neighbors;
		ArrayList<ArrayList<String>> _th;
		ArrayList<ArrayList<String>> _thMapping;
		ArrayList<String> _prodConsistent;
		ArrayList<String> _allLits;
		ArrayList<String> _sharedLit;
		
		
		public boolean allShConnected =false;
		
//		public RandomPeerTheory(TheoryParam tp){
//			init();
//			int nbClauses = (int) Math.ceil((double) tp._nbVars * (double)tp._rNbClsBynbVars);
//			randomlyInitTh( tp._nbVars , tp._nbVarByCl,nbClauses);
//			setNbProdLitsConsistent(tp._nbTargetLit);
//		}
		
		public PeerTheory(String name,HashMap<String,Object> params){
			init(name);
			int nbShared = (Integer) params.get("nbShared");
			int nbTargetLit = (Integer) params.get("nbTargetLit");
			int nbLocal = (Integer) params.get("nbLocal");
			
			_nbVars = nbShared + nbTargetLit+nbLocal;
			
			for(int v=0;v<_nbVars;v++){
				_allLits.add(int2varString(2*v));
				_allLits.add(int2varString(2*v+1));
			}
			setNbProdLitsConsistent(nbTargetLit);
			setNbShVars(nbShared);
		}
		
		
		protected void init(String name){
			_name=name;
			_neighbors = new ArrayList<String>();
			_th= new ArrayList<ArrayList<String>>();
			_thMapping = new ArrayList<ArrayList<String>>();
			_allLits = new ArrayList<String>();
			_prodConsistent = new ArrayList<String>();
			_sharedLit = new ArrayList<String>();
		}
		
		public ArrayList<String> get_allLits() {
			return _allLits;
		}
		

		protected void setNbProdLitsConsistent(int nbProdLits){
			for(int i=0;i< nbProdLits;i++){
				_prodConsistent.add(int2varString(2*i));
			}
		}
		
		public void setNbShVars( int nbShVars){
			for(int i=0;i< nbShVars;i++){
				int iLit = _prodConsistent.size()*2 + 2*i;
				_sharedLit.add(int2varString(iLit));
				_sharedLit.add(int2varString(iLit+1));
			}
		}
		
		public ArrayList<String> get_sharedLit() {
			return _sharedLit;
		}

		public abstract Object getParam(String paramName);
		
		public abstract void addALinkWith(PeerTheory desc) ;
		
		
		
		public String get_name() {
			return _name;
		}

		public void set_name(String _name) {
			this._name = _name;
		}



		public ArrayList<String> get_neighbors() {
			return _neighbors;
		}



		public void set_neighbors(ArrayList<String> _neighbors) {
			this._neighbors = _neighbors;
		}



		public ArrayList<ArrayList<String>> get_th() {
			return _th;
		}



		public void set_th(int[][] th) {
			for(int iCl =0;iCl<th.length;iCl++){
				ArrayList<String> cl = new ArrayList<String>();
				for(int iLit=0;iLit<th[iCl].length;iLit++){
					cl.add(int2varString(th[iCl][iLit]));
				}
				_th.add(cl);
			}
		}
		
		protected  String int2varString(int i){
			String var = _name+":v"+(i/2);
			return (i%2==0)? "!"+var:var;
		}



		public ArrayList<ArrayList<String>> get_thMapping() {
			return _thMapping;
		}



		public void set_thMapping(ArrayList<ArrayList<String>> mapping) {
			_thMapping = mapping;
		}

		public ArrayList<String> get_prod() {
			return _prodConsistent;
		}

		public void set_prod(ArrayList<String> prod) {
			_prodConsistent = prod;
		}
		
		public String toString(){
			String result =_name+"\n";
			for(ArrayList<String> cl : _th ){
				for(String lit : cl){
					result += lit+" ";
				}
				result+="\n";
			}
			result+="Mappings\n";
			for(ArrayList<String> cl : _thMapping ){
				for(String lit : cl){
					result += lit+" ";
				}
				result+="\n";
			}
			result+="Production\n";
			for(String lit : _prodConsistent){
				result += lit+" ";
			}
			
			return result;
		}

		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
