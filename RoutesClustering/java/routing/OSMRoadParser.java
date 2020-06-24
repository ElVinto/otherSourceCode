package routing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import tools.ObjectSerializer;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.TLongDoubleIterator;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongByteHashMap;
import gnu.trove.map.hash.TLongDoubleHashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;

public class OSMRoadParser {

	private static void readCSVElevetor(String f,
			TLongDoubleHashMap elevatorLat,
			TLongDoubleHashMap elevatorLon) throws IOException{

		BufferedReader reader = new BufferedReader(new FileReader(f));

		String line = reader.readLine();
		line = reader.readLine(); // pass the first line
		int iElevator =1;
		while (line != null) {
			String [] vals =line.trim().split(",");
			int oLat = vals.length -2;
			int oLon = vals.length -1;

			try {
				double latitudeNode = Double.parseDouble(vals[oLat]);
				double longitudeNode =  Double.parseDouble(vals[oLon]);
				elevatorLat.put(iElevator,latitudeNode);
				elevatorLon.put(iElevator,longitudeNode);
			}catch(Exception e){
				//				e.printStackTrace();
			}
			iElevator++;
			line = reader.readLine();
		}
		reader.close();

		System.out.println("Frist Eleve loc: "+elevatorLat.get(1) +" "+elevatorLon.get(1));
		System.out.println("elevatorLat "+elevatorLat.size());

	}


	private static void displayOsmNodeCoor(String f,
			TLongDoubleHashMap nodesLat,
			TLongDoubleHashMap nodesLon,
			HashMap<Long,String > nodes2StreetName
			) throws IOException{




		BufferedReader reader = new BufferedReader(new FileReader(f));

		String line = reader.readLine();

		TLongHashSet residentialNodeIds = new TLongHashSet();
		ArrayList<String> residentialStreets = new ArrayList<String>();

		int nbRes =0;
		while (line != null) {


			if(line.indexOf("residential")!= -1){
				nbRes++;
				//				System.out.println(line);
			}
			if(line.indexOf("<node id") != -1){
				long idNode = parseLongAttribut(" id",line);
				//				double latitudeNode = parseDbleAttribut(" lat",line);
				//				double longitudeNode = parseDbleAttribut(" lon",line);
				//
				//				nodesLat.put(idNode,latitudeNode);
				//				nodesLon.put(idNode,longitudeNode);



				if((line.indexOf("residential")!= -1 )||
						(line.indexOf("k=\"building\" v=\"residential\"")!= -1) ||
						(line.indexOf("k=\"highway\" v=\"residential\"")!= -1) ||
						(line.indexOf("k=\"landuse\" v=\"residential\"")!= -1)

						){
					residentialNodeIds.add(idNode);
					line = reader.readLine();
					continue;
				}



				line = reader.readLine();
				while ( line.indexOf("<node id") == -1){
					if(line.indexOf("residential")!= -1){
						residentialNodeIds.add(idNode);
						//						System.out.println("select line "+line);
						line = reader.readLine();
						break;
					}
					line = reader.readLine();
				}
				continue;
			}

			if(line.indexOf("<way id") != -1){
				TLongArrayList wayNodesId = new TLongArrayList();
				String roadType = "";
				String roadName ="";
				while(line.indexOf("/way>") == -1){

					if(line.indexOf("<nd")!=-1){
						wayNodesId.add(parseLongAttribut("ref",line));
					}

					if(line.indexOf("k=\"name\"")!=-1){
						roadName = parseStringAttribut(" v",line);
					}

					if(line.indexOf("k=\"highway\"")!=-1){
						roadType = parseStringAttribut(" v",line);
						//						break;
					}
					line = reader.readLine();
				}
				if(roadType.contains("residential")){
					//					residentialNodeIds.addAll(wayNodesId);
					int medNode = wayNodesId.size()/2;
					residentialNodeIds.add(wayNodesId.get(medNode));
					nodes2StreetName.put(wayNodesId.get(medNode),roadName);
				}

				line = reader.readLine();
				continue;
			}

			line = reader.readLine();

		}
		reader.close();

		System.out.println("nbRes "+nbRes);
		System.out.println("resNodes "+residentialNodeIds.size());


		reader = new BufferedReader(new FileReader(f));
		line = reader.readLine();
		while (line != null) {
			if(line.indexOf("<node id") != -1){
				long idNode = parseLongAttribut(" id",line);
				if (residentialNodeIds.contains(idNode)){
					double latitudeNode = parseDbleAttribut(" lat",line);
					double longitudeNode = parseDbleAttribut(" lon",line);

					nodesLat.put(idNode,latitudeNode);
					nodesLon.put(idNode,longitudeNode);
				}
			}

			line = reader.readLine();
		}
		reader.close();



		//		TLongDoubleIterator itNodesLat = nodesLat.iterator();
		//		TLongDoubleIterator itNodesLon = nodesLon.iterator();
		//		while(itNodesLat.hasNext()){
		//			itNodesLat.advance();
		//			itNodesLon.advance();
		//			long idNode = itNodesLat.key();
		//			if(!residentialNodeIds.contains(idNode)){
		//				itNodesLat.remove();
		//				itNodesLon.remove();
		//			}
		//		}


		System.out.println("nodesLon "+nodesLon.size());

	}

	private static void populateRoadObjects(String f,
			ArrayList<String> selectedRoadTyp,
			TLongObjectHashMap<TLongArrayList> roadMap,
			TLongObjectHashMap<TLongArrayList> roadSpeed,
			TLongObjectHashMap<TDoubleArrayList> roadDist,
			TLongDoubleHashMap nodesLat,
			TLongDoubleHashMap nodesLon) throws IOException{


		BufferedReader reader = new BufferedReader(new FileReader(f));

		String line = reader.readLine();

		int nbArcs = 0;
		while (line != null) {

			if(line.indexOf("<node") != -1){
				long idNode = parseLongAttribut(" id",line);
				double latitudeNode = parseDbleAttribut(" lat",line);
				double longitudeNode = parseDbleAttribut(" lon",line);

				if(idNode != -1  ){
					nodesLat.put(idNode,latitudeNode);
					nodesLon.put(idNode,longitudeNode);
				}
			}

			if(line.indexOf("<way") != -1){
				TLongArrayList wayNodesId = new TLongArrayList();
				String roadType = "";
				while(line.indexOf("/way>") == -1){

					if(line.indexOf("<nd")!=-1){
						wayNodesId.add(parseLongAttribut("ref",line));
					}

					if(line.indexOf("highway")!=-1){
						roadType = parseStringAttribut(" v",line);
					}
					line = reader.readLine();
				}

				if(selectedRoadTyp.contains(roadType)){

					for(int i=1; i< wayNodesId.size();i++){
						long iPred = wayNodesId.get(i-1);
						long iCur = wayNodesId.get(i);

						//							Cutting link > 3 km
						//							double dist_Pred_Cur = distGPSCoordonates(nodesLat.get(iPred),nodesLon.get(iPred)
						//									,nodesLat.get(iCur),nodesLon.get(iCur));
						//							if( dist_Pred_Cur>5.0){
						//								System.out.println(" cut way of "+ dist_Pred_Cur+ " km");
						//								continue;
						//							}

						roadMap.putIfAbsent(iPred, new TLongArrayList());
						roadMap.get(iPred).add(iCur);

						roadMap.putIfAbsent(iCur, new TLongArrayList());
						roadMap.get(iCur).add(iPred);

						int speed = roadTyp2Speed(roadType); // km/h
						roadSpeed.putIfAbsent(iPred, new TLongArrayList());
						roadSpeed.get(iPred).add(speed);

						roadSpeed.putIfAbsent(iCur, new TLongArrayList());
						roadSpeed.get(iCur).add(speed);

						double dist = distGPSCoordonates(nodesLat.get(iPred),nodesLon.get(iPred)
								,nodesLat.get(iCur),nodesLon.get(iCur));

						if(nodesLat.get(iPred)==nodesLat.getNoEntryValue() ||
								nodesLon.get(iPred)==nodesLon.getNoEntryValue() ||
								nodesLat.get(iCur) == nodesLat.getNoEntryValue() ||
								nodesLon.get(iCur) == nodesLon.getNoEntryValue() ||
								dist<0
								)
							System.err.println(" Node not found ");

						roadDist.putIfAbsent(iPred, new TDoubleArrayList());
						roadDist.get(iPred).add(dist);

						roadDist.putIfAbsent(iCur, new TDoubleArrayList());
						roadDist.get(iCur).add(dist);

						nbArcs+=2;
					}
				}
			}
			line = reader.readLine();
		}
		reader.close();

		TLongObjectIterator<TLongArrayList> it = roadMap.iterator();
		while(it.hasNext()){
			it.advance();
			long node =it.key();
			if(!nodesLat.containsKey(node) || !nodesLon.containsKey(node)){
				System.err.println("Unknown Road Node"+node);
			}
		}

		System.out.println("NBRoadNodes: "+roadMap.size());
		System.out.println("NBArcs: "+(nbArcs));

	}

	private static void retainRoadMapNodes(
			TLongObjectHashMap<TLongArrayList> roadMap,
			TLongDoubleHashMap nodesLat, TLongDoubleHashMap nodesLon) {

		TLongDoubleIterator itNodesLat = nodesLat.iterator();
		TLongDoubleIterator itNodesLon = nodesLon.iterator();
		while(itNodesLat.hasNext()){
			itNodesLat.advance();
			itNodesLon.advance();
			long idNode = itNodesLat.key();
			if(!roadMap.contains(idNode)){
				itNodesLat.remove();
				itNodesLon.remove();
			}
		}
	}

	private static TLongArrayList largestConnectedComponent(TLongObjectHashMap<TLongArrayList> roadMap){

		TLongArrayList unvisitNodes = new TLongArrayList(roadMap.keys());

		TLongArrayList largestCC = new TLongArrayList();
		while(!unvisitNodes.isEmpty()){
			long curNode = unvisitNodes.get(0);
			TLongArrayList currentCC = new TLongArrayList();
			currentCC.add(curNode);
			int iNextNodeInCurrentCC = 0;
			while(iNextNodeInCurrentCC < currentCC.size()){
				TLongArrayList neighs = roadMap.get(currentCC.get(iNextNodeInCurrentCC));
				if(neighs != null){
					for(int iNeigh=0;iNeigh< neighs.size();iNeigh++){
						long neigh = neighs.get(iNeigh);
						if(!currentCC.contains(neigh))
							currentCC.add(neigh);
					}
				}
				iNextNodeInCurrentCC ++;
			}

			//			System.out.println("currentCC.size(): "+ currentCC.size());

			unvisitNodes.removeAll(currentCC);
			if(largestCC.size()<currentCC.size())
				largestCC = currentCC;
		}
		//		System.out.println("largestCC.size(): "+ largestCC.size());
		return largestCC;
	}

	private static void retainLargestConnectedComponent(TLongObjectHashMap<TLongArrayList> roadMap,
			TLongObjectHashMap<TLongArrayList> roadSpeed){
		TLongArrayList lcc = largestConnectedComponent(roadMap);
		TLongObjectIterator<TLongArrayList> itLCC = roadMap.iterator();
		while(itLCC.hasNext()){
			itLCC.advance();
			long curNode = itLCC.key();
			if(! lcc.contains(curNode)){
				for(int i=roadMap.get(curNode).size();i-->0;){
					long neigh = roadMap.get(curNode).get(i);
					roadMap.get(neigh).remove(curNode);
					roadSpeed.get(neigh).remove(curNode);
				}
				itLCC.remove();
				roadSpeed.remove(curNode);
			}
		}
	}

	// TODO exploit if the roadMap size id huge.
	public static void contractNodeWithDegEq2(
			TIntObjectHashMap<TIntArrayList> roadMap){

		System.out.println("Cur NBRoadNodes: "+roadMap.size());

		int nbNodesWithDegLt2 =0;
		int nbNodes = roadMap.size();

		int nbArcs =0;
		TIntObjectIterator<TIntArrayList> it = roadMap.iterator();
		for(int idx_node=roadMap.size();idx_node-->0;){
			it.advance();
			int node = it.key();

			if(roadMap.get(node).size()<=2){
				int left = roadMap.get(node).get(0);
				int right = roadMap.get(node).get(1);
				if(!roadMap.get(left).contains(right))
					roadMap.get(left).add(right);
				if(!roadMap.get(right).contains(left))
					roadMap.get(right).add(left);

				roadMap.remove(node);
				nbNodesWithDegLt2 ++;
			}else{
				nbArcs+= roadMap.get(node).size();
			}
		}

		System.out.println("NBNodesWithDegLT2: "+ nbNodesWithDegLt2 +
				" = "+ ((double)nbNodesWithDegLt2/(double)nbNodes)*100 	+" % of the nodes");
		System.out.println("New NBRoadNodes: "+roadMap.size());
		System.out.println("New NBArcs: "+(nbArcs));

	}


	private static int removeDeg2NodesDistLessThan(
			double distMax, // km
			TLongObjectHashMap<TLongArrayList> roadMap,
			TLongObjectHashMap<TLongArrayList> roadSpeed,
			TLongObjectHashMap<TDoubleArrayList> roadDist,
			TLongDoubleHashMap nodesLat,
			TLongDoubleHashMap nodesLon){

		if(roadMap.isEmpty())
			return 0;

		// Initialization
		TLongArrayList selected =  new TLongArrayList();
		TLongByteHashMap  visited = new TLongByteHashMap();
		selected.add(roadMap.keys()[0]);

		while(!selected.isEmpty()){
			long curNode = selected.removeAt(0);
			visited.put(curNode,(byte)1);

			for(int i=roadMap.get(curNode).size();i-->0;){
				long neigh = roadMap.get(curNode).get(i);

				if(!visited.containsKey(neigh)){
					long prev = curNode;

					double distNode2Desc = roadDist.get(curNode).get(i);
					double savedDist = distNode2Desc;
					long curSpeed = roadSpeed.get(curNode).get(i);
					long nvSpeed = curSpeed;
					while( roadMap.get(neigh).size()==2 && distNode2Desc<distMax && nvSpeed==curSpeed){

						if(prev != curNode){
							visited.put(prev,(byte)0);
						}

						savedDist = distNode2Desc;

						int iNext = (roadMap.get(neigh).indexOf(prev)+1)%2;
						prev = neigh;
						neigh = roadMap.get(neigh).get(iNext);

						int iNeigh = roadMap.get(prev).indexOf(neigh);
						distNode2Desc += roadDist.get(prev).get(iNeigh);
						nvSpeed =  roadSpeed.get(prev).get(iNeigh);

					}

					if(prev==curNode){
						if(!selected.contains(neigh))
							selected.add(neigh);
					}else{

						visited.put(prev,(byte)1);

						if(!selected.contains(prev)){
							selected.add(prev);
						}

						if(prev!= roadMap.get(curNode).get(i) // prev != curNode neighbor
								&& !roadMap.get(curNode).contains(prev)
								){

							roadMap.get(curNode).add(prev);
							roadMap.get(prev).add(curNode);

							roadSpeed.get(curNode).add(curSpeed);
							roadSpeed.get(prev).add(curSpeed);

							roadDist.get(curNode).add(savedDist);
							roadDist.get(prev).add(savedDist);

						}

					}
				}
			}
		}


		/*
		 * End retaining selected road nodes
		 */
		int nbRemoved =0;
		TLongObjectIterator<TLongArrayList> itNodes = roadMap.iterator();
		for(int i=roadMap.size();i-->0;){
			itNodes.advance();
			long node = itNodes.key();
			if(visited.get(node)==(byte)0){
				for(int iNeigh=roadMap.get(node).size();iNeigh-->0;){
					long neigh = roadMap.get(node).get(iNeigh);
					int iNode = roadMap.get(neigh).indexOf(node);
					roadMap.get(neigh).remove(node);
					roadSpeed.get(neigh).removeAt(iNode);
					roadDist.get(neigh).removeAt(iNode);
				}
				itNodes.remove();
				roadSpeed.remove(node);
				roadDist.remove(node);
				nbRemoved++;
			}
		}
		return nbRemoved;
	}

	private static void replaceOsmNodeIdByRoadMapNodeId(
			TLongDoubleHashMap osmNodeId2Coord
			,TLongIntHashMap osmNodeId2roadMapNodeId
			, TIntDoubleHashMap roadMapNodeId2Coord){

		TLongDoubleIterator itOsmNodeId = osmNodeId2Coord.iterator();
		for(int i=osmNodeId2Coord.size(); i-->0; ){
			itOsmNodeId.advance();
			long osmNodeId = itOsmNodeId.key();
			int roadMapNodeId = osmNodeId2roadMapNodeId.get(osmNodeId);
			roadMapNodeId2Coord.put(roadMapNodeId, osmNodeId2Coord.get(osmNodeId));
		}
	}

	public static void createEnrichedRoadMap(
			TLongObjectHashMap<TLongArrayList> roadMap,
			TLongObjectHashMap<TLongArrayList> roadSpeed,
			TLongObjectHashMap<TDoubleArrayList> roadDist,
			TLongDoubleHashMap nodesLat, TLongDoubleHashMap nodesLon,
			TLongIntHashMap old2NvIds
			,TIntObjectHashMap<TIntObjectHashMap<TIntArrayList>> roadMapToCmMiliEuroSec) {

		TLongObjectIterator<TLongArrayList> itIdPred = roadMap.iterator();
		while(itIdPred.hasNext()){
			itIdPred.advance();
			long iPred = itIdPred.key() ;
			int nvIPred = old2NvIds.get(iPred);
			roadMapToCmMiliEuroSec.put(nvIPred, new TIntObjectHashMap<TIntArrayList>(roadMap.get(iPred).size()));

			TLongIterator itIdCur = roadMap.get(iPred).iterator();
			while(itIdCur.hasNext()){
				long iCur = itIdCur.next();
				int nvICur = old2NvIds.get(iCur);
				roadMapToCmMiliEuroSec.get(nvIPred).put(nvICur, new TIntArrayList(3));

				int posNeigh = roadMap.get(iPred).indexOf(iCur);

				double dist_Pred_Cur = roadDist.get(iPred).get(posNeigh); // distGPSCoordonates(nodesLat.get(iPred),nodesLon.get(iPred),nodesLat.get(iCur),nodesLon.get(iCur)); // km
				if(dist_Pred_Cur<0)
					System.err.println(" debug dist_Pred_Cur <0");
				roadMapToCmMiliEuroSec.get(nvIPred).get(nvICur).add((int)Math.round(dist_Pred_Cur * 100000.0)); // km -> cm

				double speed = roadSpeed.get(iPred).get(posNeigh);
				double consoLowSpeed = 4.0; // l/100km
				double lowSpeed = 70.0; // km/h
				double conso = (speed <= lowSpeed )?
						consoLowSpeed:
							consoLowSpeed *(1+(speed-lowSpeed)/lowSpeed); // l/100 km
				double oilPrice = 1.6 ;// euro/l;
				//				System.out.println("cost:"+oilPrice*conso*dist_Pred_Cur);
				roadMapToCmMiliEuroSec.get(nvIPred).get(nvICur).add((int)Math.round(oilPrice*conso*dist_Pred_Cur*10)); // (euro/l)*(l/100km)*km*10 -> milli euro


				double time =   dist_Pred_Cur/speed; // km/(km/h) -> h
				//				System.out.println("dist_Pred_Cur:"+ dist_Pred_Cur+ " speed:"+ speed+"  time:"+time*3200);
				roadMapToCmMiliEuroSec.get(nvIPred).get(nvICur).add((int)Math.round(time*3600)); // h -> s

			}
		}

	}

	public static double costInMilliEuro(double dist, double speed){
		double consoLowSpeed = 4.0; // l/100km
		double lowSpeed = 70.0; // km/h
		double conso = (speed <= lowSpeed )?
				consoLowSpeed:
					consoLowSpeed *(1+(speed-lowSpeed)/lowSpeed); // l/100 km
		double oilPrice = 1.6 ;// euro/l;
		return oilPrice*conso*dist*10;
	}

	public static void writeCSVNodes(
			TLongDoubleHashMap nodesLat,
			TLongDoubleHashMap nodesLon,
			String fName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(fName));
		writer.write("osmId,latitude,longitude\n");

		TLongDoubleIterator it = nodesLat.iterator();
		while(it.hasNext()){
			it.advance();
			long iNode = it.key();
			writer.write(
					iNode
					+","+nodesLat.get(iNode)
					+","+nodesLon.get(iNode)+"\n");
		}

		writer.flush();
		writer.close();
	}

	public static void writePrettyCSVMap(
			TLongObjectHashMap<TLongArrayList> roadMap,
			TLongDoubleHashMap nodesLat,
			TLongDoubleHashMap nodesLon,
			String fName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(fName));
		writer.write("osmId1,osmId2,link\n");

		TLongHashSet treated = new TLongHashSet(roadMap.size());
		TLongObjectIterator<TLongArrayList> it = roadMap.iterator();
		for(int i=roadMap.size();i-->0;){
			it.advance();
			long node = it.key();
			double nodeLat = nodesLat.get(node);
			double nodeLon = nodesLon.get(node);

			for(int j=roadMap.get(node).size();j-->0;){
				long neigh = roadMap.get(node).get(j);
				if(!treated.contains(neigh)){
					double neighLat = nodesLat.get(neigh);
					double neighLon = nodesLon.get(neigh);
					String link="<LineString><coordinates>"
							+nodeLon+","+nodeLat+" "
							+neighLon+","+neighLat+
							"</coordinates></LineString>";
					writer.write(
							node
							+","+neigh
							+ ",\"<MultiGeometry>"
							//	+"<Point><coordinates>"+nodeLon+","+nodeLat+"</coordinates></Point>"
							+ link
							+"</MultiGeometry>\""
							+"\n");
				}
			}
			treated.add(node);
		}

		writer.flush();
		writer.close();
	}

	public static void writePrettyLocAnsPathsMap(
			TIntObjectHashMap<TIntObjectHashMap<TIntArrayList>> cspRoadMap,
			TIntObjectHashMap<TIntArrayList> loc2roadNodes,
			TLongDoubleHashMap nodesLat, TLongDoubleHashMap nodesLon,TIntLongHashMap nv2OldIds,
			String fName) throws IOException {

		TIntHashSet locAndAreas = new TIntHashSet();
		TIntObjectIterator<TIntArrayList> itLocs = loc2roadNodes.iterator();
		for(int iLoc = loc2roadNodes.size();iLoc-->0;){
			itLocs.advance();
			int loc = itLocs.key();
			locAndAreas.add(loc);
			locAndAreas.addAll(loc2roadNodes.get(loc));
		}

		BufferedWriter writer = new BufferedWriter(new FileWriter(fName));
		writer.write("nodeId1,nodeId2,link\n");

		TIntHashSet treated = new TIntHashSet(cspRoadMap.size());
		TIntObjectIterator<TIntObjectHashMap<TIntArrayList>> itNodes = cspRoadMap.iterator();
		for(int i=cspRoadMap.size();i-->0;){
			itNodes.advance();
			int node = itNodes.key();
			double nodeLat = nodesLat.get(nv2OldIds.get(node));
			double nodeLon = nodesLon.get(nv2OldIds.get(node));

			String point = locAndAreas.contains(node)?"<Point><coordinates>"+nodeLon+","+nodeLat+"</coordinates></Point>":"";
			boolean pointWriten = false;
			TIntObjectIterator<TIntArrayList> itNeigh = cspRoadMap.get(node).iterator();
			for(int j=cspRoadMap.get(node).size();j-->0;){
				itNeigh.advance();
				int neigh = itNeigh.key();
				if(!treated.contains(neigh)){
					double neighLat = nodesLat.get(nv2OldIds.get(neigh));
					double neighLon = nodesLon.get(nv2OldIds.get(neigh));
					String link="<LineString><coordinates>"
							+nodeLon+","+nodeLat+" "
							+neighLon+","+neighLat+
							"</coordinates></LineString>";
					writer.write(
							node
							+","+neigh
							+ ",\"<MultiGeometry>"
							+(pointWriten?"":point)
							+ link
							+"</MultiGeometry>\""
							+"\n");
					pointWriten =true;
				}
			}
			if(!pointWriten && !point.equals("")){
				writer.write(
						node
						+","+node
						+ ",\"<MultiGeometry>"
						+point
						+"</MultiGeometry>\""
						+"\n");
			}
			treated.add(node);
		}

		writer.flush();
		writer.close();

		System.out.println("write new "+fName);
	}

	public static double  distGPSCoordonates(double latX, double lonX, double latY, double lonY){
		int R = 6371; // km
		double dLat = Math.PI*(latX-latY)/180;
		double dLon = Math.PI*(lonX-lonY)/180;
		double lat1 = Math.PI*(latX)/180;
		double lat2 = Math.PI*(latY)/180;

		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
				Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

		//		System.out.println("dist ("+latX+","+lonX+");("+latY+","+lonY+")="+R*c);
		return R * c;
	}

	public static double dist2LatDiff(double k){

		double landmarkLon1 = -6.40 ;
		double landmarkLat1 = 54.0 ;

		double landmarkLon2 = -6.40 ;
		double landmarkLat2 = 53.0 ;

		double dist = distGPSCoordonates(landmarkLat1,landmarkLon1,landmarkLat2,landmarkLon2);

		return k/dist;
	}

	public static double dist2LonDiff(double k){

		double landmarkLon1 = -6.40 ;
		double landmarkLat1 = 54.0 ;

		double landmarkLon2 = -7.40 ;
		double landmarkLat2 = 54.0 ;

		double dist = distGPSCoordonates(landmarkLat1,landmarkLon1,landmarkLat2,landmarkLon2);

		return k/dist;
	}

	/*
	 * return km/h
	 */
	public static int roadTyp2Speed(String highwayType){


		if( highwayType.equals("motorway"))
			return 110;
		if( highwayType.equals("trunk"))
			return 110;
		if( highwayType.equals("primary"))
			return 70;
		if( highwayType.equals("secondary"))
			return 60;
		if( highwayType.equals("tertiary"))
			return 50;
		if( highwayType.equals("motorway_link"))
			return 50;
		if( highwayType.equals("trunk_link"))
			return 50;
		if( highwayType.equals("primary_link"))
			return 50;
		if( highwayType.equals("secondary_link"))
			return 50;
		if( highwayType.equals("road"))
			return 40;
		if( highwayType.equals("unclassified"))
			return 40;
		if( highwayType.equals("residential"))
			return 30;
		if( highwayType.equals("unsurfaced"))
			return 20;
		if( highwayType.equals("living_street"))
			return 10;
		if( highwayType.equals("service"))
			return 5;

		return 5;


	}

	public static double parseDbleAttribut(String attName, String line){
		String val = parseStringAttribut(attName, line);
		if (val!= null){
			return Double.parseDouble(val);
		}else {
			System.err.println(attName+ " NOT FOUND IN "+ line);
			return -1 ;
		}
	}

	public static Long parseLongAttribut(String attName, String line){
		String val = parseStringAttribut(attName, line);
		if (val!= null){
			return Long.parseLong(val);
		}else {
			System.err.println(attName+ " NOT FOUND IN "+ line);
			return (long) -1 ;
		}
	}

	public static String parseStringAttribut(String attName, String line){
		int idxAtt = line.indexOf(attName);
		if(idxAtt!= -1){
			int startValIdx = line.indexOf("\"", idxAtt)+1;
			int endValIdx = line.indexOf("\"", startValIdx);
			return line.substring(startValIdx, endValIdx);
		}
		return null;
	}


	public static double [][] fullClosureMatrix(TIntObjectHashMap<TIntDoubleHashMap> roadMap){
		// TODO Finish
		int [][] matrix = new int [roadMap.size()][roadMap.size()];
		int[] keys =roadMap.keys();
		for(int idx_k1 =0;idx_k1< keys.length;idx_k1++){
			for(int idx_k2 =0;idx_k2< keys.length;idx_k2++){

			}
		}
		return null;
	}



	public static void populateEnrichedRoadMapStructures(
			String osmFName
			, TIntObjectHashMap<TIntObjectHashMap<TIntArrayList>> roadMap2KmMiliEuroSec
			, TIntDoubleHashMap roadMapNodeId2Lat
			, TIntDoubleHashMap roadMapNodeId2Lon
			) throws IOException{

		long time = System.currentTimeMillis();
		//		TIntObjectHashMap<TIntObjectHashMap<TIntArrayList>> roadMap = parseNodesAndRoadMap(inputDataFile1);

		/*
		 * Select roads
		 */
		ArrayList<String> selectedRoadTyp = new ArrayList<String>();
		//		selectedRoadTyp.add("motorway");
		//		selectedRoadTyp.add("motorway_link");
		//		selectedRoadTyp.add("trunk");
		//		selectedRoadTyp.add("trunk_link");
		//		selectedRoadTyp.add("primary");
		//		selectedRoadTyp.add("primary_link");
		//		selectedRoadTyp.add("bridge");
		//		selectedRoadTyp.add("secondary");
		//		selectedRoadTyp.add("secondary_link");
		//		selectedRoadTyp.add("tertiary");
		selectedRoadTyp.add("residential");
		selectedRoadTyp.add("living_street");
		//		selectedRoadTyp.add("service");
		//		selectedRoadTyp.add("road");
		//		selectedRoadTyp.add("unclassified");

		TLongObjectHashMap<TLongArrayList>  roadMap = new TLongObjectHashMap<TLongArrayList> ();
		TLongObjectHashMap<TLongArrayList> roadSpeed = new TLongObjectHashMap<TLongArrayList>();
		TLongObjectHashMap<TDoubleArrayList> roadDist = new TLongObjectHashMap<TDoubleArrayList>();
		TLongDoubleHashMap osmNodeId2Lat = new TLongDoubleHashMap();
		TLongDoubleHashMap osmNodeId2Lon = new TLongDoubleHashMap();


		populateRoadObjects(osmFName, selectedRoadTyp, roadMap, roadSpeed, roadDist, osmNodeId2Lat, osmNodeId2Lon);
		time = (System.currentTimeMillis()- time)/1000;
		System.out.println("roadMap of "+osmFName+" in "+time+" sec");

		double distMax =0.5;  //TODO: change this back to 0.5

		int nbRemoved = removeDeg2NodesDistLessThan( distMax, roadMap, roadSpeed, roadDist, osmNodeId2Lat, osmNodeId2Lon);
		System.out.println("After Removing deg2-nodes distant of at most "+ distMax +"km NBRoadNodes: "+roadMap.size());

		// TODO UNCOMMENT IN FINAL VERSION
		// retains largest connected component
		//		retainLargestConnectedComponent(roadMap, roadSpeed);
		//		System.out.println("After retaining Largest Connected Component NBRoadNodes: "+roadMap.size());

		// remove from non road nodes from nodes Lat and nodes Lon,
		retainRoadMapNodes(roadMap, osmNodeId2Lat, osmNodeId2Lon);

		// re index the osm road nodes from 0 to nb nodes of the roadmap
		TLongIntHashMap osmNodeId2roadMapNodeId = new TLongIntHashMap(roadMap.size());
		int nvId = 0;
		TLongObjectIterator<TLongArrayList> itOsmRoadMapNodeId =roadMap.iterator();
		while(itOsmRoadMapNodeId.hasNext()){
			itOsmRoadMapNodeId.advance();
			osmNodeId2roadMapNodeId.put(itOsmRoadMapNodeId.key(),nvId);
			nvId++;
		}

		replaceOsmNodeIdByRoadMapNodeId(osmNodeId2Lat, osmNodeId2roadMapNodeId,roadMapNodeId2Lat);
		replaceOsmNodeIdByRoadMapNodeId(osmNodeId2Lon, osmNodeId2roadMapNodeId, roadMapNodeId2Lon);
		createEnrichedRoadMap(roadMap,roadSpeed, roadDist, osmNodeId2Lat, osmNodeId2Lon, osmNodeId2roadMapNodeId, roadMap2KmMiliEuroSec);
	}

	//	<tag k="building" v="residential"/>
	//	<tag k="building" v="residential"/>
	//	<tag k="building" v="residential"/>
	//	<tag k="highway" v="residential"/>
	//	<tag k="highway" v="residential"/>
	//	<tag k="highway" v="residential"/>
	//	<tag k="highway" v="residential"/>
	//	<tag k="highway" v="residential"/>
	//	<tag k="highway" v="residential"/>
	//	<tag k="highway" v="residential"/>
	//	<tag k="highway" v="residential"/>

	/**
	 *
	 * @param fName
	 * @throws IOException
	 */
	public static HashMap<Integer,Integer> parseAGI(String fInName, String fOutName)  {

		HashMap<Integer,Integer> zipCode2MedAgi = new HashMap<Integer,Integer>();

		try{
			BufferedReader br;
			br = new BufferedReader(new FileReader(fInName));
			String line = br.readLine();
			line = br.readLine(); // pass the Attribute Names

			BufferedWriter bw = new BufferedWriter(new FileWriter(fOutName));
			bw.write("zip_code,Agi\n");

			/*
			 * adjusted gross income : AGI
			 * zipCode2MedAgi.get(code) -> med AGI
			 * AGI 1 $1 under $25,000
			 * Agi 2 $25,000 under $50,000
			 * Agi 3 $50,000 under $75,000
			 * Agi 4 $75,000 under $100,000
			 * Agi 5 $100,000 under $200,000
			 * Agi 6 $200,000 or more
			 */



			long [] agi2NbReturns = new long [7];
			while(line!=null){

				String [] vals = line.split(",");
				int zip_code= (int)Integer.parseInt(vals[2]);
				int cur_agi= (int)Integer.parseInt(vals[3]);
				long nbReturns= (int)Double.parseDouble(vals[4]);

				agi2NbReturns[cur_agi] =nbReturns;

				if(cur_agi==6){
					int sumNbReturns =0;
					for (int i=1;i<agi2NbReturns.length;i++){
						sumNbReturns += agi2NbReturns[i];
					}
					int med = sumNbReturns/2;
					int nb =0;
					for (int i=1;i<agi2NbReturns.length;i++){
						nb += agi2NbReturns[i];
						if(nb>med){
							bw.write(""+zip_code+","+i+"\n");
							zipCode2MedAgi.put(zip_code, i);
							break;
						}
					}


				}


				cur_agi= (cur_agi==6)?1: cur_agi+1;

				line = br.readLine();
			}
			bw.close();
			br.close();

		}catch(IOException e){
			e.printStackTrace();
		}
		return zipCode2MedAgi;
	}

	public static HashMap<Integer,double []> parseZipCodeLoc(String fInName)  {

		HashMap<Integer,double []> zipCode2LatLon = new HashMap<Integer,double []>();

		try{
			BufferedReader br;
			br = new BufferedReader(new FileReader(fInName));
			String line = br.readLine();
			line = br.readLine(); // pass the Attribute Names


			while(line!=null){

				String [] vals = line.split(",");
				int zip_code= (int)Integer.parseInt(vals[0]);
				double lat= Double.parseDouble(vals[1]);
				double lon= Double.parseDouble(vals[2]);

				zipCode2LatLon.put(zip_code,new double[]{lat,lon});


				line = br.readLine();
			}

			br.close();

		}catch(IOException e){
			e.printStackTrace();
		}
		return zipCode2LatLon;
	}


	public static void main(String args[]){

		//		parseAGI("./src/test/ressources/data/osm/tax-Income/14zpallagi.csv","./src/test/ressources/data/osm/tax-Income/zipCodeMedAgi.csv");
		//		if(true)
		//			System.exit(0);

		//		System.out.println("1 km to lat diff:"+dist2LatDiff(1));
		//		System.out.println("5 km to lat diff:"+dist2LatDiff(5));

		// INPUTS
		String osmFolderName = "./src/test/ressources/data/osm/";
		String mName = "new-york";
		int distMinFromElev =0;
		int distMaxFromElev =60;
		String distInfo = "-"+distMinFromElev+"-"+distMaxFromElev+"-kmFromElev4000";

		String mapName = mName+"-latest";//"california-latest";// "texas-latest"//"illinois-latest";
		//"ireland-and-northern-ireland-latest";//norway-latest";//"ireland-and-northern-ireland-latest"; // new-york-latest

		TIntObjectHashMap<TIntObjectHashMap<TIntArrayList>> roadMap2DistCostTime = new TIntObjectHashMap<TIntObjectHashMap<TIntArrayList>>();
		TLongDoubleHashMap nodeId2Lat = new TLongDoubleHashMap() ;
		TLongDoubleHashMap nodeId2Lon = new TLongDoubleHashMap();
		HashMap<Long,String > nodes2StreetName = new HashMap<Long,String >();
		String osmFileName = osmFolderName+mapName+".osm";



		try {

			OSMRoadParser.displayOsmNodeCoor(osmFileName
					,nodeId2Lat
					,nodeId2Lon
					,nodes2StreetName
					);

			TLongDoubleHashMap elevId2Lat = new TLongDoubleHashMap();
			TLongDoubleHashMap elevId2Lon = new TLongDoubleHashMap();
			String elevFileName =  "./src/test/ressources/data/osm/elevatorsSample4000.csv";

			OSMRoadParser.readCSVElevetor(elevFileName,
					elevId2Lat,
					elevId2Lon
					);


			HashMap<Integer,Integer> zipCode2MedAgi = parseAGI("./src/test/ressources/data/osm/tax-Income/14zpallagi.csv","./src/test/ressources/data/osm/tax-Income/"+mName+"ZipCodeMedAgi.csv");
//			System.out.println(zipCode2MedAgi);

			HashMap<Integer,double []> zipCode2LatLon =parseZipCodeLoc("./src/test/ressources/data/osm/US_ZIP-CODE_LAT_LON.csv");
//			System.out.println(zipCode2LatLon);


			BufferedWriter writer = new BufferedWriter(new FileWriter("./src/test/ressources/data/osm/"+mName+"Resid"+distInfo+".csv"));
			writer.write("osmId,latitude,longitude,street_name,closest_zip_code,med_AGI,med_income\n");


			 String []agiIncome = new String[]{"<1$"
					,"1$ under 25000$"
					,"25000$ under 50000$"
					,"50000$ under 75000$"
					,"75000$ under 100000$"
					,"100000$ under 200000$"
					,">200000$"
			};

			HashSet<Integer> selectZipCodes = new HashSet<Integer>();


			TLongDoubleIterator itNodesLat = nodeId2Lat.iterator();
			TLongDoubleIterator itNodesLon = nodeId2Lon.iterator();
			int nbPotRes =0;
			while(itNodesLat.hasNext()){
				itNodesLat.advance();
				itNodesLon.advance();
				long idNode = itNodesLat.key();
				double nodeLat = itNodesLat.value();
				double nodeLon = itNodesLon.value();


				TLongDoubleIterator itElevLat = elevId2Lat.iterator();
				TLongDoubleIterator itElevLon = elevId2Lon.iterator();

				boolean nodeInCentre = false;
				while(itElevLat.hasNext()){
					itElevLat.advance();
					itElevLon.advance();

					//					long idElev = itElevLat.key();
					double elevLat = itElevLat.value();
					double elevLon = itElevLon.value();


					if(distGPSCoordonates(nodeLat, nodeLon, elevLat, elevLon)<=distMinFromElev){
						nodeInCentre=true;
						break;
					}
				}

				if(nodeInCentre)
					continue;

				itElevLat = elevId2Lat.iterator();
				itElevLon = elevId2Lon.iterator();

				while(itElevLat.hasNext()){
					itElevLat.advance();
					itElevLon.advance();

					//					long idElev = itElevLat.key();
					double elevLat = itElevLat.value();
					double elevLon = itElevLon.value();


					if(distGPSCoordonates(nodeLat, nodeLon, elevLat, elevLon)<=distMaxFromElev){

						int closestZipCode = -1;
						double closestDist = Double.MAX_VALUE;
						for (int z :zipCode2LatLon.keySet()){
							if(zipCode2MedAgi.containsKey(z)){ // ("unfound closestZipCode in Taxe Data is not considered "
								double zLat = zipCode2LatLon.get(z)[0];
								double zLon = zipCode2LatLon.get(z)[1];
								double dist = distGPSCoordonates(nodeLat, nodeLon,  zLat, zLon);
								if(dist<closestDist){
									closestZipCode = z;
									closestDist=dist;
								}
							}
						}


						int medAGI = zipCode2MedAgi.get(closestZipCode);

						writer.write(
								idNode
								+","+nodeLat
								+","+nodeLon
								+","+nodes2StreetName.get(idNode)
								+","+closestZipCode
								+","+medAGI
								+","+agiIncome[medAGI]
								+"\n"

								);

						selectZipCodes.add(closestZipCode);
						//						System.out.println(" idNode "+idNode);
						nbPotRes++;
						break;
					}
				}




				//				if (!nodeIn){
				//					itNodesLat.remove();
				//					itNodesLon.remove();
				//				}


			}
			writer.flush();
			writer.close();



			System.out.println("Nb Potential Residence between "+distMinFromElev+" and "+distMaxFromElev+" kms far from elevators "+nbPotRes);


			BufferedWriter br = new BufferedWriter(new FileWriter("./src/test/ressources/data/osm/tax-Income/"+mName+"ZipCode"+distInfo+".csv"));
			br.write("zip_code,latitude,longitude,med_AGI,med_income\n");
			for(int z : selectZipCodes){
				int medAGI = zipCode2MedAgi.get(z);
				double zLat = zipCode2LatLon.get(z)[0];
				double zLon = zipCode2LatLon.get(z)[1];
				br.write(""+z+""
						+ ","+zLat
						+","+zLon
						+ ","+medAGI
						+","+agiIncome[medAGI]
						+ "\n");
			}
			br.flush();
			br.close();


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}
