package initializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import tools.Dprint;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;


public class SSHconnection {
	
	public static boolean testConnection(String hostname,String username,String keyfileName,String keyfilePass){
		try
		{
			/* Create a connection instance */
			Connection conn = new Connection(hostname);
			
			
			File keyfile = new File(keyfileName);
			/* Now connect */

			conn.connect();

			/* Authenticate.
			 * If you get an IOException saying something like
			 * "Authentication method password not supported by the server at this stage."
			 * then please check the FAQ.
			 */

			boolean isAuthenticated = conn.authenticateWithPublicKey(username, keyfile,keyfilePass);
			//conn.authenticateWithPassword(username, password);

			if (isAuthenticated == false){
				throw new IOException("Authentication failed.");
			}

			/* Create a session */

			Session sess = conn.openSession();

			//sess.execCommand("uname -a && date && uptime && who");
			sess.execCommand("echo \" connexion test on "+hostname+" ok" );

			//System.out.println("Here is some information about the remote host:");

			/* 
			 * This basic example does not handle stderr, which is sometimes dangerous
			 * (please read the FAQ).
			 */

			InputStream stdout = new StreamGobbler(sess.getStdout());


			BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

			while (true)
			{
				String line = br.readLine();
				if (line == null)
					break;
				System.out.println(line);
			}
			
			

			
			/* Show exit status, if available (otherwise "null") */

			//System.out.println("ExitCode: " + sess.getExitStatus());

			/* Close this session */

			sess.close();

			/* Close the connection */

			conn.close();

		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
			System.exit(2);
			return false;
		}
		return true;
	}
	
	public static boolean execCmd(String hostname,String username,String keyfileName,String keyfilePass,
			String cmd){
		try
		{
			/* Create a connection instance */
			Connection conn = new Connection(hostname);
			
			File keyfile = new File(keyfileName);
			/* Now connect */

			conn.connect();

			/* Authenticate.
			 * If you get an IOException saying something like
			 * "Authentication method password not supported by the server at this stage."
			 * then please check the FAQ.
			 */

			boolean isAuthenticated = conn.authenticateWithPublicKey(username, keyfile,keyfilePass);
			//conn.authenticateWithPassword(username, password);

			if (isAuthenticated == false){
				throw new IOException("Authentication failed.");
			}

			/* Create a session */

			Session sess = conn.openSession();

			//sess.execCommand("uname -a && date && uptime && who");
			sess.execCommand(cmd);

			//System.out.println("Here is some information about the remote host:");

			/* 
			 * This basic example does not handle stderr, which is sometimes dangerous
			 * (please read the FAQ).
			 */
			
			 class OutputReader extends Thread{
				 InputStream _s ;
				 
				 public OutputReader (InputStream s){
					 _s = s;
				 }
				
				 public void run(){
					InputStream stdinput =  new StreamGobbler(_s);
					BufferedReader br = new BufferedReader(new InputStreamReader(stdinput));
					while (true)
					{	
						String line;
						try {
							line = br.readLine();
							if (line == null)
								break;
							Dprint.println(line);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			};
			
			OutputReader out = new OutputReader(sess.getStdout());
			OutputReader err = new OutputReader(sess.getStderr());
			
			out.start();
			err.start();
			
			try {
				out.join();
				err.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/* Show exit status, if available (otherwise "null") */

			// System.out.println("ExitCode: " + sess.getExitStatus());

			/* Close this session */

			sess.close();

			/* Close the connection */

			conn.close();

		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			System.exit(2);
			return false;
		}
		return true;
	}
	

}
