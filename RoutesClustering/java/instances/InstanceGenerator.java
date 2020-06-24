package instances;
import java.io.DataOutput;
import java.io.IOException;




/*
 * This class generates random elevator field service instances
 */
public class InstanceGenerator {

	static String ny_osm_fName ="";
	static String nj_osm_fName ="";

	static String elevatorsFName="";
	static String residencesFName ="";

	public static void buildRoadMap(){}

	public static void generateInstance(int nbElevators, int nbTechnicians, int nbDays, int nbActivities){

	}

//	public void write(DataOutput out) throws IOException {
//		out.writeInt(partitionWorkerMap.size());
//		for (Int2ObjectMap.Entry<IntArrayList> entry : partitionWorkerMap
//				.int2ObjectEntrySet()) {
//			out.writeInt(entry.getIntKey());
//			out.writeInt(entry.getValue().size());
//			for (int workerID : entry.getValue()) {
//				out.writeInt(workerID);
//			}
//		}
//		out.writeInt(this.workerPartitionCountMap.size());
//		for (Int2IntMap.Entry entry : this.workerPartitionCountMap
//				.int2IntEntrySet()) {
//			out.writeInt(entry.getIntKey());
//			out.writeInt(entry.getIntValue());
//		}
//	}

	public static void main(String[] args) {
		System.out.println("Coucou");
	}

}
