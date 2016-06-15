package org.snpeff.pdb;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.biojava.nbio.core.util.InputStreamProvider;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.structure.io.PDBFileParser;

/**
 * A structure that reads PDB files
 * 
 * This code is similar to 'PDBFileReader' from BioJava, but the BioJava version 
 * doesn't close file descriptors and eventually produces a crash when reading 
 * many files.
 * 
 * @author pcingola
 */
public class PdbFile {

	FileParsingParameters params = new FileParsingParameters();

	/** 
	 * Opens filename, parses it and returns aStructure object .
	 */
	public Structure getStructure(String filename) throws IOException {
		File file = new File(filename);
		InputStreamProvider isp = new InputStreamProvider();
		InputStream inStream = isp.getInputStream(file);
		PDBFileParser pdbpars = new PDBFileParser();
		pdbpars.setFileParsingParameters(params);
		Structure struc = pdbpars.parsePDBFile(inStream);
		inStream.close();
		return struc;
	}
}
