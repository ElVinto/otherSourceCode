package main;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.jar.*;

import specificException.InvalidArgumentException;

public class ArgsHandler {
        
        /* ****************************************
         *  Handling args or an array of String  
         ******************************************/ 
        
        public static String Array2String(ArrayList<String> a){
                String result = a.toString();
                result = result.replace(Character.toString('['),"");
                result = result.replace(Character.toString(']'),"");
                result = result.replace(",","");
                return result;
        }
        
        
        public static boolean tabContainsVal(String[] tab, String val){
        	if(tab == null)
    			return false;
                for(String elmt:tab){
                        if(val.equals(elmt))
                                return true;
                }
                return false;
        }
        
        public static boolean tabContainsExpr(String [] tab, String expr){
        		if(tab == null)
        			return false;
                for(String elmt:tab){
                        if(elmt.contains(expr))
                                return true;
                }
                return false;
        }
        
        public static int indexOf( String val,String [] tab){
                for(int i=0;i< tab.length;i++){
                        if(tab[i].equals(val))
                                return i;
                }
                return -1;
        }
        
        public static String paramFrom(String []args,int i)throws InvalidArgumentException{
        	String result = "";
                if(i<args.length && !args[i].contains("-")){
                        result = args[i];
                }else{
                        throw new InvalidArgumentException(" Invalid cmd line");
                }
                return result;
        }
        
        public static String paramFrom(String []args,String paramName)throws InvalidArgumentException{
        	String result = "";
        	int iparamName = ArgsHandler.indexOf("-"+paramName,args);
    		if(iparamName != -1){
    			result = paramFrom(args,iparamName+1);
    		}
        	return result;
        }
        
        public static void setDefaultParamTo(String paramName, Object o, String[] args,
        		HashMap<String,Object> params)throws Exception{
        	if(ArgsHandler.tabContainsExpr(args, paramName))
        		params.put(paramName,ArgsHandler.paramFrom(args,paramName));
        	else
        		params.put(paramName,o);
        }
        
        public static void setDefaultFileNameTo(String paramName, String f, String[] args,
        		HashMap<String,Object> params	)throws Exception{
        	if(ArgsHandler.tabContainsExpr(args, paramName))
        		params.put(paramName,(new File((String)ArgsHandler.paramFrom(args,paramName))).getPath());
        	else
        		params.put(paramName,f);
         }
        
        public static void setDefaultDblTo(String paramName, double d, String[] args,
        		HashMap<String,Object> params	)throws Exception{
        	if(ArgsHandler.tabContainsExpr(args, paramName))
        		params.put(paramName,Double.valueOf((String)ArgsHandler.paramFrom(args,paramName)));
        	else
        		params.put(paramName,d);
         }
        
        public static  ArrayList<Object> intervalDbleFrom(String paramName,String [] args ){
        	ArrayList<Object> result = new ArrayList<Object>();
        	int iparamName = ArgsHandler.indexOf("-"+paramName,args);
    		if(iparamName != -1){
    			ArrayList<String> intervalParams = paramsFrom(args,iparamName+1);
   
    			BigDecimal start = new BigDecimal((String)intervalParams.get(0));
    			BigDecimal end = new BigDecimal((String)intervalParams.get(1));
    			BigDecimal step = new BigDecimal((String)intervalParams.get(2));
    			while(start.compareTo(end)<=0){
    				result.add(Double.valueOf(start.toString()));
    				start =start.add(step);
    			}
    		}
        	return result;
        }
        
        public static void setDefaultIntervalDbleTo(String paramName, ArrayList<Object> interval,
        		String[] args, HashMap<String,ArrayList<Object>> paramsList) throws Exception{
        	if(ArgsHandler.tabContainsExpr(args, paramName))
        		paramsList.put(paramName,ArgsHandler.intervalDbleFrom(paramName,args));
        	else{
        		
        		ArrayList<Object> result = new ArrayList<Object>();
        		BigDecimal start = new BigDecimal(interval.get(0).toString());
    			BigDecimal end = new BigDecimal(interval.get(1).toString());
    			BigDecimal step = new BigDecimal(interval.get(2).toString());
    			while(start.compareTo(end)<=0){
    				result.add(Double.valueOf(start.toString()));
    				start =start.add(step);
    			}
        		paramsList.put(paramName,result);
        	}
        }
        
        public static  ArrayList<Object> intervalIntFrom(String paramName,String [] args ){
        	ArrayList<Object> result = new ArrayList<Object>();
        	int iparamName = ArgsHandler.indexOf("-"+paramName,args);
    		if(iparamName != -1){
    			ArrayList<String> intervalParams = paramsFrom(args,iparamName+1);
    
        		int start = Integer.valueOf(intervalParams.get(0).toString());
    			int end = Integer.valueOf(intervalParams.get(1).toString());
    			int step = Integer.valueOf(intervalParams.get(2).toString());
    			while(start-end<=0){
    				result.add(Integer.valueOf(start));
    				start =start+step;
    			}
 
    		}
        	return result;
        }
        
        public static void setDefaultIntervalIntTo(String paramName, ArrayList<Object> interval,
        		String[] args, HashMap<String,ArrayList<Object>> paramsList) throws Exception{
        	if(ArgsHandler.tabContainsExpr(args, paramName))
        		paramsList.put(paramName,ArgsHandler.intervalDbleFrom(paramName,args));
        	else{
        		
        		ArrayList<Object> result = new ArrayList<Object>();
        		int start = Integer.valueOf(interval.get(0).toString());
    			int end = Integer.valueOf(interval.get(1).toString());
    			int step = Integer.valueOf(interval.get(2).toString());
    			while(start-end<=0){
    				result.add(Integer.valueOf(start));
    				start =start+step;
    			}
        		paramsList.put(paramName,result);
        	}
        }
        
        
        
        public static void setDefaultLongTo(String paramName, long l, String[] args,
        		HashMap<String,Object> params	)throws Exception{
        	if(ArgsHandler.tabContainsExpr(args, paramName))
        		params.put(paramName,Long.valueOf((String)ArgsHandler.paramFrom(args,paramName)));
        	else
        		params.put(paramName,l);
         }
        
        public static void setDefaultBoolTo(String paramName, boolean b, String[] args,
        		HashMap<String,Object> params	)throws Exception{
        	if(ArgsHandler.tabContainsExpr(args, paramName))
        		params.put(paramName,Boolean.valueOf((String)ArgsHandler.paramFrom(args,paramName)));
        	else
        		params.put(paramName,b);
         }
        
        public static void setDefaultIntTo(String paramName, int i, String[] args,
        		HashMap<String,Object> params	)throws Exception{
        	if(ArgsHandler.tabContainsExpr(args, paramName))
        		params.put(paramName,Integer.valueOf((String)ArgsHandler.paramFrom(args,paramName)));
        	else
        		params.put(paramName,i);
         }
        
        
        public static void setDefaultParamTo(String paramName, Object o, HashMap<String,Object> paramsIn,
        		HashMap<String,Object> params	)throws Exception{
        	if(paramsIn.containsKey(paramName)){
        		params.put(paramName, paramsIn.get(paramName));
        	}else
        		params.put(paramName,o);
        		
        }
        
        
        public static ArrayList<String> paramsFrom(String []args,int i){
                ArrayList<String> result = new ArrayList<String>();
                while(i<args.length && !args[i].contains("-")){
                        result.add(args[i]);
                        i++;
                }
                return result;
        }
        
        public static ArrayList<String> paramsFrom(String [] args, String paramName){
        	ArrayList<String> result = new ArrayList<String>();
        	int iparamName = ArgsHandler.indexOf("-"+paramName,args);
    		if(iparamName != -1){
    			for(String par :paramsFrom(args,iparamName+1))
    				result.add(par) ;
    		}
        	return result;
        }
        
       
        
        public static  ArrayList<Object> intervalFrom(String [] args, String paramName ){
        	ArrayList<Object> result = new ArrayList<Object>();
        	int iparamName = ArgsHandler.indexOf("-"+paramName,args);
    		if(iparamName != -1){
    			ArrayList<String> intervalParams = paramsFrom(args,iparamName+1);
    			
    			BigDecimal start = new BigDecimal((String)intervalParams.get(0));
    			BigDecimal end = new BigDecimal((String)intervalParams.get(1));
    			BigDecimal step = new BigDecimal((String)intervalParams.get(2));
    			while(start.compareTo(end)<=0){
    				result.add(start.toString());
    				start =start.add(step);
    			}
    		}
        	return result;
        }
        
        public static void setDefaultIntervalTo(String paramName, ArrayList<Object> interval,
        		String[] args, HashMap<String,ArrayList<Object>> paramsList) throws Exception{
        	if(ArgsHandler.tabContainsExpr(args, paramName))
        		paramsList.put(paramName,ArgsHandler.intervalFrom(args,paramName));
        	else{
        		ArrayList<Object> result = new ArrayList<Object>();
        		BigDecimal start = new BigDecimal(interval.get(0).toString());
    			BigDecimal end = new BigDecimal(interval.get(1).toString());
    			BigDecimal step = new BigDecimal(interval.get(2).toString());
    			while(start.compareTo(end)<=0){
    				result.add(start.toString());
    				start =start.add(step);
    			}
        		paramsList.put(paramName,result);
        	}
        		
        }
        
        
       public static Integer defaultIntIfAny(String paramName, int val, HashMap<String, Object> params){
        	if(params.containsKey(paramName)){
        		if( params.get(paramName)instanceof String ){
        			return Integer.valueOf((String)params.get(paramName));
        		}
        		return (Integer)params.get(paramName);
        	}
        	return val;
       }
       
       public static Double defaultDblIfAny(String paramName, double val, HashMap<String, Object> params){
       	if(params.containsKey(paramName)){
       		if( params.get(paramName)instanceof String ){
       			return Double.valueOf((String)params.get(paramName));
       		}
       		return (Double)params.get(paramName);
       	}
       	return val;
      }
       
       public static  Long defaultLongIfAny(String paramName, Object val, HashMap<String, Object> params){
    	   if(params.containsKey(paramName)){
          		if( params.get(paramName)instanceof String ){
          			return Long.valueOf((String)params.get(paramName));
          		}
          		return (Long)params.get(paramName);
          	}
          	return (Long) val;
       }
       
       public static  Boolean defaultBoolIfAny(String paramName, Object val, HashMap<String, Object> params){
    	   if(params.containsKey(paramName)){
          		if( params.get(paramName)instanceof Boolean ){
          			return Boolean.valueOf((String)params.get(paramName));
          		}
          		return (Boolean)params.get(paramName);
          	}
          	return (Boolean)val;
       }
      
       public static Object defaultIfAny(String paramName, Object val, HashMap<String, Object> params){
       	if(params.containsKey(paramName)){
       		return params.get(paramName);
       	}
       	return val;
      }
       
       
  
        
}
