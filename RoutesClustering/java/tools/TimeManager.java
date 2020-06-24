/**
 * @author Vincent Armant, Post-Doctoral Researcher
 * Copyright (c) 2013,2014. The Insight Centre for Data Analytics, University College Cork, Ireland.
 *  All rights reserved.
 */
package tools;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeManager {

	public static final int MS_IN_ONE_SECOND = 1000; // = 60*1000 ms
	public static final int MS_IN_ONE_MINUTE = 60000; // = 60*1000 ms
	public static final int MS_IN_ONE_HOUR = 3600000; // = 60*60*1000 ms
	public static final int MS_IN_ONE_DAY = 86400000; // = 24*60*60*1000 ms
	public static final int MS_IN_ONE_WEEK =  604800000; // = 7*dayInterval ms

	public static long getCpuTimeInNano( ) {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
	    return bean.isCurrentThreadCpuTimeSupported( ) ?
	        bean.getCurrentThreadCpuTime( ) : 0L;
	}

	/** Get JVM CPU time in nanoseconds */
	@SuppressWarnings("restriction")
	public static long getJVMCpuTimeInNano( ) {
	    OperatingSystemMXBean bean =
	        ManagementFactory.getOperatingSystemMXBean( );
	    if ( ! (bean instanceof
	        com.sun.management.OperatingSystemMXBean) )
	        return 0L;
	    return ((com.sun.management.OperatingSystemMXBean)bean)
	        .getProcessCpuTime( );
	}

	public static String dayHourMinSecFromSec(long timeInSec){
		long timeInMilliSec= timeInSec*1000;
		long day =  timeInMilliSec/(24*3600000L);
		long hour = (timeInMilliSec-(day*(24*3600000L)))/3600000L; // 60*60*1000
		long min = (timeInMilliSec-(day*(24*3600000L))-hour*3600000L)/60000L;
		long sec = (timeInMilliSec-(day*(24*3600000L))-(hour*3600000L)-(min*60000L))/1000L;
		return ""
				+((day!=0)?""+day+"days":"")
				+((hour!=0)?""+hour+"h":"")
				+((min!=0)?""+min+"min":"")
				+(sec+"s")
				;
	}

	public static String hourMinSec(int timeInSec){
		int hour = timeInSec/3600; // 60*60
		int min = (timeInSec-hour*3600)/60;
		int sec = (timeInSec-(hour*3600)-(min*60));
		return ""+((hour!=0)?""+hour+"h":"")
				+((min!=0)?""+min+"m":"")
				+(sec+"s")
				;
	}

	public static String hourMinSec(long timeInMilliSec){
		long hour = timeInMilliSec/3600000L; // 60*60*1000
		long min = (timeInMilliSec-hour*3600000L)/60000L;
		long sec = (timeInMilliSec-(hour*3600000L)-(min*60000L))/1000L;
		return ""+((hour!=0)?""+hour+"h":"")
				+((min!=0)?""+min+"m":"")
				+(sec+"s")
				;
	}

	public static String hourMinSecMs(long timeInMilliSec){
		long hour = timeInMilliSec/3600000L; // 60*60*1000
		long min = (timeInMilliSec-hour*3600000L)/60000L;
		long sec = (timeInMilliSec-(hour*3600000L)-(min*60000L))/1000L;
		long ms = (timeInMilliSec-(hour*3600000L)-(min*60000L)-(sec*1000L));
		return ""
				+((hour!=0)?""+hour+"h":"")
				+((min!=0)?""+min+"m":"")
				+((sec!=0)?""+sec+"s":"")
				+ms+"ms"
				;
	}

	public static String hourMinSecMsNano(long timeInNanoSec){
		long hour= (timeInNanoSec/3600000000000L); // 60*60*1000*1000000
		long min = (timeInNanoSec-hour*3600000000000L)/60000000000L;
		long sec = (timeInNanoSec-(hour*3600000000000L)-(min*60000000000L))/1000000000L;
		long ms  = (timeInNanoSec-(hour*3600000000000L)-(min*60000000000L)-(sec*1000000000L))/1000000L;
		long ns  = (timeInNanoSec-(hour*3600000000000L)-(min*60000000000L)-(sec*1000000000L)-(ms*1000000L));
		return ""
				+((hour!=0)?""+hour+"h":"")
				+((min!=0)?""+min+"min":"")
				+((sec!=0)?""+sec+"s":"")
				+((ms!=0)?""+ms+"ms":"")
				+((ns!=0)?""+ns+"ns":"")
				;
	}

	public static double nanoToMilliSec(long timeInNanoSec){
		return timeInNanoSec/1000000.0;
	}

	public static double nanoToMilliSec(double timeInNanoSec){
		return timeInNanoSec/1000000.0;
	}

	public static double nanoToSec(long timeInNanoSec){
		return timeInNanoSec/1000000000.0;
	}

	public static double nanoToSec(double timeInNanoSec){
		return timeInNanoSec/1000000000.0;
	}

	public static double nanoToMin(long timeInNanoSec){
		return timeInNanoSec/60000000000.0;
	}

	public static long hourToMilliSec(double timeInHour){
		return (long)Math.round(timeInHour * 3600000);
	}

	public static long getEpochTimeForDayIdIn2038(int dayId) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE yyyy-MM-dd HH:mm:ss zzz");
	 	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	 	Long aFutureMonday = null;
		try {
			aFutureMonday = sdf.parse("Mon 2038-02-01 00:00:00 UTC").getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		long futureDate = aFutureMonday+ (long)dayId*(long)TimeManager.MS_IN_ONE_DAY;
		return futureDate;

	}

	/**
	* This determines the effective time of a schedule in utc time
	* @param daysOfOperationV2
	* @param scheduleType
	* @param scheduleStartTime (in ms in ? in local time ? )
	* @param timezone
	* @param startMinutes (in local time)
	* @param relativeStartTime The relative start time
	* @return The effective time
	*/
	public static long determineEffectiveTime(boolean[] daysOfOperationV2, String scheduleType, long scheduleStartTime, String timezone, int startMinutes,
	long relativeStartTime) {

		if (scheduleType.equals("ONE_OFF")) {
			if (scheduleStartTime <= 0) {
				throw new IllegalArgumentException("Could not get ONE_OFF effective time " + scheduleStartTime);
			}
			// for one-off trips, simply return the time as is, but remove sec and ms components
			return scheduleStartTime-scheduleStartTime%MS_IN_ONE_MINUTE;
		} else
			if (scheduleType.equals("REPEATING_WEEKLY")) {
				TimeZone tz = TimeZone.getTimeZone(timezone);
				Calendar c = Calendar.getInstance(tz);
				if (relativeStartTime > 0) {
					c.setTime(new Date(relativeStartTime));
				}
				c.set(Calendar.HOUR_OF_DAY, startMinutes / 60);
				c.set(Calendar.MINUTE, startMinutes % 60);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);

				while (c.getTimeInMillis() < relativeStartTime || !daysOfOperationV2[c.get(Calendar.DAY_OF_WEEK) - 1]) {
					c.add(Calendar.DAY_OF_WEEK, 1);
				}

				return c.getTimeInMillis();
			} else {
			throw new IllegalArgumentException("Unrecognized schedule type:  " + scheduleType);
		}
	}



//	% Cum  Allocated resource in seconds
//
//	%LB - >  60139144
//	%FMF_DDUR - > 60611508
//	%FF_DDUR - > 64500971
//	%NF_DDUR - > 64599527
//	%BFD_DDUR - > 68251281
//	%FF_DREQ - > 75393796
//	%FF_LIST - > 75461160
//
//
//	%# Cumulative gap to LB
//
//	%FMF_DDUR -> 472940
//	%FF_DDUR -> 4361989
//	%NF_DDUR -> 4460499
//	%BFD_DDUR -> 8112143
//	%FF_DREQ -> 15254660
//	%FF_LIST -> 15322024

	public static void main(String[] args){
//		long [] vals = new long [] {60139144,60611508,64500971,64599527,68251281,75393796,75461160};
//		for(long val : vals){
//			System.out.println(" val "+ val+" -> "+dayHourMinSecFromSec(val));
//		}

		long [] gaps = new long [] {472940,4361989,4460499,8112143,15254660,15322024};

		for(long gap : gaps){
			System.out.println(" gap "+ gap+" -> "+ (100*gap)/60139144.0);
		}




	}



}
