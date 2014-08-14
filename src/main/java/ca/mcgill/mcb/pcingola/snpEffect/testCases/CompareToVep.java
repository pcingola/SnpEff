package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
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

	boolean compareEffect = true;
	boolean compareHgvs = false;
	boolean debug = false;
	boolean verbose = false;
	boolean throwException = true;
	String genomeName;
	String addArgs[];
	VcfConsequenceHeader vcfCsqHeader;

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
		args.add(genomeName);
		args.add(vcf);

		return args.toArray(new String[0]);
	}

	boolean canCompare(VcfEffect eff, VcfConsequence csq) {
		if (compareHgvs) {

			// These do not produce HGSV notation, so we cannot compare them
			if (eff.getEffectType() == EffectType.DOWNSTREAM || eff.getEffectType() == EffectType.UPSTREAM) return false;
		}
		return true;
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
		if (compareHgvs) return compareHgvs(eff, csq);
		throw new RuntimeException("Nothing to compare!");
	}

	/**
	 * Compare a single effect to CSQ
	 */
	boolean compareEffect(VcfEffect eff, VcfConsequence csq) {
		List<EffectType> effectTypes = eff.getEffectTypes();
		String consecuences[] = csq.getConsequence().split("&");

		for (EffectType et : effectTypes) {
			for (String cons : consecuences) {
				if (compare(et.toString(), cons)) {
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

		String effHgsvDna = eff.getHgvsDna();
		String effHgsvProt = eff.getHgvsProt();

		if (verbose) System.out.println("\t\t\teff     : " + eff.getEffectTypesStr() //
				+ "\n\t\t\tcsq     : " + csq.getConsequence() //
				+ "\n\t\t\ttrId    : " + eff.getTranscriptId() + "\t" + csq.getFeature() //
				+ "\n\t\t\thgsv.c  : '" + effHgsvDna + "'\t'" + csq.getHgvsDna() + "'" //
				+ "\n\t\t\thgsv.p  : '" + effHgsvProt + "'\t'" + csq.getHgvsProt() + "'" //
		);

		return compareHgvsDna(eff, csq) && compareHgvsProt(eff, csq);
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
		return csqHgvs.equals(effHgsv);
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

		csqHgvs = csqHgvs.substring(csqHgvs.indexOf(':') + 1);
		return csqHgvs.equals(effHgsv);
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
			if (verbose) {
				System.out.println(ve);

				System.out.println("\tCSQ:");
				List<VcfConsequence> csqs = VcfConsequence.parse(vcfCsqHeader, ve);
				for (VcfConsequence csq : csqs)
					if (verbose) System.out.println("\t\t" + csq);

				System.out.println("\tEFF:");
				List<VcfEffect> effs = ve.parseEffects();
				for (VcfEffect eff : effs)
					if (verbose) System.out.println("\t\t" + eff);

				System.out.println("\tCompare:");
				Assert.assertTrue("EFF and CSQ do not match", compare(effs, csqs));
			}
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
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);
		cmdEff.setDebug(debug);

		List<VcfEntry> vcfEnties = cmdEff.run(true);
		return vcfEnties;
	}

	public void setCompareHgvs() {
		compareEffect = false;
		compareHgvs = true;
	}

}
