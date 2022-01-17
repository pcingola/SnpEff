/*
 * Created on Nov 24, 2003
 */
package org.snpeff.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.net.JarURLConnection;
import java.net.URLConnection;
import java.sql.Date;
import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * General pupose rutines
 * @author root
 */
public class Gpr {

	@SuppressWarnings("unchecked")
	static private class StringTokenizer {

		String string = null;
		int tokens = 0;
		int[] separatorPosition = new int[1000];

		StringTokenizer(String value, char delim) {
			string = value;
			// Loop on the characters counting the separators and remembering
			// their positions
			StringCharacterIterator sci = new StringCharacterIterator(string);
			char c = sci.first();
			while (c != CharacterIterator.DONE) {
				if (c == delim) {
					// Remember its position
					separatorPosition[tokens] = sci.getIndex();
					tokens++;

					// Resize the position array if needed
					if (tokens >= separatorPosition.length) {
						int[] copy = new int[separatorPosition.length * 10];
						System.arraycopy(separatorPosition, 0, copy, 0, separatorPosition.length);
						separatorPosition = copy;
					}
				}
				c = sci.next();
			}
			// Add one token: tokens = separatorCount + 1
			tokens++;
		}

		<T> T[] tokens(Class<T> componentType) {
			T[] r = (T[]) Array.newInstance(componentType, tokens);
			Constructor<T> ctor;
			try {
				ctor = componentType.getConstructor(String.class);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("Cannot create an array of type [" + componentType + "] from an array of String. The type [" + componentType.getSimpleName() + "] must define a single arg constructor that takes a String.class instance.");
			}

			String currentValue = null;
			int i = 0;
			try {
				int start = 0;
				for (i = 0; i < tokens; i++) {
					// Index of the token's last character (exclusive)
					int nextStart = separatorPosition[i];
					// Special case for the last token
					if (i == tokens - 1) nextStart = string.length();

					// Calculate the size of the token
					int length = nextStart - start;
					if (length > 0) {
						currentValue = string.substring(start, nextStart);
						r[i] = ctor.newInstance(currentValue);
					}
					start = nextStart + 1;
				}
			} catch (Exception e) {
				throw new RuntimeException("Cannot create an instance of type [" + componentType + "] from the " + i + "th string value [" + currentValue + "].", e);
			}
			return r;
		}
	}

	public static final long KB = 1024;
	public static final long MB = KB * KB;
	public static final long GB = KB * MB;
	public static final long TB = KB * GB;

	// Number of cores in this computer
	public static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

	// User's home directory
	public static final String HOME = System.getProperty("user.home");

	// Valid extensions for GZIPPED files
	public static final String[] GZIP_EXTENTIONS = { ".gz", ".bgz" };

	/**
	 * Return file's name (without the path)
	 */
	public static String baseName(String file) {
		File f = new File(file);
		return f.getName();
	}

	/**
	 * Return file's name (without the path)
	 */
	public static String baseName(String file, String ext) {
		File f = new File(file);
		String base = f.getName();
		if (base.endsWith(ext)) return base.substring(0, base.length() - ext.length());
		return base;
	}

	/**
	 *  Show a long as a 64 bit binary number
	 */
	public static String bin64(long l) {
		String bl = Long.toBinaryString(l);
		String out = "";
		for (int i = bl.length(); i < 64; i++)
			out += "0";
		return out + bl;
	}

	/**
	 * Can we read this file (either exact name or append a '.gz'
	 */
	public static boolean canRead(String fileName) {
		if (fileName.equals("-")) return true; // Assume we can always read STDIN

		File inputFile = new File(fileName);
		if (inputFile.exists() && inputFile.canRead() && inputFile.isFile()) return true;

		inputFile = new File(fileName + ".gz");
		if (inputFile.exists() && inputFile.canRead() && inputFile.isFile()) return true;

		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static int compareNull(Comparable c1, Comparable c2) {
		if (c1 == null && c2 == null) return 0;
		if (c1 == null) return 1;
		if (c2 == null) return -1;
		return c1.compareTo(c2);
	}

	public static String compileDate(Class<?> cl) {
		return compileTimeStamp(cl, new SimpleDateFormat("yyyy-MM-dd"));
	}

	public static String compileTimeStamp() {
		return compileTimeStamp(Gpr.class);
	}

	/**
	 * Return a time-stamp showing When was the JAR file
	 * created OR when was a class compiled
	 */
	public static String compileTimeStamp(Class<?> cl) {
		return compileTimeStamp(cl, new SimpleDateFormat("yyyy-MM-dd HH:mm"));
	}

	/**
	 * Return a time-stamp showing When was the JAR file
	 * created OR when was a class compiled
	 */
	public static String compileTimeStamp(Class<?> cl, SimpleDateFormat dateFormat) {
		try {
			String resName = cl.getName().replace('.', '/') + ".class";
			URLConnection conn = ClassLoader.getSystemResource(resName).openConnection();

			long epoch = 0;
			if (conn instanceof JarURLConnection) {
				// Is it a JAR file? Get manifest time
				epoch = ((JarURLConnection) conn).getJarFile().getEntry("META-INF/MANIFEST.MF").getTime();
			} else {
				// Regular file? Get file compile time
				epoch = conn.getLastModified();
			}

			// Format as timestamp
			Date epochDate = new Date(epoch);
			return dateFormat.format(epochDate);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Count number of column in a file
	 */
	public static int countColumns(String file) {
		try {
			// Open a file and read one line
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			reader.close();

			// Nothing to read?
			if (line == null) return 0;

			// Count fields
			String fields[] = line.split("\\s+");
			return fields.length;
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Count lines in a file (same as 'wc -l file' in unix)
	 * @param file
	 * @return
	 */
	public static int countLines(String file) {
		try {
			BufferedReader reader;
			reader = new BufferedReader(new FileReader(file));
			int lines = 0;
			while (reader.readLine() != null)
				lines++;
			reader.close();
			return lines;
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Return file's dir
	 */
	public static String dirName(String file) {
		File f = new File(file);
		String paren = f.getParent();
		return paren == null ? "." : paren;
	}

	/**
	 * Does 'file' exist?
	 */
	public static boolean exists(String file) {
		return new File(file).exists();
	}

	/**
	 * Get a file's extension (all letters after the last '.'
	 */
	public static String extName(String file) {
		String base = baseName(file);
		int idx = base.lastIndexOf('.');
		if (idx >= 0) return base.substring(idx + 1);
		return "";
	}

//	/**
//	 * Generate an evenly separated pallette of colors
//	 * @param num	Number of colors
//	 */
//	public static Paint[] getPaints(int num) {
//		return getPaints(num, false);
//	}
//
//	public static Paint[] getPaints(int num, boolean goUp) {
//		int r, g, b, i, jump, jumpr, jumpg, jumpb;
//		Paint paints[] = new Paint[num];
//
//		for (jump = 1; (jump * jump * jump) <= num; jump++);
//
//		jumpr = jumpg = jumpb = jump;
//		if (((jumpr - 1) * jumpg * jumpb) <= num) jumpr--;
//
//		if ((jumpr * (jumpg - 1) * jumpb) <= num) jumpg--;
//
//		if (jumpr > 1) jumpr = 255 / (jumpr - 1);
//		else jumpr = 255;
//
//		if (jumpg > 1) jumpg = 255 / (jumpg - 1);
//		else jumpg = 255;
//
//		if (jumpb > 1) jumpb = 255 / (jumpb - 1);
//		else jumpb = 255;
//
//		if (goUp) for (i = 0, r = 0; (r <= 255) && (i < num); r += jumpr)
//			for (g = 0; (g <= 255) && (i < num); g += jumpg)
//				for (b = 0; (b <= 255) && (i < num); b += jumpb, i++)
//					paints[i] = new Color(r, g, b);
//		else for (i = 0, r = 255; (r >= 0) && (i < num); r -= jumpr)
//			for (g = 255; (g >= 0) && (i < num); g -= jumpg)
//				for (b = 255; (b >= 0) && (i < num); b -= jumpb, i++)
//					paints[i] = new Color(r, g, b);
//		return paints;
//	}

	public static String head(Object o) {
		StringBuilder sb = new StringBuilder();

		String lines[] = o.toString().split("\n");
		for (int i = 0; i < 10; i++)
			sb.append(lines[i] + "\n");

		return sb.toString();
	}

	public static StringBuffer inputStream2StringBuffer(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		StringBuffer file = new StringBuffer();

		String line = reader.readLine();
		while (line != null) {
			file.append(line + "\n");
			line = reader.readLine();
		}
		return file;
	}

	/**
	 * Empty or '-' means STDIN
	 */
	public static boolean isStdin(String fileName) {
		return fileName.isEmpty() || fileName.equals("-");
	}

	public static boolean isValidIp(String ip) {
		if (ip == null) return false;

		String[] ipSplitted = ip.split("\\.");

		if (ipSplitted.length != 4) return false;

		for (int i = 0; i < ipSplitted.length; i++) {
			if (ipSplitted[i].length() > 3) return false;
			try {
				int ipPart = Integer.parseInt(ipSplitted[i]);
				if (ipPart < 0 || ipPart > 255) return false;
			} catch (NumberFormatException e) {
				return false;
			}
		}

		return true;
	}

	/** Is this a valid Rid */
	public static boolean isValidRid(int i) {
		return (i > 0);
	}

	/**
	 * Remove spaces and tabs from string.
	 */
	public static String noSpaces(String str) {
		String lines[] = str.split("\n");
		StringBuilder sb = new StringBuilder();
		for (String line : lines)
			sb.append(line.trim().replaceAll("\\s", "") + "\n");

		if (lines.length == 1) sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * Equivalent to Boolean.parseBoolean, except it returns 0 on invalid integer (NumberFormatException)
	 */
	public static boolean parseBoolSafe(String s) {
		try {
			return Boolean.parseBoolean(s);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Equivalent to Double.parseDouble(), except it returns 0 on invalid double (NumberFormatException)
	 */
	public static double parseDoubleSafe(String s) {
		try {
			return Double.parseDouble(s);
		} catch (Exception e) {
			return 0.0;
		}
	}

	/**
	 * Equivalent to Float.parseFloat(), except it returns 0 on invalid double (NumberFormatException)
	 * @param s
	 * @return	int
	 */
	public static float parseFloatSafe(String s) {
		try {
			return Float.parseFloat(s);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Equivalent to Integer.parseInt, except it returns 0 on invalid integer (NumberFormatException)
	 * @param s
	 * @return	int
	 */
	public static int parseIntSafe(String s) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Equivalent to Integer.parseInt, except it returns 0 on invalid integer (NumberFormatException)
	 * @param s
	 * @return	int
	 */
	public static long parseLongSafe(String s) {
		try {
			return Long.parseLong(s);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Prepend a message to each line
	 */
	public static String prependEachLine(String prepend, Object lines) {
		StringBuilder sb = new StringBuilder();
		for (String line : lines.toString().split("\n"))
			sb.append(prepend + line + "\n");
		return sb.toString();
	}

	/**
	 * Read an input stream
	 * @param is
	 * @return
	 */
	public static String read(InputStream is) {
		if (is == null) return null;
		StringBuffer strb = new StringBuffer();
		char buff[] = new char[10240];
		int len = 0;

		try {
			BufferedReader inFile = new BufferedReader(new InputStreamReader(is));
			while ((len = inFile.read(buff)) >= 0)
				strb.append(buff, 0, len);
			inFile.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return strb.toString();

	}

	/**
	 * Try to open a file (BufferedReader) using either the file or a gzip file (appending '.gz' to fileName)
	 */
	public static BufferedReader reader(String fileName) {
		return reader(fileName, false);
	}

	/**
	 * Try to open a file (BufferedReader) using either the file or a gzip file (appending '.gz' to fileName)
	 * @param gzip : If true, file is assumed to be gzipped
	 */
	public static BufferedReader reader(String fileName, boolean gzip) {
		BufferedReader reader = null;

		try {
			if (fileName.equals("-")) {
				return new BufferedReader(new InputStreamReader(System.in));
			} else if (fileName.endsWith(".gz") || fileName.endsWith(".bgz") || gzip) {
				// This is a gzip compressed file
				File inputFile = new File(fileName);
				if (inputFile.exists()) return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(new File(fileName)))));
				else throw new RuntimeException("File not found '" + inputFile.getAbsolutePath() + "'");
			} else {
				// Try opening the file
				File inputFile = new File(fileName);
				if (inputFile.exists()) return new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
				else {
					// Doesn't exists? => append GZIP extentions to file name and try gzipped file
					for (String ext : GZIP_EXTENTIONS) {
						String fileNameGz = fileName + ext;
						inputFile = new File(fileNameGz);
						if (inputFile.exists()) return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(new File(fileNameGz)))));
					}
					throw new RuntimeException("File not found '" + fileName + "'");
				}
			}
		} catch (FileNotFoundException e) {
			reader = null;
			Log.debug(e);
		} catch (IOException e) {
			reader = null;
			Log.debug(e);
		}

		return reader;
	}

	/**
	 * Read a file as a String.
	 * Note: the file can be compressed using gzip (file name must have a ".gz" extension).
	 *
	 * @param fileName : File to read (null on error)
	 * @param showExceptions : show exceptions if true
	 */
	public static String readFile(String fileName) {
		return readFile(fileName, false);
	}

	/**
	 * Read a file as a String.
	 * Note: the file can be compressed using gzip (file name must have a ".gz" extension).
	 *
	 * @param fileName : File to read (null on error)
	 * @param showExceptions : show exceptions if true
	 */
	public static String readFile(String fileName, boolean gzipped) {
		BufferedReader inFile;
		StringBuffer strb = new StringBuffer();
		char buff[] = new char[10240];
		int len = 0;
		try {
			inFile = reader(fileName, gzipped);
			while ((len = inFile.read(buff)) >= 0)
				strb.append(buff, 0, len);
			inFile.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return strb.toString();
	}

	/**
	 * Read an object from a file (supposing an object was previously serialized to that file)
	 * @param fileName
	 * @return An object from that file
	 */
	public static Object readFileSerialized(String fileName) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
			Object obj = in.readObject();
			in.close();
			return obj;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Read an object from a file (supposing an object was previously serialized to that file)
	 * Note: The file is compressed using GZIP
	 * @param fileName
	 * @return An object from that file
	 */
	public static Object readFileSerializedGz(String fileName) {
		try {
			ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(fileName)));
			Object obj = in.readObject();
			in.close();
			return obj;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Read an object from a file (supposing an object was previously serialized to that file)
	 * Note: Same as 'readFileSerializedGz' but it throws all the exceptions
	 *
	 *
	 * @param fileName
	 * @return An object from that file
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 */
	public static Object readFileSerializedGzThrow(String fileName) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(fileName)));
		Object obj = in.readObject();
		in.close();
		return obj;
	}

	/**
	 * Remove trailing '\r'
	 */
	public static String removeBackslashR(String line) {
		if ((line != null) //
				&& (!line.isEmpty()) //
				&& line.charAt(line.length() - 1) == '\r' //
		) {
			line = line.substring(0, line.length() - 1);
		}

		return line;
	}

	public static String removeExt(String file) {
		int lastDot = file.indexOf('.');
		if (lastDot >= 0) return file.substring(0, lastDot);
		return file;
	}

	/**
	 * Remove extension from a file (if matches one of 'fileExtensions[]')
	 */
	public static String removeExt(String file, String fileExtensions[]) {
		for (String ext : fileExtensions)
			if (file.toLowerCase().endsWith(ext)) return file.substring(0, file.length() - ext.length());

		return file;
	}

	/**
	 * Create a string of n time 'c'
	 */
	public static String repeat(char c, int n) {
		if (n <= 0) return "";

		char str[] = new char[n];
		Arrays.fill(str, c);
		return new String(str);
	}

	public static String sanityzeFileName(String fileName) {
		String out = fileName.trim().replaceAll("[^0-9_a-zA-Z\\(\\)\\%\\-\\.\\[\\]\\:\\,]", "_");
		return out.replaceAll("_+", "_");
	}

	public static String sanityzeName(String fileName) {
		String out = fileName.trim().replaceAll("[^0-9_a-zA-Z\\.]", "_");
		out = out.replaceAll("_+", "_");
		if (out.startsWith("_")) out = out.substring(1);
		if (out.endsWith("_")) out = out.substring(0, out.length() - 1);
		return out;
	}

	/**
	 * Show a mark
	 */
	public static void showMark(int i, int showEvery) {
		if (showEvery <= 0) return;
		if (i % showEvery == 0) {
			if (i % (100 * showEvery) == 0) System.err.print(".\n" + i + "\t");
			else System.err.print('.');
		}
	}

	/**
	 * Show a mark
	 */
	public static void showMark(int i, int showEvery, String newLineStr) {
		if (showEvery <= 0) return;
		if (i % showEvery == 0) {
			if (i % (100 * showEvery) == 0) System.err.print(".\n" + newLineStr + i + "\t");
			else System.err.print('.');
		}
	}

	/**
	 * Show a mark (on STDERR)
	 */
	public static void showMarkStderr(int i, int showEvery) {
		if (i % showEvery == 0) {
			if (i % (100 * showEvery) == 0) System.err.print(".\n" + i + "\t");
			else System.err.print('.');
		}
	}

	public static void showStackTrace(int steps) {
		showStackTrace(steps, 1);
	}

	/**
	 * Prints a stack trace for a number of steps
	 * @param count
	 */
	public static void showStackTrace(int steps, int offset) {
		Exception e = new Exception();
		String sep = "";
		for (int i = 0; i < steps; i++) {
			int num = i + offset + 1;
			if (num < e.getStackTrace().length) {
				StackTraceElement ste = e.getStackTrace()[num];
				String steStr = ste.getClassName();
				int ind = steStr.lastIndexOf('.');
				steStr = steStr.substring(ind + 1);
				steStr += "." + ste.getMethodName() + "(" + ste.getLineNumber() + "):";
				System.err.println(sep + steStr);
				sep = "\t";
			}
		}
	}

	/**
	 * Splits a separated string into an array of <code>String</code> tokens. If
	 * the input string is null, this method returns null.
	 *
	 * <p/>
	 * Implementation note: for performance reasons, this implementation uses
	 * neither StringTokenizer nor String.split(). StringTokenizer does not
	 * return all tokens for strings of the form "1,2,,3," unless you use an
	 * instance that returns the separator. By doing so, our code would need to
	 * modify the token string which would create another temporary object and
	 * would make this method very slow. <br/>
	 * String.split does not return all tokens for strings of the form
	 * "1,2,3,,,". We simply cannot use this method.
	 * <p/>
	 * The result is a custom String splitter algorithm which performs well for
	 * large Strings.
	 *
	 * @param value
	 *            the string value to split into tokens
	 * @return an array of String Objects or null if the string value is null
	 */
	static public String[] split(final String value, char delim) {
		if (value == null) return null;
		StringTokenizer st = new StringTokenizer(value, delim);
		return st.tokens(String.class);
	}

	public static String tabs(int tabs) {
		String t = "";
		for (int i = 0; i < tabs; i++)
			t += "\t";
		return t;
	}

	public static String tail(Object o) {
		StringBuilder sb = new StringBuilder();

		String lines[] = o.toString().split("\n");
		for (int i = lines.length - 10; i < lines.length; i++)
			sb.append(lines[i] + "\n");

		return sb.toString();
	}

	public static String toByteSize(long l) {
		if (l > TB) return String.format("%1.2f TB", l / ((double) TB));
		if (l > GB) return String.format("%1.2f GB", l / ((double) GB));
		if (l > MB) return String.format("%1.2f MB", l / ((double) MB));
		if (l > KB) return String.format("%1.2f KB", l / ((double) KB));
		return l + " bytes";
	}

	/**
	 * Write an object to a file
	 */
	public static void toFile(String fileName, Object obj) {
		toFile(fileName, obj, false);
	}

	/**
	 * Write an object to a file
	 */
	public static void toFile(String fileName, Object obj, boolean append) {
		BufferedWriter outFile;
		try {
			outFile = new BufferedWriter(new FileWriter(fileName, append));
			outFile.write(obj.toString());
			outFile.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Write an object to a file (as a string)
	 * Note: The file is compressed using GZIP
	 * @param fileName: File to write
	 * @param obj: Object
	 */
	public static void toFileGz(String fileName, Object obj) {
		PrintStream outFile;
		try {
			outFile = new PrintStream(new GZIPOutputStream(new FileOutputStream(fileName)));
			outFile.print(obj.toString());
			outFile.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Write an object to a file by invoking Serialization methods
	 * @param fileName: File to write
	 * @param obj: Object
	 */
	public static void toFileSerialize(String fileName, Object obj) {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(fileName));
			out.writeObject(obj);
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Write an object to a file by invoking Serialization methods
	 * Note: The file is compressed using GZIP
	 * @param fileName: File to write
	 * @param obj: Object
	 */
	public static void toFileSerializeGz(String fileName, Object obj) {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(fileName)));
			out.writeObject(obj);
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String toString(boolean vals[]) {
		if (vals == null) return "null";

		StringBuilder sb = new StringBuilder();
		sb.append("[ ");

		for (int i = 0; i < vals.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(vals[i] ? '1' : '0');
		}

		sb.append(" ]");
		return sb.toString();
	}

	public static String toString(double vals[]) {
		if (vals == null) return "null";

		StringBuilder sb = new StringBuilder();
		sb.append("[ ");

		for (int i = 0; i < vals.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(toString(vals[i]));
		}

		sb.append(" ]");
		return sb.toString();
	}

	public static String toString(double vals[][]) {
		if (vals == null) return "null";

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < vals.length; i++) {
			sb.append("|");
			for (int j = 0; j < vals[i].length; j++) {
				sb.append(" " + toString(vals[i][j]));
			}

			sb.append(" |\n");
		}

		return sb.toString();
	}

	public static String toString(double val) {
		double aval = Math.abs(val);
		if (aval < 1000000 && aval >= 100000.0) return String.format("% 6.2f", val);
		if (aval < 100000 && aval >= 10000.0) return String.format("% 5.2f ", val);
		if (aval < 10000 && aval >= 1000.0) return String.format("% 4.2f  ", val);
		if (aval < 1000 && aval >= 100.0) return String.format("% 3.2f   ", val);
		if (aval < 100 && aval >= 10.0) return String.format("% 2.2f    ", val);
		if (aval < 10 && aval >= 1.0) return String.format("% 1.3f    ", val);
		if (aval < 1.0 && aval >= 0.01) return String.format("% 1.3f    ", val);
		if (aval < 1.0 && aval >= 0.001) return String.format("% 1.4f   ", val);
		if (aval < 1.0 && aval >= 0.000001) return String.format("% 1.6f ", val);
		if (aval < 1.0 && aval >= 0.0000001) return String.format("% 1.7f", val);
		if (val == 0.0) return String.format(" 0        ", val);
		return String.format("% 1.3e", val);
	}

	public static String toString(int vals[]) {
		if (vals == null) return "null";

		StringBuilder sb = new StringBuilder();
		sb.append("[ ");

		for (int i = 0; i < vals.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(vals[i]);
		}

		sb.append(" ]");
		return sb.toString();
	}

	public static String toStringHead(double vals[]) {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");

		for (int i = 0; i < 10 && i < vals.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(toString(vals[i]));
		}

		double sum = 0;
		for (int i = 0; i < vals.length; i++)
			sum += vals[i];
		sb.append("\tsum: " + sum);

		sb.append(" ]");
		return sb.toString();
	}
}
