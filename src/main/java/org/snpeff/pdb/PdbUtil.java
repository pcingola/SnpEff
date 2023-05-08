package org.snpeff.pdb;
import org.biojava.nbio.structure.*;
import java.util.ArrayList;
import java.util.List;

public class PdbUtil {

    /**
     * Get all AAs in a chain
     */
    public static List<AminoAcid> aminoAcids(Chain chain) {
        ArrayList<AminoAcid> aas = new ArrayList<>();
        for (Group group : chain.getAtomGroups())
            if (group instanceof AminoAcid) aas.add((AminoAcid) group);
        return aas;
    }

    /**
     * Minimum distance between all atoms in two amino acids
     */
    public static double distanceMin(AminoAcid aa1, AminoAcid aa2) {
        double distMin = Double.POSITIVE_INFINITY;

        for (Atom atom1 : aa1.getAtoms())
            for (Atom atom2 : aa2.getAtoms()) {
                double dist = Calc.getDistance(atom1, atom2);
                distMin = Math.min(distMin, dist);
            }

        return distMin;
    }

}
