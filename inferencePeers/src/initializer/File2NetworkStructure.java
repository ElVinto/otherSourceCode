package initializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StreamTokenizer;

import java.util.ArrayList;
import java.util.HashMap;

import tools.Dprint;

import communication.MyAddress;

public class File2NetworkStructure {

	protected static FileReader _fichier;

	protected static StreamTokenizer _entree;

	public synchronized static HashMap<String, MyAddress> parseNetworkFic(ArrayList<String> locServ,File f) {
		HashMap<String, MyAddress> _adrBook = new HashMap<String, MyAddress>();
		try {		
			initParser(f);
			String pName = "";
			int id = 0;
			int port = 0;
			String host = "";

			// 1rst line of fic.res contains the address of the local server
			// name

			
			host = locServ.get(0);
			port = (int) Integer.parseInt(locServ.get(1));

			MyAddress adLocalServ = new MyAddress(port, host);

//			Dprint.println("Parsing file res for "+adLocalServ);
			
			_adrBook.put("servLoc", adLocalServ);

			// We jump to the end of line
			int tokenType ;//=_entree.nextToken();			
//			while(tokenType!= StreamTokenizer.TT_EOL)
//				tokenType = _entree.nextToken();

			// The other line of contains address for the peers of the network
			do{

				 _entree.nextToken();
				 if(_entree.ttype == StreamTokenizer.TT_EOF)
					 break;

				pName = _entree.sval;
				
				host = getHostName(f);

				_entree.nextToken();
				port = (int) _entree.nval;

				_adrBook.put(pName, new MyAddress(port, host));

				id++;
				tokenType = _entree.nextToken();

			} while  (tokenType == StreamTokenizer.TT_EOL &&  tokenType != StreamTokenizer.TT_EOF) ;
			_fichier.close();

		} catch (Exception e) {
			Dprint.println("exception parseNetworkFic(File f) ");
			e.printStackTrace();
		}
		return _adrBook;
	}

	private static void initParser(File f) {
		// adjust parameters for the parser
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
		} catch (Exception ex) {
			Dprint.println(" exception initParser(File f) ");
			ex.printStackTrace();
		}

	}
	
	public static ArrayList<String> getLocalServerSettingFrom(File servdir) throws Exception{
		// oneOptArgs (0->servName, 1->port, 2->login, 3->pubKey)
		ArrayList<String> servSettingline = new ArrayList<String>();
		if(servdir.exists()){
			for(File f :servdir.listFiles() )
			if(f.getName().contains(".res")&& !f.getName().contains(".svn")){
				BufferedReader r = new BufferedReader(new FileReader(f));
				String firstLine =r.readLine();
				for(String s :firstLine.split("\\s+")){
					servSettingline.add(s);
				}
				// removing the number of peers in the local network
				servSettingline.remove(servSettingline.size()-1);
				r.close();
			} 
		}
		if(servSettingline.isEmpty() && servSettingline.size()!=4 && servSettingline.size()!=2){
			throw new Exception(servdir.getName()+" does not contian a valid fic.res");
		}
		return servSettingline;
	}
	
	

	@SuppressWarnings("static-access")
	private static String getHostName(File f) throws Exception {
		_entree.nextToken();
		String host = "";
		if (_entree.ttype == _entree.TT_NUMBER) {
			host = Double.toString(_entree.nval);
			_entree.nextToken();
			host += Double.toString(_entree.nval).substring(1);
			_entree.nextToken();
			host += Double.toString(_entree.nval).substring(1);
		} else if (_entree.ttype == _entree.TT_WORD) {
			host = _entree.sval;
		}
		return host;
	}

}
