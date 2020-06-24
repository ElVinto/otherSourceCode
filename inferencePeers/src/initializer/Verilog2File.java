package initializer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;

import peers.PeerDescription;
import tools.Dprint;

public class Verilog2File {
	protected FileReader _fichier;

	protected StreamTokenizer _entree;

	ArrayList<File> _cnfFiles;
	

	public Verilog2File(File f) {
		initParser(f);
		parseVerilogFile(f);
		
	}

	private void initParser(File f) {
		//		 adjust paramaters for the parser
		try {
			_fichier = new FileReader(f);
			_entree = new StreamTokenizer(_fichier);
			_entree.eolIsSignificant(true);
			_entree.wordChars('0', '9');
			_entree.wordChars(':', ':');
			_entree.wordChars('!', '!');
			_entree.wordChars('_', '_');
			_entree.wordChars('&', '&');
			_entree.wordChars('-', '-');
			_entree.wordChars('/', '/');
			_entree.wordChars('(', '(');
			_entree.wordChars(')', ')');
			_entree.wordChars('.', '.');
			_entree.wordChars('/', '/');
			_entree.wordChars('*', '*');
		} catch (Exception ex) {
			Dprint.println(" exception initParser(File f) ");
			ex.printStackTrace();
		}

	}
	
	private void parseVerilogFile(File f){
		try {
			
			_entree.nextToken();
			while(_entree.ttype!=StreamTokenizer.TT_EOL){
			
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}


}
