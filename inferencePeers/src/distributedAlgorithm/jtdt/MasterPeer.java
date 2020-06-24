package distributedAlgorithm.jtdt;

import java.util.TreeMap;
import java.util.ArrayList;

import tools.WeightedElmt;

public class MasterPeer {
	
	

	String _mPName ;
	ArrayList<WeightedElmt<ArrayList<String>>> _weightTH ;
	ArrayList<String> _diagLit ;
	TreeMap <String, ArrayList<String>> _neighbors2Sh;
	
	
	// master peer of implicants
	// it maintains a tables of some rImplicants that comes peers' clauses
	//   rImplicants
	// mappings // with the other Master Peer
	// getMasterImplicants
	
	public MasterPeer(){
		_mPName = "" ;
		_diagLit = new ArrayList<String>();
		_weightTH = initWeighTH();
		_neighbors2Sh = new TreeMap<String, ArrayList<String>>();
			
	}

	// initially th is trivially true
	private ArrayList<WeightedElmt<ArrayList<String>>> initWeighTH(){
		ArrayList<WeightedElmt<ArrayList<String>>>wTH = new ArrayList<WeightedElmt<ArrayList<String>>>();//new ArrayList<ArrayList<String>>();
		wTH.add(new WeightedElmt<ArrayList<String>>(0,new ArrayList<String>()));
		wTH.add(new WeightedElmt<ArrayList<String>>(0,new ArrayList<String>()));
		wTH.get(0).getElement().add("e");
		wTH.get(1).getElement().add("!e");
		return wTH;
	}


	public void reInitWeightTH(ArrayList<ArrayList<String>> th){
		_weightTH.clear();
		for(ArrayList<String> prod: th){
			int nbAB = 0;
			for(String lit : prod)
				if (_diagLit.contains(lit))
					nbAB++;
			_weightTH.add(new WeightedElmt<ArrayList<String>>(nbAB,prod));
		}
	}

	public String get_mPName() {
		return _mPName;
	}

	public void set_mPName(String name) {
		_mPName = name;
	}

	public ArrayList<WeightedElmt<ArrayList<String>>> get_weightTH() {
		return _weightTH;
	}

	public void set_weightTH(ArrayList<WeightedElmt<ArrayList<String>>> _weightth) {
		_weightTH = _weightth;
	}

	public ArrayList<String> get_diagLit() {
		return _diagLit;
	}

	public void set_diagLit(ArrayList<String> lit) {
		_diagLit = lit;
	}

	public TreeMap<String, ArrayList<String>> get_neighbors2Sh() {
		return _neighbors2Sh;
	}

	public void set_neighbors2Sh(TreeMap<String, ArrayList<String>> sh) {
		_neighbors2Sh = sh;
	}
	
	/*
	 * Precond th and this are in DNF
	 * Update this._th by this._th and thConj 
	 * return true if the conjunction is consistent
	 * return false otherwise
	 */
	public boolean wedge(ArrayList<WeightedElmt<ArrayList<String>>> thConj){
		if(thConj.isEmpty()||_weightTH.isEmpty())
			return false;
		if(isTriviallyTrue(thConj))
			return true;
		if(isTriviallyTrue(_weightTH)){
			_weightTH.clear();
			_weightTH.addAll(thConj);
			return true;
		}
		ArrayList<WeightedElmt<ArrayList<String>>> updateTh = new ArrayList<WeightedElmt<ArrayList<String>>>();
		for(WeightedElmt<ArrayList<String>> prodConj :thConj)
			for(WeightedElmt<ArrayList<String>> prod : _weightTH)
				if(isConsistent(prod.getElement(),prodConj.getElement())){
					
					ArrayList<String> nvprod = new ArrayList<String>();
					nvprod.addAll(prod.getElement());
					for(String lit : prodConj.getElement())
						nvprod.add(lit);
					float weight = prod.getWeight()+prodConj.getWeight();
					addwI2TH((int)weight,nvprod,updateTh);
				}
		_weightTH.clear();
		_weightTH.addAll(updateTh);
		return _weightTH.isEmpty();
	}
	

	
	public boolean isConsistent(ArrayList<String> prod,ArrayList<String> prodConj){
		for(String lit : prod)
			for(String lit2 :prodConj )
				if(getOpposed(lit).equals(lit2))
					return false;
		return true;
	}
	
	public boolean isTriviallyTrue(ArrayList<WeightedElmt<ArrayList<String>>> thConj){
		
		for(int i = 0; i<thConj.size()-1;i++)
			if(thConj.get(i).getElement().size()== 1)
				for(int j = i; j<thConj.size();j++)
					if(thConj.get(j).getElement().size()== 1)
						if(thConj.get(i).getElement().get(0).
								equals(getOpposed(thConj.get(j).getElement().get(0))))
							return true;
					
		return false;
	}
	
	/*
	 * return the implicants of the theory restrics on the Lit lits
	 */
	public ArrayList<WeightedElmt<ArrayList<String>>> restrictThOn(ArrayList<String> lits ){
		ArrayList<WeightedElmt<ArrayList<String>>> result = new ArrayList<WeightedElmt<ArrayList<String>>>();
		
		for(WeightedElmt<ArrayList<String>> id: _weightTH){
			ArrayList<String> f = new ArrayList<String>();
			
			for(String l : id.getElement())
				if(lits.contains(l))
					f.add(l);
			
			if (f.isEmpty()){
				result =initWeighTH();
				return result;
			}
			addwI2TH((int)id.getWeight(),f,result);
		}
		
		return result;
	}
	
	
	private void addwI2TH(int weight,ArrayList<String> impl, ArrayList<WeightedElmt<ArrayList<String>>> th ){
		boolean addsF = true;
		ArrayList<WeightedElmt<ArrayList<String>>>  toRem = new ArrayList<WeightedElmt<ArrayList<String>>>();
		for(WeightedElmt<ArrayList<String>> idR: th){
			if (idR.getElement().containsAll(impl)){
				if(weight< idR.getWeight()){
					toRem.add(idR);
				}
			}else{
				if(impl.containsAll(idR.getElement())){
					if(weight< idR.getWeight()){
						addsF = false;
						break;
					}
				}	
			}
			
		}
		if(addsF)
			th.add(new WeightedElmt<ArrayList<String>>(weight,impl));
		th.removeAll(toRem);
	}
	
	
	public String rImplicants2String(){
		String s= _mPName+ " :\n";
		for(WeightedElmt<ArrayList<String>> idImpl:_weightTH){
			s+= idImpl.getWeight()+" : "+idImpl.getElement().toString();
			s+="\n";
		}
		return s ;
	}
	
	private String getOpposed(String l){
		if(l.contains("!"))
			return l.replace("!", "");
		else
			return "!"+l;
		
	}
}
