package clustering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import tools.PlotBox;
import tools.Stats.Metric;
import tools.TimeManager;

public class ClusteringPlots {

	public static String defaultStatClusteringCSV = "statClustering.csv";

	public static enum ClusteringAtts {
		Clusterer,Unit,Limit,NbOfBuildings,NbOfClusters,NbOfSingletonClusters,NbOfViolatingPairs,NbOfMissingPairs,NbOfIncorrectPairs,NbOfCorrectPairs,NbOfPairs

		,NbTruePositive
				,NbFalseNegative
				,NbFalsePositive
				,NbTrueNegative
				,Precision
				,Recall
				,FMeasure

				,MinDiameter
				,Q1Diameter
				,Q2Diameter
				,Q3Diameter
				,MaxDiameter
	};

	public static void plotTimeAnalysis(String folderNameOfSerieFolders, String [] clustererNames, String unit) throws IOException{

		TreeMap<String,TreeMap<Double,ArrayList<Double>>> serie2XVal2YVal = null;

			Integer xValPattern =null;
			Double yMaxVal = null;


			double xCoeef =1.0;
			double yCoeef =1.0;


			String meas = "";
			if(unit.equals("distance")){
				meas = "in kms";
				xCoeef =0.001;
				yCoeef =0.001;
			}


			if(unit.equals("time")){
				meas = "in minutes";
				yCoeef = 1/60.0;

			}


			// Limit vs NbOfIncorrectPairs
			serie2XVal2YVal =
					PlotBox.getSerie2XVal2YVals(folderNameOfSerieFolders
							, defaultStatClusteringCSV
							, ClusteringAtts.Limit.ordinal()
							, ClusteringAtts.NbOfIncorrectPairs.ordinal()
							, clustererNames);

			PlotBox.keyLocation ="graph 0.3, graph 0.9";
			PlotBox.plotLinePointsSeries(
					serie2XVal2YVal
					, "clusterer"
					, ""+ClusteringAtts.Limit, meas, xCoeef, xValPattern
					, ""+ClusteringAtts.NbOfIncorrectPairs, " x1000", 0.001,  yMaxVal
					, Metric.Mean, folderNameOfSerieFolders, unit, "linespoints");
			PlotBox.keyLocation =null;




			// Limit vs Q2Diameter = Mean Diameter
			serie2XVal2YVal =
					PlotBox.getSerie2XVal2YVals(folderNameOfSerieFolders
							, defaultStatClusteringCSV
							, ClusteringAtts.Limit.ordinal()
							, ClusteringAtts.Q2Diameter.ordinal()
							, clustererNames);

			PlotBox.keyLocation ="graph 0.3, graph 0.9";
			PlotBox.plotLinePointsSeries(
					serie2XVal2YVal
					, "clusterer"
					, ""+ClusteringAtts.Limit, meas, xCoeef, xValPattern
					, "MedianDiameter", meas, yCoeef,  yMaxVal
					, Metric.Mean, folderNameOfSerieFolders, unit, "linespoints");
			PlotBox.keyLocation =null;

			// Limit vs Max Diameter
			serie2XVal2YVal =
					PlotBox.getSerie2XVal2YVals(folderNameOfSerieFolders
							, defaultStatClusteringCSV
							, ClusteringAtts.Limit.ordinal()
							, ClusteringAtts.MaxDiameter.ordinal()
							, clustererNames);

			PlotBox.keyLocation ="graph 0.3, graph 0.9";
			PlotBox.plotLinePointsSeries(
					serie2XVal2YVal
					, "clusterer"
					, ""+ClusteringAtts.Limit, meas, xCoeef, xValPattern
					, ""+ClusteringAtts.MaxDiameter, meas, yCoeef,  yMaxVal
					, Metric.Mean, folderNameOfSerieFolders, unit, "linespoints");
			PlotBox.keyLocation =null;


			// Limit vs FMeasure

			serie2XVal2YVal =
					PlotBox.getSerie2XVal2YVals(folderNameOfSerieFolders
							, defaultStatClusteringCSV
							, ClusteringAtts.Limit.ordinal()
							, ClusteringAtts.FMeasure.ordinal()
							, clustererNames);

			PlotBox.keyLocation = "graph 0.8, graph 0.5";
			PlotBox.plotLinePointsSeries(
					serie2XVal2YVal
					, "clusterer"
					, ""+ClusteringAtts.Limit, meas, xCoeef, xValPattern
					, ""+ClusteringAtts.FMeasure, "in percent", 100,  yMaxVal
					, Metric.Mean, folderNameOfSerieFolders, unit, "linespoints");
			PlotBox.keyLocation =null;



			// Limit vs StatMeasure
			for(String cName:clustererNames){
				serie2XVal2YVal =
				PlotBox.getSerie2XVal2YVals(folderNameOfSerieFolders
						, defaultStatClusteringCSV
						, ClusteringAtts.Limit.ordinal()
						, new String[]{ClusteringAtts.Precision.toString(),ClusteringAtts.Recall.toString(),ClusteringAtts.FMeasure.toString()}
						, new int[]{ClusteringAtts.Precision.ordinal(),ClusteringAtts.Recall.ordinal(),ClusteringAtts.FMeasure.ordinal()}
						, cName);

				PlotBox.keyLocation ="graph 0.9, graph 0.2";
				if(cName.contains("CC")){
					PlotBox.keyLocation ="graph 0.9, graph 0.8";
				}
				PlotBox.plotLinePointsSeries(
						serie2XVal2YVal
						, "statMeasure"
						, ""+ClusteringAtts.Limit, meas, xCoeef, xValPattern
						, "StatMeasures"+cName, "in percent", 100,  yMaxVal
						, Metric.Mean, folderNameOfSerieFolders, unit, "linespoints");
				PlotBox.keyLocation = null;
			}




	}

	public static void main(String args[]) throws IOException{

		String [] clustererNamesTime = {"HCDTime","HCRTime","CCTime"};
		ClusteringPlots.plotTimeAnalysis("./src/main/resources/results/stat/", clustererNamesTime,"time");

		String [] clustererNamesDist = {"HCDDist","HCRDist","CCDist"};
		ClusteringPlots.plotTimeAnalysis("./src/main/resources/results/stat/", clustererNamesDist,"distance");

	}



}
