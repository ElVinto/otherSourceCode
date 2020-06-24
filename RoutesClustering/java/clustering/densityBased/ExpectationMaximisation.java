package clustering.densityBased;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.DensityBasedClusterer;
import weka.clusterers.EM;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class ExpectationMaximisation {

  public static Int2IntOpenHashMap buildClusters(String dataFName, int nbClusters) throws Exception {

    	Instances data = DataSource.read(dataFName);

    	EM em = new EM();
    em.setNumClusters(nbClusters);
    em.buildClusterer(data);

//    ClusterEvaluation eval = new ClusterEvaluation();
//    eval.setClusterer(em);
//    eval.evaluateClusterer(new Instances(data));
//  	  System.out.println(eval.clusterResultsToString());

    Int2IntOpenHashMap instIdx2Cluster = new Int2IntOpenHashMap(data.size());
    for(int idxData=0;idxData<data.size();idxData++){
		Instance inst = data.get(idxData);
		// System.out.println(inst+" "+cl.clusterInstance(inst));
		instIdx2Cluster.put(idxData, em.clusterInstance(inst));
    }
    return instIdx2Cluster;
  }

 }
