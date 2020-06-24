package routing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.carrotsearch.hppc.DoubleArrayList;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SrcDestTimeDistMap implements BasicRoutingInterface {

	int nbNodes ;
	ObjectArrayList<String> labels;
	boolean selectedNodes =true;

	public Int2ObjectOpenHashMap<String> node2Label;
	public Int2ObjectOpenHashMap<IntArrayList> snode2dNodes;
	public Int2ObjectOpenHashMap<IntArrayList> snode2dTimes;
	public Int2ObjectOpenHashMap<DoubleArrayList> snode2dDists;


	public SrcDestTimeDistMap(String fName, ObjectArrayList<String> labels) throws IOException{
		if(labels!=null){
			selectedNodes =true;
			this.labels = labels;
			this.nbNodes = labels.size();
		}else{
			selectedNodes =false;
			this.labels = new ObjectArrayList<String>();
		}
		initStructures(fName);

		System.out.println("Map initialised from "+fName);
//		for(int snode: node2Label.keySet()){
//			System.out.println("node: "+ snode+" label "+node2Label.get(snode));
//			System.out.println(" neigh:"+snode2dNodes.get(snode));
//			System.out.println(" times:"+snode2dTimes.get(snode));
//			System.out.println(" dists:"+snode2dDists.get(snode));
//		}


	}

	private void initStructures(String fname) throws IOException{

		if(selectedNodes){
			node2Label = new Int2ObjectOpenHashMap<String>(nbNodes);
			for(int idx_label=0;idx_label<labels.size();idx_label++)
				node2Label.put(idx_label, labels.get(idx_label));
			snode2dNodes = new Int2ObjectOpenHashMap<IntArrayList>(nbNodes);
			snode2dTimes = new Int2ObjectOpenHashMap<IntArrayList>(nbNodes);
			snode2dDists = new Int2ObjectOpenHashMap<DoubleArrayList>(nbNodes);
		}else{
			node2Label = new Int2ObjectOpenHashMap<String>();
			snode2dNodes = new Int2ObjectOpenHashMap<IntArrayList>();
			snode2dTimes = new Int2ObjectOpenHashMap<IntArrayList>();
			snode2dDists = new Int2ObjectOpenHashMap<DoubleArrayList>();
		}

		BufferedReader reader = new BufferedReader(new FileReader(fname));
		String line = reader.readLine();
		if (line.contains("source"))
			line = reader.readLine(); // pass the first line
		while (line != null) {
			String [] svals = line.trim().split(",");
			String slabel = svals[0];
			String dlabel = svals[1];
			double dist = Double.parseDouble(svals[2]);
			int time = (int)Double.parseDouble(svals[3]);


			if(selectedNodes){
				if(labels.contains(slabel) && labels.contains(dlabel)){
					int idx_snode = labels.indexOf(slabel);
					int idx_dnode = labels.indexOf(dlabel);

					if(!snode2dNodes.containsKey(idx_snode)){
						snode2dNodes.put(idx_snode, new IntArrayList(nbNodes));
						snode2dTimes.put(idx_snode, new IntArrayList(nbNodes));
						snode2dDists.put(idx_snode, new DoubleArrayList(nbNodes));
					}
					snode2dNodes.get(idx_snode).add(idx_dnode);
					snode2dTimes.get(idx_snode).add(time);
					snode2dDists.get(idx_snode).add(dist);

				}
			}else{

				int idx_snode = labels.contains(slabel)?
						labels.indexOf(slabel)
						:(labels.add(slabel)?labels.indexOf(slabel):-1);

				int idx_dnode =labels.contains(dlabel)?
						labels.indexOf(dlabel)
						:(labels.add(dlabel)?labels.indexOf(dlabel):-1);

				if(!snode2dNodes.containsKey(idx_snode)){
					snode2dNodes.put(idx_snode, new IntArrayList());
					snode2dTimes.put(idx_snode, new IntArrayList());
					snode2dDists.put(idx_snode, new DoubleArrayList());
				}
				snode2dNodes.get(idx_snode).add(idx_dnode);
				snode2dTimes.get(idx_snode).add(time);
				snode2dDists.get(idx_snode).add(dist);


			}

			line = reader.readLine();
		}

		if(selectedNodes)
			if(nbNodes != labels.size()) {
				throw new IOException("   nbNodes != labels.size() ");
			}

		nbNodes = labels.size();

		reader.close();

	}


	public double distance(int s, int d) {
		if(snode2dNodes.containsKey(s)){
			int idx_d = snode2dNodes.get(s).indexOf(d);
			if(idx_d!=-1)
				return snode2dDists.get(s).get(idx_d);
		}
		return -1;
	}

	public double distance(String lab_s, String lab_d) {
		int s = labels.indexOf(lab_s);
		int d = labels.indexOf(lab_d);
		return distance(s,d);
	}

	public int time(int s, int d) {
		if(snode2dNodes.containsKey(s)){
			int idx_d = snode2dNodes.get(s).indexOf(d);
			if(idx_d!=-1)
				return snode2dTimes.get(s).getInt(idx_d);
		}
		return -1;
	}

	public int time(String lab_s, String lab_d) {
		int s = labels.indexOf(lab_s);
		int d = labels.indexOf(lab_d);
		return time(s,d);
	}

	public void readMap(String fName) throws IOException {
		selectedNodes =false;
		this.labels = new ObjectArrayList<String>();
		initStructures(fName);
	}



	public void writeMap(String fName) throws IOException{
		BufferedWriter writer;
		writer = new BufferedWriter(new FileWriter(fName));
		writer.write("source,destination,time_secs,dist_m\n");
		for(int s: node2Label.keySet()){
			for(int d: snode2dNodes.get(s)){
				int idx_d = snode2dNodes.get(s).indexOf(d);
				writer.write(node2Label.get(s)
						+","+node2Label.get(d)
						+","+snode2dTimes.get(s).getInt(idx_d)
						+","+snode2dDists.get(s).get(idx_d)
						+"\n");
				}
		}
		writer.flush();
		writer.close();
	}


}
