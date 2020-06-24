package initializer;

import java.io.File;
import java.io.FileReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import communication.MyAddress;

import peers.PeerDescription;
import propositionalLogic.Base;
import tools.Dprint;
import tools.Voc2SeenPeers;

public class Files2PeerDescriptions {

	protected FileReader _fichier;

	protected StreamTokenizer _entree;

	ArrayList<PeerDescription> _peerDescriptions;

	public Files2PeerDescriptions(File dir,HashMap<String, MyAddress> adrBook) {
		_peerDescriptions = new ArrayList<PeerDescription>();
		if (!dir.isDirectory()) {
			Dprint.println(" you have to indicate the network repository ");
			return;
		}
		parseDirectory(dir,adrBook);

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
		} catch (Exception ex) {
			Dprint.println(" exception initParser(File f) ");
			ex.printStackTrace();
		}

	}

	/*
	 * create a pool of peers and communications medium for the pool
	 */
	private void parseDirectory(File dir,HashMap<String, MyAddress> adrBook) {
		try {
			for (File f : dir.listFiles()) {
				if (f.getName().contains(".fnc")&& isLocal(f.getName().replace(".fnc", ""),adrBook)) {
					initParser(f);
					parsePeerFile();
					_fichier.close();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private boolean isLocal( String pName, HashMap<String, MyAddress> adrBook){
		return adrBook.get(pName).equals(adrBook.get("servLoc"));
	}
	
	private void addLitToVoc(Voc2SeenPeers voc2Peers,String lit){
		if(!voc2Peers.getVoc().contains(lit)){
			voc2Peers.addLit(lit, new TreeSet<String>());
			voc2Peers.addLit( Base.oppLit(lit),new TreeSet<String>());
			String peerName =  Base.extractPeerFromLit(lit);
			voc2Peers.addPeerTo(lit, peerName);
			voc2Peers.addPeerTo( Base.oppLit(lit),peerName);
			}
	}
	
	private void parsePeerFile() {
		try {
			// parse peer name
			_entree.nextToken();

			String pName = _entree.sval;

			// parse theory
			ArrayList<ArrayList<String>> th = new ArrayList<ArrayList<String>>();
			ArrayList<String> neighbors = new ArrayList<String>();
			Voc2SeenPeers voc2Peers = new Voc2SeenPeers();
			
			int tokenType = _entree.nextToken();
			while (tokenType == StreamTokenizer.TT_EOL) {
				ArrayList<String> f = new ArrayList<String>();
				TreeSet<String> linkPeer = new TreeSet<String>();
				while (_entree.nextToken() == StreamTokenizer.TT_WORD
						&& !_entree.sval.equals("Mappings")) {
					String lit = _entree.sval;
					f.add(lit);
					linkPeer.add( Base.extractPeerFromLit(lit));
					addNeighborsIfNeeded(neighbors, pName, lit);
					// in p:lit, add p to lit and not lit invoc2Peers
					addLitToVoc(voc2Peers,lit);
				}
				if (!f.isEmpty()){
					th.add(f);
					// For each lit in a constraint add 
					// add to lit peer name in the constraint.
					for(String l: f){
						for(String p: linkPeer)
							voc2Peers.addPeerTo(l, p);
				}
				}
				tokenType = _entree.ttype;
			}
			
			// parse mappings and neighbors
			ArrayList<ArrayList<String>> thMappings = new ArrayList<ArrayList<String>>();
			tokenType = _entree.nextToken();
			while (tokenType == StreamTokenizer.TT_EOL) {
				ArrayList<String> f = new ArrayList<String>();
				TreeSet<String> linkPeer = new TreeSet<String>();
				while (_entree.nextToken() == StreamTokenizer.TT_WORD
						&& !_entree.sval.equals("Production")) {
					String lit = _entree.sval;
					f.add(lit);
					
					// We link literal that are part of same conjunction
					linkPeer.add( Base.extractPeerFromLit(lit));
					addNeighborsIfNeeded(neighbors, pName, lit);
					addLitToVoc(voc2Peers,lit);
				}
				if (!f.isEmpty()) {
					thMappings.add(f);
					for(String l: f){
						for(String p: linkPeer)
						voc2Peers.addPeerTo(l, p);
					}
					
				}
				tokenType = _entree.ttype;
			}

			// parse diaglit
			ArrayList<String> diagLit = new ArrayList<String>();
			if (_entree.nextToken() == StreamTokenizer.TT_EOL) {
				while (_entree.nextToken() == StreamTokenizer.TT_WORD) {
					String lit = _entree.sval;
					diagLit.add(lit);
					addLitToVoc(voc2Peers,lit);
					voc2Peers.addPeerTo(lit,"Diag:"+ Base.extractPeerFromLit(lit));
				}
				
			}

			_peerDescriptions.add(new PeerDescription(pName, th,
					thMappings, neighbors, voc2Peers , diagLit));

		} catch (Exception ex) {
			Dprint.println(" Exception parsePeerFiles ");
			ex.printStackTrace();
		}
	}

	private void addNeighborsIfNeeded(ArrayList<String> neighbors,
			String pName, String l) {
		String p2Name = Base.extractPeerFromLit(l);
		if (!neighbors.contains(p2Name) && !pName.equals(p2Name))
			neighbors.add(p2Name);
	}


	

	public ArrayList<PeerDescription> getDescriptions() {
		return _peerDescriptions;
	}

}
