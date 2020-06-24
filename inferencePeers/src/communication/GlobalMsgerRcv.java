package communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import peers.LocalPeer;
import tools.Dprint;

public class GlobalMsgerRcv extends LocalPeerThread {
	MyAddress _adServ;
	ServerSocket _server;
	ArrayList<Client> clients;
	HashMap<String,LocalPeer> _customers;
	HashMap<String, MsgBox> _customerRcvdBoxes ;
	int _backlog; /* queue length for incoming connection */
	boolean _terminate = false;

	public GlobalMsgerRcv(MyAddress adServ, int backlog) {
		super("GMsger " + adServ.toString());
		_adServ = adServ;
		_backlog = backlog;
		clients = new ArrayList<Client>();
		_customers = new HashMap<String,LocalPeer>();
		_customerRcvdBoxes =  new HashMap<String, MsgBox>();
	}

	public MyAddress getAddress() {
		return _adServ;
	}

	public  synchronized <MContent extends Serializable> void  terminate () {
		_terminate = true;
		try {
//			for(Client c: clients)
//				c._close=true;
			
			for(Client c: clients)
				c.close();
			_server.close();
			
//			Dprint.println("GlobalMsgerRcv "+ _adServ+" IS RUNNING ");
//			InetAddress inAddr = _adServ.host().equals("localhost") ? InetAddress
//					.getLocalHost()
//					: InetAddress.getByName(_adServ.host());
//			Socket s = new Socket(inAddr, _adServ.port());
//			ObjectOutputStream _writerExt = new  ObjectOutputStream(s.getOutputStream());
//			Msg<MContent> m = new Msg<MContent>("","",MTypeNetwork.Close,null);
//			_writerExt.writeObject(m);
			
		} catch (Exception e) {
			if(!_terminate){
				Dprint.println("GlobalMsgerRcv terminate "+ _adServ);
			 e.printStackTrace();
			}
		}
		
		
	}

	public synchronized boolean isTerminate() {
		return _terminate;
	}
	
	public HashMap<String,LocalPeer> getCustomers(){
		return _customers;
	}

	public HashMap<String, MsgBox> getCustomersRcvdBox(){
		return _customerRcvdBoxes;
	}
	
	
	public void removeCustomer(String pName){
		_customerRcvdBoxes.remove(pName);
		_customers.remove(pName);
		
	}
	public void run() {
		try {
			
			InetAddress inAddr =  InetAddress.getByName(_adServ.host());
			_server = new ServerSocket(_adServ.port(), _backlog, inAddr);						
			
			while (!isTerminate()) {
//				Dprint.println("GlobalMsgerRcv "+ _adServ+" IS RUNNING ");
				Client c = new Client( _server.accept());
				clients.add(c);
				c.start();
			}

			
		} catch (SocketException e) {
			if(!_terminate){
				e.getStackTrace();
				//Dprint.println("GlobalMsgerRcv "+ _adServ+" HAS TERMINATED with a Socket Exception");
			}
		} catch (IOException e) {
			if(!_terminate){
				e.printStackTrace();
			}
		} catch (Exception e) {
			if(!_terminate)
				e.printStackTrace();
		}
//		Dprint.println("GlobalMsgerRcv "+ _adServ+
//		" HAS TERMINATED");
	}

	class Client extends LocalPeerThread {
		
		
		public  Socket client;
		public InputStream is;
		public ObjectInputStream reader;
		public boolean _close ;
		
		Client(Socket s) {
			super("Client of GMsger" + _adServ.toString());
			_close = false;
			try{
				client = s;
				is = s.getInputStream();
				reader = new ObjectInputStream(is);
			}catch(Exception ex){
				if(!isClose())
					ex.printStackTrace();
			}
		}
		
		private  synchronized void setClose(boolean b){
			_close = b;
		}
		
		private synchronized boolean isClose(){
			return _close;
		}
		
		public void close(){
			setClose(true);
			try{
				client.shutdownInput();
				client.shutdownOutput();
				client.close();
				is.close();
				reader.close();
			}catch(Exception ex){
				if(!isClose())
					ex.printStackTrace();
			}
		}

		@SuppressWarnings("unchecked")
		public void run() {
			Msg<? extends Serializable> m;
			try {
				boolean terminate =false;
				while (!terminate){
					while (((m = (Msg<? extends Serializable>) reader.readObject()) != null)) {
					switch (m.getType()) {
					case MTypeNetwork.Test: {
						Dprint.println("TEST "+_adServ +" receive Ping ");
						ObjectOutputStream w = new ObjectOutputStream(client
								.getOutputStream());
						w.writeObject(m);
						// Dprint.println("TEST Msger Local "+ _adServ
						// +" send Pong ");
						w.close();
						break;
					}
					
//					case MTypeNetwork.Close: {
//						_close = true;
//						Dprint.println("CLOSE " + _adServ + " receive MsgType.Close");		
//						terminate();
//						break;
//					}
					case MTypeNetwork.PeerClose:{
						String pName = m.getSender();
						if(_customerRcvdBoxes.containsKey(pName))
							removeCustomer(pName);
					}
					
					
					
					default: {
//						Dprint.println(" server "+_adServ +" is delivering msgType "+m.getType()
//								+" from "+m.getSender()+" to "+m.getRecipient());
						
						String recipient = m.getRecipient();
						if(_customerRcvdBoxes.containsKey(recipient)){
							_customerRcvdBoxes.get(recipient).add(m);
							_customers.get(recipient).unPauses();
						}
						// LocalPeer._hostedPeers.get(recipient).checkAgainMboxes(true);
					}
					
					}

				}
				}
				
			}catch(java.io.EOFException e ){
				// We interupt the  clients
//				if(!isClose())
//					e.printStackTrace();
			} catch (IOException e) {
				if(!isClose())
					e.printStackTrace();
			} catch (ClassNotFoundException e) {
				if(!isClose())
					e.printStackTrace();
			}catch(Exception ex){
				if(!isClose())
					ex.printStackTrace();
			}
		}
	}

}
