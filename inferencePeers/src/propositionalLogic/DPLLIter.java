package propositionalLogic;

/*
 * This implements an iterative dpll algorithm
 * a literal l is a number
 * if l is even then it is a positive literal
 * if l is odd then it is a negative literal
 */

import java.util.ArrayList;

import tools.Dprint;
import distributedAlgorithm.DistributedAlgorithm;

public class DPLLIter  {

	int NOVALUE = -1;
	/*
	 * the number of literals
	 */
	int _nbLITs;

	/*
	 * the number of clauses
	 */
	int _nbCLs;

	/*
	 * nbOccurenceLit[l] is the number of occurence l among the not satisfyed
	 * clauses
	 */
	int[] nbOccurenceLit;

	/*
	 * if assignedLit[l] == NOVALUE then l is not assigned if assignedLit[l] ==
	 * i , i>-1 then l is assigned at iteration i
	 */
	int[] assignedLits;
	int _nbAssignedLits;

	/*
	 * clauses is the theory, clauses [cl] is the clth clause (disjounctions of
	 * literals) clauses [cl][l] is the lth lit of the ith clause
	 */
	int[][] _clauses;

	/*
	 * if satisfiedClauses [cl] == NOVALUE then cl is notsatisfisfied if
	 * satisfiedClauses [cl] == l then cl is satisfisfied by the lit l
	 */
	int[] satisfiedClauses;
	
	/*
	 * _i represents the current value for the iteration
	 */
	int _i ;

	/*
	 * if choiceAt[i] = NOVALUE then at i no literal is choosen if choiceAt[i] =
	 * l then l was chosen at i
	 */
	int[] chosenAt;

	/*
	 * if backAtChoice [i] = 0 then at i no lit is chosen if backAtChoice [i] =
	 * 1 then at i a literal is chosen if backATChoice [i] = 2 then at i all
	 * possibilities for the current assignment were explored
	 */
	int[] backAtChoice;

	/*
	 * for the literal l this structure stores the corresponding clauses
	 * literalInCls[l]
	 */
	int[][] literalInCls;

	/*
	 * gives the number of unresolved  clauses.
	 */
	int[] currentSizeOfCls;

	/*
	 * current assignment ;
	 */
	int[] L;

	/*
	 * _dA incrementally receives found implicants   
	 */
	DistributedAlgorithm _dA= null;

	/*
	 * implicants  incrementally receives the found implicants
	 * when no _dA is specified
	 */
	ArrayList<int[]> _implicants = null;

	
	int[] _target =null;
	
	int[] _litOrder = null;
	
	boolean checktime=false;
	long _timeOut ;
	private boolean _dpllEnd = false;
	private long _dpllStart = 0;
	private long _dpllTime =0;
	public boolean _forceEnd = false;
	private boolean _foundOne = false;
	private boolean _byNext = false;

	/*
	 * 
	 */
	public DPLLIter(int[][] th,int nBLits, DistributedAlgorithm dA) {
		_i=0;
		_clauses= th;
		_dA = dA;
		_implicants = new ArrayList<int[]>();

		//		Dprint.println("Theory : ");
		//		Base.printBase(th);

		initializeLiteralStructures(nBLits);
		initializeClauseStructures();

	}
	
	public void setTarget(int [] target){
		_target = target;
	}
	
	public void setByNext(boolean byNext){
		_byNext = byNext;
	}
	
	public boolean isByNext(){
		return _byNext;
	}

//	public synchronized boolean dpllIsEnd(){
////		Dprint.println("dpll end "+ _dpllEnd+" for "+ _dA.getName());
//		return _dpllEnd ;
//	}
	
	public void setLitOrder(int [] lits){
		if(lits ==null)
			return;
		if(lits.length >0)
			_litOrder =lits;
	}
	
	public long getDPLLTime(){
		if(_byNext)
			return _dpllTime;
		else
			return System.currentTimeMillis()-_dpllTime ;
	}
	
	public void run(){
//		Dprint.println("dpll starts for "+ _dA.getName());
		 _dpllTime= System.currentTimeMillis();
		implicants();
		_dA.notifyEndFor("dpll");
//		Dprint.println("dpll ends for "+ _dA.getName());
		clean();
	}
	
	public void nextImplicants(){
		implicants();
	}
	
	private void clean(){
		nbOccurenceLit=null;
		assignedLits = null;
		_clauses=null;
		satisfiedClauses=null;;
		chosenAt=null;;
		 backAtChoice=null;;
		 literalInCls=null;;		
		 currentSizeOfCls=null;;
		L=null;
		_dA= null;
		 _implicants = null;
		_target =null;		
	}

	void initializeLiteralStructures(int nbLITs) {
		_nbAssignedLits = 0;

		// initialize litNames
		_nbLITs = nbLITs;

		// building the other literal's structures
		nbOccurenceLit = new int[nbLITs];
		literalInCls = new int[nbLITs][];
		assignedLits = new int[nbLITs];
		chosenAt = new int[(nbLITs / 2) + 1];
		backAtChoice = new int[(nbLITs / 2) + 1];
		L = new int[(nbLITs / 2) + 1];

		// initializing structures
		for (int l = 0; l < nbLITs; l++) {
			nbOccurenceLit[l] = 0;
			assignedLits[l] = NOVALUE;
			// Now l is an iteration
			if (l <= nbLITs / 2) {
				chosenAt[l] = NOVALUE;
				backAtChoice[l] = 0;
				L[l] = NOVALUE;
			}
		}
	}

	void initializeClauseStructures() {
		_nbCLs = _clauses.length;
		currentSizeOfCls = new int[_nbCLs];
		satisfiedClauses = new int[_nbCLs];
		for (int cl = 0; cl < _clauses.length; cl++) {
			currentSizeOfCls[cl] = _clauses[cl].length;
			satisfiedClauses[cl] = NOVALUE;
			for (int i = 0; i < _clauses[cl].length; i++) {
				int l = _clauses[cl][i];
				nbOccurenceLit[l]++;
			}
		}

		// initialize literal in Clauses
		int[] curSizeLitInCls = new int[_nbLITs];
		for (int l = 0; l < _nbLITs; l++) {
			literalInCls[l] = new int[nbOccurenceLit[l]];
			curSizeLitInCls[l] = 0;
		}

		for (int cl = 0; cl < _clauses.length; cl++) {
			for (int indl = 0; indl < _clauses[cl].length; indl++) {
				int l = _clauses[cl][indl];
				literalInCls[l][curSizeLitInCls[l]] = cl;
				curSizeLitInCls[l]++;
			}
		}

	}



	int choseLit() {
		
		
		if(_litOrder!=null){
			for(int l: _litOrder)
				if ((assignedLits[oppositeOf(l)] == NOVALUE && assignedLits[l] == NOVALUE))
					return l;
			
		}
		int litMaxOcc = 0;
		int maxOcc = 0;
		int litMaxOccNontarget = -1;
		int maxOccNonTarget = -1;
		
		
		for (int l = 0; l < _nbLITs; l++) {
			//			 System.out.println(" nbOccurence " + l + " " +
			//			 nbOccurenceLit[l]);
			//			 System.out.println(l+ " is not Assigned "+ (assignedLits[l] ==
			//			 NOVALUE));
			
			if ((assignedLits[oppositeOf(l)] == NOVALUE && assignedLits[l] == NOVALUE)){
				if(nbOccurenceLit[l] > maxOcc) {
					maxOcc = nbOccurenceLit[l];
					litMaxOcc = l;
				}
				
				if(_target!=null){
					if(!Base.contains(_target, l)){
						if(nbOccurenceLit[l] > maxOccNonTarget) {
							maxOccNonTarget = nbOccurenceLit[l];
							litMaxOccNontarget = l;
						}
					}
				}
				
				
				
			}
		}
		
		
		if(maxOccNonTarget >0)
			return litMaxOccNontarget;
		else
			return litMaxOcc;
		
		
	}

	/*
	 * return the opposite of l
	 */
	public int oppositeOf(int l) {
		if (l % 2 == 0)
			return l + 1;
		else
			return l - 1;
	}

	/* Check if the desired can be done */
	boolean validAssignement(int l){
		if (assignedLits[l] != NOVALUE || assignedLits[oppositeOf(l)]!=NOVALUE)
			return false;
		return true;
	}	

	/*
	 * this method checks the satisfiability and updates the non-resolve
	 * literal for the remaining clauses
	 */
	boolean assignAndUpdateClauses(int i) {
		boolean stop = false;
		int nbToAssign = 1;
		int indToAssigne = 0;
		while (indToAssigne < nbToAssign && !stop ) {

			// prolongate the current assignment by l
			int l = L[indToAssigne];
			if( !validAssignement(l))
				return false ;
			//			System.out.println("ASSIGNMENT of " + l + " at iteration " + i);
			assignedLits[l] = i;
			_nbAssignedLits++;

			// satisfy the corresponding clauses
			for (int cl : literalInCls[l]) {
				//				System.out.println(l + " is in " + cl);
				if (satisfiedClauses[cl] == NOVALUE) {
					satisfiedClauses[cl] = l;
					//					System.out.println("satisfiedClauses " + cl);
				}
			}

			// update clauses
			for (int cl : literalInCls[oppositeOf(l)]) {
				//				System.out.println("reduce clause " + cl);
				currentSizeOfCls[cl]--;

				if (satisfiedClauses[cl] == NOVALUE) {
					// inconsistency detection
					if (currentSizeOfCls[cl] == 0) {
						// premature stop we wait for the update of dependent
						// clauses
						stop = true;
						//						System.out.println(cl + " is inconsistent");
					}

					// prepare Assignment and update for clauses of size 1
					if (currentSizeOfCls[cl] == 1) {
						// find the remaining literal in cl
						int remLit = NOVALUE;
						//						 System.out.println("size of "+cl+ "is one ");
						for (int litCl : _clauses[cl]) {
							if (assignedLits[litCl] == NOVALUE
									&& assignedLits[oppositeOf(litCl)] == NOVALUE)
								remLit = litCl;
						}
						if (remLit != NOVALUE) {
							boolean waitForAssignement = false;
							for (int indL = 0; indL < nbToAssign; indL++)
								if (L[indL] == remLit)
									waitForAssignement = true;
							if (!waitForAssignement) {
								// System.out.println("Lit "+remLit+
								// "have to be assigned");
								// Add the remaining literal for assignment
								L[nbToAssign] = remLit;
								nbToAssign++;
							}
						}
					}
				}
			}
			indToAssigne++;
		}
		return !stop;
	}

	boolean allClausesAreSatisfied() {
		for (int i = 0; i < _nbCLs; i++) {
			if (satisfiedClauses[i] == NOVALUE) {
				return false;
			}
		}
		return true;
	}

	void deleteChoiceAt(int i) {

		//		System.out.println("UNASSIGNMENT of  literals at iteration "+i);

		backAtChoice[i] ++;

		for (int l = 0; l < _nbLITs; l++) {
			if (assignedLits[l] == i) {
				assignedLits[l] = NOVALUE;
				_nbAssignedLits--;

				// unsatisfaction of clauses satisfied by l and its dependent
				// literals
				for (int cl : literalInCls[l])
					if (satisfiedClauses[cl] == l) {
						satisfiedClauses[cl] = NOVALUE;
					}

				for (int cl : literalInCls[oppositeOf(l)])
					currentSizeOfCls[cl]++;
			}

		}
	}

	void implicantFoundProcessing() {


		int[] impl = new int[_nbAssignedLits];
		int i = 0;
		for (int l = 0; l < _nbLITs; l++) {
			if (assignedLits[l] != NOVALUE) {
				impl[i] = l;
				i++;
			}
		}
//		Dprint.print("\nFound at it "+_i);
//		afficheImplFound(impl);
		
		if(_target !=null)
			impl = Base.restrictOn(impl, _target);

//		Dprint.print("\nFound at it "+_i);
//		afficheImplFound(impl);
		if(Base_AL.addIfnotSubSubme(_implicants, impl)){
//			Dprint.print("\nFound at it "+_i);
//			afficheImplFound(impl);
//			Dprint.println("added");
			if(_dA!=null){
				_dA.middleResult(impl);
				//					Dprint.println(_dA.getName());
				//					afficheImplFound(impl);
			}
			if(impl.length==0){
				_forceEnd= true;
				
			}
			_foundOne = true;

		}
					

	}

	public void afficheImplFound(int [] impl){
		Dprint.print(" Implicant ");
		for(int l=0;l<impl.length;l++)
			Dprint.print(impl[l]+" ");
		Dprint.println("");
	}

	void afficheCurImpl() {
		System.out.print("IMPLIQUANT: ");
		for (int l = 0; l < _nbLITs; l++)
			if (assignedLits[l] != NOVALUE)
				System.out.print(l + " ");
		System.out.println();
	}

	/*
	 * itertive dpll
	 */
	public int[][] implicants() {
		
	
		if(_nbCLs <=0)
			return _implicants.toArray(new int[0][]);
		_dpllStart = System.currentTimeMillis();

		while (_i <= _nbLITs / 2) {
			//			System.out.println("ITERATION " + i);
			//			displayClausesSize();
			if (backAtChoice[_i] == 0 || backAtChoice[_i] == 1) {
				chosenAt[_i] = backAtChoice[_i] == 0 ? choseLit()
						: oppositeOf(chosenAt[_i]);
				L[0] = chosenAt[_i];
//							System.out.println("chosen lit  " + L[0] + " at iteration " + _i);
				if (assignAndUpdateClauses(_i)) {
					if (allClausesAreSatisfied()) {
						implicantFoundProcessing();
						deleteChoiceAt(_i);
						_i--;
					}
				} else {
					deleteChoiceAt(_i);
					_i--;
				}
				
				// Checking the remaining time
//				if(_dA!=null)
//					if(_dA.getLocalPeer().isTimeOut()){
//						_dA.terminate();
//						return null; 
//					}
				
			} else {
				if (_i > 0) {
					deleteChoiceAt(_i-1);
					chosenAt[_i] = NOVALUE;
					backAtChoice[_i] = 0;
					_i =_i-2;
				} else
					break;
			}
			_i++;
			
			
			if(checktime){
				if(_timeOut<System.currentTimeMillis())
					break;
			}
			
			if(_forceEnd )
				break;
			if(_foundOne  && _byNext ){
				_foundOne = false;
				_dpllTime += System.currentTimeMillis() -_dpllStart;
				
				return null;
			}
			
		}
		_dpllEnd = true;
		if(_dA!=null && (_byNext)){
			_dA.notifyEndFor("dpll");
//			Dprint.println("dpll ends for "+ _dA.getName());
			//clean();
		}

		//Dprint.println(_dA.getName()+ " dpll ends ");
//		if(_dA!=null)
//			_dA.ends(_dA.getName());
		return _implicants.toArray(new int[0][]) ;
	}
	
	public void setTimeOut( long end){
		checktime =true;
		_timeOut= end;
	}
	
	public int [][] minRimplicants(int [] targetLits){
		_target = targetLits;
		return implicants();
	}
	

	void displayClausesSize() {
		System.out.println();
		for (int cl = 0; cl < currentSizeOfCls.length; cl++)
			System.out.println("-Clause: " + cl + " size: "
					+ currentSizeOfCls[cl]);
		System.out.println();
	}

	

}
