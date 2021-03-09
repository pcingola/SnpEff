package org.snpeff.vcf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;
import org.snpeff.util.Tuple;

/**
 * A pedigree for cancer samples
 */
public class Pedigree implements Iterable<PedigreeEntry> {

	boolean debug;
	boolean verbose;
	List<PedigreeEntry> pedigree;
	List<PedigreeEntry> pedigreeDerived;
	int[] pedigreeDerivedArray;
	VcfFileIterator vcfFile;

	public Pedigree(VcfFileIterator vcfFile) {
		this.vcfFile = vcfFile;
		readPedigreeVcf();
	}

	public Pedigree(VcfFileIterator vcfFile, String cancerSamples) {
		this.vcfFile = vcfFile;
		readPedigreeTxt(cancerSamples);
	}

	/**
	 * Are there any "back to reference" cancer variant
	 */
	public boolean anyBackToRef(VcfEntry vcfEntry) {
		for (PedigreeEntry pe : derived()) {
			int numOri = pe.getOriginalNum();
			int numDer = pe.getDerivedNum();
			VcfGenotype gtOri = vcfEntry.getVcfGenotype(numOri);
			VcfGenotype gtDer = vcfEntry.getVcfGenotype(numDer);

			int gd[] = gtDer.getGenotype(); // Derived genotype
			int go[] = gtOri.getGenotype(); // Original genotype

			// Skip if one of the genotypes is missing
			if (gd == null || go == null) continue;

			if (gtOri.isPhased() && gtDer.isPhased()) {
				// Phased, we only have two possible comparisons
				for (int i = 0; i < 2; i++)
					if ((go[i] > 0) && (gd[i] == 0)) return true;
			} else {
				// Not phased, compare all possible combinations
				for (int o = 0; o < go.length; o++)
					for (int d = 0; d < gd.length; d++)
						if ((go[o] > 0) && (gd[d] == 0)) return true;
			}
		}

		return false;
	}

	/**
	 * Is there any derived sample?
	 */
	public boolean anyDerived() {
		return pedigree.stream().anyMatch(pe -> pe.isDerived());
	}

	/**
	 * Analyze which comparisons to make in cancer genomes
	 */
	public Set<Tuple<Integer, Integer>> compareCancerGenotypes(VcfEntry vcfEntry) {
		HashSet<Tuple<Integer, Integer>> comparisons = new HashSet<>();

		// Find out which comparisons have to be analyzed
		for (PedigreeEntry pe : derived()) {
			int numOri = pe.getOriginalNum();
			int numDer = pe.getDerivedNum();
			VcfGenotype gtOri = vcfEntry.getVcfGenotype(numOri);
			VcfGenotype gtDer = vcfEntry.getVcfGenotype(numDer);

			int gd[] = gtDer.getGenotype(); // Derived genotype
			int go[] = gtOri.getGenotype(); // Original genotype

			// Skip if one of the genotypes is missing
			if (gd == null || go == null) continue;

			if (gtOri.isPhased() && gtDer.isPhased()) {
				// Phased, we only have two possible comparisons
				for (int i = 0; i < 2; i++) {
					// Add comparisons
					if ((go[i] >= 0) && (gd[i] >= 0) // Both genotypes are non-missing?
							&& (go[i] != 0) // Origin genotype is non-reference? The case "REF -> ALT" is always analyzed in the default (non-cancer) annotation, so we skip it here
							&& (gd[i] != go[i]) // Both genotypes are different?
					) {
						Tuple<Integer, Integer> compare = new Tuple<>(gd[i], go[i]);
						comparisons.add(compare);
					}
				}
			} else {
				// Not phased, compare all possible combinations
				for (int o = 0; o < go.length; o++) {
					// Origin genotype is non-missing and non-reference?
					// The case "REF -> ALT" is always analyzed in the default (non-cancer) annotation, so we skip it here
					if (go[o] > 0) {
						for (int d = 0; d < gd.length; d++) {
							// Add comparisons
							if ((gd[d] >= 0) // Derived genotype is non-missing?
									&& (gd[d] != go[o]) // Derived genotype differs from original genotype?
							) {
								Tuple<Integer, Integer> compare = new Tuple<>(gd[d], go[o]);
								comparisons.add(compare);
							}
						}
					}
				}
			}
		}

		return comparisons;
	}

	/**
	 * A list of all derived pedigree entries
	 */
	public List<PedigreeEntry> derived() {
		if (pedigreeDerived == null) pedigreeDerived = pedigree.stream().filter(pe -> pe.isDerived()).collect(Collectors.toList());
		return pedigreeDerived;
	}

	@Override
	public Iterator<PedigreeEntry> iterator() {
		return pedigree.iterator();
	}

	/**
	 * Read pedigree from TXT file
	 */
	void readPedigreeTxt(String cancerSamples) {
		// Read from TXT file
		if (verbose) Log.info("Reading cancer samples pedigree from file '" + cancerSamples + "'.");

		List<String> sampleNames = vcfFile.getVcfHeader().getSampleNames();
		pedigree = new ArrayList<>();

		for (String line : Gpr.readFile(cancerSamples).split("\n")) {
			String recs[] = line.split("\\s", -1);
			String original = recs[0];
			String derived = recs[1];

			PedigreeEntry pe = new PedigreeEntry(original, derived);
			pe.sampleNumbers(sampleNames);

			pedigree.add(pe);
		}
	}

	/**
	 * Read pedigree from VCF file's header
	 */
	void readPedigreeVcf() {
		// Read from VCF header
		if (verbose) Log.info("Reading cancer samples pedigree from VCF header.");
		pedigree = vcfFile.getVcfHeader().getPedigree();
		if (verbose) Log.info("Pedigree: " + pedigree);
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
