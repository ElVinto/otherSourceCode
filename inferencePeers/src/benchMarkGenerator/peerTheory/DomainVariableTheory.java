package benchMarkGenerator.peerTheory;

import java.util.ArrayList;
import java.util.HashMap;


import main.ArgsHandler;

import propositionalLogic.Base;

/*
 * A domain variable theory initially represents
 * by a propositional formula the set of values
 * of the domain of a variable. 
 * Then, clauses are added to encode constrains
 * between variables
 */
public class DomainVariableTheory extends PeerTheory{

	ArrayList<String> _clOfValues = new ArrayList<String>();
	HashMap<String, Object> _attSimple = new HashMap<String, Object>();
	
	public DomainVariableTheory(String name, HashMap<String, Object> params) throws Exception {
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
		
		if(!(Boolean)_attSimple.get("defaultBehavior"))
			// add a clause of  targeted negative literals
			for(String  lit :_prodConsistent )
				_clOfValues.add(lit);
		// add a clause of positive literals
		for(String  lit :_sharedLit )
			if(!lit.contains("!"))
				_clOfValues.add(lit);
		_th.add(_clOfValues);
		_attSimple.put("clOfValues", _clOfValues);
		
		if((Boolean)_attSimple.get("exclusiveDomVar")){
			for(int iL1=0;iL1<_clOfValues.size()-1;iL1++ ){
				for(int iL2=iL1+1;iL2<_clOfValues.size();iL2++ ){
					ArrayList<String> clTmp = new ArrayList<String>();
					clTmp.add(Base.oppLit(_clOfValues.get(iL1)));
					clTmp.add(Base.oppLit(_clOfValues.get(iL2)));
					_th.add(clTmp);
				}
			}
		}
	}

	public Object getParam(String paramName){
		return _attSimple.get(paramName);
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public void addALinkWith(PeerTheory desc) {
		ArrayList<String> neighClOfValues =(ArrayList<String>)desc.getParam("clOfValues");
		
// 		add the initial values of the neighbor
		_th.add(neighClOfValues);
		if((Boolean)_attSimple.get("exclusiveDomVar")){
		for(int iL1=0;iL1<neighClOfValues.size()-1;iL1++ ){
			for(int iL2=iL1+1;iL2<neighClOfValues.size();iL2++ ){
				ArrayList<String> clTmp = new ArrayList<String>();
				clTmp.add(Base.oppLit(neighClOfValues.get(iL1)));
				clTmp.add(Base.oppLit(neighClOfValues.get(iL2)));
				_th.add(clTmp);
			}
		}
		}
		
		// add the constraint between this peer and the neighbor
		int iLitProd =0;
		for(String l1 :_clOfValues){
			for(String l2 :neighClOfValues){
				if(	Math.random()<(Double)_attSimple.get("tightness")){
					ArrayList<String> clTmp = new ArrayList<String>();
					if((Boolean)_attSimple.get("defaultBehavior")){
						clTmp.add(_prodConsistent.get(iLitProd%_prodConsistent.size()));
						iLitProd++;
					}
					clTmp.add(Base.oppLit(l1));
					clTmp.add(Base.oppLit(l2));
					_th.add(clTmp);
					desc.get_th().add(clTmp);
					
				}
			}
		}
		
		
	}


}
