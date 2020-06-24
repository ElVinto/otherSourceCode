package tools;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.Externalizable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectSerializer {

	// TODO remove
//	public static TIntObjectHashMap<TIntObjectHashMap<TIntArrayList>> readTIntIntIntArrayList( String location)
//			throws FileNotFoundException, IOException, ClassNotFoundException{
//		ObjectInputStream oi = new ObjectInputStream(new FileInputStream(location));
//		TIntObjectHashMap<TIntObjectHashMap<TIntArrayList>> cspRoadMap = new TIntObjectHashMap<TIntObjectHashMap<TIntArrayList>> ();
//		cspRoadMap.readExternal(oi);
//		oi.close();
//		return cspRoadMap;
//	}
//
//	public static void writeTIntIntIntArrayList(TIntObjectHashMap<TIntObjectHashMap<TIntArrayList>> cspRoadMap, String location) throws FileNotFoundException, IOException{
//		ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(location));
//		cspRoadMap.writeExternal(oo);
//		oo.flush();
//		oo.close();
//	}
//
//	public static void writeTIntIntArrayList(TIntObjectHashMap<TIntArrayList> roadMap, String location) throws FileNotFoundException, IOException{
//		ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(location));
//		roadMap.writeExternal(oo);
//		oo.flush();
//		oo.close();
//	}
//
//	public static TIntObjectHashMap<TIntArrayList> readTIntIntArrayList( String location)
//			throws FileNotFoundException, IOException, ClassNotFoundException{
//		ObjectInputStream oi = new ObjectInputStream(new FileInputStream(location));
//		TIntObjectHashMap<TIntArrayList> roadMap = new TIntObjectHashMap<TIntArrayList> ();
//		roadMap.readExternal(oi);
//		oi.close();
//		return roadMap;
//	}

	public static void writeExternalizable(Externalizable e, String location)
			throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(location));
		e.writeExternal(oo);
		oo.flush();
		oo.close();
		System.out.println("write file "+location);
	}

	public static Externalizable readExternalizable(Externalizable e, String location)
			throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream oi = new ObjectInputStream(new FileInputStream(location));
		e.readExternal(oi);
		oi.close();
		System.out.println("read file "+ location);
		return e;
	}

	public static void writeSerializable(Serializable s, String location)
			throws FileNotFoundException, IOException, ClassNotFoundException{

		FileOutputStream fileOut =   new FileOutputStream(location);
		ObjectOutputStream oo = new ObjectOutputStream(fileOut);
		oo.writeObject(s);
		oo.flush();
		oo.close();
		fileOut.close();
		System.out.println("write file "+location);
	}

	public static Serializable readSerializable( String location)
			throws FileNotFoundException, IOException, ClassNotFoundException{
		FileInputStream fileIn = new FileInputStream(location);
		ObjectInputStream oi = new ObjectInputStream(fileIn);
		Serializable s = (Serializable) oi.readObject();
		oi.close();
		fileIn.close();

		System.out.println("read file "+location);
		return s;
	}

}
