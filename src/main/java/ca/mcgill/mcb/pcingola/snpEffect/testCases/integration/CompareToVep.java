package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.vcf.VcfConsequence;
import ca.mcgill.mcb.pcingola.vcf.VcfConsequenceHeader;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Compare our results to ENSEML's Variant Effect predictor's output
 *
 * @author pcingola
 */
public class CompareToVep {

	boolean strict = false;
	boolean onlyProtein = false;
	boolean compareEffect = true;
	boolean compareHgvsDna = false;
	boolean compareHgvsProt = false;
	boolean debug = false;
	boolean verbose = false;
	boolean throwException = true;
	String genomeName;
	String addArgs[];
	VcfConsequenceHeader vcfCsqHeader;
	SnpEffCmdEff cmdEff;
	int countHgvsDna, countHgvsProt, countEff;

	public CompareToVep(String genomeName, boolean verbose) {
		this.genomeName = genomeName;
		this.verbose = verbose;
	}

	public CompareToVep(String genomeName, String addArgs[], boolean verbose) {
		this.genomeName = genomeName;
		this.addArgs = addArgs;
		this.verbose = verbose;
	}

	/**
	 * Create command line arguments
	 */
	String[] args(String genomeName, String vcf) {
		List<String> args = new LinkedList<String>();

		if (addArgs != null) {
			for (String arg : addArgs)
				args.add(arg);
		}

		args.add("-noStats");
		args.add("-noLog");
		if (strict) args.add("-strict");
		if (onlyProtein) args.add("-onlyProtein");
		args.add(genomeName);
		args.add(vcf);

		return args.toArray(new String[0]);
	}

	boolean canCompare(VcfEffect eff, VcfConsequence csq) {
		if (compareHgvsDna) {
			// These do not produce HGSV notation, so we cannot compare them
			if (eff.getEffectType() == EffectType.DOWNSTREAM || eff.getEffectType() == EffectType.UPSTREAM) return false;
		}

		return true;
	}

	public boolean checkComapred() {
		if (compareHgvsDna || compareHgvsProt) return (countHgvsDna + countHgvsProt) > 0;
		return countEff > 0;
	}

	/**
	 * Compare two lists of results
	 */
	boolean compare(List<VcfEffect> effs, List<VcfConsequence> csqs) {
		HashSet<String> trIds = new HashSet<String>();
		for (VcfEffect eff : effs)
			trIds.add(eff.getTranscriptId());

		// All transcripts have to match (at least one effect)
		for (String trId : trIds) {
			boolean match = compare(effs, csqs, trId);
			if (!match) return false;
		}

		return true;
	}

	/**
	 * Compare two lists of results, focusing only on transcript 'trId'
	 */
	boolean compare(List<VcfEffect> effs, List<VcfConsequence> csqs, String trId) {
		if (trId == null) return true;
		boolean ok = false;

		if (verbose) {
			Transcript tr = cmdEff.getConfig().getSnpEffectPredictor().getTranscript(trId);
			if (tr != null) {
				System.out.println("\n\t\tTranscript : '" + tr.getId() + "'");
				System.out.println("\t\tStrand     : '" + (tr.isStrandPlus() ? "+" : "-") + "'");
				System.out.println("\t\tCDS        : '" + tr.cds() + "'");
				System.out.println("\t\tProtein    : '" + tr.protein() + "'");
			} else System.out.println("Transcript " + trId + " not found.");

		}

		// At least one effect has to match for this transcript
		for (VcfEffect eff : effs)
			if (trId.equals(eff.getTranscriptId())) {
				boolean match = compare(eff, csqs);
				if (verbose) {
					String matched = match ? "OK" : "NO";
					System.out.println("\t\t\t" + matched + " Match");
				}
				ok |= match;
			}

		return ok;
	}

	/**
	 * Compare two SO terms, return true if they match
	 */
	boolean compare(String effSo, String csqSo) {
		if (effSo.equals(csqSo)) return true;
		if (effSo.equals("inframe_deletion") && csqSo.equals("feature_truncation")) return true;
		if (effSo.equals("disruptive_inframe_insertion") && csqSo.equals("inframe_insertion")) return true;
		if (effSo.equals("disruptive_inframe_deletion") && csqSo.equals("inframe_deletion")) return true;
		if (effSo.equals("disruptive_inframe_deletion") && csqSo.equals("feature_truncation")) return true;
		if (effSo.equals("synonymous_variant") && csqSo.equals("coding_sequence_variant")) return true;
		if (effSo.equals("non_coding_exon_variant") && csqSo.equals("NMD_transcript_variant")) return true;
		return false;
	}

	/**
	 * Compare a single SnpEff results to a list of CSQs (ENSEMBL's VEP results)
	 * @return true if 'eff' matches any CSQ
	 */
	boolean compare(VcfEffect eff, List<VcfConsequence> csqs) {
		String effStr = eff.getEffectsStrSo();

		boolean foundTranscript = false;

		// Split all effects
		for (String et : effStr.split("\\+")) {
			if (verbose) System.out.println("\t\t" + et + "\t" + eff.getTranscriptId());

			// Match all consequences
			for (VcfConsequence csq : csqs) {
				// Check in same transcript
				if (csq.getFeature().equals(eff.getTranscriptId())) {

					if (canCompare(eff, csq)) {
						foundTranscript = true;
						if (compare(eff, csq)) return true;
					}
				}
			}
		}

		return !foundTranscript; // If transcript was not found, then no match is expected
	}

	/**
	 * Comparison type dispatcher
	 */
	boolean compare(VcfEffect eff, VcfConsequence csq) {
		if (compareEffect) return compareEffect(eff, csq);
		if (compareHgvsDna || compareHgvsProt) return compareHgvs(eff, csq);
		throw new RuntimeException("Nothing to compare!");
	}

	/**
	 * Compare a single effect to CSQ
	 */
	boolean compareEffect(VcfEffect eff, VcfConsequence csq) {
		String effectTypes[] = eff.getEffectTypesStr().split("\\+");
		String consecuences[] = csq.getConsequence().split("&");

		for (String et : effectTypes) {
			for (String cons : consecuences) {
				if (compare(et, cons)) {
					countEff++;
					if (verbose) System.out.println("\t\t\tOK :" + eff.getTranscriptId() + "\t" + et + "\t" + cons);
					return true;
				}
				if (verbose) System.out.println("\t\t\t    " + eff.getTranscriptId() + "\t" + et + "\t" + cons);
			}
		}

		return false;
	}

	/**
	 * Compare a single effect to CSQ
	 */
	boolean compareHgvs(VcfEffect eff, VcfConsequence csq) {

		if (verbose) {
			String effHgsvDna = eff.getHgvsDna();
			String effHgsvProt = eff.getHgvsProt();
			System.out.println("\t\t\teff     : " + eff.getEffectTypesStr() //
					+ "\n\t\t\tcsq     : " + csq.getConsequence() //
					+ "\n\t\t\ttrId    : " + eff.getTranscriptId() + "\t" + csq.getFeature() //
					+ "\n\t\t\thgsv.c  : '" + effHgsvDna + "'\t'" + csq.getHgvsDna() + "'\t" + (compareHgvsDna(eff, csq) ? "OK" : "BAD") //
					+ "\n\t\t\thgsv.p  : '" + effHgsvProt + "'\t'" + csq.getHgvsProt() + "'\t" + (compareHgvsProt(eff, csq) ? "OK" : "BAD") //
					+ "\n" //
			);
		}

		return (!compareHgvsDna || compareHgvsDna(eff, csq)) //
				&& //
				(!compareHgvsProt || compareHgvsProt(eff, csq));
	}

	/**
	 * Compare HGSV DNA
	 */
	boolean compareHgvsDna(VcfEffect eff, VcfConsequence csq) {
		String effHgsv = eff.getHgvsDna();
		String csqHgvs = csq.getHgvsDna();
		if (csqHgvs.isEmpty() && effHgsv == null) return true;
		if (!csqHgvs.isEmpty() && effHgsv == null) return false;
		if (csqHgvs.isEmpty() && effHgsv != null) return false;

		csqHgvs = csqHgvs.substring(csqHgvs.indexOf(':') + 1);
		boolean eq = csqHgvs.equals(effHgsv);
		if (eq) countHgvsDna++;
		return eq;
	}

	/**
	 * Compare HGSV Protein
	 */
	boolean compareHgvsProt(VcfEffect eff, VcfConsequence csq) {

		String effHgsv = eff.getHgvsProt();
		String csqHgvs = csq.getHgvsProt();

		if (csqHgvs.isEmpty() && effHgsv == null) return true;
		if (!csqHgvs.isEmpty() && effHgsv == null) return false;
		if (csqHgvs.isEmpty() && effHgsv != null) return false;

		// This seems to be a bug in ENSEMBL's VEP
		// E.g.: 'ENST00000241356.4:c.945G>A(p.%3D)'
		if (csqHgvs.endsWith("(p.%3D)")) return true;

		csqHgvs = csqHgvs.substring(csqHgvs.indexOf(':') + 1);
		boolean eq = csqHgvs.equals(effHgsv);

		// We use short frame-shift description, whereas CSQ uses long terminology
		if (csqHgvs.indexOf("fs") > 0 && effHgsv.endsWith("fs")) {
			String effHgsvFs = effHgsv.substring(0, effHgsv.length() - 2); // Remove trailing 'fs'
			String csqHgsvFs = csqHgvs.substring(0, csqHgvs.indexOf("fs") - 3); // Remove last part, including last AA before 'fs' (3 leter code)
			eq = csqHgsvFs.equals(effHgsvFs);
		}

		if (eq) countHgvsProt++;
		return eq;
	}

	/**
	 * Compare all VCF entries
	 */
	public void compareVep(String vcf) {
		if (verbose) System.out.println(this.getClass().getSimpleName() + ": Compare VEP, genome " + genomeName + ", file " + vcf);

		parseCsqHeader(vcf);

		// Run and compare VCF entries
		List<VcfEntry> vcfEnties = runSnpEff(args(genomeName, vcf));
		for (VcfEntry ve : vcfEnties) {
			List<VcfConsequence> csqs = VcfConsequence.parse(vcfCsqHeader, ve);
			List<VcfEffect> effs = ve.parseEffects();

			if (verbose) {
				System.out.println(ve);

				System.out.println("\tCSQ:");
				for (VcfConsequence csq : csqs)
					System.out.println("\t\t" + csq);

				System.out.println("\tEFF:");
				for (VcfEffect eff : effs)
					System.out.println("\t\t" + eff);

				System.out.println("\tCompare:");
			}

			Assert.assertTrue("EFF and CSQ do not match", compare(effs, csqs));
		}
	}

	void parseCsqHeader(String vcfFileName) {
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		vcf.next();
		vcfCsqHeader = new VcfConsequenceHeader(vcf);
	}

	/**
	 * Run SnpEFf and return a list of results
	 */
	List<VcfEntry> runSnpEff(String[] args) {
		SnpEff cmd = new SnpEff(args);
		cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);
		cmdEff.setDebug(debug);

		List<VcfEntry> vcfEnties = cmdEff.run(true);
		return vcfEnties;
	}

	public void setCompareHgvs() {
		compareEffect = false;
		compareHgvsDna = compareHgvsProt = true;
	}

	public void setCompareHgvsDna(boolean compareHgvsDna) {
		this.compareHgvsDna = compareHgvsDna;
	}

	public void setCompareHgvsProt(boolean compareHgvsProt) {
		this.compareHgvsProt = compareHgvsProt;
	}

	public void setOnlyProtein(boolean onlyProtein) {
		this.onlyProtein = onlyProtein;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	@Override
	public String toString() {
		if (compareHgvsDna || compareHgvsProt) return "HGVS DNA OK: " + countHgvsDna + "\tHGVS protein OK: " + countHgvsProt;
		return "Effects OK: " + countEff;
	}

}
