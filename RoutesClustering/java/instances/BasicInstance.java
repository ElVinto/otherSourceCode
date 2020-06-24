package instances;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import ilog.concert.IloIntVar;
import it.unimi.dsi.fastutil.ints.Int2CharArrayMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import routing.BasicRoutingInterface;
import routing.SrcDestTimeDistMap;

public class BasicInstance {


	String mapFName = "";
	public BasicRoutingInterface router;

	/*
	 * Mechanics
	 */

	public int nbMechanics=100;
	public int maxNbWorkdays =31;

	/** mech2label.get(int m) -> String label of the mechanic m */
	public Int2ObjectOpenHashMap<String> mech2label ;
	/** mech2locId.get(int m) -> long location id of mechanic m's home */
	public Int2LongOpenHashMap mech2locId ;
	/** mech2homlat.get(int m) -> double latitude gps-location of mechanic m's home */
	public Int2DoubleOpenHashMap mech2homlat ;
	/** mech2homlat.get(int m) -> double longitude gps-location of mechanic m's home */
	public Int2DoubleOpenHashMap mech2homlon ;
	/** mech2workdays.get(int m) -> [int .. int] mechanic m's workdays in the month */
	public Int2ObjectOpenHashMap<IntArrayList> mech2workdays ;
	/** mech2skills.get(int m) -> [int .. int] mechanic m's skils */
	public Int2ObjectOpenHashMap<IntArrayList> mech2skills ;
	/** mech2elevs.get(int m) -> [int .. int] mechanic m's elevators  */
	public Int2ObjectOpenHashMap<IntArrayList> mech2elevs ;


	/*
	 * elevators
	 */

	public int nbElevators=100;
	/** elev2label.get(int e) -> int  elevator e's label*/
	public Int2ObjectOpenHashMap<String> elev2label ;
	/** elev2locId.get(int e) -> long location id of elevator  */
	public Int2LongOpenHashMap elev2locId ;
	/** elev2lat.get(int e) -> double elevator e's latitude */
	public Int2DoubleOpenHashMap elev2lat ;
	/** elev2lon.get(int e) -> double elevator e's longitude */
	public Int2DoubleOpenHashMap elev2lon ;
	/** elev2typ.get(int e) -> int elevator e's type */
	public Int2IntOpenHashMap elev2typ ;
	/** elev2age.get(int e) -> int elevator e's age */
	public Int2IntOpenHashMap elev2age ;
	/** elev2floors.get(int e) -> double elevator e's number of floors */
	public Int2IntOpenHashMap elev2nbFloors ;


	/*
	 * Activities
	 */

	public int nbActivities=100;
	/** activ2label.get(int a) -> int  activity a's label*/
	public Int2ObjectOpenHashMap<String> activ2label ;
	/** activ2elev.get(int a) -> int  elevator associated to the activity a*/
	public Int2IntOpenHashMap activ2elev ;
	/** activ2rls.get(int a) -> int released day of activity a*/
	public Int2IntOpenHashMap activ2rls ;
	/** activ2due.get(int a) -> int due day of activity a*/
	public Int2IntOpenHashMap activ2due ;
	/** activ2dur.get(int a) -> int duration in Minutes of activity a*/
	public Int2IntOpenHashMap activ2dur ;
	/** activ2typ.get(int a) -> int type of activity a*/
	public Int2IntOpenHashMap activ2typ ;
	/** activ2skills.get(int a) -> {int} required skills of activity a*/
	public Int2ObjectOpenHashMap<IntArrayList> activ2skills ;
	public BasicInstance(String fname){
		readBasicInstance(fname);
	}

	public void readBasicInstance(String fname){
		System.out.println("Read instance from: "+fname);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fname));

			String line = reader.readLine();
			while (line != null) {

				if(!line.contains("#")){

					if (line.contains("Instance Params")) {
						parseInstanceParams(reader);
						initStructures();
						line = reader.readLine();
					}


					if (line.contains("Mechanics")) {
						parseMechanics(reader);
						line = reader.readLine();
					}

					if (line.contains("Elevators")) {
						parseElevators(reader);
						line = reader.readLine();
					}

					if (line.contains("Activities")) {
						parseActivities(reader);
					}
				}
				if(line!=null)
					line = reader.readLine();

			}

			reader.close();

			initMap();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void parseInstanceParams(BufferedReader reader) throws IOException{

		String line = reader.readLine(); // pass line InstanceParams
		line = reader.readLine(); // pass empty line

		nbMechanics = Integer.parseInt(line.trim().split("\t")[1]); line = reader.readLine();
		nbElevators =Integer.parseInt(line.trim().split("\t")[1]); line = reader.readLine();
		nbActivities = Integer.parseInt(line.trim().split("\t")[1]); line = reader.readLine();



		mapFName = line.trim().split("\t")[1]; line = reader.readLine();


		System.out.println(
				"nbMechanics:"+nbMechanics
				+ "\t nbElevators:"+nbElevators
				+ "\t nbActivities:"+nbActivities
//				+ "\t mapFName:"+mapFName
				);
	}

	public void initStructures(){

		// Mechanics


		mech2label = new Int2ObjectOpenHashMap<String>(nbMechanics);
		mech2locId = new Int2LongOpenHashMap(nbMechanics);
		mech2homlat = new Int2DoubleOpenHashMap(nbMechanics);
		mech2homlon = new Int2DoubleOpenHashMap(nbMechanics);
		mech2workdays = new Int2ObjectOpenHashMap<IntArrayList>(nbMechanics);
		mech2skills = new Int2ObjectOpenHashMap<IntArrayList>(nbMechanics);
		mech2elevs = new Int2ObjectOpenHashMap<IntArrayList>(nbMechanics);


		// Elevators

		elev2label = new Int2ObjectOpenHashMap<String>(nbElevators);
		elev2locId = new Int2LongOpenHashMap(nbElevators);
		elev2lat = new  Int2DoubleOpenHashMap(nbElevators);
		elev2lon = new Int2DoubleOpenHashMap(nbElevators) ;
		elev2typ = new Int2IntOpenHashMap(nbElevators);
		elev2age = new Int2IntOpenHashMap(nbElevators);
		elev2nbFloors = new Int2IntOpenHashMap(nbElevators);


		// activities

		activ2label = new Int2ObjectOpenHashMap<String>(nbActivities);
		activ2elev = new Int2IntOpenHashMap(nbActivities);
		activ2rls = new Int2IntOpenHashMap(nbActivities);
		activ2due = new Int2IntOpenHashMap(nbActivities);
		activ2dur = new Int2IntOpenHashMap(nbActivities);
		activ2typ = new Int2IntOpenHashMap(nbActivities);
		activ2skills = new Int2ObjectOpenHashMap<IntArrayList>(nbActivities) ;


	}

	private void parseMechanics(BufferedReader reader) throws IOException{
		String line = reader.readLine(); // pass line Mechanics
		line = reader.readLine(); // pass empty line
		while (!line.isEmpty()) {

			int m = Integer.parseInt(line.trim().split("\t")[1]); line = reader.readLine();
			String label =line.trim().split("\t")[1]; line = reader.readLine();
			long locId = Long.parseLong(line.trim().split("\t")[1]); line = reader.readLine();
			double home_lat = Double.parseDouble(line.trim().split("\t")[1]); line = reader.readLine();
			double home_lon = Double.parseDouble(line.trim().split("\t")[1]); line = reader.readLine();

			String []workday_svals =line.trim().split("\t");
			IntArrayList workdays = new IntArrayList(workday_svals.length-1);
			for( String s_val: workday_svals){
				if(!s_val.contains("workdays"))
					workdays.add(Integer.parseInt(s_val));
			}
			line = reader.readLine();

			String []skill_svals =line.trim().split("\t");
			IntArrayList skills = new IntArrayList(skill_svals.length-1);
			for( String s_val: skill_svals){
				if(!s_val.contains("skills"))
					skills.add(Integer.parseInt(s_val));
			}
			line = reader.readLine();

			String []elevs_svals =line.trim().split("\t");
			IntArrayList elevs = new IntArrayList(elevs_svals.length-1);
			for( String s_val: elevs_svals){
				if(!s_val.contains("elevators"))
					elevs.add(Integer.parseInt(s_val));
			}
			line = reader.readLine();

			mech2label.put(m,label);
			mech2locId.put(m, locId);
			mech2homlat.put(m, home_lat);
			mech2homlon.put(m, home_lon);
			mech2workdays.put(m, workdays);
			mech2skills.put(m, skills);
			mech2elevs.put(m, elevs);

			System.out.println(
					"\n m:\t"+m+"\n"
							+" label:\t"+mech2label.get(m)+"\n"
							+" locId:\t"+ mech2locId.get(m)+"\n"
							+" home-lat:\t"+ mech2homlat.get(m)+"\n"
							+" home-lon:\t"+mech2homlon.get(m) +"\n"
							+" wordays:\t"+mech2workdays.get(m)+"\n"
							+" skills:\t"+ mech2skills.get(m)+"\n"
							+" elevators:\t"+ mech2elevs.get(m)+"\n"
					);

			line = reader.readLine(); // pass empty line
		}
		nbMechanics = mech2label.size();
	}

	private void parseElevators(BufferedReader reader) throws IOException{
		String line = reader.readLine(); // pass line Elevators
		line = reader.readLine(); // pass empty line
		while (!line.isEmpty()) {


			int e = Integer.parseInt(line.trim().split("\t")[1]); line = reader.readLine();
			String label =line.trim().split("\t")[1]; line = reader.readLine();
			long locId = Long.parseLong(line.trim().split("\t")[1]); line = reader.readLine();
			double gps_lat = Double.parseDouble(line.trim().split("\t")[1]); line = reader.readLine();
			double gps_lon = Double.parseDouble(line.trim().split("\t")[1]); line = reader.readLine();
			int type = Integer.parseInt(line.trim().split("\t")[1]); line = reader.readLine();
			int age = Integer.parseInt(line.trim().split("\t")[1]); line = reader.readLine();
			int nbFloors = Integer.parseInt(line.trim().split("\t")[1]); line = reader.readLine();


			elev2label.put(e,label);
			elev2locId.put(e, locId);
			elev2lat.put(e, gps_lat);
			elev2lon.put(e, gps_lon);
			elev2typ.put(e, type);
			elev2age.put(e, age);
			elev2nbFloors.put(e, nbFloors);

			System.out.println(
					" e:\t"+e+"\n"
							+" label:\t"+elev2label.get(e)+"\n"
							+" locId:\t"+ elev2locId.get(e)+"\n"
							+" gps-lat:\t"+ elev2lat.get(e)+"\n"
							+" gps-lon:\t"+elev2lon.get(e) +"\n"
							+" type:\t"+elev2typ.get(e)+"\n"
							+" age:\t"+elev2age.get(e)+"\n"
							+" nbFloors:\t"+ elev2nbFloors.get(e)+"\n"
					);

			line = reader.readLine(); // pass empty line
		}
		nbElevators= elev2label.size();
	}

	private void parseActivities(BufferedReader reader) throws IOException{
		String line = reader.readLine(); // pass line Elevators
		line = reader.readLine(); // pass empty line
		while ( line!=null && !line.isEmpty()) {

			int a = Integer.parseInt(line.trim().split("\t")[1]); line = reader.readLine();
			String label =line.trim().split("\t")[1]; line = reader.readLine();
			int e = Integer.parseInt(line.trim().split("\t")[1]); line = reader.readLine();
			int released_day = Integer.parseInt(line.trim().split("\t")[1]); line = reader.readLine();
			int due_day = Integer.parseInt(line.trim().split("\t")[1]); line = reader.readLine();
			int duration = Integer.parseInt(line.trim().split("\t")[1]); line = reader.readLine();


			String []skill_svals =line.trim().split("\t");
			IntArrayList skills = new IntArrayList(skill_svals.length-1);
			for( String s_val: skill_svals){
				if(!s_val.contains("skills"))
					skills.add(Integer.parseInt(s_val));
			}
			line = reader.readLine();

			activ2label.put(a,label);
			activ2elev.put(a, e);
			activ2rls.put(a, released_day);
			activ2due.put(a, due_day);
			activ2dur.put(a, duration);
			activ2skills.put(a, skills);

			System.out.println(
					" a:\t"+a+"\n"
							+" label:\t"+activ2label.get(a)+"\n"
							+" idElev:\t"+ activ2elev.get(a)+"\n"
							+" released_day:\t"+activ2rls.get(a) +"\n"
							+" due_day:\t"+activ2due.get(a)+"\n"
							+" duration:\t"+activ2dur.get(a)+"\n"
							+" skills:\t"+ activ2skills.get(a)+"\n"
					);

			line = reader.readLine(); // pass empty line
		}
		nbActivities = activ2label.size();
	}

	private void initMap() throws IOException{
		ObjectArrayList<String> nodeLabels= new ObjectArrayList<String>(nbElevators+nbActivities); ;
		for(int e :elev2label.keySet()){
			String label = elev2label.get(e);
			if(!nodeLabels.contains(label))
				nodeLabels.add(label);
		}


		for(int m :mech2label.keySet()){
			String label = mech2label.get(m);
			if(!nodeLabels.contains(label))
				nodeLabels.add(label);
		}

		if(mapFName.contains("sdtd")){
			router = new SrcDestTimeDistMap(mapFName, nodeLabels);
//			router.writeMap("./src/main/resources/data/efsi-1-sdtd.csv");
		}
	}

	public int travelTime(int a,int aBis){
		String lab_e =  elev2label.get(activ2elev.get(a));
		String lab_eBis =  elev2label.get(activ2elev.get(aBis));
		int time_a_to_aBis = lab_e.equals(lab_eBis)?0:router.time(lab_e,lab_eBis);
		return time_a_to_aBis;
	}

	public void writeBasicInstance(String fName) throws IOException{
		BufferedWriter writer;
		writer = new BufferedWriter(new FileWriter(fName));


		writer.write("# ElEVATOR FIELD SERVICE INSTANCE DESCRIPTION \n");
		writer.write("#\n");
		writer.write("#\n");
		writer.write("# Comments start with #  \n");
		writer.write("# The character '\\t' is used as a separator between labels and values  \n");
		writer.write("# Meaningfull lines have the following form:  \n");
		writer.write("#     label:'\\t'value or \n");
		writer.write("#     label:'\\t'value1'\\t'...'\\t'\\tvalueN \n");
		writer.write("#\n");
		writer.write("# Intance Params, Mechanics, Elevators and Activities are described as follow:\n");
		writer.write("#\n");
		writer.write("# Instance Params \n");
		writer.write("#\n");
		writer.write("#  nbMechanics:  int                      the number of mechanics \n");
		writer.write("#  nbElevators:  int                      the number of elevators\n");
		writer.write("#  nbActivities: int                      the number of activities \n");
		writer.write("#  mapFName: String                       the map file name \n");
		writer.write("#\n");
		writer.write("# Mechanics \n");
		writer.write("#\n");
		writer.write("#  idMech: int                            the mechanic's id \n");
		writer.write("#  label:  String                         the mechanic's label \n");
		writer.write("#  idLoc:  long                           the mechanic's home location id (road node osmId ) \n");
		writer.write("#  home-lat:  double                      the mechanic's home gps-latitude \n");
		writer.write("#  home-lon:  double                      the mechanic's home gps-longitude \n");
		writer.write("#  workdays:  int ... int                 the mechanics's workdays in the month in [1-31] \n");
		writer.write("#  skills:  int ... int                   the mechanics's skills \n");
		writer.write("#  elevators:  int ... int                the mechanics's elevators \n");
		writer.write("#\n");
		writer.write("# 		... more mechanics ...\n");
		writer.write("#\n");
		writer.write("#\n");
		writer.write("# Elevators\n");
		writer.write("#\n");
		writer.write("#  idElev:  int                           the elevator's id \n");
		writer.write("#  label:  String                         the elevator's label\n");
		writer.write("#  idLoc:  long                           the elevator's location id (road node osmId ) \n");
		writer.write("#  gps-lat:  double                       the elevator's gps-latitude \n");
		writer.write("#  gps-lon:  double                       the elevator's gps-longitude  \n");
		writer.write("#  type:  int                             the elevator's type\n");
		writer.write("#  age:  int                              the elevator's age (in years)\n");
		writer.write("#  nbFloors:  int                         the elevator's number of floors \n");
		writer.write("#\n");
		writer.write("#		... more elevators ...\n");
		writer.write("#\n");
		writer.write("#\n");
		writer.write("# Activities \n");
		writer.write("#\n");
		writer.write("#  idActiv:  int                          the activity's id \n");
		writer.write("#  label:  String                         the activity's label\n");
		writer.write("#  idElev:  int                           an elevator's id\n");
		writer.write("#  release_day:  int                      the activity's release day within the month \n");
		writer.write("#  due_day:  int                          the activity's due day within the month \n");
		writer.write("#  duration:  int                         the activity's duration (in seconds) \n");
		writer.write("#  skills:  int                           the activity's required skills\n");
		writer.write("#\n");
		writer.write("#		... more activities ...\n");
		writer.write("#\n");
		writer.write("#\n");
		writer.write("#   BEGINNING OF THE INSTANCE \n");
		writer.write("#\n");
		writer.write("#\n");



		writer.write("Instance Params\n");
		writer.write(
				"\n nbMechanics:\t"+mech2label.size()+"\n"
						+" nbElevators:\t"+elev2label.size()+"\n"
						+" nbActivities:\t"+activ2label.size()+"\n"
						+" mapFName:\t"+mapFName+"\n"
				);

		writer.write("\n\n");


		writer.write("Mechanics\n\n");
		for(int m: mech2label.keySet()){
			writer.write(
					" idMech:\t"+m+"\n"
							+" label:\t"+mech2label.get(m)+"\n"
							+" locId:\t"+ mech2locId.get(m)+"\n"
							+" home-lat:\t"+ mech2homlat.get(m)+"\n"
							+" home-lon:\t"+mech2homlon.get(m) +"\n"
					);

			writer.write(" workdays:");
			for(int wd : mech2workdays.get(m)){
				writer.write("\t"+wd);
			}
			writer.write("\n");

			writer.write(" skills:");
			for(int s : mech2skills.get(m)){
				writer.write("\t"+s);
			}
			writer.write("\n");

			writer.write(" elevators:");
			for(int s : mech2elevs.get(m)){
				writer.write("\t"+s);
			}
			writer.write("\n");


			writer.write("\n");
		}
		writer.write("\n");


		writer.write("Elevators\n\n");
		for(int e: elev2label.keySet()){
			writer.write(
					" idElev:\t"+e+"\n"
							+" label:\t"+elev2label.get(e)+"\n"
							+" locId:\t"+ elev2locId.get(e)+"\n"
							+" gps-lat:\t"+ elev2lat.get(e)+"\n"
							+" gps-lon:\t"+elev2lon.get(e) +"\n"
							+" type:\t"+elev2typ.get(e)+"\n"
							+" age:\t"+elev2age.get(e)+"\n"
							+" nbFloors:\t"+ elev2nbFloors.get(e)+"\n"
					);

			writer.write("\n");
		}
		writer.write("\n");

		writer.write("Activities\n\n");
		for(int a: activ2label.keySet()){
			writer.write(
					" idActiv:\t"+a+"\n"
							+" label:\t"+activ2label.get(a)+"\n"
							+" idElev:\t"+ activ2elev.get(a)+"\n"
							+" released_day:\t"+activ2rls.get(a) +"\n"
							+" due_day:\t"+activ2due.get(a)+"\n"
							+" duration:\t"+activ2dur.get(a)+"\n"
					);

			writer.write(" skills:");
			for(int s : activ2skills.get(a)){
				writer.write("\t"+s);
			}
			writer.write("\n");

			writer.write("\n");
		}
		writer.write("\n");


		writer.flush();
		writer.close();
	}

	public static void main(String []args){
		String fNameIn = "./src/main/resources/data/efsi-1.txt";
		BasicInstance efsi = new BasicInstance(fNameIn);

		String fNameOut = "./src/main/resources/data/efsi-1Bis.txt";
		try {
			efsi.writeBasicInstance(fNameOut);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}

