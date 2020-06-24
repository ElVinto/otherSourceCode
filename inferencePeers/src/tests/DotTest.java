package tests;

import java.io.File;
import java.io.IOException;

public class DotTest {

	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		System.out.println("Coucou");
		
		File fileDot =
			new File("C:/Users/VinTo/workspace/expe240220011/jt/bench=BAGDVTv0/nbPeers=20/tightness=0.1/nbInstances=20/graph.dot");		
		
		Runtime.getRuntime().exec(
				"dot -Tps " + fileDot.getPath() + " -o " + fileDot.getPath()+ ".ps");
		
		
	}

}
