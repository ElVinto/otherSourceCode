package benchMarkGenerator.peerTheory;

import java.util.ArrayList;
import java.util.HashMap;

import main.ArgsHandler;
import propositionalLogic.Base;

public class BinaryConstraintTheory extends PeerTheory{
	
	
	
	ArrayList<String> _clOfValues1 = new ArrayList<String>();
	ArrayList<String> _clOfValues2 = new ArrayList<String>();
	HashMap<String, Object> _attSimple = new HashMap<String, Object>();
	
	public BinaryConstraintTheory(String name, HashMap<String, Object> params) throws Exception {
		super(name, params);
		initDefautParameters(params); 
		initTh();
	}
	
	protected void initDefautParameters(HashMap<String, Object> params) throws Exception {
		ArgsHandler.setDefaultParamTo("tightness", 0.4, params, _attSimple);
		ArgsHandler.setDefaultParamTo("exclusiveDomVar", false, params, _attSimple);
		ArgsHandler.setDefaultParamTo("defaultBehavior", false, params, _attSimple);
	}
	
	private void initTh() {
		
		if(!(Boolean)_attSimple.get("defaultBehavior")){
			// we split positive literals in two sets
			for(int iLit = 0; iLit<_allLits.size();iLit++){
				if(iLit <_allLits.size()/2){
					if(!_allLits.get(iLit).contains("!")){
						_clOfValues1.add(_allLits.get(iLit));
					}
				}else{
					if(!_allLits.get(iLit).contains("!")){
						_clOfValues2.add(_allLits.get(iLit));
					}
				}
			}
		}
		
		_attSimple.put("clOfValues1", _clOfValues1);
		_th.add(_clOfValues1);
		_attSimple.put("clOfValues2", _clOfValues2);
		_th.add(_clOfValues2);
		
		// add the behavior of the constraint
		int iLitProd =0;
		for(String l1 :_clOfValues1)
			for(String l2: _clOfValues2){
				if(	Math.random()<(Double)_attSimple.get("tightness")){
					ArrayList<String> clTmp = new ArrayList<String>();
					if((Boolean)_attSimple.get("defaultBehavior")){
						clTmp.add(_prodConsistent.get(iLitProd%_prodConsistent.size()));
						iLitProd++;
					}
					clTmp.add(Base.oppLit(l1));
					clTmp.add(Base.oppLit(l2));
					_th.add(clTmp);
				}
			}
		
		//if one one domain  satisfied a variable  the others should not
		
		if((Boolean)_attSimple.get("exclusiveDomVar")){
			for(int iL1=0;iL1<_clOfValues1.size()-1;iL1++ ){
				for(int iL2=iL1+1;iL2<_clOfValues1.size();iL2++ ){
					ArrayList<String> clTmp = new ArrayList<String>();
					clTmp.add(Base.oppLit(_clOfValues1.get(iL1)));
					clTmp.add(Base.oppLit(_clOfValues1.get(iL2)));
					_th.add(clTmp);
				}
			}
			for(int iL1=0;iL1<_clOfValues2.size()-1;iL1++ ){
				for(int iL2=iL1+1;iL2<_clOfValues2.size();iL2++ ){
					ArrayList<String> clTmp = new ArrayList<String>();
					clTmp.add(Base.oppLit(_clOfValues2.get(iL1)));
					clTmp.add(Base.oppLit(_clOfValues2.get(iL2)));
					_th.add(clTmp);
				}
			}
		}
		
		
	}

	public Object getParam(String paramName){
		return _attSimple.get(paramName);
	}
	

	
	@Override
	public void addALinkWith(PeerTheory desc) {
		
	}
	

	/**
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
