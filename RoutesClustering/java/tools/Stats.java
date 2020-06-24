package tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

public class Stats {

	public static enum Metric{Mean, Q1, Q2, Q3};

	public static enum Metrics{Distrib, Quartiles};



	public static <E extends Number> void addToNbOccDistrib(TreeMap<E,Integer> tm, E key){
		if(!tm.containsKey(key)){
			tm.put(key, 1);
		}else{
			tm.put(key,tm.get(key)+1);
		}
	}

	public static <E,F extends Number> void addToValsDistrib(TreeMap<E,ArrayList<F>> tm, E key, F val){
		if(!tm.containsKey(key)){
			tm.put(key, new ArrayList<F>());
		}
		tm.get(key).add(val);
	}

	public static <E extends Number> E getSupportVal(TreeMap<E,Integer> distrib, double support){

		int nbToVisit =0;
		for(E elmt :distrib.keySet()){
			nbToVisit += distrib.get(elmt);
	    }

		int nbVisited = 0;
	    for(E elmt :distrib.keySet()){
	    	nbVisited += distrib.get(elmt);
	    	double prctRecord = (double)nbVisited/(double)nbToVisit;
	    	if(prctRecord>=support){
	    		return elmt;
	    	}
	    }
	    return null;
	}

	public static <E extends Number> Double getOutlierUB(TreeMap<E,Integer> distrib){

		E q1 = null;
		E q3 = null;

		int nbToVisit =0;
		for(E elmt :distrib.keySet()){
			nbToVisit += distrib.get(elmt);
	    }

		int nbVisited = 0;
	    for(E elmt :distrib.keySet()){
	    	nbVisited += distrib.get(elmt);
	    	double prctRecord = (double)nbVisited/(double)nbToVisit;
	    	if(prctRecord>=0.25 && q1 ==null){
	    		q1 = elmt;
	    	}

	    	if(prctRecord>=0.75 && q3 == null){
	    		q3= elmt;
	    		break;
	    	}
	    }

	    if(q1==null || q3==null)
	    	return null;
	    return q3.doubleValue()+((q3.doubleValue()- q1.doubleValue())*1.5);
	}

	public static <E extends Number> Double getOutlierLB(TreeMap<E,Integer> distrib){

		E q1 = null;
		E q3 = null;

		int nbToVisit =0;
		for(E elmt :distrib.keySet()){
			nbToVisit += distrib.get(elmt);
	    }

		int nbVisited = 0;
	    for(E elmt :distrib.keySet()){
	    	nbVisited += distrib.get(elmt);
	    	double prctRecord = (double)nbVisited/(double)nbToVisit;
	    	if(prctRecord>=0.25 && q1 ==null){
	    		q1 = elmt;
	    	}

	    	if(prctRecord>=0.75 && q3 == null){
	    		q3= elmt;
	    		break;
	    	}
	    }

	    if(q1==null || q3==null)
	    	return null;
	    return q1.doubleValue()-((q3.doubleValue()- q1.doubleValue())*1.5);
	}

	public static <E extends Number> Number getQuartil(TreeMap<E,Integer> distrib, int q){

		if(q<0||q>4)
			return null;

		if(q==0)
			return distrib.get(distrib.firstKey());

		if(q==4)
			return distrib.get(distrib.firstKey());

		E valQ = null;


		int nbToVisit =0;
		for(E elmt :distrib.keySet()){
			nbToVisit += distrib.get(elmt);
	    }

		int nbVisited = 0;
	    for(E elmt :distrib.keySet()){
	    	nbVisited += distrib.get(elmt);
	    	double prctRecord = (double)nbVisited/(double)nbToVisit;
	    	if(prctRecord>=(0.25*q) && valQ ==null){
	    		valQ = elmt;
	    		break;
	    	}
	    }

	    if(valQ==null )
	    	return null;

	    return valQ;
	}

	public static void removeOutliers(TreeMap<Double, ArrayList<Double>> x2y){

		Double q1 = null;
		Double q3 = null;

		int nbToVisit =x2y.size();

		ArrayList<Double> toRemove = new ArrayList<Double>();
		int nbVisited = 0;
	    for(Double elmt :x2y.keySet()){
	    	nbVisited ++;
	    	double prctRecord = (double)nbVisited/(double)nbToVisit;

	    	if(prctRecord>=0.25 && q1 ==null){
	    		q1 = elmt;
	    	}

	    	if(prctRecord>=0.75 && q3 == null){
	    		q3= elmt;
	    	}

	    	if( q1==null || q3 !=null )
	    		toRemove.add(elmt);

	    }

	    for(Double elmt:toRemove)
	    	x2y.remove(elmt);


	}





	public static <E,F extends Number> TreeMap<String,TreeMap<E,Double>> series2X2metricY (TreeMap<String,TreeMap<E,ArrayList<F>>> serie2Key2Values, Metric sm){
		TreeMap<String,TreeMap<E,Double>> serie2Key2metricY= new TreeMap<String,TreeMap<E, Double>>();
		for(String serie : serie2Key2Values.keySet()){
			if(sm.equals(Metric.Mean))
				serie2Key2metricY.put(serie, meanPerKey(serie2Key2Values.get(serie)));
			if(sm.equals(Metric.Q1)|| sm.equals(Metric.Q2) || sm.equals(Metric.Q3))
				serie2Key2metricY.put(serie, quantilPerKey(serie2Key2Values.get(serie),sm));

		}
		return serie2Key2metricY;
	}

	public static <E,F extends Number> TreeMap<E,Double> meanPerKey (TreeMap<E,ArrayList<F>> key2Values){
		TreeMap<E,Double> meanPerKey = new TreeMap<E, Double>();
		for(E key : key2Values.keySet()){

			if(key2Values.get(key).size()==1){
				meanPerKey.put(key,(Double)key2Values.get(key).get(0));
			}else{
				double mean = 0;
				for(F val:key2Values.get(key)){
					mean +=  val.doubleValue();
				}
				meanPerKey.put(key,  (mean/key2Values.get(key).size()));
			}
		}
		return meanPerKey;
	}

	public static <E,F extends Number> TreeMap<E,Double> quantilPerKey (TreeMap<E,ArrayList<F>> tm, Metric quartil){
		int quantil =2;
		switch(quartil){
		case Q1 : quantil=1;break;
		case Q2 : quantil=2;break;
		case Q3 : quantil=3;break;
		default : quantil=2;break;
		}

		TreeMap<E,Double> key2Quartile = new TreeMap<E, Double>();
		for(E key : tm.keySet()){
			ArrayList<Double> sortedVals = new ArrayList<Double>();
			for(F val:tm.get(key)){
				sortedVals.add( val.doubleValue());
			}
			if(!sortedVals.isEmpty()){
				Collections.sort(sortedVals);
				key2Quartile.put(key, sortedVals.get((int)((double)sortedVals.size()*(quantil/4.0))));
			}
		}
		return key2Quartile;
	}


	public static <E,F extends Number> TreeMap<E,ArrayList<Long>> getSortedValuesPerKey (TreeMap<E,ArrayList<F>> tm){

		TreeMap<E,ArrayList<Long>> key2SortedValues = new TreeMap<E, ArrayList<Long>>();
		for(E key : tm.keySet()){
			ArrayList<Long> sortedVals = new ArrayList<Long>();
			for(F val:tm.get(key)){
				sortedVals.add( val.longValue());
			}
			if(!sortedVals.isEmpty()){
				Collections.sort(sortedVals);
			}
			key2SortedValues.put(key, sortedVals);
		}
		return key2SortedValues;
	}

	public static <E,F extends Number> TreeMap<String,TreeMap<E,ArrayList<Double>>> series2X2QuartilesY (TreeMap<String,TreeMap<E,ArrayList<F>>> serie2Key2Values){
		TreeMap<String,TreeMap<E,ArrayList<Double>>> serie2Key2QuartilesY= new TreeMap<String,TreeMap<E, ArrayList<Double>>>();
		for(String serie : serie2Key2Values.keySet()){
			serie2Key2QuartilesY.put(serie, getQuartilesPerKey(serie2Key2Values.get(serie)));
		}
		return serie2Key2QuartilesY;
	}


	public static <E,F extends Number> TreeMap<E,ArrayList<Double>> getQuartilesPerKey (TreeMap<E,ArrayList<F>> tm){

		TreeMap<E,ArrayList<Double>> key2Quartiles = new TreeMap<E, ArrayList<Double>>();
		for(E key : tm.keySet()){
			ArrayList<Double> sortedVals = new ArrayList<Double>();
			for(F val:tm.get(key)){
				sortedVals.add( val.doubleValue());
			}
			if(!sortedVals.isEmpty()){
				Collections.sort(sortedVals);
				key2Quartiles.put(key, new ArrayList<Double>());
				key2Quartiles.get(key).add(sortedVals.get(0));
				key2Quartiles.get(key).add(sortedVals.get((int)((double)sortedVals.size()*(1/4.0))));
				key2Quartiles.get(key).add(sortedVals.get((int)((double)sortedVals.size()*(2/4.0))));
				key2Quartiles.get(key).add(sortedVals.get((int)((double)sortedVals.size()*(3/4.0))));
				key2Quartiles.get(key).add(sortedVals.get(sortedVals.size()-1));
			}
		}
		return key2Quartiles;
	}


	public static double meanSquaredError( double m, double b, String valDistribFileName) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(valDistribFileName));

		double sumSquaredDev=0.0;
		int nbValues =0;
		String line = br.readLine();
		while(line!= null){
			nbValues++;
			String [] raw = line.split(",");
			double x= Double.parseDouble(raw[0]);
			double y= Double.parseDouble(raw[1]);
			double yPredict = m*x+b;

			sumSquaredDev += (yPredict-y)*(yPredict-y);

			line = br.readLine();
		}
		br.close();

		return (sumSquaredDev/(double)nbValues);

	}

	public static double standardDeviation( double m, double b, String valDistribFileName) throws IOException{
		return Math.sqrt( meanSquaredError(  m,  b, valDistribFileName));
	}

	public static double pearsonCorelationCoeff(String valDistribFileName) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(valDistribFileName));

		double Ex=0.0;
		double Exx =0.0;

		double Ey=0.0;
		double Eyy =0.0;

		double Exy=0.0;

		int nbValues =0;
		String line = br.readLine();
		while(line!= null){
			nbValues++;
			String [] raw = line.split(",");
			double x= Double.parseDouble(raw[0]);
			double y= Double.parseDouble(raw[1]);

			Ex +=x;
			Exx += x*x;

			Ey +=y;
			Eyy += y*y;

			Exy+=x*y;

			line = br.readLine();
		}
		br.close();

		Ex = Ex/(double)nbValues;
		Exx = Exx/(double)nbValues;

		Ey = Ey/(double)nbValues;
		Eyy = Eyy/(double)nbValues;

		Exy =Exy/(double)nbValues;

		return ( (Exy-(Ex*Ey))/(Math.sqrt(Exx-(Ex*Ex))*Math.sqrt(Eyy-(Ey*Ey))) );
	}




}
