package ca.mcgill.mcb.pcingola;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ca.mcgill.mcb.pcingola.util.Gpr;

public class Zzz {

	public static final String PDB_EXT = ".ent";
	public static final String PDB_EXT_GZ = ".ent.gz";

	boolean debug = true;
	String pdbDir;
	Collection<String> pdbFiles;

	public static void main(String[] args) {
		String pdbDir = Gpr.HOME + "/snpEff/db/pdb";
		Zzz zzz = new Zzz(pdbDir);
		zzz.setDebug(true);
		zzz.findPdbFiles();
	}

	public Zzz(String pdbDir) {
		this.pdbDir = pdbDir;
	}

	public void findPdbFiles() {
		pdbFiles = findPdbFiles(new File(pdbDir));
	}

	Collection<String> findPdbFiles(File dir) {
		List<String> list = new LinkedList<>();

		for (File f : dir.listFiles()) {
			String fileName = f.getName();
			if (f.isDirectory()) {
				findPdbFiles(f);
			} else if (f.isFile() && (fileName.endsWith(PDB_EXT) || fileName.endsWith(PDB_EXT_GZ))) {
				list.add(f.getAbsolutePath());
				if (debug) Gpr.debug("Adding file: " + f.getAbsolutePath());
			}
		}

		return list;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

}
