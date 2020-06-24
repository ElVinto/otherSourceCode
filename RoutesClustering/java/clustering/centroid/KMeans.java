package clustering.centroid;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class KMeans {

	public static Int2IntOpenHashMap buildClusters(String dataFName, int nbClusters) throws Exception {

    	Instances data = DataSource.read(dataFName);

    	SimpleKMeans km = new SimpleKMeans();

    km.setNumClusters(nbClusters);
    km.buildClusterer(data);

    Int2IntOpenHashMap instIdx2Cluster = new Int2IntOpenHashMap(data.size());
    for(int idxData=0;idxData<data.size();idxData++){
		Instance inst = data.get(idxData);
		// System.out.println(inst+" "+cl.clusterInstance(inst));
		instIdx2Cluster.put(idxData, km.clusterInstance(inst));
    }
    return instIdx2Cluster;
  }


}
