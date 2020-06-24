package tools;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.TreeSet;


public class PlotBox {

	public static int idxLBAtt = -1;
	public static double valLB = -1;

	public static int idxUBAtt = -1;
	public static double valUB = -1;


	public static String keyLocation =null; // e.g.: graph 0.4, graph 1 <=> 0.4*width from left border, top



	public static void initSerieAtts(String csvFName, String ... attNames)throws IOException {
		BufferedWriter writer;
		writer = new BufferedWriter(new FileWriter(csvFName));
		String lineOfAttNames ="";
		for(String attName : attNames){
			lineOfAttNames += lineOfAttNames==""?attName:","+attName;
		}

 	    writer.write(lineOfAttNames+"\n");
 	    writer.flush();
		writer.close();
	}


	public static void addSerieValues(String csvFName, Double ... vals)throws IOException {
		BufferedWriter writer;
		writer = new BufferedWriter(new FileWriter(csvFName,true));
		String lineOfAttNames ="";
		for(Double val : vals){
			lineOfAttNames += lineOfAttNames==""?val:","+val;
		}

 	    writer.write(lineOfAttNames+"\n");
 	    writer.flush();
		writer.close();
	}



	public static void addSerieValues(BufferedWriter writer, Object ... vals)throws IOException {
		StringBuffer bs = new StringBuffer();
		bs.append(vals[0]);
		for(int i=1;i<vals.length;i++){
			bs.append(","+vals[i]);
		}
		bs.append("\n");
		writer.write(bs.toString());
	}



	public static  TreeMap<String,TreeMap<Double,ArrayList<Double>>> getSerie2XVal2YVals(String folderNameOfSerieFolders, String csvFName,int xAtt,int yAtt, String ... serieNames) throws IOException{

			if(serieNames!=null && serieNames.length>0 )
				Arrays.sort(serieNames);

			TreeMap<String,TreeMap<Double,ArrayList<Double>>> serie2XVal2YVal
			= new TreeMap<String,TreeMap<Double,ArrayList<Double>>>();

			File [] folderOfSerieFolders = (new File(folderNameOfSerieFolders)).listFiles();

			System.out.println("folderNameOfSerieFolders "+folderNameOfSerieFolders);

			for(int i=0;i<folderOfSerieFolders.length;i++){
				File serieFolder = folderOfSerieFolders[i];
				if(!serieFolder.isDirectory())
					continue;
				File csvFile = new File (serieFolder.getPath()+"/"+csvFName);

				if(!csvFile.exists())
					continue;


				String serieName = serieFolder.getName();
				if(serieNames!=null && serieNames.length>0 ){
					if(Arrays.binarySearch(serieNames, serieName)<0)
						continue;
				}

				System.out.println("csvFile "+csvFile);

				serie2XVal2YVal.put(serieName, new TreeMap<Double, ArrayList<Double>>());
				BufferedReader reader = new BufferedReader(new FileReader(serieFolder.getPath()+"/"+csvFName));
				String line = reader.readLine(); // we pass the culumn names
				line = reader.readLine();
				while (line != null) {
					String [] raw = line.split(",");

//					System.out.println(" x "+raw[xAtt]+" y "+raw[yAtt] );

					if(idxLBAtt !=-1){
//						System.out.println("ckeck "+Double.parseDouble(raw[idxLBAtt]));
						if (valLB > Double.parseDouble(raw[idxLBAtt])){
							Double xVal = Double.parseDouble(raw[xAtt]);
							serie2XVal2YVal.get(serieName).remove(xVal);
//							System.out.println(" CUT xVal: "+xVal+" ");
							break;
						}
					}

					if(idxUBAtt !=-1){
						System.out.println("ckeck "+Double.parseDouble(raw[idxUBAtt]));
						if (valUB < Double.parseDouble(raw[idxUBAtt])){
							Double xVal = Double.parseDouble(raw[xAtt]);
							serie2XVal2YVal.get(serieName).remove(xVal);
							System.out.println(" CUT xVal: "+xVal+" ");
							break;
						}
					}



					Double xVal = Double.parseDouble(raw[xAtt]);
					Double yVal = Double.parseDouble(raw[yAtt]);
					Stats.addToValsDistrib(serie2XVal2YVal.get(serieName), xVal, yVal);

					line = reader.readLine();
				}
				reader.close();

			}
			return serie2XVal2YVal;
	}

	public static  TreeMap<String,TreeMap<Double,ArrayList<Double>>> getSerie2XVal2YVals(
			String folderNameOfSerieFolders
			, String csvFName
			,int xAtt
			,String [] yAttNames
			,int [] yAttOffsets
			, String ... serieNames) throws IOException{


		if(serieNames!=null && serieNames.length>0 )
			Arrays.sort(serieNames);

		TreeMap<String,TreeMap<Double,ArrayList<Double>>> serie2XVal2YVal
		= new TreeMap<String,TreeMap<Double,ArrayList<Double>>>();

		File [] folderOfSerieFolders = (new File(folderNameOfSerieFolders)).listFiles();
//		System.out.println(folderNameOfSerieFolders);
		for(int i=0;i<folderOfSerieFolders.length;i++){
			File serieFolder = folderOfSerieFolders[i];
			if(!serieFolder.isDirectory())
				continue;
			File csvFile = new File (serieFolder.getPath()+"/"+csvFName);
			if(!csvFile.exists())
				continue;

//			System.out.println(csvFile);

			String serieName = serieFolder.getName();

			if(serieNames!=null && serieNames.length>0 ){
				if(Arrays.binarySearch(serieNames, serieName)<0)
					continue;
			}


			for(int o=0;o<yAttOffsets.length;o++){
				serie2XVal2YVal.put(serieName+yAttNames[o], new TreeMap<Double, ArrayList<Double>>());
			}

			BufferedReader reader = new BufferedReader(new FileReader(serieFolder.getPath()+"/"+csvFName));
			String line = reader.readLine(); // we pass the culumn names
			line = reader.readLine();
			while (line != null) {
				String [] raw = line.split(",");

//				System.out.println(" x "+raw[xAtt]+" y "+raw[yAtt] );
				for(int o=0;o<yAttOffsets.length;o++){
					int yAtt= yAttOffsets[o];
					Double xVal = Double.parseDouble(raw[xAtt]);
					Double yVal = Double.parseDouble(raw[yAtt]);
					Stats.addToValsDistrib(serie2XVal2YVal.get(serieName+yAttNames[o]), xVal, yVal);

				}

				line = reader.readLine();
			}
			reader.close();

		}
		return serie2XVal2YVal;
	}

	/**
	 * @param serieCSVFNames
	 * @param xAttOffset
	 * @param yAttNames
	 * @param yAttOffsets
	 * @param serieNames
	 * @return
	 * @throws IOException
	 */
	public static  TreeMap<String,TreeMap<Double,ArrayList<Double>>> getSerie2XVal2YVals(
			String [] serieCSVFNames
			,int xAttOffset
			,String [] yAttNames
			,int [] yAttOffsets
			, String ... serieNames) throws IOException{

		if(serieNames!=null && serieNames.length>0 )
			Arrays.sort(serieNames);

		TreeMap<String,TreeMap<Double,ArrayList<Double>>> serie2XVal2YVal
		= new TreeMap<String,TreeMap<Double,ArrayList<Double>>>();


//		System.out.println(folderNameOfSerieFolders);
		for(int i=0;i<serieCSVFNames.length;i++){

//			System.out.println("serieCSVFNames[i]" +serieCSVFNames[i]);

			File csvFile = new File (serieCSVFNames[i]);
			if(!csvFile.exists())
				continue;


			System.out.println("csvFile "+csvFile);

			String serieName = serieNames[i];


			for(int o=0;o<yAttNames.length;o++){
				serie2XVal2YVal.put(serieName+yAttNames[o], new TreeMap<Double, ArrayList<Double>>());
			}

			BufferedReader reader = new BufferedReader(new FileReader(csvFile));
			String line = reader.readLine(); // we pass the column names

//			System.out.println("\n\nheadline "+line);
//			for(int o=0;o<yAttOffsets.length;o++){
//				System.out.println("yAttOffset "+yAttOffsets[o]+" name "+yAttNames[o]);
//			}


			line = reader.readLine();
			while (line != null) {
				String [] raw = line.split(",");

//				System.out.println("line "+line);



				for(int o=0;o<yAttOffsets.length;o++){

//					System.out.println(" x "+raw[xAttOffset]+" y "+ raw[yAttOffsets[o]] );

					if(idxLBAtt !=-1){
//						System.out.println("ckeck "+Double.parseDouble(raw[idxLBAtt]));
						if (valLB > Double.parseDouble(raw[idxLBAtt])){
							Double xVal = Double.parseDouble(raw[xAttOffset]);
							serie2XVal2YVal.get(serieName+yAttNames[o]).remove(xVal);
//							System.out.println(" CUT xVal: "+xVal+" ");
							break;
						}
					}

					if(idxUBAtt !=-1){
//						System.out.println("ckeck "+Double.parseDouble(raw[idxUBAtt]));
						if (valUB < Double.parseDouble(raw[idxUBAtt])){
							Double xVal = Double.parseDouble(raw[xAttOffset]);
							serie2XVal2YVal.get(serieName+yAttNames[o]).remove(xVal);
//							System.out.println(" CUT xVal: "+xVal+" ");
							break;
						}
					}



					int yAtt= yAttOffsets[o];
					Double xVal = Double.parseDouble(raw[xAttOffset]);
					Double yVal = Double.parseDouble(raw[yAtt]);
					Stats.addToValsDistrib(serie2XVal2YVal.get(serieName+yAttNames[o]), xVal, yVal);

				}

				line = reader.readLine();
			}
			reader.close();
		}
		return serie2XVal2YVal;
	}



	/**
	 * @param serie2xVal2yVals
	 * @param seriesName
	 * @param xName
	 * @param xUnit
	 * @param xCoeff
	 * @param xModuloPattern
	 * @param yName
	 * @param yUnit
	 * @param yCoeff
	 * @param yMaxVal
	 * @param sm
	 * @param pmPlotDayIdRegionFolderName
	 * @param info
	 * @param style
	 * @throws IOException
	 */
	public static void plotLinePointsSeries(
			TreeMap<String, TreeMap<Double, ArrayList<Double>>> serie2xVal2yVals
			, String seriesName
			, String xName, String xUnit, double xCoeff, Integer xModuloPattern
			, String yName, String yUnit, double yCoeff
			, Double yMaxVal, Stats.Metric sm, String pmPlotDayIdRegionFolderName
			, String info, String style
			) throws IOException{

		int i=0;
		StringBuffer xValVSyMetricPerSerieTxt = new StringBuffer(xName+",");
		TreeSet<Double> xValues = new TreeSet<Double>();

		for(String serieName: serie2xVal2yVals.keySet()){
			xValVSyMetricPerSerieTxt.append(serieName
				+((i==serie2xVal2yVals.size()-1)?"\n":",")
					);
			xValues.addAll(serie2xVal2yVals.get(serieName).keySet());
			i++;
		}

		// TODO remove
//		double maxXvalue =0;
//		for(Double xVal: xValues ){
//			if(maxXvalue<xVal)
//				maxXvalue = xVal;
//		}
//		if(maxXvalue<1400){
//			xValues.add(maxXvalue+xModuloPattern);
//		}



		if(xModuloPattern!= null){
			ArrayList<Double> nonXModulo =new ArrayList<Double>();
			for(Double xVal: xValues ){
				if(xVal%xModuloPattern !=0){
					nonXModulo.add(xVal);
				}
			}
			xValues.removeAll(nonXModulo);
		}




		TreeMap<String, TreeMap<Double, Double>> serie2xVal2metricY = Stats.series2X2metricY(serie2xVal2yVals,sm);

//		System.out.println("serie2xVal2metricY: "+serie2xVal2metricY);

		ArrayList<String> mtNullBefore = new ArrayList<String>();
		for(double xVal :  xValues){
			xValVSyMetricPerSerieTxt.append(""+(xVal*xCoeff)+",");

			i=0;
			for(String mtName :serie2xVal2metricY.keySet()){

				String val = "";
				if(serie2xVal2metricY.get(mtName).containsKey(xVal)){
					val = val+ serie2xVal2metricY.get(mtName).get(xVal)*yCoeff;
				}else{
					if(!mtNullBefore.contains(mtName)){
						if(yMaxVal!=null){
							val = val+ yMaxVal;
						}
						mtNullBefore.add(mtName);

					}
				}


				xValVSyMetricPerSerieTxt.append(val
						+((i==serie2xVal2metricY.size()-1)?"\n":",")
						);

				i++;
			}
		}



//		System.out.println(xValVSyMetricPerSerieTxt);

		;
		String comparaison = xName+"_vs_"+sm+yName;
		String plotsFolderName =pmPlotDayIdRegionFolderName+"plots/"+( info!=null?info+"/":"")+comparaison+"_per_"+seriesName+"/";
		FileManager.createFolderIfNeeded(plotsFolderName);
		String xVSmetricYPerSerieCSV = plotsFolderName+"data.csv";
		FileManager.writeInFile(xValVSyMetricPerSerieTxt.toString()
				, xVSmetricYPerSerieCSV
				, false);

		try {
			LinePointsPlotter plotter = new LinePointsPlotter(xVSmetricYPerSerieCSV, plotsFolderName);
			plotter.setKeyLocation(keyLocation);
			plotter.plot(comparaison, xName+" "+xUnit, yName+" "+yUnit, style); // style points lines linespoints

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	public static void plotCandleStickSerie(
			 String serieName
			, TreeMap<Double,ArrayList<Double>> xVal2Quartiles
			, String xName, String xUnit, double xCoeff
			, String yName, String yUnit, double yCoeff
			, String pmPlotDayIdRegionFolderName) throws IOException{

		String plotName = serieName+"_"+xName+"_vs_"+yName+"Quartiles";
		String regionPlotsFolderName =pmPlotDayIdRegionFolderName+"/"+plotName+"/";
		FileManager.createFolderIfNeeded(regionPlotsFolderName);

		try {
			CandleSticksPlotter candlePlotter = new CandleSticksPlotter( regionPlotsFolderName);
			candlePlotter.addUnikCandleSerie(xName, serieName, xVal2Quartiles, xCoeff, yCoeff, plotName);
			candlePlotter.plot(plotName,  xName+" "+xUnit,  yName+" "+yUnit);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void plotCandleStickSeries(
			 TreeMap<String,TreeMap<Double,ArrayList<Double>>> serie2XVal2YQuartiles
			, String xName, String xUnit, double xCoeff
			, String yName, String yUnit, double yCoeff
			, String pmPlotDayIdRegionFolderName) throws IOException{



		String regionPlotsFolderName =pmPlotDayIdRegionFolderName+"/series/";
		FileManager.createFolderIfNeeded(regionPlotsFolderName);

		try {
			CandleSticksPlotter candlePlotter = new CandleSticksPlotter( regionPlotsFolderName);
			candlePlotter.addCandlesSeries(xName, yName, serie2XVal2YQuartiles, xCoeff, yCoeff);
			String plotName = "series_"+xName+"_vs_"+yName+"Quartiles";
			candlePlotter.plot(plotName,  xName+" "+xUnit,  yName+" "+yUnit);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}






}
