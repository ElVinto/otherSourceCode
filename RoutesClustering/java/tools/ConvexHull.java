package tools;

//**********************************************************************
//
//<copyright>
//
//BBN Technologies, a Verizon Company
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: ConvexHull.java,v $
//$Revision: 1.5 $
//$Date: 2008/04/15 21:51:01 $
//$Author: dietrick $
//
//**********************************************************************

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Enumeration;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;

/**
* This class contains static methods that can be used to create convex hull
* GeoRegions from arrays of Geos. The only algorithm implemented is Graham's,
* where the highest point is selected (called the pivot), the other points are
* sorted according to their relative azimuths from the pivot, and then a path
* is created around the other points. Any right turn encountered traversing the
* points means that point should be skipped when creating the convex hull.
*
* @author dietrick
*/

class Length implements Serializable {

  /** Miles, in WGS 84 spherical earth model units. */
  public final static Length MILE = new Length("mile", "miles", Planet.wgs84_earthEquatorialCircumferenceMiles_D);
  /** Feet, in WGS 84 spherical earth model units. */
  public final static Length FEET = new Length("feet", "ft", Planet.wgs84_earthEquatorialCircumferenceMiles_D * 5280.0);
  /** Feet, in WGS 84 spherical earth model units. */
  public final static Length YARD = new Length("yards", "yd", Planet.wgs84_earthEquatorialCircumferenceMiles_D * 5280.0 / 3.0);
  /** Meters, in WGS 84 Spherical earth model units. */
  public final static Length METER = new Length("meter", "m", Planet.wgs84_earthEquatorialCircumferenceMeters_D);
  /** Kilometers, in WGS 84 Spherical earth model units. */
  public final static Length KM = new Length("kilometer", "km", Planet.wgs84_earthEquatorialCircumferenceKM_D);
  /** Nautical Miles, in WGS 84 Spherical earth model units. */
  public final static Length NM = new Length("nautical mile", "nm", Planet.wgs84_earthEquatorialCircumferenceNMiles_D);
  /** Decimal Degrees, in WGS 84 Spherical earth model units. */
  public final static Length DECIMAL_DEGREE = new Length("decimal degree", "deg", 360.0);
  /** Data Mile, in WGS 84 spherical earth model units. */
  public final static Length DM =
          new Length("datamile", "dm", Planet.wgs84_earthEquatorialCircumferenceMiles_D * 5280.0 / 6000.0);

  /** Radians, in terms of a spherical earth. */
  public final static Length RADIAN = new Length("radian", "rad", MoreMath.TWO_PI_D) {
      public float toRadians(float numUnits) {
          return numUnits;
      }

      public double toRadians(double numUnits) {
          return numUnits;
      }

      public float fromRadians(float numRadians) {
          return numRadians;
      }

      public double fromRadians(double numRadians) {
          return numRadians;
      }
  };

  /** Unit/radians */
  protected final double constant;
  protected final String name;
  protected final String abbr;
  protected double unitEquatorCircumference;
  //protected transient I18n i18n = Environment.getI18n();

  /**
   * Create a Length, with a name an the number of it's units that go around
   * the earth at its equator. The name and abbreviation are converted to
   * lower case for consistency.
   */
  public Length(String name, String abbr, double unitEquatorCircumference) {
	this.name = "";
      //this.name = i18n.get(this, abbr + ".name", name).toLowerCase().intern();
      this.unitEquatorCircumference = unitEquatorCircumference;
      this.constant = unitEquatorCircumference / MoreMath.TWO_PI_D;
      this.abbr = abbr.toLowerCase().intern();
  }

  /**
   * Given a number of units provided by this Length, convert to a number of
   * radians.
   */
  public float toRadians(float numUnits) {
      return numUnits / (float) constant;
  }

  public double toRadians(double numUnits) {
      return numUnits / constant;
  }

  /**
   * Given a number of radians, convert to the number of units represented by
   * this length.
   */
  public float fromRadians(float numRadians) {
      return numRadians * (float) constant;
  }

  /**
   * Given a number of radians, convert to the number of units represented by
   * this length.
   */
  public double fromRadians(double numRadians) {
      return numRadians * constant;
  }

  /**
   * Return the name for this length type.
   */
  public String toString() {
      return name;
  }

  /**
   * Return the abbreviation for this length type.
   */
  public String getAbbr() {
      return abbr;
  }

  /**
   * Get a list of the Lengths currently defined as static implementations of
   * this class.
   */
  public static Length[] getAvailable() {
      return new Length[] {
          METER,
          KM,
          FEET,
          YARD,
          MILE,
          DM,
          NM,
          DECIMAL_DEGREE
      };
  }

  /**
   * Get the Length object with the given name or abbreviation. If nothing
   * exists with that name, then return null. The lower case version of the
   * name or abbreviation is checked against the available options.
   */
  public static Length get(String name) {
      Length[] choices = getAvailable();
      if (name != null) {
          name = name.toLowerCase();
          for (Length choice : choices) {
              if (name.equals(choice.toString()) || name.equals(choice.getAbbr())) {
                  return choice;
              }
          }
      }
      return null;
  }
}

class Planet {

  // Solar system id's. Add new ones as needed.
  final public static transient int Earth = 3;
  final public static transient int Mars = 4;

  // WGS84 / GRS80 datums
  final public static transient float wgs84_earthPolarRadiusMeters = 6356752.3142f;
  final public static transient double wgs84_earthPolarRadiusMeters_D = 6356752.3142;
  final public static transient float wgs84_earthEquatorialRadiusMeters = 6378137.0f;
  final public static transient double wgs84_earthEquatorialRadiusMeters_D = 6378137.0;
  /* 1 - (minor/major) = 1/298.257 */
  final public static transient float wgs84_earthFlat = 1 - (wgs84_earthPolarRadiusMeters / wgs84_earthEquatorialRadiusMeters);
  /* sqrt(2*f - f^2) = 0.081819221f */
  final public static transient float wgs84_earthEccen = (float) Math.sqrt(2
          * wgs84_earthFlat - (wgs84_earthFlat * wgs84_earthFlat));
  final public static transient float wgs84_earthEquatorialCircumferenceMeters = MoreMath.TWO_PI
          * wgs84_earthEquatorialRadiusMeters;
  final public static transient float wgs84_earthEquatorialCircumferenceKM = wgs84_earthEquatorialCircumferenceMeters / 1000f;
  final public static transient float wgs84_earthEquatorialCircumferenceMiles = wgs84_earthEquatorialCircumferenceKM * 0.62137119f;// HACK
  /* 60.0f * 360.0f -- sixty nm per degree units? */
  final public static transient float wgs84_earthEquatorialCircumferenceNMiles = wgs84_earthEquatorialCircumferenceMeters / 1852f;
  final public static transient double wgs84_earthEquatorialCircumferenceMeters_D = MoreMath.TWO_PI_D
          * wgs84_earthEquatorialRadiusMeters_D;
  final public static transient double wgs84_earthEquatorialCircumferenceKM_D = wgs84_earthEquatorialCircumferenceMeters_D / 1000;
  final public static transient double wgs84_earthEquatorialCircumferenceMiles_D = wgs84_earthEquatorialCircumferenceKM_D * 0.62137119;// HACK
  /* 60.0f * 360.0f; sixty nm per degree */// units?
  final public static transient double wgs84_earthEquatorialCircumferenceNMiles_D = wgs84_earthEquatorialCircumferenceMeters / 1852;
  // wgs84_earthEquatorialCircumferenceKM*0.5389892f; // calculated,
  // same as line above.
  // wgs84_earthEquatorialCircumferenceKM*0.5399568f;//HACK use UNIX
  // units? << This was wrong.

  // Mars
  final public static transient float marsEquatorialRadius = 3393400.0f;// meters
  final public static transient float marsEccen = 0.101929f;// eccentricity
  // e
  final public static transient float marsFlat = 0.005208324f;// 1-(1-e^2)^1/2

  // International 1974
  final public static transient float international1974_earthPolarRadiusMeters = 6356911.946f;
  final public static transient float international1974_earthEquatorialRadiusMeters = 6378388f;
  /* 1 - (minor/major) = 1/297 */
  final public static transient float international1974_earthFlat = 1 - (international1974_earthPolarRadiusMeters / international1974_earthEquatorialRadiusMeters);
  /*
   * Extra scale constant for better viewing of maps (do not use
   * this to calculate anything but points to be viewed!) 3384: mattserver/Map.C, 3488: dcw
   */
  public transient static int defaultPixelsPerMeter = 3272;

  // cannot construct
  private Planet() {}
}

class MoreMath {

  /**
   * 2*Math.PI
   */
  final public static transient float TWO_PI = (float) Math.PI * 2.0f;

  /**
   * 2*Math.PI
   */
  final public static transient double TWO_PI_D = Math.PI * 2.0d;

  /**
   * Math.PI/2
   */
  final public static transient float HALF_PI = (float) Math.PI / 2.0f;

  /**
   * Math.PI/2
   */
  final public static transient double HALF_PI_D = Math.PI / 2.0d;

  final public static double EQUIVALENT_TOLERANCE = 0.0000001;

  // cannot construct
  private MoreMath() {}

  /**
   * Checks if a ~= b. Use this to test equality of floating point
   * numbers.
   * <p>
   *
   * @param a double
   * @param b double
   * @param epsilon the allowable error
   * @return boolean
   */
  final public static boolean approximately_equal(double a, double b,
                                                  double epsilon) {
      return (Math.abs(a - b) <= epsilon);
  }

  /**
   * Checks if a ~= b. Use this to test equality of floating point
   * numbers against EQUIVALENT_TOLERANCE.
   * <p>
   *
   * @param a double
   * @param b double
   * @return boolean
   */
  final public static boolean approximately_equal(double a, double b) {
      return (Math.abs(a - b) <= EQUIVALENT_TOLERANCE);
  }

  /**
   * Checks if a ~= b. Use this to test equality of floating point
   * numbers.
   * <p>
   *
   * @param a float
   * @param b float
   * @param epsilon the allowable error
   * @return boolean
   */
  final public static boolean approximately_equal(float a, float b,
                                                  float epsilon) {
      return (Math.abs(a - b) <= epsilon);
  }

  /**
   * Hyperbolic arcsin.
   * <p>
   * Hyperbolic arc sine: log (x+sqrt(1+x^2))
   *
   * @param x float
   * @return float asinh(x)
   */
  public static final float asinh(float x) {
      return (float) Math.log(x + Math.sqrt(x * x + 1));
  }

  /**
   * Hyperbolic arcsin.
   * <p>
   * Hyperbolic arc sine: log (x+sqrt(1+x^2))
   *
   * @param x double
   * @return double asinh(x)
   */
  public static final double asinh(double x) {
      return (double) Math.log(x + Math.sqrt(x * x + 1));
  }

  // HACK - are there functions that already exist?
  /**
   * Return sign of number.
   *
   * @param x short
   * @return int sign -1, 1
   */
  public static final int sign(short x) {
      return (x < 0) ? -1 : 1;
  }

  /**
   * Return sign of number.
   *
   * @param x int
   * @return int sign -1, 1
   */
  public static final int sign(int x) {
      return (x < 0) ? -1 : 1;
  }

  /**
   * Return sign of number.
   *
   * @param x long
   * @return int sign -1, 1
   */
  public static final int sign(long x) {
      return (x < 0) ? -1 : 1;
  }

  /**
   * Return sign of number.
   *
   * @param x float
   * @return int sign -1, 1
   */
  public static final int sign(float x) {
      return (x < 0f) ? -1 : 1;
  }

  /**
   * Return sign of number.
   *
   * @param x double
   * @return int sign -1, 1
   */
  public static final int sign(double x) {
      return (x < 0d) ? -1 : 1;
  }

  /**
   * Check if number is odd.
   *
   * @param x short
   * @return boolean
   */
  public static final boolean odd(short x) {
      return !even(x);
  }

  /**
   * Check if number is odd.
   *
   * @param x int
   * @return boolean
   */
  public static final boolean odd(int x) {
      return !even(x);
  }

  /**
   * Check if number is odd.
   *
   * @param x long
   * @return boolean
   */
  public static final boolean odd(long x) {
      return !even(x);
  }

  /**
   * Check if number is even.
   *
   * @param x short
   * @return boolean
   */
  public static final boolean even(short x) {
      return ((x & 0x1) == 0);
  }

  /**
   * Check if number is even.
   *
   * @param x int
   * @return boolean
   */
  public static final boolean even(int x) {
      return ((x & 0x1) == 0);
  }

  /**
   * Check if number is even.
   *
   * @param x long
   * @return boolean
   */
  public static final boolean even(long x) {
      return ((x & 0x1) == 0);
  }

  /**
   * Converts a byte in the range of -128 to 127 to an int in the
   * range 0 - 255.
   *
   * @param b (-128 &lt;= b &lt;= 127)
   * @return int (0 &lt;= b &lt;= 255)
   */
  public static final int signedToInt(byte b) {
      return ((int) b & 0xff);
  }

  /**
   * Converts a short in the range of -32768 to 32767 to an int in
   * the range 0 - 65535.
   *
   * @param w (-32768 &lt;= b &lt;= 32767)
   * @return int (0 &lt;= b &lt;= 65535)
   */
  public static final int signedToInt(short w) {
      return ((int) w & 0xffff);
  }

  /**
   * Convert an int in the range of -2147483648 to 2147483647 to a
   * long in the range 0 to 4294967295.
   *
   * @param x (-2147483648 &lt;= x &lt;= 2147483647)
   * @return long (0 &lt;= x &lt;= 4294967295)
   */
  public static final long signedToLong(int x) {
      return ((long) x & 0xFFFFFFFFL);
  }

  /**
   * Converts an int in the range of 0 - 65535 to an int in the
   * range of 0 - 255.
   *
   * @param w int (0 &lt;= w &lt;= 65535)
   * @return int (0 &lt;= w &lt;= 255)
   */
  public static final int wordToByte(int w) {
      return w >> 8;
  }

  /**
   * Build short out of bytes (in big endian order).
   *
   * @param bytevec bytes
   * @param offset byte offset
   * @return short
   */
  public static final short BuildShortBE(byte bytevec[], int offset) {
      return (short) (((int) (bytevec[0 + offset]) << 8) | (signedToInt(bytevec[1 + offset])));
  }

  /**
   * Build short out of bytes (in little endian order).
   *
   * @param bytevec bytes
   * @param offset byte offset
   * @return short
   */
  public static final short BuildShortLE(byte bytevec[], int offset) {
      return (short) (((int) (bytevec[1 + offset]) << 8) | (signedToInt(bytevec[0 + offset])));
  }

  /**
   * Build short out of bytes.
   *
   * @param bytevec bytes
   * @param offset byte offset
   * @param MSBFirst BE or LE?
   * @return short
   */
  public static final short BuildShort(byte bytevec[], int offset,
                                       boolean MSBFirst) {
      if (MSBFirst) {
          return (BuildShortBE(bytevec, offset));
      } else {
          return (BuildShortLE(bytevec, offset));
      }
  }

  /**
   * Build short out of bytes (in big endian order).
   *
   * @param bytevec bytes
   * @param MSBFirst BE or LE?
   * @return short
   */

  public static final short BuildShortBE(byte bytevec[], boolean MSBFirst) {
      return BuildShortBE(bytevec, 0);
  }

  /**
   * Build short out of bytes (in little endian order).
   *
   * @param bytevec bytes
   * @param MSBFirst BE or LE?
   * @return short
   */
  public static final short BuildShortLE(byte bytevec[], boolean MSBFirst) {
      return BuildShortLE(bytevec, 0);
  }

  /**
   * Build short out of bytes.
   *
   * @param bytevec bytes
   * @param MSBFirst BE or LE?
   * @return short
   */
  public static final short BuildShort(byte bytevec[], boolean MSBFirst) {
      return BuildShort(bytevec, 0, MSBFirst);
  }

  /**
   * Build int out of bytes (in big endian order).
   *
   * @param bytevec bytes
   * @param offset byte offset
   * @return int
   */
  public static final int BuildIntegerBE(byte bytevec[], int offset) {
      return (((int) (bytevec[0 + offset]) << 24)
              | (signedToInt(bytevec[1 + offset]) << 16)
              | (signedToInt(bytevec[2 + offset]) << 8) | (signedToInt(bytevec[3 + offset])));
  }

  /**
   * Build int out of bytes (in little endian order).
   *
   * @param bytevec bytes
   * @param offset byte offset
   * @return int
   */
  public static final int BuildIntegerLE(byte bytevec[], int offset) {
      return (((int) (bytevec[3 + offset]) << 24)
              | (signedToInt(bytevec[2 + offset]) << 16)
              | (signedToInt(bytevec[1 + offset]) << 8) | (signedToInt(bytevec[0 + offset])));
  }

  /**
   * Build int out of bytes.
   *
   * @param bytevec bytes
   * @param offset byte offset
   * @param MSBFirst BE or LE?
   * @return int
   */
  public static final int BuildInteger(byte bytevec[], int offset,
                                       boolean MSBFirst) {
      if (MSBFirst)
          return BuildIntegerBE(bytevec, offset);
      else
          return BuildIntegerLE(bytevec, offset);
  }

  /**
   * Build int out of bytes (in big endian order).
   *
   * @param bytevec bytes
   * @return int
   */
  public static final int BuildIntegerBE(byte bytevec[]) {
      return BuildIntegerBE(bytevec, 0);
  }

  /**
   * Build int out of bytes (in little endian order).
   *
   * @param bytevec bytes
   * @return int
   */
  public static final int BuildIntegerLE(byte bytevec[]) {
      return BuildIntegerLE(bytevec, 0);
  }

  /**
   * Build int out of bytes.
   *
   * @param bytevec bytes
   * @param MSBFirst BE or LE?
   * @return int
   */
  public static final int BuildInteger(byte bytevec[], boolean MSBFirst) {
      if (MSBFirst)
          return BuildIntegerBE(bytevec, 0);
      else
          return BuildIntegerLE(bytevec, 0);
  }

  /**
   * Build long out of bytes (in big endian order).
   *
   * @param bytevec bytes
   * @param offset byte offset
   * @return long
   */
  public static final long BuildLongBE(byte bytevec[], int offset) {
      return (((long) signedToInt(bytevec[0 + offset]) << 56)
              | ((long) signedToInt(bytevec[1 + offset]) << 48)
              | ((long) signedToInt(bytevec[2 + offset]) << 40)
              | ((long) signedToInt(bytevec[3 + offset]) << 32)
              | ((long) signedToInt(bytevec[4 + offset]) << 24)
              | ((long) signedToInt(bytevec[5 + offset]) << 16)
              | ((long) signedToInt(bytevec[6 + offset]) << 8) | ((long) signedToInt(bytevec[7 + offset])));
  }

  /**
   * Build long out of bytes (in little endian order).
   *
   * @param bytevec bytes
   * @param offset byte offset
   * @return long
   */
  public static final long BuildLongLE(byte bytevec[], int offset) {
      return (((long) signedToInt(bytevec[7 + offset]) << 56)
              | ((long) signedToInt(bytevec[6 + offset]) << 48)
              | ((long) signedToInt(bytevec[5 + offset]) << 40)
              | ((long) signedToInt(bytevec[4 + offset]) << 32)
              | ((long) signedToInt(bytevec[3 + offset]) << 24)
              | ((long) signedToInt(bytevec[2 + offset]) << 16)
              | ((long) signedToInt(bytevec[1 + offset]) << 8) | ((long) signedToInt(bytevec[0 + offset])));
  }

  /**
   * Build long out of bytes.
   *
   * @param bytevec bytes
   * @param offset byte offset
   * @param MSBFirst BE or LE?
   * @return long
   */
  public static final long BuildLong(byte bytevec[], int offset,
                                     boolean MSBFirst) {
      if (MSBFirst)
          return BuildLongBE(bytevec, offset);
      else
          return BuildLongLE(bytevec, offset);
  }

  /**
   * Build long out of bytes (in big endian order).
   *
   * @param bytevec bytes
   * @return long
   */
  public static final long BuildLongBE(byte bytevec[]) {
      return BuildLongBE(bytevec, 0);
  }

  /**
   * Build long out of bytes (in little endian order).
   *
   * @param bytevec bytes
   * @return long
   */
  public static final long BuildLongLE(byte bytevec[]) {
      return BuildLongLE(bytevec, 0);
  }

  /**
   * Build long out of bytes.
   *
   * @param bytevec bytes
   * @param MSBFirst BE or LE?
   * @return long
   */
  public static final long BuildLong(byte bytevec[], boolean MSBFirst) {
      if (MSBFirst)
          return BuildLongBE(bytevec, 0);
      else
          return BuildLongLE(bytevec, 0);
  }

  /*
   * public static final void main(String[] args) { byte[] b = new
   * byte[4]; b[0] = (byte)0xff; b[1] = (byte)0x7f;
   * com.bbn.openmap.util.Debug.output("32767="+BuildShortLE(b, 0));
   * b[0] = (byte)0x7f; b[1] = (byte)0xff;
   * com.bbn.openmap.util.Debug.output("32767="+BuildShortBE(b, 0));
   * b[1] = (byte)0xff; b[2] = (byte)0xff; b[3] = (byte)0xff;
   * com.bbn.openmap.util.Debug.output("2147483647="+BuildIntegerBE(b,
   * 0));
   * com.bbn.openmap.util.Debug.output("maxuint="+signedToLong(0xffffffff)); }
   */
}

class Rotation {

  protected Geo g;
  double m00, m11, m22, m01, m10, m12, m21, m02, m20;

  public Rotation(Geo g, double angle) {
      this.g = g;
      this.setAngle(angle);
  }

  private void setAngle(double angle) {
      double x = g.x();
      double y = g.y();
      double z = g.z();
      double c = Math.cos(angle);
      double s = Math.sin(angle);
      double b = 1.0 - c;
      double bx = b * x;
      double by = b * y;
      double bz = b * z;
      double bxx = bx * x;
      double bxy = bx * y;
      double bxz = bx * z;
      double byy = by * y;
      double byz = by * z;
      double bzz = bz * z;
      double sx = s * x;
      double sy = s * y;
      double sz = s * z;
      m00 = c + bxx;
      m11 = c + byy;
      m22 = c + bzz;
      m01 = (-sz) + bxy;
      m10 = sz + bxy;
      m12 = (-sx) + byz;
      m21 = sx + byz;
      m02 = sy + bxz;
      m20 = (-sy) + bxz;

      /**
       * System.out.println (" Rotation " + m00 + " " + m11 + " " + m22 + "\n" +
       * m01 + " " + m10 + " " + m12 + "\n" + m21 + " " + m02 + " " + m20);
       */
  }

  public Geo rotate(Geo v) {
      double x = v.x(), y = v.y(), z = v.z();
      return new Geo(m00 * x + m01 * y + m02 * z, m10 * x + m11 * y + m12 * z, m20
              * x + m21 * y + m22 * z);
  }

  /**
   * Static method that does what creating a Rotation object can calling
   * rotate() on it does. Rotates vector v2 an angle counter clockwise about
   * the Geo, v1.
   *
   * @param v1
   * @param angle
   * @param v2
   * @param ret The Geo to load the results in, may be null which will cause
   *        the method to allocate a new Geo object.
   * @return the ret Geo passed in, or a new one if ret was null.
   */
  public final static Geo rotate(Geo v1, double angle, Geo v2, Geo ret) {
      double x = v1.x();
      double y = v1.y();
      double z = v1.z();
      double c = Math.cos(angle);
      double s = Math.sin(angle);
      double b = 1.0 - c;
      double bx = b * x;
      double by = b * y;
      double bz = b * z;
      double bxx = bx * x;
      double bxy = bx * y;
      double bxz = bx * z;
      double byy = by * y;
      double byz = by * z;
      double bzz = bz * z;
      double sx = s * x;
      double sy = s * y;
      double sz = s * z;
      double m00 = c + bxx;
      double m11 = c + byy;
      double m22 = c + bzz;
      double m01 = (-sz) + bxy;
      double m10 = sz + bxy;
      double m12 = (-sx) + byz;
      double m21 = sx + byz;
      double m02 = sy + bxz;
      double m20 = (-sy) + bxz;

      /**
       * System.out.println (" Rotation " + m00 + " " + m11 + " " + m22 + "\n" +
       * m01 + " " + m10 + " " + m12 + "\n" + m21 + " " + m02 + " " + m20);
       */
      double x2 = v2.x();
      double y2 = v2.y();
      double z2 = v2.z();

      if (ret == null) {
          return new Geo(m00 * x2 + m01 * y2 + m02 * z2, m10 * x2 + m11 * y2
                  + m12 * z2, m20 * x2 + m21 * y2 + m22 * z2);
      }

      ret.initialize(m00 * x2 + m01 * y2 + m02 * z2, m10 * x2 + m11 * y2
              + m12 * z2, m20 * x2 + m21 * y2 + m22 * z2);

      return ret;
  }
}

public class ConvexHull {
  private ConvexHull() {}

  /**
   * Using Graham's scan.
   *
   * @param geos
   * @return GeoRegion outlining the convex hull of the geos
  public static final GeoRegion getRegion(Geo[] geos) {
      Geo[] regionGeos = hull(geos);
      return new GeoRegion.Impl(regionGeos);
  }
   */

  /**
   * Using Graham's scan.
   *
   * @param geos
   * @return a convex hull of the geos
   */

  public static final Geo[] hull(Geo[] geos) {
      return hull(geos, 0);
  }

  /**
   * @return (Lat, Lon) as double[]
   */
  public static double[] getCentroidOfPolygon(PointList points) {
	  double cx = 0, cy = 0;
	  double area = 0;

	  for( int i=0 ; i<points.size() ; i++  ) {
		  int i_p1 = (i+1) % points.size();
		  area += points.getLat(i) * points.getLon(i_p1) - points.getLat(i_p1) * points.getLon(i);
	  }

	  area /= 2;

	  for( int i=0 ; i<points.size() ; i++  ) {
		  int i_p1 = (i+1) % points.size();

		  double p2 = points.getLat(i) * points.getLon(i_p1) - points.getLat(i_p1) * points.getLon(i);

		  cx += (points.getLat(i) + points.getLat(i_p1)) * p2;
		  cy += (points.getLon(i) + points.getLon(i_p1)) * p2;

	  }

	  if( Math.abs( area ) > 0 ) {
		  cx /= 6*area;
		  cy /= 6*area;
	  }

	  return new double[]{cx, cy};
  }

  public static final PointList hull(PointList points) {
	  Geo[] geos = new Geo[points.size()];

	  for( int i=0 ; i<points.size() ; i++  ) {
		  Geo geo = new Geo(points.getLat(i), points.getLon(i));
		  geos[i] = geo;
	  }

	  Geo[] out = hull(geos, 0);

      PointList outPoints = new PointList(out.length, false);
      for(int i=0 ; i<out.length ; i++) {
    	  //outPoints.add(nodeAccess, rp.get(i));
    	  outPoints.add(out[i].getLatitude(), out[i].getLongitude());
      }
      return outPoints;
  }

  /**
   * Using segments Intersection method calculate Intersection point if Intersects null otherwise in O(n) complexity.
   *
   * @param route a path that need to check with parameter polygon
   * @param rpHull parameter polygon, note that it has to be a Convex Hull
   *
   * @return Intersection point if Intersects null otherwise
   */

  public static final GHPoint routeToReachablePerimeterIntersection(PointList route, PointList rpHull) {

	  Geo[] routePoints = new Geo[route.size()];
	  Geo[] rpPoints = new Geo[rpHull.size()];

	  double maxLat = -Double.MAX_VALUE;
	  double maxLon = -Double.MAX_VALUE;
	  double minLat = Double.MAX_VALUE;
	  double minLon = Double.MAX_VALUE;

	  for( int i=0 ; i<route.size() ; i++  ) {
		  Geo geo = new Geo(route.getLat(i), route.getLon(i));
		  routePoints[i] = geo;
	  }
	  for( int i=0 ; i<rpHull.size() ; i++  ) {
		  double lat = rpHull.getLat(i), lon = rpHull.getLon(i);
		  Geo geo = new Geo(lat, lon);
		  rpPoints[i] = geo;
		  maxLat = Math.max( lat, maxLat );
		  minLat = Math.min( lat, minLat );
		  maxLon = Math.max( lon, maxLon );
		  minLon = Math.min( lon, minLon );
	  }

	  Geo[] bbox = new Geo[] {
		new Geo(minLat, minLon),
		new Geo(minLat, maxLon),
		new Geo(maxLat, maxLon),
		new Geo(maxLat, minLon)
	  };

	  for( int i=0 ; i<routePoints.length - 1 ; i++  ) {
		  for( int j=0 ; j<4 ; j++) {
			  if( Geo.segmentsIntersect(routePoints[i], routePoints[i+1], bbox[j], bbox[(j+1)%4] ) != null ) {
				  //potential line segment
				  for(int k=0 ; k<rpPoints.length ; k++) {
					  Geo intersectPint = Geo.segmentsIntersect(routePoints[i], routePoints[i+1], rpPoints[k], rpPoints[(k+1)%rpPoints.length] );
					  if( intersectPint != null ) {
						  return new GHPoint( intersectPint.getLatitude(), intersectPint.getLongitude() );
					  }
				  }
			  }
		  }
	  }

	  return null;
  }

  /**
   * Using Graham's scan.
   *
   * @param geos
   * @param tolerance the distance between points where they would be
   *        considered equals, in radians.
   * @return a convex hull of the geos
   */
  public static final Geo[] hull(Geo[] geos, double tolerance) {
      Geo pivot = findHighest(geos);
      TreeSet sortedGeos = new TreeSet(new PivotAngleComparator(pivot));
      for (int i = 0; i < geos.length; i++) {
          Geo g = geos[i];
          if (g != pivot) {
              sortedGeos.add(g);
          }
      }

      Stack hullStack = new Stack();
      hullStack.push(pivot);

      Geo gCross, midCross = null;
      Geo geo = null, endGeo = null, midGeo = null;

      Iterator sortedGeoIt = sortedGeos.iterator();
      if (sortedGeoIt.hasNext()) {
          midGeo = (Geo) sortedGeoIt.next();

          while (midGeo.distance(pivot) == 0 && sortedGeoIt.hasNext()) {
              midGeo = (Geo) sortedGeoIt.next();
          }
      }

      Geo lastGeoRead = midGeo;

      while (sortedGeoIt.hasNext() && midGeo != null) {
          geo = (Geo) sortedGeoIt.next();
          double dist = geo.distance(lastGeoRead);
          if (dist <= tolerance) {
              // Debug.output("Skipping duplicate geo");
              continue;
          }

          endGeo = (Geo) hullStack.peek();

          midCross = endGeo.crossNormalize(midGeo);
          gCross = midGeo.crossNormalize(geo);
          Geo i = gCross.crossNormalize(midCross).antipode();

          // Debug.output("Evaluating:\n\tendGeo: " + endGeo + "\n\tmidGeo: "
          // + midGeo + "\n\tto " + geo
          // + "\n ****** intersection point: " + i);

          if (midGeo.distance(i) < Math.PI / 2) {
              // Debug.output("+++++++++++++ midGeo to hull");

              // left turn, OK for hull
              hullStack.push(midGeo);
              endGeo = midGeo;
              midGeo = geo;

          } else {

              // right turn, need to backtrack
              while (hullStack.size() > 1) {

                  // Debug.output("-------- midGeo dropped");

                  midGeo = (Geo) hullStack.pop();
                  endGeo = (Geo) hullStack.peek();

                  midCross = endGeo.crossNormalize(midGeo);
                  gCross = midGeo.crossNormalize(geo);
                  i = gCross.crossNormalize(midCross).antipode();

                  // Debug.output("Evaluating:\n\tendGeo: " + endGeo
                  // + "\n\tmidGeo: " + midGeo + "\n\tto " + geo
                  // + "\n ****** intersection point: " + i);

                  if (midGeo.distance(i) < Math.PI / 2) {

                      // Debug.output("+++++++++++++ midGeo to hull");

                      hullStack.push(midGeo);
                      midGeo = geo;
                      break;
                  }
              }
          }

          lastGeoRead = geo;
      }

      if (midGeo != null) {
          hullStack.push(midGeo);
      }

      hullStack.push(pivot);

      Geo[] regionGeos = new Geo[hullStack.size()];

      int i = 0;
      // Need to reverse order to get inside of poly on the right side of
      // line.
      while (!hullStack.isEmpty()) {
          regionGeos[i++] = (Geo) hullStack.pop();
      }

      return regionGeos;
  }

  protected static Geo findHighest(Geo[] geos) {
      Geo ret = null;
      double highest = Double.NEGATIVE_INFINITY;
      for (int i = 0; i < geos.length; i++) {
          double lat = geos[i].getLatitude();
          if (lat > highest) {
              highest = lat;
              ret = geos[i];
          }
      }
      return ret;
  }

  // XXX: does this need to be serializable?
  protected static final class PivotAngleComparator implements Comparator,
          Serializable {
      private Geo pivot;

      public PivotAngleComparator(Geo pivot) {
          this.pivot = pivot;
      }

      public int compare(Object obj1, Object obj2) {
          double ang1 = Double.MAX_VALUE, ang2 = Double.MAX_VALUE;
          int ret = 0;

          if (obj1 instanceof Geo) {
              ang1 = Math.toDegrees(pivot.azimuth((Geo) obj1));
          }

          if (obj2 instanceof Geo) {
              ang2 = Math.toDegrees(pivot.azimuth((Geo) obj2));
          }

          // ts1 is the one being tested/added to the TreeSet, so we
          // want later items with the same time being added after
          // previous items in the file with the same time.

          if (ang1 < ang2) {
              ret = 1;
          } else if (ang1 >= ang2) {
              ret = -1;
          }

          return ret;
      }

      public Geo getPivot() {
          return pivot;
      }

      public boolean equals(Object obj) {
          if (obj == null) {
              return false;
          }
          if (getClass() != obj.getClass()) {
              return false;
          }
          if (obj instanceof PivotAngleComparator) {
              return pivot.equals(((PivotAngleComparator) obj).pivot);
          } else {
              return false;
          }
      }

      public int hashCode() {
          return pivot.hashCode();
      }
  }

  public static void main(String[] args) {
	double[] arr =  new double[]{ 51.886060689823196, -8.475641536913251,  51.88663699224525, -8.476774583982854,  51.886270796200726, -8.476969975463374,  51.887365659043915, -8.476452532629352,  51.88602157427419, -8.473456840368886,  51.8863281656726, -8.478115129726719,  51.886349772356816, -8.478564958540312,  51.887978096782675, -8.476185429308982,  51.88663252189679, -8.47809855218452,  51.886653383522926, -8.473781685690172,  51.88516643386706, -8.476958427063192,  51.886531566527445, -8.47328882977267,  51.88658595576702, -8.473268526940089,  51.88664295270986, -8.47853273477851 };


	Geo[] points = new Geo[arr.length/2];
	for(int i=0 ; i<arr.length ; i+=2) {
	    Geo geo = new Geo(arr[i], arr[i+1]);
	    points[i/2] = geo;
	}

	Geo[] hullpoints = hull( points );
	System.out.println( points.length );
	System.out.println( hullpoints.length );

	for( Geo p : hullpoints ) {
	    System.out.print( p.getLatitude() + ", " +  p.getLongitude() + ", " );
	}
  }
}
