package clustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.carrotsearch.hppc.DoubleArrayList;

import clustering.centroid.KMeans;
import clustering.connectedComponents.ConnectedComponents;
import clustering.densityBased.ExpectationMaximisation;
import clustering.hierarchical.ClustroidDiameterClusterer;
import clustering.hierarchical.ClustroidRadiusClusterer;
import clustering.hierarchical.HierarchicalClustererEuclidDist;
import instances.BasicInstance;
import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import routing.BasicRoutingInterface;
import routing.SrcDestTimeDistMap;
import tools.ConvexHull;
import tools.Geo;
import weka.clusterers.HierarchicalClusterer;

public class ClustersOfElevators {


	/** elev2label.get(int e) -> String  elevator e's label*/
	public Int2ObjectOpenHashMap<String> elev2label ;
	/** label.getI(i) -> String elevator e's label */
	ObjectArrayList<String> labels =null;

	/** elev2lat.get(int e) -> double elevator e's latitude */
	public Int2DoubleOpenHashMap elev2lat ;
	/** elev2lon.get(int e) -> double elevator e's longitude */
	public Int2DoubleOpenHashMap elev2lon ;

	String mapFName = "";
	public BasicRoutingInterface router;


	public ClustersOfElevators(String buildingLocsFName, String srcDestMapFName) throws IOException{

		elev2label = new Int2ObjectOpenHashMap<String>();
		elev2lat = new Int2DoubleOpenHashMap() ;
		elev2lon = new Int2DoubleOpenHashMap() ;
		labels = readLocations( buildingLocsFName);
		router = new SrcDestTimeDistMap(srcDestMapFName,  labels);

	}

	private ObjectArrayList<String>readLocations(String fname) throws IOException{
		System.out.println("Read file of building locations from: "+fname);
		int elevId=0;
		ObjectArrayList<String> labels = new  ObjectArrayList<String>();

		BufferedReader reader = new BufferedReader(new FileReader(fname));

		String line = reader.readLine();
		line = reader.readLine(); // pass first line of attributes name

		while (line != null) {
			String [] vals = line.split(",");

			for(int i=0;i< vals.length;i++){
				vals[i]=vals[i].trim();
			}

			String label ="" ;
			int i=0;
			if(vals[i].contains("\"")){
				label = label+vals[i].replace("\"", "").trim();
				i++;
				while(!vals[i].contains("\"")){
					label = label+": "+vals[i];
					i++;
				}
				if(!vals[i].replace("\"", "").trim().isEmpty())
					label = label+": "+vals[i].replace("\"", "").trim();

			}else{
				label = label+vals[i].trim();
			}
			i++;
			if(vals[i].contains("\"")){
				label = label+": "+vals[i].replace("\"", "").trim();
				i++;
				while(!vals[i].contains("\"")){
					label = label+": "+vals[i];
					i++;
				}
				if(!vals[i].replace("\"", "").trim().isEmpty())
					label = label+": "+vals[i].replace("\"", "").trim();

			}else{
				label = label+": "+vals[i].trim();
			}
			labels.add(label);
			elev2label.put(elevId,label);
			elev2lat.put(elevId,Double.parseDouble(vals[vals.length-2].trim()));
			elev2lon.put(elevId,Double.parseDouble(vals[vals.length-3].trim()));


			elevId++;
			line = reader.readLine();
		}
		reader.close();

		return labels;
	}

	public String createLatLonArff() throws IOException{
		BufferedWriter writer;

		String folderName = "./src/main/resources/data/";
		File folder = new File(folderName);
		if(!folder.exists())
			folder.mkdirs();

		String fileName = folderName+"lon_lat.arff";

		writer = new BufferedWriter(new FileWriter(fileName));

		writer.write("@relation elevators \n");
		writer.write("@attribute longitude numeric \n");
		writer.write("@attribute latitude numeric \n");

		writer.write("\n@data\n");

		for(int e=0;e<elev2label.size();e++){
			writer.write(elev2lon.get(e)+","+elev2lat.get(e)+"\n");
		}

		writer.flush();
		writer.close();

		return fileName;
	}

	public Int2IntOpenHashMap buildInstIdx2CLuster(String [] args) throws Exception{
		Int2IntOpenHashMap instIdx2Cluster=null;

		String clustererName = args[0];

		String desc = clustererName;

		if(clustererName.equals("EM")
				|| clustererName.equals("KMeansED")
				|| clustererName.equals("KMinS")
				|| clustererName.equals("HCED")

				){

			String lonLatArff = createLatLonArff();
			int nbClusters = Integer.parseInt(args[1]);

			if(clustererName.equals("EM")){

				instIdx2Cluster = ExpectationMaximisation.buildClusters(lonLatArff, nbClusters);
			}

			if(clustererName.equals("HCED")){

				instIdx2Cluster = HierarchicalClustererEuclidDist.buildClusters(lonLatArff, nbClusters);
			}


			if(clustererName.equals("KMeansED")){

				instIdx2Cluster = KMeans.buildClusters(lonLatArff, nbClusters);
			}

			desc += " nbClusters "+nbClusters;

		}

		if(clustererName.equals("CC")
				|| clustererName.equals("HCR")
				|| clustererName.equals("HCD")
				){
			String unit = args[1];
			int limit = Integer.parseInt(args[2]);

			if(clustererName.equals("CC"))
				instIdx2Cluster = ConnectedComponents.buildClusters(this,unit, limit);

			if(clustererName.equals("HCD"))
				instIdx2Cluster = ClustroidDiameterClusterer.buildClusters(this,unit, limit);

			if(clustererName.equals("HCR"))
				instIdx2Cluster = ClustroidRadiusClusterer.buildClusters(this,unit, limit);



			desc+= unit+" "+limit;


		}

		return instIdx2Cluster;
	}

	public static Int2ObjectOpenHashMap<IntArrayList> getCluster2InstIdxs(Int2IntOpenHashMap instIdx2cluster){
		Int2ObjectOpenHashMap<IntArrayList> cluster2InstIdx = new Int2ObjectOpenHashMap<IntArrayList>();
		for(int i: instIdx2cluster.keySet()){
			int c = instIdx2cluster.get(i);
			if(!cluster2InstIdx.containsKey(c)){
				cluster2InstIdx.put(c, new IntArrayList());
			}
			cluster2InstIdx.get(c).add(i);

		}
		return cluster2InstIdx;
	}

	public static int getEdgeMeasFromTo(String unit, int src, int dest,  BasicRoutingInterface router  ){
		if(unit.equals("distance")){ // in meters
			int dist1 = (int) router.distance(src,dest);
			int dist2 = (int) router.distance(dest,src);

			return Math.max(dist1,dist2);
		}

		if(unit.equals("time")){ // in seconds
			int time1 = router.time(src,dest);
			int time2 = router.time(dest,src);
			return Math.max(time1, time2);
		}

		System.err.println("WARNING src: "+src+" dest "+dest+" NOT FOUND");
		return -1;
	}


	public void writeClustersInCSV (String fNameOut
			, Int2IntOpenHashMap instIdx2cluster
			)throws IOException{

		Int2ObjectOpenHashMap<IntArrayList> cluster2InstIdxs = getCluster2InstIdxs(instIdx2cluster);

		BufferedWriter writer = new BufferedWriter(new FileWriter(fNameOut));

		writer.write("buildingIdx,label,longitude,latitude,clusterId\n");

		for(int cluster: cluster2InstIdxs.keySet()){
			for(int instIdx : cluster2InstIdxs.get(cluster)){
				writer.write(""+instIdx
						+","+elev2label.get(instIdx)
						+","+elev2lon.get(instIdx)
						+","+elev2lat.get(instIdx)
						+","+cluster
						+"\n");
			}
		}

		writer.flush();
		writer.close();

	}


	public static BufferedWriter  initClustersStatInCSV (String fNameOut) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(fNameOut));
		writer.write("Clusterer,Unit,Limit,NbOfBuildings,NbOfClusters,NbOfSingletonClusters,NbOfViolatingPairs,NbOfMissingPairs,NbOfIncorrectPairs,NbOfCorrectPairs,NbOfPairs"

				+",NbTruePositive"
				+",NbFalseNegative"
				+",NbFalsePositive"
				+",NbTrueNegative"
				+",Precision"
				+",Recall"
				+",FMeasure"

				+",MinDiameter"
				+",Q1Diameter"
				+",Q2Diameter"
				+",Q3Diameter"
				+",MaxDiameter"

				+ "\n");
		return writer;
	}

	public static void closeClustersStatInCSV (BufferedWriter writer) throws IOException{
		writer.flush();
		writer.close();
	}

	public static void writeClustersStatInCSV (
			BufferedWriter writer
			, Int2IntOpenHashMap instIdx2cluster
			, String clustererName
			, String unit
			, int limit
			, BasicRoutingInterface router
			)throws IOException{


		int nbLimitViolationsWithinCluster = 0;
		int nbLimitMissOpportunitiesOutsideCluster = 0;
		int nbPairs =0;

		int nbTruePositive = 0;
		int nbTrueNegative = 0;
		int nbFalsePositive = 0;
		int nbFalseNegative = 0;

		int [] arrayOfInstIdxes = instIdx2cluster.keySet().toIntArray();
		for(int pos1=0;pos1<arrayOfInstIdxes.length-1;pos1++){
			int inst1 = arrayOfInstIdxes[pos1];
			int c1 = instIdx2cluster.get(inst1);
			for(int pos2=pos1+1;pos2<arrayOfInstIdxes.length;pos2++){
				int inst2 = arrayOfInstIdxes[pos2];
				int c2 = instIdx2cluster.get(inst2);

				int measInst1ToInst2 = getEdgeMeasFromTo(unit, inst1, inst2, router);

				if(measInst1ToInst2 <= limit){
					if( c1 == c2){
						nbTruePositive ++;
					}else{
						nbFalseNegative ++;
						nbLimitMissOpportunitiesOutsideCluster ++;
					}
				}else{
					if(c1 == c2){
						nbLimitViolationsWithinCluster ++;
						nbFalsePositive++;
					}else{
						nbTrueNegative ++;

					}
				}

				nbPairs++;
			}
		}

		int nbErrors = nbLimitMissOpportunitiesOutsideCluster+nbLimitViolationsWithinCluster;
		int nbCorrects = nbPairs-nbErrors;

		double precision = nbTruePositive /(double)( nbTruePositive+ nbFalsePositive);
		double recall = nbTruePositive /(double)( nbTruePositive+ nbFalseNegative);
		double fMeasure = 2*(precision*recall/(double)(precision+recall));

		Int2ObjectOpenHashMap<IntArrayList> cluster2InstIdxs = getCluster2InstIdxs(instIdx2cluster);

		int [] sortedDiameters = sortDiameters(cluster2InstIdxs, unit, router);

		int nbClusters = cluster2InstIdxs.size();

		int minDiameter = sortedDiameters[0];
		int q1Diameter = sortedDiameters[nbClusters/4];
		int q2Diameter = sortedDiameters[nbClusters/2];
		int q3Diameter = sortedDiameters[nbClusters*3/4];
		int maxDiameter = sortedDiameters[nbClusters-1];


		int nbSingletonClusters =0;
		for(int c: cluster2InstIdxs.keySet() ){
			if(cluster2InstIdxs.get(c).size()==1)
				nbSingletonClusters++;
		}

		if( unit.equals("distance")){
			unit = "meters";
		}
		if( unit.equals("time")){
			unit = "minutes";
			limit = limit/60;
		}

		writer.write(clustererName
						+","+unit
						+","+(double)limit
						+","+(double)instIdx2cluster.size()
						+","+(double)cluster2InstIdxs.size()
						+","+(double)nbSingletonClusters
						+","+(double)nbLimitViolationsWithinCluster
						+","+(double)nbLimitMissOpportunitiesOutsideCluster
						+","+(double)nbErrors
						+","+(double)nbCorrects
						+","+(double)nbPairs

						+","+(double)nbTruePositive
						+","+(double)nbFalseNegative
						+","+(double)nbFalsePositive
						+","+(double)nbTrueNegative
						+","+(double)precision
						+","+(double)recall
						+","+(double)fMeasure

						+","+(double)minDiameter
						+","+(double)q1Diameter
						+","+(double)q2Diameter
						+","+(double)q3Diameter
						+","+(double)maxDiameter



						+"\n");

	}


	public static int[] sortDiameters (
			Int2ObjectOpenHashMap<IntArrayList> cluster2InstIdxs
			, String unit
			, BasicRoutingInterface router
			){

		int [] diameters = new int [cluster2InstIdxs.size()];
		int iC = 0;
		for(int c: cluster2InstIdxs.keySet()){
			int d = 0;
			int [] aInsts = cluster2InstIdxs.get(c).toIntArray();
			for(int oInst1=0; oInst1 < aInsts.length-1; oInst1++){
				for(int oInst2=oInst1+1; oInst2 < aInsts.length; oInst2++){
					int measInst1ToInst2 = getEdgeMeasFromTo(unit, aInsts[oInst1], aInsts[oInst2], router);
					if(d<measInst1ToInst2)
						d=measInst1ToInst2;
				}
			}
			diameters[iC]=d;
			iC++;
		}
		java.util.Arrays.sort(diameters);
		return diameters;
	}

	public IntArrayList sortByLongitude(IntArrayList elevs){
		IntArrayList sorted = new IntArrayList(elevs);
		Collections.sort(sorted,new Comparator<Integer>(){
			public int compare(Integer e1, Integer e2) {

				double diffLon = (elev2lon.get((int)e1) - elev2lon.get((int)e2));
				if (diffLon>0){
					return (1+(int)diffLon);
				}else{
					if (diffLon == 0){
						double diffLat = (elev2lat.get((int)e1) - elev2lat.get((int)e2));
						if (diffLat>0){
							return (1+(int)diffLat);
						}else{
							if (diffLat == 0)
								return 0;
							else
								return -(1-(int)diffLat);
						}
					}else
						return -(1-(int)diffLon);
				}
			}});
		return sorted;
	}

	public IntArrayList sortByLatitude(){
		IntArrayList sorted = new IntArrayList(elev2lat.keySet());
		Collections.sort(sorted,new Comparator<Integer>(){
			public int compare(Integer e1, Integer e2) {
				double diffLatitude = (elev2lat.get((int)e1) - elev2lat.get((int)e2));
				if (diffLatitude>0){
					return (1+(int)diffLatitude);
				}else{
					if (diffLatitude == 0){
						double diffLongitude = (elev2lon.get((int)e1) - elev2lon.get((int)e2));
						if (diffLongitude>0){
							return (1+(int)diffLongitude);
						}else{
							if (diffLongitude == 0)
								return 0;
							else
								return -(1+(int)diffLongitude);
						}
					}else
						return -(1+(int)diffLatitude);
				}
			}});
		return sorted;
	}

	public IntArrayList polygonOfCluster( IntArrayList elevs, double stepSize){

		if (elevs.size() <=2){
			return elevs;
		}

		IntArrayList upperLine = new IntArrayList();
		IntArrayList lowerLine = new IntArrayList();


		IntArrayList eSortByLon = sortByLongitude(elevs);
		int firstNode = eSortByLon.getInt(0);

		// Debug
		System.out.println("eSortByLon size: "+eSortByLon.size()+" "+eSortByLon);

		upperLine.add(firstNode);
		lowerLine.add(firstNode);


		double lonUB=elev2lon.get(firstNode) + stepSize;
		IntArrayList curBucket = new IntArrayList();
		for(int i=1;i< eSortByLon.size();i++){
			int e = eSortByLon.getInt(i);
			double curLon = elev2lon.get(e);
//			System.out.println("e: "+e+" lng: "+ curLon+ " lonUB: "+lonUB );
			if(curLon>lonUB){
				if(curBucket.size()>=2 || (i == eSortByLon.size()-1)){
//					System.out.println(" treating curBucket.size()>=2: "+curBucket);
					int eBMinLat = curBucket.getInt(0);
					double minLat = elev2lat.get(eBMinLat);
					int eBMaxLat = curBucket.getInt(0);
					double maxLat = elev2lat.get(eBMaxLat);
					for(int j =1;j< curBucket.size();j++){
						int eB = curBucket.getInt(j);
						double lat = elev2lat.get(eB);

						if(minLat>lat){
							minLat = lat;
							eBMinLat = eB;
						}

						if(maxLat<lat){
							maxLat = lat;
							eBMaxLat = eB;
						}
					}
					upperLine.add(eBMaxLat);
					lowerLine.add(0,eBMinLat);

					// Debug
//					System.out.println(" UpperLine :"+upperLine);
//					System.out.println(" LowerLine :"+lowerLine);
//					if(upperLine.getInt(upperLine.size()-1)== upperLine.getInt(upperLine.size()-2)){
//						System.exit(-1);
//					}
//					System.out.println(" clear curBucket: "+curBucket);

					curBucket.clear();
				}
				lonUB=curLon+stepSize;
			}
			curBucket.add(e);
//			System.out.println(" curBucket: "+curBucket);
		}

		int lastNode = eSortByLon.getInt(eSortByLon.size()-1);
		upperLine.rem(lastNode);
		lowerLine.rem(lastNode);
		upperLine.add(lastNode);


		upperLine.addAll(lowerLine);

		return upperLine;


	}

	public void writeClustersInHtml (String fNameOut,Int2IntOpenHashMap instIdx2cluster)throws IOException{

		Int2ObjectOpenHashMap<IntArrayList> cluster2InstIdxs = getCluster2InstIdxs(instIdx2cluster);


		int nbClusters = cluster2InstIdxs.size();
		StringBuffer elevsDesc = new StringBuffer();
		StringBuffer clusterDesc = new StringBuffer();
		for (int cluster : cluster2InstIdxs.keySet() ){
			int clusterColorId = (int) (cluster*(360.0/(nbClusters)));
			String clusterColorDesc= "\'hsl("+clusterColorId+", 100%, 30%)\'";
			for(int e: cluster2InstIdxs.get(cluster)){

				String infoMarker = "\'"
						+ "<p><b>elevator</b>: "+e+"</p>"
						+ "<p><b>address</b>: "+elev2label.get(e).replace("\'","").replace(":",",")+"</p>"
						+ "<p><b>latitude</b>: "+elev2lat.get(e)+"</p>"
						+ "<p><b>longitude</b>: "+elev2lon.get(e)+"</p>"
						+ "<p><b>cluster</b>: "+cluster+"</p>"
						+"\'";
				elevsDesc.append(""
						+"       var marker"+e+" = new google.maps.Marker({\n"
						+"          position: {lat: "+elev2lat.get(e)+", lng: "+elev2lon.get(e)+"},\n"
						+"          icon: {\n"
						+"            path: google.maps.SymbolPath.CIRCLE,\n"
						+"			fillColor: "+clusterColorDesc+",\n"
						+"			fillOpacity: 0.0,\n"
						+"			strokeColor: "+clusterColorDesc+",\n"
						+"			strokeWeight: 2,\n"
						+"			scale: 3,\n"
						+"			title: \' elevatorId: "+e+"\'\n"
						+"          },\n"
						+"          map: map\n"
						+"        });\n"
						+"       var infoMarker"+e+" = new google.maps.InfoWindow({\n"
						+"          content: "+infoMarker+"\n"
						+"       });\n"
						+"       marker"+e+".addListener('click', function() {\n"
						+"          infoMarker"+e+".open(map, marker"+e+");\n"
						+"       });\n"
						+"\n"
						);

				}

			if(cluster2InstIdxs.get(cluster).size()>=2){
//				IntArrayList polygon = polygonOfCluster(cluster2InstIdxs.get(cluster),0.1);
				IntArrayList polygon = convexHull(cluster2InstIdxs.get(cluster));

				clusterDesc.append("\n       var polygonCoord"+cluster+" = [\n");
				for(int i=0;i<polygon.size();i++){
					int e = polygon.getInt(i);

					clusterDesc.append("          {lat:"+elev2lat.get(e)+", lng: "+elev2lon.get(e)+"}");
					if(i!=polygon.size()-1){
						clusterDesc.append(",\n");
					}else{
						clusterDesc.append("];\n");
					}
				}

				clusterDesc.append("\n"
						+"       var polygon"+cluster+" = new google.maps.Polygon({\n"
						+"          paths: polygonCoord"+cluster+",\n"
						+"          strokeColor: "+clusterColorDesc+",\n"
						+"          strokeOpacity: 0.4,\n"
						+"          strokeWeight: 2,\n"
						+"          fillColor: "+clusterColorDesc+",\n"
						+"          fillOpacity: 0.35\n"
						+"       });\n"
						+"       polygon"+cluster+".setMap(map);\n"
						);
			}
		}



		double minLat = -1;
		double minLon =-1;
		double maxLat = -1;
		double maxLon = -1;

		for(int e : elev2label.keySet()){
			double lat = elev2lat.get(e);
			double lon = elev2lon.get(e);

			if (minLat == -1){
				minLat = lat;
				maxLat= lat;
				minLon = lon;
				maxLon =lon;
			}

			if(lat>maxLat)
				maxLat = lat;

			if(lat<minLat)
				minLat = lat;

			if(lon>maxLon)
				maxLon = lon;

			if(lon<minLon)
				minLon =lon;


		}

		double centerLat = minLat+(maxLat-minLat)/2.0;
		double centerLon = minLon+(maxLon-minLon)/2.0;

		String []vals = fNameOut.split("/");
		String title = vals[vals.length-1].replaceAll("_", " ");
		title = title.replaceAll("cluster ", "");
		title = title.replaceAll(".html", "");
		if (title.contains("EM")){
			title = title.replaceAll(" nbClusters","");
			title = title+" cluters";
		}else{
			title = title+" "+nbClusters+" cluters";
		}
		String html =
				"<!DOCTYPE html>\n"
						+"<html>\n"
						+"  <head>\n"
						+"    <meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\">\n"
						+"    <meta charset=\"utf-8\">\n"
						+"    <title>"+title+"</title>\n"
						+"    <style>\n"
//						+"      /* Always set the map height explicitly to define the size of the div\n"
//						+"       * element that contains the map. */\n"
						+"     #map {\n"
						+"        height: 100%;\n"
						+"      }"
						+"\n"
//						+"      /* Optional: Makes the sample page fill the window. */\n"
						+"      html, body {\n"
						+"        height: 100%;\n"
						+"        margin: 0;\n"
						+"        padding: 0;\n"
						+"      }\n"
						+"    </style>\n"
						+"  </head>\n"
						+"  <body>\n"
						+"    <div id=\"map\"></div>\n"
						+"    <script>\n"
						+"\n"
						+"      function initMap() {\n"
						+"        var map = new google.maps.Map(document.getElementById(\'map\'), {\n"
						+"         zoom: 5,\n"
						+"          center: {lat: "+centerLat+", lng: "+centerLon+"}\n"
						+"        });\n"
						+"\n"

						+ elevsDesc.toString()
						+ clusterDesc.toString()

//						+"       var marker = new google.maps.Marker({\n"
//						+"          position: {lat: "+centerLat+", lng: "+centerLon+"},\n"
//						+"          icon: {\n"
//						+"            path: google.maps.SymbolPath.CIRCLE,\n"
//						+"			fillColor: \'hsl(120, 100%, 30%)\',\n"
//						+"			fillOpacity: 0.7,\n"
//						+"			strokeColor: \'hsl(120, 100%, 30%)\',\n"
//						+"			strokeWeight: 2,\n"
//						+"			scale: 1\n"
//						+"          },\n"
//						+"          map: map\n"
//						+"        });\n"


						+"\n"
						+"      }\n"
						+"    </script>\n"
						+"    <script async defer\n"
						+"    src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyDruwko3WVTx-j8wgl5f4BkjwFcudlGhDA&callback=initMap\">\n"
						+"    </script>\n"
						+"  </body>\n"
						+"</html>\n";

		BufferedWriter writer = new BufferedWriter(new FileWriter(fNameOut));
		writer.write(html);
		writer.flush();
		writer.close();


	}

	public IntArrayList convexHull(IntArrayList elevs){
		IntArrayList convexHull = new IntArrayList();

		Geo [] geoPoints = new Geo[elevs.size()];
		for(int i=0;i<elevs.size();i++){
			int e = elevs.getInt(i);
			geoPoints[i]= new Geo(elev2lat.get(e),elev2lon.get(e),e);
		}

		Geo [] hull =ConvexHull.hull(geoPoints);
		for(int i=0;i<hull.length;i++){
			convexHull.add(hull[i].nodeId);
		}

		return convexHull;
	}

	public static ObjectArrayList<String> formatSrcDestMapFName(String fNameIn,String fNameOut) throws IOException{

		BufferedReader reader = new BufferedReader(new FileReader(fNameIn));
		System.out.println("Read file of  from: "+fNameIn);

		ObjectArrayList<String> mapLabels = new ObjectArrayList<String>() ;

		BufferedWriter writer = new BufferedWriter(new FileWriter(fNameOut));

		String line = reader.readLine();
		line = reader.readLine(); // pass first line of attributes name

		while (line != null) {
			String [] vals = line.split(";");

			for(int i=0;i< vals.length;i++){
				vals[i]=vals[i].trim();
			}
			String buildingSource ="";
			String [] valSrc = vals[0].split(",");
			for(int i=0;i< valSrc.length-1;i++){
				buildingSource=buildingSource+ valSrc [i].trim()+": ";
			}
			buildingSource=buildingSource+ valSrc [valSrc.length-1].trim();

			String buildingDest = "";
			String [] valDest = vals[1].split(",");
			for(int i=0;i< valDest.length-1;i++){
				buildingDest=buildingDest+ valDest [i].trim()+": ";
			}
			buildingDest=buildingDest+ valDest [valDest.length-1].trim();

			if (!mapLabels.contains(buildingSource))
				mapLabels.add(buildingSource);
			if (!mapLabels.contains(buildingDest))
				mapLabels.add(buildingDest);

			String nvLine =buildingSource
					+","+buildingDest
					+","+vals[2]
						+","+vals[3];
			writer.write(nvLine+"\n");

			line = reader.readLine();
		}
		reader.close();
		writer.flush();
		writer.close();

		return mapLabels;
	}

	public static void main(String[] args) throws Exception {


		String mapFName ="./src/main/resources/data/newTripData.csv";
		ObjectArrayList<String> mapLabels=null;
		if(!new File(mapFName).exists())
			mapLabels =formatSrcDestMapFName("./src/main/resources/data/tripData.csv",mapFName);


		String buildingsLocFName= "./src/main/resources/data/newGeocode_postal.csv";

		ClustersOfElevators ce = new ClustersOfElevators(buildingsLocFName,mapFName);

		// Check that every building label in geocode_postal  has trip data is also format buildingLocFName
		if(mapLabels!=null){
			for (String eLabel :ce.elev2label.values()){
				if(!mapLabels.contains(eLabel)){
					System.err.println(eLabel+ " has no entry in trip map Label");
				}
			}
			for(String mapLabel:mapLabels){
				if(!ce.elev2label.values().contains(mapLabel)){
					System.err.println(mapLabel+ " has no entry in ce.elev2label ");
				}
			}
			/* Line remove from geocode_postal
			 *  VISITOR RECEPTION CENTRE: CHURCHILL has no entry in trip map Label
			 *	SHAMATTAWA SCHOOL: 100 TROUT CREEK RD has no entry in trip map Label
			 */
		}

		Int2IntOpenHashMap instIdx2Cluster = null;
		BufferedWriter writer =null;


//		// Expectation Maximization by number of clusters
//		new File("./src/main/resources/results/csv/EM/").mkdirs();
//		new File("./src/main/resources/results/html/EM/").mkdirs();
//		for(int nbClusts =10;nbClusts<=100;nbClusts+=10){
//			instIdx2Cluster = ce.buildInstIdx2CLuster(new String[]{"EM",""+nbClusts});
//			ce.writeClustersInCSV("./src/main/resources/results/csv/EM/cluster_EM_nbClusters_"+nbClusts+".csv",instIdx2Cluster);
//			ce.writeClustersInHtml("./src/main/resources/results/html/EM/cluster_EM_nbClusters_"+nbClusts+".html",instIdx2Cluster);
//
//		}


//		// Hierarchical clustering by number of clusters euclidean distance
//		new File("./src/main/resources/results/csv/HCED/").mkdirs();
//		new File("./src/main/resources/results/html/HCED/").mkdirs();
//		for(int nbClusts =10;nbClusts<=100;nbClusts+=10){
//			instIdx2Cluster = ce.buildInstIdx2CLuster(new String[]{"HCED",""+nbClusts});
//			ce.writeClustersInCSV("./src/main/resources/results/csv/HCED/cluster_HCED_nbClusters_"+nbClusts+".csv",instIdx2Cluster);
//			ce.writeClustersInHtml("./src/main/resources/results/html/HCED/cluster_HCED_nbClusters_"+nbClusts+".html",instIdx2Cluster);
//		}
//
		// Hierarchical clustering, Radius in time

		new File("./src/main/resources/results/csv/HCRTime/").mkdirs();
		new File("./src/main/resources/results/html/HCRTime/").mkdirs();
		new File("./src/main/resources/results/stat/HCRTime/").mkdirs();
		writer = initClustersStatInCSV("./src/main/resources/results/stat/HCRTime/statClustering.csv");
		for(int timeInMins =1;timeInMins<=180;timeInMins+=1){
			instIdx2Cluster = ce.buildInstIdx2CLuster(new String[]{"HCR","time",""+(timeInMins*60) }); // time in seconds
			ce.writeClustersInCSV("./src/main/resources/results/csv/HCRTime/cluster_HCR_time_"+timeInMins+"_mins.csv",instIdx2Cluster);
			ce.writeClustersInHtml("./src/main/resources/results/html/HCRTime/cluster_HCR_time_"+timeInMins+"_mins.html",instIdx2Cluster);
			writeClustersStatInCSV(writer,instIdx2Cluster,"HCR","time",timeInMins*60, ce.router);
		}
		closeClustersStatInCSV(writer);


		// Hierarchical clustering, Radius in distance

		new File("./src/main/resources/results/csv/HCRDist/").mkdirs();
		new File("./src/main/resources/results/html/HCRDist/").mkdirs();
		new File("./src/main/resources/results/stat/HCRDist/").mkdirs();
		writer = initClustersStatInCSV("./src/main/resources/results/stat/HCRDist/statClustering.csv");
		for(int dist =1000;dist<=200000;dist+=1000){
			instIdx2Cluster = ce.buildInstIdx2CLuster(new String[]{"HCR","distance",""+dist }); // distance in meter
			int distInKms = dist/1000;
			ce.writeClustersInCSV("./src/main/resources/results/csv/HCRDist/cluster_HCR_dist_"+distInKms+"_kms.csv",instIdx2Cluster);
			ce.writeClustersInHtml("./src/main/resources/results/html/HCRDist/cluster_HCR_dist_"+distInKms+"_kms.html",instIdx2Cluster);
			writeClustersStatInCSV(writer,instIdx2Cluster,"HCR","distance",dist, ce.router);
		}
		closeClustersStatInCSV(writer);



		// Hierarchical clustering, Diameter in time

		new File("./src/main/resources/results/csv/HCDTime/").mkdirs();
		new File("./src/main/resources/results/html/HCDTime/").mkdirs();
		new File("./src/main/resources/results/stat/HCDTime/").mkdirs();
		writer = initClustersStatInCSV("./src/main/resources/results/stat/HCDTime/statClustering.csv");
		for(int timeInMins =1;timeInMins<=180;timeInMins+=1){
			instIdx2Cluster = ce.buildInstIdx2CLuster(new String[]{"HCD","time",""+(timeInMins*60) }); // time in seconds
			ce.writeClustersInCSV("./src/main/resources/results/csv/HCDTime/cluster_HCD_time_"+timeInMins+"_mins.csv",instIdx2Cluster);
			ce.writeClustersInHtml("./src/main/resources/results/html/HCDTime/cluster_HCD_time_"+timeInMins+"_mins.html",instIdx2Cluster);
			writeClustersStatInCSV(writer,instIdx2Cluster,"HCD","time",timeInMins*60, ce.router);
		}
		closeClustersStatInCSV(writer);


		// Hierarchical clustering, Diameter in distance

		new File("./src/main/resources/results/csv/HCDDist/").mkdirs();
		new File("./src/main/resources/results/html/HCDDist/").mkdirs();
		new File("./src/main/resources/results/stat/HCDDist/").mkdirs();
		writer = initClustersStatInCSV("./src/main/resources/results/stat/HCDDist/statClustering.csv");
		for(int dist =1000;dist<=200000;dist+=1000){
			instIdx2Cluster = ce.buildInstIdx2CLuster(new String[]{"HCD","distance",""+dist }); // distance in meter
			int distInKms = dist/1000;
			ce.writeClustersInCSV("./src/main/resources/results/csv/HCDDist/cluster_HCD_dist_"+distInKms+"_kms.csv",instIdx2Cluster);
			ce.writeClustersInHtml("./src/main/resources/results/html/HCDDist/cluster_HCD_dist_"+distInKms+"_kms.html",instIdx2Cluster);
			writeClustersStatInCSV(writer,instIdx2Cluster,"HCD","distance",dist, ce.router);
		}
		closeClustersStatInCSV(writer);



// 		KMeans by number of clusters
//		new File("./src/main/resources/results/csv/KMeansED/").mkdirs();
//		new File("./src/main/resources/results/html/KMeansED/").mkdirs();
//		for(int nbClusts =10;nbClusts<=100;nbClusts+=10){
//			instIdx2Cluster = ce.buildInstIdx2CLuster(new String[]{"KMeansED",""+nbClusts});
//			ce.writeClustersInCSV("./src/main/resources/results/csv/KMeansED/cluster_KMeansED_nbClusters_"+nbClusts+".csv",instIdx2Cluster);
//			ce.writeClustersInHtml("./src/main/resources/results/html/KMeansED/cluster_KMeansED_nbClusters_"+nbClusts+".html",instIdx2Cluster);
//		}

		new File("./src/main/resources/results/csv/CCDist/").mkdirs();
		new File("./src/main/resources/results/html/CCDist/").mkdirs();
		new File("./src/main/resources/results/stat/CCDist/").mkdirs();
		writer = initClustersStatInCSV("./src/main/resources/results/stat/CCDist/statClustering.csv");
		for(int dist =1000;dist<=200000;dist+=1000){
			instIdx2Cluster = ce.buildInstIdx2CLuster(new String[]{"CC","distance",""+dist }); // distance in meter
			int distInKms = dist/1000;
			ce.writeClustersInCSV("./src/main/resources/results/csv/CCDist/cluster_CC_dist_"+distInKms+"_kms.csv",instIdx2Cluster);
			ce.writeClustersInHtml("./src/main/resources/results/html/CCDist/cluster_CC_dist_"+distInKms+"_kms.html",instIdx2Cluster);
			writeClustersStatInCSV(writer,instIdx2Cluster,"CC","distance",dist, ce.router);
		}
		closeClustersStatInCSV(writer);


		new File("./src/main/resources/results/csv/CCTime/").mkdirs();
		new File("./src/main/resources/results/html/CCTime/").mkdirs();
		new File("./src/main/resources/results/stat/CCTime/").mkdirs();
		writer = initClustersStatInCSV("./src/main/resources/results/stat/CCTime/statClustering.csv");
		for(int timeInMins =1;timeInMins<=180;timeInMins+=1){
			instIdx2Cluster = ce.buildInstIdx2CLuster(new String[]{"CC","time",""+(timeInMins*60) }); // time in seconds
			ce.writeClustersInCSV("./src/main/resources/results/csv/CCTime/cluster_CC_time_"+timeInMins+"_mins.csv",instIdx2Cluster);
			ce.writeClustersInHtml("./src/main/resources/results/html/CCTime/cluster_CC_time_"+timeInMins+"_mins.html",instIdx2Cluster);
			writeClustersStatInCSV(writer,instIdx2Cluster,"CC","time",timeInMins*60, ce.router);
		}
		closeClustersStatInCSV(writer);


	}

}
