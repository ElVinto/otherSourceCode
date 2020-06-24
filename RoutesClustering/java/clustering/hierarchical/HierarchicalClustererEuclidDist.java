package clustering.hierarchical;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import weka.clusterers.HierarchicalClusterer;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class HierarchicalClustererEuclidDist {

	 public static Int2IntOpenHashMap buildClusters(String dataFName, int nbClusters) throws Exception {

	    	Instances data = DataSource.read(dataFName);

	    	HierarchicalClusterer hm = new HierarchicalClusterer();

	    hm.setNumClusters(nbClusters);
	    hm.buildClusterer(data);

	    Int2IntOpenHashMap instIdx2Cluster = new Int2IntOpenHashMap(data.size());
	    for(int idxData=0;idxData<data.size();idxData++){
			Instance inst = data.get(idxData);
			// System.out.println(inst+" "+cl.clusterInstance(inst));
			instIdx2Cluster.put(idxData, hm.clusterInstance(inst));
	    }
	    return instIdx2Cluster;
	  }


}
