package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class FileTools {

	public static void recursiveDelete(File f) {
		if (f.isDirectory()) {
			for (File fChild : f.listFiles()) {
				recursiveDelete(fChild);
			}
			f.delete();
		} else {
			f.delete();
		}
	}
	
	public static boolean dirContainsFilesWithExt(File dir, String ext){
		if (dir.isDirectory())
			for(String  fName : dir.list())
				if(fName.contains(ext))
					return true;
		return false;
	}
	
	public static File writeContentInFile(String content, String fName){
		
		File f = new File(fName);
		Writer output;
		try {
			output = new BufferedWriter(new FileWriter(f));
			output.write(content);
			output.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return f;
	}
	
}
