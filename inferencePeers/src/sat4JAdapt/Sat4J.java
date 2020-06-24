package sat4JAdapt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.InstanceReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.tools.ModelIterator;

import propositionalLogic.Base;

import benchMarkGenerator.peerTheory.PeerTheory;
import benchMarkGenerator.peerTheory.RandomPeerTheory;

//import org.sat4j.pb.IPBSolver;
//import org.sat4j.pb.SolverFactory;
//import org.sat4j.pb.reader.PBInstanceReader;
//import org.sat4j.reader.ParseFormatException;
//import org.sat4j.specs.ContradictionException;
//import org.sat4j.specs.IProblem;



public class Sat4J {
	
	/*
	 * return the set of cnf files contained in dir
	 */
	private static ArrayList<File> extractCNFFrom(File dir){
		ArrayList<File> res= new ArrayList<File>();
		if(dir.isDirectory()){
			for(File f: dir.listFiles()){
				if(f.isFile()){
					if(f.getPath().contains(".fnc")&& !f.getPath().contains("svn"))
						res.add(f);
				}else{
					res.addAll(extractCNFFrom(f));
				}
			}
		}
		return res ; 
	}
	
	/*
	 * extracts the clauses from a line of cnf file
	 */
	private static String[] extractClauseFrom(String line){
		if(line.contains("Production"))
			return null;
		if(!line.contains("Mapping"))
			return line.split(" ");
		else
			return new String[0];
	}
	
	/*
	 * This method recursively parse a directory of .cnf
	 * and transform it in a Dimacs file
	 */
	public static String dir2Dimacs(File dir, ArrayList<String> litsString){
		
		ArrayList<File> ficsCNF = extractCNFFrom(dir);
		if(ficsCNF.isEmpty())
			return null;
		
//		for(File f :ficsCNF)
//			System.out.println(f.getAbsolutePath());
		
		
		
		int nbClauses =0;
		
		File dimacs = new File("th.dimacs"); 
		if(dimacs.exists())
			dimacs.delete();
		try {
			dimacs.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
		String s="";
		for(File fic: ficsCNF){
			try {
				BufferedReader input = new BufferedReader(new FileReader(fic));
				String line = input.readLine(); // we escape the 1rst line
//				System.out.println(line);
				line = input.readLine();
//				System.out.println(line);
				boolean itsAclause = true;
				while(line !=null){
					String [] lits = extractClauseFrom(line);
					if(lits== null){
						String line2=  input.readLine();
						if(line2==null)
							break;
						else{
							lits = extractClauseFrom(line2);
							itsAclause=false;
							}
					}
					if(lits.length!=0){
						nbClauses++;
						for(String lit : lits){
							if(!litsString.contains(lit)){
								if(lit.contains("!")){
									litsString.add(lit);
									litsString.add(lit.replace("!", ""));
								}else{
									litsString.add("!"+lit);
									litsString.add(lit);
								}
							}
							int iLit = ((litsString.indexOf(lit))/2)+1;
							iLit = lit.contains("!")?-iLit:iLit;
							if(itsAclause)
								s=s+iLit+" " ;
						}
						if(itsAclause)
							s=s+"0\n";
					}
					line = input.readLine();
				}
				input.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		s= "\np cnf "+(litsString.size()/2)+" "+nbClauses+" \n"+s;
		for (String litS :litsString){
			s=" "+litS+s;
		}
		s="\nc"+s;
		for (String litS :litsString){
			int iLit = ((litsString.indexOf(litS))/2)+1;
			iLit = litS.contains("!")?-iLit:iLit;
			s="  "+iLit+"  "+s;
		}
		s="c "+s;
		Writer output;
		try {
			output = new BufferedWriter(new FileWriter(dimacs));
			output.write(s);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return dimacs.getPath();
	}
	
	
	public static void graph2dimacs(PeerTheory [] g, String emptyDimacs){
		
		ArrayList<String> litsString = new ArrayList<String>();
		int nbClauses =0;
		
		
		String s="";
		for(PeerTheory rpt: g){
			ArrayList<ArrayList<String>>  tmpTh = new ArrayList<ArrayList<String>>();
			tmpTh.addAll(rpt.get_th());
			tmpTh.addAll(rpt.get_thMapping());
			
			for(ArrayList<String>  lits: tmpTh){
				if(lits.size()!=0){
					nbClauses++;
					for(String lit : lits){
						if(!litsString.contains(lit)){
							if(lit.contains("!")){
								litsString.add(lit);
								litsString.add(lit.replace("!", ""));
							}else{
								litsString.add("!"+lit);
								litsString.add(lit);
							}
						}
						int iLit = ((litsString.indexOf(lit))/2)+1;
						iLit = lit.contains("!")?-iLit:iLit;
						s=s+iLit+" " ;
					}
					s=s+"0\n";
				}
			}

		}
		s= "\np cnf "+(litsString.size()/2)+" "+nbClauses+" \n"+s;
		for (String litS :litsString){
			s=" "+litS+s;
		}
		s="\nc"+s;
		for (String litS :litsString){
			int iLit = ((litsString.indexOf(litS))/2)+1;
			iLit = litS.contains("!")?-iLit:iLit;
			s="  "+iLit+"  "+s;
		}
		s="c "+s;
		Writer output;
		try {
			
			output = new BufferedWriter(new FileWriter(emptyDimacs, false));
			output.write(s);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static void graph2OneDesc(RandomPeerTheory [] g,
			ArrayList<String> litsString ,
			ArrayList<String> target ,
			ArrayList<ArrayList<String>>  allTh){
	
		for(RandomPeerTheory rpt: g){
			ArrayList<ArrayList<String>>  tmpTh = new ArrayList<ArrayList<String>>();
			tmpTh.addAll(rpt.get_th());
			tmpTh.addAll(rpt.get_thMapping());
			
			for(ArrayList<String>  lits: tmpTh){
				if(lits.size()!=0){
					for(String lit : lits){
						if(!litsString.contains(lit)){
							if(lit.contains("!")){
								litsString.add(lit);
								litsString.add(lit.replace("!", ""));
							}else{
								litsString.add("!"+lit);
								litsString.add(lit);
							}
						}
						int iLit = ((litsString.indexOf(lit))/2)+1;
						iLit = lit.contains("!")?-iLit:iLit;
					}
				}
			}
			allTh.addAll(tmpTh);
			target.addAll(rpt.get_prod());
		}
	
	}
	
	public static void addLitAndOppIfNeeded(ArrayList<String> litsString,String lit){
		if(!litsString.contains(lit)){
			if(lit.contains("!")){
				litsString.add(lit);
				litsString.add(lit.replace("!", ""));
			}else{
				litsString.add("!"+lit);
				litsString.add(lit);
			}
		}
	}
	
	
	public static void dir2OneDesc(File dir,
			ArrayList<String> litsString ,
			ArrayList<String> target ,
			ArrayList<ArrayList<String>>  allTh){
		
		ArrayList<File> ficsCNF = extractCNFFrom(dir);
		if(ficsCNF.isEmpty()){
			System.out.println(" Empty directory ");
			return ;
		}
		
		for(File fic: ficsCNF){
			try {
				BufferedReader input = new BufferedReader(new FileReader(fic));
				String line = input.readLine(); // we escape the 1rst line
//				System.out.println(line);
				line = input.readLine();
//				System.out.println(line);
				while(line !=null){
					String [] lits = extractClauseFrom(line);
					if(lits== null){
						line = input.readLine();
						 lits = extractClauseFrom(line);
						 for(String lit : lits){
							 target.add(lit);
							 addLitAndOppIfNeeded(litsString,lit);
						 }
						 
						break;
					}
					if(lits.length!=0){
						ArrayList<String> cl= new ArrayList<String>();
						for(String lit : lits){
							cl.add(lit);
							if(!litsString.contains(lit)){
								addLitAndOppIfNeeded(litsString,lit);
							}
						}
						allTh.add(cl);
					}
					line = input.readLine();
				}
				
				input.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	/*
	 * return 1 if isSat
	 * return 0 if unsat
	 * return -1 if timeout
	 * return -2 Others exception
	 */
	public static int isSat(String ficThName, int millisec){
		// Reader reader = new DimacsReader(solver);
        // CNF filename is given on the command line 
       // try {
		ISolver solver = SolverFactory.newMiniSAT();// SolverFactory.newDefault();
        ModelIterator mi = new ModelIterator(solver);
        int timeOutSec = (int) ((double)millisec/(double)1000);
        timeOutSec= timeOutSec<1?1: timeOutSec;
        solver.setTimeout(timeOutSec);
        Reader reader = new InstanceReader(mi);
        boolean unsat = true;
        try{
        	IProblem problem = reader.parseInstance(ficThName);
        	unsat = !problem.isSatisfiable();
//            while (problem.isSatisfiable()) {
//            	unsat = false;
//                System.out.println(reader.decode(problem.model()));
//            } 
//            if(unsat) {
//                System.out.println("Unsatisfiable !");
//            }
        } catch (FileNotFoundException e) {
        	 System.out.println("FileNotFoundException");
        	 return -2;
        } catch (ParseFormatException e) {
        	System.out.println("ParseFormatException");
        	return -2;
        } catch (IOException e) {
        	System.out.println("IOException");
        	return -2;
        } catch (ContradictionException e) {
            System.out.println("Unsatisfiable (trivial)!");
            return -2;
        } catch (Exception e) {
            System.out.println("Timeout, sorry!");
            return -1;
        } 

        return  unsat? 0:1 ;
       
	}
	
	 
	public static boolean isSat(String ficThName) {
			// Reader reader = new DimacsReader(solver);
	        // CNF filename is given on the command line 
	       // try {
			ISolver solver = SolverFactory.newMiniSAT();
	        ModelIterator mi = new ModelIterator(solver);
	        solver.setTimeout(3600); // 1 hour timeout
	        Reader reader = new InstanceReader(mi);
	        boolean unsat = true;
	        try{
	        	IProblem problem = reader.parseInstance(ficThName);
	        	unsat = !problem.isSatisfiable();
//	            while (problem.isSatisfiable()) {
//	            	unsat = false;
//	                System.out.println(reader.decode(problem.model()));
//	            } 
//	            if(unsat) {
//	                System.out.println("Unsatisfiable !");
//	            }
	        } catch (FileNotFoundException e) {
	        	 System.out.println("FileNotFoundException");
	        } catch (ParseFormatException e) {
	        	System.out.println("ParseFormatException");
	        } catch (IOException e) {
	        	System.out.println("IOException");
	        } catch (ContradictionException e) {
	            System.out.println("Unsatisfiable (trivial)!");
	        } catch (Exception e) {
	            System.out.println("Timeout, sorry!");      
	        } 
	        
	        return !unsat;
	       
	    }
	
	public static int[][] restrictModelsOn(String ficThName, int[] target){
		ISolver solver = SolverFactory.newMiniSAT();
        ModelIterator mi = new ModelIterator(solver);
        solver.setTimeout(3600); // 1 hour timeout
        Reader reader = new InstanceReader(mi);
        boolean unsat = true;
        
        int [][][] tabRes = new int [1][0][];

        
        try{
        	IProblem problem = reader.parseInstance(ficThName);
        	
            while (problem.isSatisfiable()) {
            	unsat = false;
            	int [] model = problem.model();
//            	Base.printSet(model);
            	Base.addIfnotSubSubme(tabRes, 0, Base.restrictOn(model, target));    
            } 
            if(unsat) {
                System.out.println("Unsatisfiable !");
            }
        } catch (FileNotFoundException e) {
        	 System.out.println("FileNotFoundException");
        } catch (ParseFormatException e) {
        	System.out.println("ParseFormatException");
        } catch (IOException e) {
        	System.out.println("IOException");
        } catch (ContradictionException e) {
            System.out.println("Unsatisfiable (trivial)!");
        } catch (Exception e) {
            System.out.println("Timeout, sorry!");      
        } 
        return 	tabRes[0];
	}

	
	public static void main(String[] args) {
		File dir = new File("./dataGenerate");
		ArrayList<String> litsString = new ArrayList<String>();
		isSat(dir2Dimacs(dir,litsString));
		
	}

}
