package problems.mip;

import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import instances.BasicInstance;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import tools.TimeManager;

public class MaxJustInTimeAssignment{

	boolean CODE_ASSERT = false;
	String VERBOSE = "";

	public IloCplex cplex;
	BasicInstance inst;

	int maxNbVisitsPerDay = 5;

	/** EGC denotes the Earliness Global Cost*/
	int EGC =1;
	/** LGC denotes the Lateness Global Cost*/
	int LGC =2;
	/** LGC denotes the Unassignement Global Cost*/
	int UGC = 10;


	/** x_amdk.get(int a).get(int m).get(int d).get(int k) returns IloIntVar  denoting if activity a has been to the k^th visited location
	 * of the mechanic m during the workday.
	 * */
	public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IloIntVar>>>> x_amdk;

	/** x_a.get(int a) returns IloIntVar denoting if activity a has been assigned */
	public Int2ObjectOpenHashMap<IloIntVar> x_a;

	/** t_a.get(int a)  returns IloNumVar denoting the starting time in seconds, when activity a has been assigned,  */
	public  Int2ObjectOpenHashMap<IloNumVar> t_a;

	/** y_a.get(int a) returns IloIntVar denoting the working day activity a has been assigned */
	public Int2ObjectOpenHashMap<IloIntVar> y_a;

	/** e_a.get(int a)  returns IloNumVar denoting the earliness time in days the activity a has been assigned  */
	public  Int2ObjectOpenHashMap<IloIntVar> e_a;

	/** l_a.get(int a)  returns IloNumVar denoting the lateness time in days the activity a has been assigned  */
	public  Int2ObjectOpenHashMap<IloIntVar> l_a;


	/*
	 * Solution stricture
	 */
	Int2IntOpenHashMap a2m;
	Int2DoubleOpenHashMap a2t;

	Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>> d2m2as;


	public MaxJustInTimeAssignment(BasicInstance instance) throws IloException{

		System.out.println("\n\n 	MaxJustInTimeAssignment \n");

		cplex = new IloCplex();
		this.inst = instance;

		initVariablesAndConstants();
		initObjective();
		initConstraints();
	}

	public void initVariablesAndConstants() throws IloException {

		cplex = new IloCplex();
		cplex.setParam(IloCplex.IntParam.Threads, 1);

		init_xamdk();
		init_xa();
		init_ta();
		init_ya();
		init_ea();
		init_la();

		init_solStructures();

	}

	private void init_xamdk() throws IloException{
		x_amdk = new Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IloIntVar>>>>(inst.nbActivities);
		for(int a: inst.activ2label.keySet()){
			x_amdk.put(a, new Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IloIntVar>>>(inst.nbMechanics));
			for(int m: inst.mech2label.keySet()){
				if(! inst.mech2skills.get(m).containsAll( inst.activ2skills.get(a)))
					continue;
				if(! inst.mech2elevs.get(m).contains( inst.activ2elev.get(a)))
					continue;
				x_amdk.get(a).put(m, new Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IloIntVar>>(inst.maxNbWorkdays));
				for(int d: inst.mech2workdays.get(m)){
					x_amdk.get(a).get(m).put(d, new Int2ObjectOpenHashMap<IloIntVar>());
					for(int k=1;k<=maxNbVisitsPerDay;k++){
						IloIntVar var = cplex.boolVar("x_{"+a+","+m+","+d+","+k+"}");
						x_amdk.get(a).get(m).get(d).put(k, var );
					}
				}
			}
		}

		//		System.out.println("x_amdk:");
		//		for(int a: x_amdk.keySet()){
		//			for(int m: x_amdk.get(a).keySet()){
		//				for(int d: x_amdk.get(a).get(m).keySet()){
		//					for(int k: x_amdk.get(a).get(m).get(d).keySet()){
		//						System.out.println(" "+x_amdk.get(a).get(m).get(d).get(k));
		//					}
		//				}
		//			}
		//		}

	}

	private void init_xa() throws IloException{
		x_a = new Int2ObjectOpenHashMap<IloIntVar>(inst.nbActivities);
		for(int a: inst.activ2label.keySet()){
			IloIntVar var =  cplex.boolVar("x_{"+a+"}");
			x_a.put(a, var);
		}
	}

	private void init_ta() throws IloException{
		t_a = new Int2ObjectOpenHashMap<IloNumVar>(inst.nbActivities);
		for(int a: inst.activ2label.keySet()){
			int startTime = 8*60*60;// 28800 secs = 8 am
			int endTime = 17*60*60-inst.activ2dur.get(a); // 61200 secs =5 pm
			IloNumVar var =  cplex.numVar(startTime, endTime, IloNumVarType.Float, "t_{"+a+"}");
			t_a.put(a, var);
		}
	}

	private void init_ya() throws IloException{
		y_a = new Int2ObjectOpenHashMap<IloIntVar>(inst.nbActivities);
		for(int a: inst.activ2label.keySet()){
			IloIntVar var =  cplex.intVar(1, inst.maxNbWorkdays,"y_{"+a+"}");
			y_a.put(a, var);
		}
	}

	private void init_ea() throws IloException{
		e_a = new Int2ObjectOpenHashMap<IloIntVar>(inst.nbActivities);
		for(int a: inst.activ2label.keySet()){
			int lb = 0;
			int ub = 31;
			IloIntVar var =  cplex.intVar(lb, ub,  "e_{"+a+"}");
			e_a.put(a, var);
		}
	}

	private void init_la() throws IloException{
		l_a = new Int2ObjectOpenHashMap<IloIntVar>(inst.nbActivities);
		for(int a: inst.activ2label.keySet()){
			int lb = 0;
			int ub = 31;
			IloIntVar var =  cplex.intVar(lb, ub,  "l_{"+a+"}");
			l_a.put(a, var);
		}
	}

	public void init_solStructures(){
		a2m = new Int2IntOpenHashMap(inst.nbActivities);
		a2t = new Int2DoubleOpenHashMap(inst.nbActivities);

		d2m2as = new Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<IntArrayList>>(maxNbVisitsPerDay);
	}

	public void initObjective() throws IloException{
		IloNumExpr obj = cplex.constant(0);
		// TODO UNCOMMENT
		//		// min earliness
		//		for(int a : e_a.keySet()){
		//			obj =cplex.sum(obj,cplex.prod(EGC,e_a.get(a)));
		//		}
		//
		//		// min lateness
		//		for(int a : l_a.keySet()){
		//			obj =cplex.sum(obj,cplex.prod(LGC,l_a.get(a)));
		//		}

		// min unassigned activities
		for(int a : x_a.keySet()){
			obj =cplex.sum(obj,cplex.prod(UGC,cplex.diff(1,x_a.get(a))));
		}

		//		System.out.println("\n Minimise \n");
		//		System.out.println(obj);
		cplex.addMinimize(obj, "MaxJustInTimeAssignment");
	}

	public void initConstraints()throws IloException{
		initActivityConstraints();
		initWorkDayConstraints();
		initRoutingConstraints();

	}

	public void initActivityConstraints()throws IloException{

		// An activity is addressed only once and by at most one workday (2)
		System.out.println("\n xa= Σ x_awk, ∀a∈A w∈Wa ,k∈K\n");
		for(int a : x_amdk.keySet()){
			IloIntExpr sum = null;
			for(int m: x_amdk.get(a).keySet()){
				for(int d: x_amdk.get(a).get(m).keySet()){
					for(int k: x_amdk.get(a).get(m).get(d).keySet()){
						if(sum == null){
							sum = x_amdk.get(a).get(m).get(d).get(k);
						}else{
							sum = cplex.sum(sum,x_amdk.get(a).get(m).get(d).get(k));
						}
					}
				}
			}
			//			System.out.println(x_a.get(a)+" = "+sum);
			cplex.addEq(x_a.get(a), sum);
		}

		//		// The earliness is the difference between the due date and the assigned day of the activity
		//
		//		for(int a: e_a.keySet()){
		//			cplex.addGe(e_a.get(a),cplex.diff(inst.activ2due.get(a), y_a.get(a)));
		//		}
		//
		//		// The lateness is the difference between the assigned day of the activity and the due date
		//
		//		for(int a: l_a.keySet()){
		//			cplex.addGe(l_a.get(a),cplex.diff(inst.activ2due.get(a), y_a.get(a)));
		//		}


	}

	public void initWorkDayConstraints()throws IloException{

		// At the kth visited location of a workday, a technician addresses at most one activity
		System.out.println("\n Σ x_awk ≤1, ∀w∈W,∀k∈K \n");
		for(int m :inst.mech2label.keySet()){
			for(int d =1;  d<= inst.maxNbWorkdays; d++){
				for(int k =1; k<= maxNbVisitsPerDay; k++){
					IloIntExpr sum = null;
					for(int a: x_amdk.keySet()){
						if(x_amdk.get(a).containsKey(m)){
							if(x_amdk.get(a).get(m).containsKey(d)){
								if(x_amdk.get(a).get(m).get(d).containsKey(k)){
									if(sum == null){
										sum = x_amdk.get(a).get(m).get(d).get(k);
									}else{
										sum = cplex.sum(sum,x_amdk.get(a).get(m).get(d).get(k));
									}
								}
							}
						}
					}
					if(sum!=null){
						//						System.out.println(sum+" <= "+1);
						cplex.addRange(0,sum, 1);
					}
				}
			}
		}


		//		// The assigned day of an activity corresponds to the working day of the work assigned to the activity
		//		//  NOTE that the linearisation of these constraints have be considered with the next constraints and the objective
		//		// (x_amdk =1)⇒(y_a = d) reformulate as y_a >= d . x_amdk instead of y_a = d.x_amdk
		//		System.out.println("\n(x_amdk =1)⇒(y_a = d) reformulate as y_a >= d . x_amdk instead of y_a = d.x_amdk \n");
		//		for(int a: x_amdk.keySet()){
		//			for(int m: x_amdk.get(a).keySet()){
		//				for(int d: x_amdk.get(a).get(m).keySet()){
		//					for(int k: x_amdk.get(a).get(m).get(d).keySet()){
		//						System.out.println(y_a.get(a)+" == "+cplex.prod(d, x_amdk.get(a).get(m).get(d).get(k)));
		//						cplex.addGe(y_a.get(a), cplex.prod(d, x_amdk.get(a).get(m).get(d).get(k)));
		//					}
		//				}
		//			}
		//		}
		//
		//
		//		// When an activity has not been assigned to any of technicians’ workdays, the assigned day is set to the due date
		//		//  As previously the linearisation of these constraints depends on the previous constraints and the objective
		//		// (x_a =0)⇒(y_a =due_a), ∀a∈A becomes y_a >= due_a . (1-x_a)
		//		System.out.println("\n (x_a =0)⇒(y_a =due_a), ∀a∈A becomes y_a >= due_a . (1-x_a)\n");
		//		for (int a: x_a.keySet()){
		//			cplex.addGe(y_a.get(a),cplex.prod(inst.activ2due.get(a), cplex.diff(1, x_a.get(a))));
		//		}
	}

	public void initRoutingConstraints() throws IloException{

		// technician’s workday is organised as a sequence of assigned activities
		// (Σ a∈A xawk =1)⇒(Σ xa′wk−1 =1)
		System.out.println("\n (Σ a∈A xawk =1)⇒(Σ xa′wk−1 =1) \n");
		for(int m :inst.mech2label.keySet()){
			for(int d =1;  d<= inst.maxNbWorkdays; d++){
				for(int k =2; k<= maxNbVisitsPerDay; k++){
					IloIntExpr sum_x_a = null;
					IloIntExpr sum_x_aBis = null;
					for(int a: x_amdk.keySet()){
						if(x_amdk.get(a).containsKey(m)){
							if(x_amdk.get(a).get(m).containsKey(d)){
								if(sum_x_a == null){
									sum_x_a = x_amdk.get(a).get(m).get(d).get(k);
								}else{
									sum_x_a = cplex.sum(sum_x_a,x_amdk.get(a).get(m).get(d).get(k));
								}
								if(sum_x_aBis == null){
									sum_x_aBis = x_amdk.get(a).get(m).get(d).get(k-1);
								}else{
									sum_x_aBis = cplex.sum(sum_x_aBis,x_amdk.get(a).get(m).get(d).get(k-1));
								}
							}
						}
					}
					if(sum_x_a!=null && sum_x_aBis != null){
						//						System.out.println(sum_x_a+" <= "+sum_x_aBis);
						cplex.addLe(sum_x_a, sum_x_aBis);
					}
				}
			}
		}

		// Moreover, two consecutive activities addressed by in the same technician, the same day,
		// satisfy the routing constraints between the two respective locations
		// (x_amdk + x_a′mdk+1 = 2) ⇒ (t_a + dur_a + pt(loc_a, loc_a′ ) ≤ t_a′ )
		//  (((x_amdk + x_a′mdk+1)-2).M + ((x_amdk + x_a′mdk+1) - 1 ) . (dur_a + pt(loc_a, loc_a′) ) ≤ t_a′ - t_a
		System.out.println("\n (x_amdk + x_a′mdk+1 = 2) ⇒ (t_a + dur_a + pt(loc_a, loc_a′ ) ≤ t_a′ )\n ");
		int M  = 17*60*60+1 ; // 5 pm + 1 sec
		for(int a: x_amdk.keySet()){
			// TODO do something better here for the complexity  create a compatible set of a a'
			for(int aBis: x_amdk.keySet()){
				if( aBis ==a )
					continue;

				int time_a_to_aBis = inst.travelTime( a, aBis);
				//				System.out.println(" time "+lab_e+" to "+lab_eBis+":"+time_a_to_aBis + " dur a:"+inst.activ2dur.get(a));
				for(int m: x_amdk.get(a).keySet()){
					for(int d: x_amdk.get(a).get(m).keySet()){
						for(int k: x_amdk.get(a).get(m).get(d).keySet()){
							if( k == maxNbVisitsPerDay )
								continue;

							if(x_amdk.get(aBis).containsKey(m)){
								if(x_amdk.get(aBis).get(m).containsKey(d)){

									IloIntExpr sum_xa_xaBis =  cplex.sum(x_amdk.get(a).get(m).get(d).get(k), x_amdk.get(aBis).get(m).get(d).get(k+1));
									cplex.addLe(
											cplex.sum(
													cplex.prod(M, cplex.diff(sum_xa_xaBis,2))
													,cplex.prod(inst.activ2dur.get(a)+time_a_to_aBis ,cplex.diff( sum_xa_xaBis,1))
													)
											, cplex.diff(t_a.get(aBis), t_a.get(a) ));

									//									System.out.println(
									//											cplex.sum(
									//													cplex.prod(M, cplex.diff( sum_xa_xaBis,2))
									//													,cplex.prod(inst.activ2dur.get(a)+inst.router.time(lab_a,lab_aBis) ,cplex.diff( sum_xa_xaBis,1))
									//													)+" <= "+
									//													cplex.diff(t_a.get(a), t_a.get(aBis) ));
								}
							}
						}
					}
				}
			}
		}

	}

	public void solve() throws IloException{

		cplex.setOut(null);

		if(cplex.solve()){

			System.out.println("obj: "+ cplex.getObjValue()+ " best bound: "+ cplex.getBestObjValue());

			for(int m :inst.mech2label.keySet()){
				for(int d =1;  d<= inst.maxNbWorkdays; d++){
					for(int k =1; k<= maxNbVisitsPerDay; k++){
						for(int a: x_amdk.keySet()){
							if(x_amdk.get(a).containsKey(m)){
								if(x_amdk.get(a).get(m).containsKey(d)){
									if(x_amdk.get(a).get(m).get(d).containsKey(k)){

										int val_x_amdk = (int) Math.round(cplex.getValue(x_amdk.get(a).get(m).get(d).get(k)));
										if(val_x_amdk==0)
											continue;

										if (!d2m2as.containsKey(d))
											d2m2as.put(d, new Int2ObjectOpenHashMap<IntArrayList>(inst.nbMechanics));
										if (!d2m2as.get(d).containsKey(m))
											d2m2as.get(d).put(m, new IntArrayList(inst.nbActivities));

										a2m.put(a,m);
										d2m2as.get(d).get(m).add(a);

										double val_t_a = Math.round(cplex.getValue(t_a.get(a)));
										a2t.put(a, val_t_a);

									}
								}
							}
						}
					}
				}
			}

			// solution

			for(int d=1;d<= inst.maxNbWorkdays;d++){
				if (d2m2as.containsKey(d)){
					System.out.println("day"+d+":");
					for(int m: d2m2as.get(d).keySet()){
						System.out.println(" mechanic:"+m+" label:"+inst.mech2label.get(m));
						for (int ik=0; ik< d2m2as.get(d).get(m).size();ik++){
							int a= d2m2as.get(d).get(m).getInt(ik);
							String lab_e =  inst.elev2label.get(inst.activ2elev.get(a));
							String prevTravelTime="";
							if(ik>0){
								int prev_a = d2m2as.get(d).get(m).getInt(ik-1);
								String lab_prev_e =  inst.elev2label.get(inst.activ2elev.get(prev_a));
								if(!lab_e.equals(lab_prev_e)){
									prevTravelTime = "\t\ttravel_from:"+lab_prev_e
											+" to:"+lab_e
											+" duration:"+TimeManager.hourMinSec(inst.travelTime(prev_a, a))+" ";
									System.out.println(prevTravelTime);
								}
							}

							System.out.println(""
									+"\tfrom:"+TimeManager.hourMinSec((int)a2t.get(a))
									+" to:"+TimeManager.hourMinSec((int)a2t.get(a)+inst.activ2dur.get(a))
									+" activity:"+inst.activ2label.get(a)
									+" elvevator:"+lab_e

							);
						}
						System.out.println();
					}
				}
			}
		}
	}

	public void checkSolution() throws IloException{
		int nb_xa = 0;

		// Checking that each assigned activity has an assigned mechanic and start time
		for(int a: inst.activ2label.keySet()){
			int val_x_a = (int) Math.round(cplex.getValue(x_a.get(a)));
			if(val_x_a==0)
				continue;
			nb_xa ++;
			// check that all assigned activities have an assigned mechanic
			assert(a2m.containsKey(a));
			// check that all assigned activities have an assigned start time
			assert(a2t.containsKey(a));
		}
		System.out.println("CHECKED: each assigned activity has an assigned mechanic and start time");

		// Checking that each activity is assigned at most once
		int nb_xamdk =0;
		for(int a: x_amdk.keySet()){
			for(int m: x_amdk.get(a).keySet()){
				for(int d: x_amdk.get(a).get(m).keySet()){
					for(int k=1;k<= maxNbVisitsPerDay;k++)
						if (x_amdk.get(a).get(m).get(d).containsKey(k)){
							int val_x_amdk = (int) Math.round(cplex.getValue(x_amdk.get(a).get(m).get(d).get(k)));
							if(val_x_amdk==0)
								continue;

							// check all activities assigned to a specific slot of a workday of a mechanic are recorded
							assert(d2m2as.get(d).get(m).contains(a));
							assert(a2m.containsKey(a));
							assert(a2t.containsKey(a));

							nb_xamdk ++;
						}
				}
			}
		}
		assert (nb_xa == nb_xamdk) ;
		System.out.println("CHECKED: an activity is assigned at most once");


		// Checking: Continuity, i.e. no empty spot, of each mechanic's journey
		for(int m :inst.mech2label.keySet()){
			for(int d =1;  d<= inst.maxNbWorkdays; d++){

				boolean activInPrecSlot =false;
				boolean activInCurSlot =false;
				for(int k =1; k<= maxNbVisitsPerDay; k++){

					for(int a: x_amdk.keySet()){
						if(x_amdk.get(a).containsKey(m)){
							if(x_amdk.get(a).get(m).containsKey(d)){
								if(x_amdk.get(a).get(m).get(d).containsKey(k)){
									int val_x_amdk = (int) Math.round(cplex.getValue(x_amdk.get(a).get(m).get(d).get(k)));
									if(val_x_amdk==0)
										continue;
									activInCurSlot =true;
								}
							}
						}
					}
				}
				if(activInPrecSlot && !activInCurSlot){
					System.out.println("m"+m+"d"+d+" path with empty slot");
					// check no empty spot between assigned activities
					assert (activInPrecSlot && !activInCurSlot);
				}
				activInPrecSlot =activInCurSlot;
				activInCurSlot =false;

			}
		}
		System.out.println("CHECKED: Continuity of each mechanic's journey,  i.e. no empty slot");

		// Checking the time consistency of each assigned journey of mechanics
		for(int d=1;d<= inst.maxNbWorkdays;d++){
			if (d2m2as.containsKey(d)){
				for(int m: d2m2as.get(d).keySet()){
					for (int ik=0; ik< d2m2as.get(d).get(m).size();ik++){
						int a= d2m2as.get(d).get(m).getInt(ik);

						if(ik>0){
							int prev_a = d2m2as.get(d).get(m).getInt(ik-1);
							double start_time_prev_a = a2t.get(prev_a) ;
							int dur_prev_a = inst.activ2dur.get(prev_a);
							double travel_time  = inst.travelTime(prev_a, a);
							double start_time_a = a2t.get(a) ;

							assert((start_time_prev_a+dur_prev_a+travel_time)<=(start_time_a+1));

						}
					}
				}
			}
		}
		System.out.println("CHECKED: Time consistency of each mechanic's journey ");

	}


	public static void main(String[] args) throws IloException {
		String fNameIn = "./src/main/resources/data/efsi-2.txt";
		BasicInstance efsi = new BasicInstance(fNameIn);
		MaxJustInTimeAssignment mip = new MaxJustInTimeAssignment(efsi);
		mip.solve();
		mip.checkSolution();
	}


}
