package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class Copyfic {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{		
		File src = new File("/users/iasi/varmant/experiments/baLight_100");
		File dest = new File(src.getPath()+"_restrict4B");
		recCopy(src,dest);
	}
	
	
	public static void recCopy(File src,File dirRes) throws Exception{
		for(File dirSrc: src.listFiles()){
			if(dirSrc.isDirectory()){
				File nvDirRes = new File(dirRes.getPath()+File.separator+dirSrc.getName());
				if(dirSrc.getName().contains("nbClsBynbVars"))
					if( !(dirSrc.getName().contains("nbClsBynbVars:3.1")||
							dirSrc.getName().contains("nbClsBynbVars:3.3")||
							dirSrc.getName().contains("nbClsBynbVars:3.5")||
							dirSrc.getName().contains("nbClsBynbVars:3.7")))
						continue;
				recCopy(dirSrc,nvDirRes);
			}else{
				
				if(!dirRes.exists())
					dirRes.mkdirs();

				BufferedReader br = new BufferedReader(new FileReader(dirSrc));
				BufferedWriter bw = new BufferedWriter( new FileWriter(dirRes.getPath()+File.separator+dirSrc.getName()));

				String line = br.readLine();
				while(line!=null){
					bw.write(line+"\n");
					line = br.readLine();
				}
				br.close();
				bw.close();
			}
		}
	}

}
