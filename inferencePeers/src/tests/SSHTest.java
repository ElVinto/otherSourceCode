package tests;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class SSHTest {

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		String hostname = "tipi01.lri.fr";
		String username = "varmant";
		
		
		File keyfile = new File("/users/iasi/varmant/.ssh/id_rsa"); // or "~/.ssh/id_dsa"
		String keyfilePass = ""; // will be ignored if not needed
		

		try
		{
			/* Create a connection instance */
			Connection conn = new Connection(hostname);
			

			/* Now connect */

			conn.connect();

			/* Authenticate.
			 * If you get an IOException saying something like
			 * "Authentication method password not supported by the server at this stage."
			 * then please check the FAQ.
			 */

			boolean isAuthenticated = conn.authenticateWithPublicKey(username, keyfile,keyfilePass);
			//conn.authenticateWithPassword(username, password);

			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");

			/* Create a session */

			Session sess = conn.openSession();

			//sess.execCommand("uname -a && date && uptime && who");
			sess.execCommand("echo \" ssh session on "+hostname+"\"" );

			System.out.println("Here is some information about the remote host:");

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

			System.out.println("ExitCode: " + sess.getExitStatus());

			/* Close this session */

			sess.close();

			/* Close the connection */

			conn.close();

		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
			System.exit(2);
		}
	}

}
