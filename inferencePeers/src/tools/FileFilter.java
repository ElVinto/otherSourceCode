package tools;

import java.io.File;
import java.io.FilenameFilter;

public class FileFilter implements FilenameFilter{
		
		String _extension = "";
		// element bidon non contenu en principe dans un nom
		String _element = "#*%^-";
		String _exclusion = ".svn";
		public FileFilter(String extension){
			_extension = extension;
		}
		
		public void setExtension(String extension){
			_extension = extension;
		}
		
		public void setElement(String element){
			_element = element;
		}
	
		public void setExclusion(String exclusion){
			_exclusion = exclusion;
		}
		
		public boolean accept(File dir, String name) {
			return ((name.endsWith(_extension) || name.contains(_element))&& !name.contains(_exclusion));
		}
		
}
