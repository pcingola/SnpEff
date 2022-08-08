package org.snpeff;

import org.biojava.nbio.core.util.InputStreamProvider;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.align.util.UserConfiguration;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.structure.io.PDBFileParser;
import org.biojava.nbio.structure.secstruc.SecStrucInfo;
import org.snpeff.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * How to calculate hydrogen bonds:
 * - https://en.wikipedia.org/wiki/Hydrogen_bond#:~:text=A%20hydrogen%20bond%20(or%20H,hydrogen%20bond%20acceptor%20(Ac).
 * - https://www.biostars.org/p/16759/
 * - DSSP: https://en.wikipedia.org/wiki/DSSP_(hydrogen_bond_estimation_algorithm)
 * - DSSP: https://github.com/PDB-REDO/dssp
 *
 *
 */
public class Zzz {

    String fileName;
    Structure structure;

    public Zzz(String fileName) {
        this.fileName = fileName;
    }

    public static void main(String args[]) throws Exception {
        Log.info("Start");
        Zzz zzz = new Zzz("tests/integration/zzz/pdb/pdb5pti.ent.gz");
        zzz.load();
        zzz.show();
        Log.info("End");
    }

    public Structure load() throws IOException {
        Log.info("Reading PDB file: " + fileName);
        var file = new File(fileName);

        // Set property to avoid "Illegal reflective access"
        //		WARNING: An illegal reflective access operation has occurred
        //		WARNING: Illegal reflective access by com.sun.xml.bind.v2.runtime.reflect.opt.Injector (file:/Users/kqrw311/.m2/repository/com/sun/xml/bind/jaxb-impl/2.3.0/jaxb-impl-2.3.0.jar) to method java.lang.ClassLoader.defineClass(java.lang.String,byte[],int,int)
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");
        // Setting system property so BioJava doesn't print 'PDB_DIR' missing warnings
        System.setProperty(UserConfiguration.PDB_DIR, file.getParent());

        // Create streams
        InputStreamProvider isp = new InputStreamProvider();
        InputStream inStream = isp.getInputStream(file);

        // Parse and load file

        FileParsingParameters params = new FileParsingParameters();
        params.setParseSecStruc(true); //this is false as DEFAULT

        //
        AtomCache cache = new AtomCache();
        cache.setFileParsingParams(params);

        // Read, parse and load file
        PDBFileParser pdbpars = new PDBFileParser();
        pdbpars.setFileParsingParameters(params);
        structure = pdbpars.parsePDBFile(inStream);


        inStream.close();
        return structure;
    }

    public void show() {
        for (Chain c : structure.getChains()) {
            for (Group g : c.getAtomGroups()) {
                if (g.hasAminoAtoms()) { //Only AA store SS
                    //Obtain the object that stores the SS
                    SecStrucInfo ss = (SecStrucInfo) g.getProperty(Group.SEC_STRUC);
                    //Print information: chain+resn+name+SS
                    System.out.println(c.getId() + " " +
                            g.getResidueNumber() + " " +
                            g.getPDBName() + " -> " + ss);
                }
            }
        }
    }
}
