package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;

public class FileManager {

	public static boolean checkFileName(String fName) {
		File f = new File(fName);
		return f.exists();
	}

	public static void createFolderIfNeeded(String fName){
		File f = new File(fName);
			if(!f.exists()){
				f.mkdirs();
			}
		}

	public static String getStringAt(int i,String args[]){
		if(args!=null)
			if(args.length>i)
				if(args[i]!=null)
					return args[i];

		System.err.println("invalide argument");
		return null;
	}

	public static String [] [] readCSVFFile(String fileName) throws IOException{
		ArrayList<String []> raws = new ArrayList<String[]>();
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line = reader.readLine();
		line = reader.readLine();
		while (line != null) {
			String [] raw = line.split(",");
			raws.add(raw);
		}
		reader.close();
		return raws.toArray(new String[raws.size()][]);
	}

	public static void writeInFile(String txt, String FileName, boolean append) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(FileName,append));
		writer.write(txt);
		writer.flush();
		writer.close();
		System.out.println("write "+FileName);
	}

	public static long getMemInBytes(){

		for(int i=40;i-->0;){
		System.gc();
//			try {
//				synchronized(r){
//					r.wait(1000); // 1 secs
//				}
//			} catch (InterruptedException e) {
//			}
		}
		Runtime r = Runtime.getRuntime();
		long mem = r.totalMemory() - r.freeMemory();
		return mem;
	}

	public static long getUsedMemInBytes(){
		for(int i=40;i-->0;)
			System.gc();
		MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
		MemoryUsage heap = memBean.getHeapMemoryUsage();
		return heap.getUsed();
	}

	public static double byte2MegaByte( long nbBytes){
		return nbBytes/Math.pow(2,20);
	}

	public static String getMegaKiloBytes(long nbBytes){
		long mb= nbBytes/(long)Math.pow(2,20);
		long kb = (nbBytes -mb * (long)Math.pow(2,20))/(long)Math.pow(2,10) ;
		long b = nbBytes -mb * (long)Math.pow(2,20)- kb*(long)Math.pow(2,10)  ;

		return ""
		+((mb!=0)?""+mb+"mb":"")
		+((kb!=0)?""+kb+"kb":"")
		+((b!=0)?""+b+"b":"");
	}


}
