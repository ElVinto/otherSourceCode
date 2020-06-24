package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class Dprint {
	
	static Writer _scenarioWriter =null ;
	static FileWriter _scenarioFileWriter =null;
	
	Writer _statWriter =null;
	FileWriter _statFileWriter = null;
		
	//*********************************************************************
	// A static Dprint object synchronizes the console printing
	//*********************************************************************
	public static synchronized void  println(Object o){
		System.out.println(o);
	}
	
	public static synchronized void print(Object o){
		System.out.print(o);
	}
	
	
	//*********************************************************************
	// The static members of _scenarioWriter synchronize the writing in the scenario files
	//*********************************************************************
	public static synchronized void initScenarioWriter (String scenarioFileName)
		throws IOException{
		if(!(_scenarioWriter !=null && _scenarioFileWriter != null)){
			_scenarioFileWriter = new  FileWriter(scenarioFileName,true);
			_scenarioWriter = new BufferedWriter(_scenarioFileWriter);
		}else
			Dprint.println("Scenarion writer has ever been instanciated");
	}
	
	public static synchronized void writeScenario(String s){
		try{
			if(_scenarioWriter!=null){
			_scenarioWriter.write(s);
			}
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
	
	public static void closeScenarioWriter(){
		try{
			if(_scenarioWriter!=null){
				_scenarioWriter.close() ;
				_scenarioFileWriter.close();
				_scenarioWriter = null ;
				_scenarioFileWriter= null;
			}
			}catch(IOException e){
				e.printStackTrace();
			}
	}
	
	
	//*********************************************************************
	// An instance of Dprint object is used for stat writing 
	//*********************************************************************
	public Dprint( String statFileName)throws IOException {
		_statFileWriter =new FileWriter(statFileName,true);
		_statWriter = new BufferedWriter(_statFileWriter);
		
	}
	
	
	public void writeStat(String s){
	try{	
			_statWriter.write(s);
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
	
	
	public void closeStatWriter(){
		try{
			if(_statWriter!=null){
				_statWriter.close();
				_statFileWriter.close();
				_statWriter= null;
				_statFileWriter= null;
			}
			}catch(IOException e){
				e.printStackTrace();
			}
	}
	
	
	
	
	
	

}
