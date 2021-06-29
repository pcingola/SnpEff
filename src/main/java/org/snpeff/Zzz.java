package org.snpeff;

import java.util.ArrayList;
import java.util.List;

import org.snpeff.codons.CodonTable;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

/**
 * Find an AA insert
 *
 */
class FindIns {
	Transcript transcript;
	int aaPos;
	String aaSequence;
	Character aaBefore, aaAfter;
	StringBuilder output;

	public FindIns(Transcript transcript, int aaPos, String aaSequence, Character aaBefore, Character aaAfter) {
		this.transcript = transcript;
		this.aaPos = aaPos;
		this.aaSequence = aaSequence;
		this.aaBefore = aaBefore;
		this.aaAfter = aaAfter;
		output = new StringBuilder();
	}

	/**
	 * Get all bases that produce the AA sequence
	 * @param aaSeq
	 * @return List of bases that can produce aaSeq
	 */
	List<String> aa2bases(String aaSeq) {
		CodonTable codonTable = transcript.getChromosome().codonTable();

		// Only one AA
		if (aaSeq.length() == 1) codonTable.codon(aaSeq);

		// Multiple AAs
		List<List<String>> basesPerAa = new ArrayList<>();
		var total = 1;
		for (char aa : aaSeq.toCharArray()) {
			var codons = codonTable.codon(aa + "");
			basesPerAa.add(codons);
			//			Log.debug("Added " + codons.size() + ", list: " + codons);
			total *= codons.size();
		}

		// Outer product for each AA
		List<String> bases = new ArrayList<>();
		dnaSequences(basesPerAa, bases, 0, "");
		//		Log.debug("Total " + bases.size() + " DNA ALT sequences");
		if (total != bases.size()) Log.fatalError("Expecting " + total + " DNA sequences, but got " + bases.size());

		return bases;
	}

	void dnaSequences(List<List<String>> basesPerAa, List<String> bases, int num, String basesPartial) {
		for (String aaBases : basesPerAa.get(num)) {
			var basesPartialNew = basesPartial + aaBases;
			if (num == basesPerAa.size() - 1) {
				bases.add(basesPartialNew);
			} else dnaSequences(basesPerAa, bases, num + 1, basesPartialNew);
		}
	}

	/**
	 * Find an insert of amino acids 'aas' at position aaPos
	 * @param aaPos
	 * @param aas
	 */
	public void find() {
		if (!transcript.isProteinCoding()) {
			Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, "Transcript '" + transcript.getId() + "' is not protein coding");
			return;
		}

		// Convert AA position to genomic position
		var protein = transcript.protein();
		if (aaPos >= protein.length()) {
			Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, "Transcript '" + transcript.getId() + "' has length " + protein.length() + ", but AA position is " + aaPos);
			return;
		}

		// Check AA before
		if (aaPos > 0 && aaBefore != null) {
			if (protein.charAt(aaPos - 1) != aaBefore) {
				Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, "Transcript '" + transcript.getId() + "' has amino acid '" + protein.charAt(aaPos - 1) + "' at possition " + aaPos + ", expecting '" + aaBefore + "'");
				return;
			}
		}

		// Check AA before
		if (aaPos < protein.length() - 1 && aaAfter != null) {
			if (protein.charAt(aaPos) != aaAfter) {
				Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, "Transcript '" + transcript.getId() + "' has amino acid '" + protein.charAt(aaPos) + "' at possition " + (aaPos + 1) + ", expecting '" + aaAfter + "'");
				return;
			}
		}

		// Get genomic coordinate
		var pos = transcript.aaNumber2Pos(aaPos);
		//		Log.debug("Genomic coordinate for transcript " + transcript.getId() + ", aa " + aaPos + " is " + transcript.getChromosomeName() + ":" + pos);

		// List of bases
		List<String> basesList = aa2bases(aaSequence);
		Log.debug("Amino Acid sequence: " + aaSequence + ", DNA bases: " + basesList);

		// Create all variants
		var ref = "";
		for (String alt : basesList) {
			if (transcript.isStrandMinus()) alt = GprSeq.reverseWc(alt);
			Variant variant = new Variant(transcript.getChromosome(), pos, ref, alt);
			showLine(transcript, variant);
		}
	}

	/**
	 * Show avariant line with HGSV notation
	 */
	void showLine(Transcript tr, Variant variant) {
		VariantEffects veffs = new VariantEffects();
		tr.variantEffect(variant, veffs);

		// Show all HGVS notations for effects
		for (VariantEffect veff : veffs) {
			String hgsvProt = veff.getTranscript().getId() + ":" + veff.getHgvsProt();
			String hgsvDna = veff.getTranscript().getId() + ":" + veff.getHgvsDna();
			write(tr.getChromosomeName() //
					+ "\t" + (variant.getStart() + 1) //
					+ "\t" + variant.getReference() //
					+ "\t" + variant.getAlt() //
					+ "\t" + hgsvProt //
					+ "\t" + hgsvDna);

		}
	}

	void write(String line) {
		output.append(line + '\n');
		System.out.println(line);
	}

}

public class Zzz {
	Config config;
	String genome = "testHg19Chr17";
	String geneName = "ERBB2";
	SnpEffectPredictor snpEffectPredictor;
	StringBuilder output;

	public static void main(String[] args) throws Exception {
		Zzz hgsvReverse = new Zzz("testHg19Chr17", "ERBB2");
		hgsvReverse.load();

		//		A775_G776insTVMA
		hgsvReverse.findInsAa("A775_G776insTVMA", "ERBB2", 775, "TVMA", 'A', 'G');

		//		A775_G776insV
		hgsvReverse.findInsAa("A775_G776insV", "ERBB2", 775, "V", 'A', 'G');

		//		A775_G776insYVMA
		hgsvReverse.findInsAa("A775_G776insYVMA", "ERBB2", 775, "YVMA", 'A', 'G');

		//		G776_V777insL
		hgsvReverse.findInsAa("G776_V777insL", "ERBB2", 776, "L", 'G', 'V');

		//		G776_V777insVC
		hgsvReverse.findInsAa("G776_V777insVC", "ERBB2", 776, "VC", 'G', 'V');

		//		G776_V777insVGC
		hgsvReverse.findInsAa("G776_V777insVGC", "ERBB2", 776, "VGC", 'G', 'V');

		//		G776>LC
		//		G776>VC
		//		G776delinsVC
		//		G778_P780dup
		//		G778_S779CVG
		//		G778_S779insCPG
		hgsvReverse.findInsAa("G778_S779insCPG", "ERBB2", 778, "CPG", 'G', 'S');

		//		G778_S779insLPS
		hgsvReverse.findInsAa("G778_S779insLPS", "ERBB2", 778, "LPS", 'G', 'S');

		//		G778dup
		//		L755_E757delinsS
		//		L755_T759delLRENT
		//		P780_Y781GSP
		//		S779_P780insVGS
		hgsvReverse.findInsAa("S779_P780insVGS", "ERBB2", 779, "VGS", 'S', 'P');

		//		V777_G778insCG
		hgsvReverse.findInsAa("V777_G778insCG", "ERBB2", 777, "CG", 'V', 'G');

		//		V777_G778insG
		hgsvReverse.findInsAa("V777_G778insG", "ERBB2", 777, "G", 'V', 'G');

		//		V777_G778insGCP
		hgsvReverse.findInsAa("V777_G778insGCP", "ERBB2", 777, "GCP", 'V', 'G');

		//		V777_G778insGSP
		hgsvReverse.findInsAa("V777_G778insGSP", "ERBB2", 777, "GSP", 'V', 'G');

		//		Y772_A775dup
		//		Y772_V773VMAT

		Gpr.toFile(Gpr.HOME + "/hgsvReverse.txt", hgsvReverse.output);

		Log.debug("DONE!");
	}

	public Zzz(String genome, String geneName) {
		this.geneName = genome;
		this.geneName = geneName;
		output = new StringBuilder();
	}

	/**
	 * Find a list of genomic variants that produce an insertion in gene 'geneName', at position aaPos of amino acids "aas"
	 * @return List of genomic variants, null on error
	 */
	List<Variant> findInsAa(String name, String geneName, int aaPos, String aaSequence, Character aaBefore, Character aaAfter) {
		var title = name + "\nFind insertions in " + geneName + ", at AA possition " + aaPos + ", inserting AA sequence '" + aaSequence + "', having AA before '" + aaBefore + "', and AA after '" + aaAfter + "'";
		output.append("\n\n" + title + "\n");
		Log.info(title);

		Gene gene = snpEffectPredictor.getGene(geneName);
		if (gene == null) return null;
		Log.info("Found gene '" + geneName + "', has " + gene.subIntervals().size() + " transcripts");
		for (Transcript tr : gene) {
			//			Log.debug("\tTranscript: " + tr.getId());
			var f = new FindIns(tr, aaPos, aaSequence, aaBefore, aaAfter);
			f.find();

			output.append(f.output);
		}
		return null;
	}

	public void load() {
		Log.info("Loading config");
		config = new Config(genome);

		Log.info("Loading database");
		snpEffectPredictor = config.loadSnpEffectPredictor();
	}

}
