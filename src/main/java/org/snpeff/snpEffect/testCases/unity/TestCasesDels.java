package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.codons.CodonTable;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;
import org.snpeff.vcf.EffFormatVersion;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test random DEL changes
 *
 * @author pcingola
 */
public class TestCasesDels extends TestCasesBase {

	public static int N = 1000;

	boolean forcePositive = false || debug; // Force positive strand (used for debugging)

	public TestCasesDels() {
		super();
	}

	/**
	 * Calculate codonsNew using a naive algorithm
	 */
	String codonsNew(Variant variant) {
		int cdsBaseNum = 0;
		String codonsNew = "";
		char[] currCodon = new char[3];

		boolean useCodon = false;
		currCodon[0] = currCodon[1] = currCodon[2] = ' ';
		for (Exon exon : transcript.sortedStrand()) {
			int step = exon.isStrandPlus() ? 1 : -1;
			int beg = exon.isStrandPlus() ? exon.getStart() : exon.getEndClosed();

			for (int pos = beg; (pos >= exon.getStart()) && (pos <= exon.getEndClosed()); pos += step, cdsBaseNum++) {
				int cdsCodonPos = cdsBaseNum % 3;

				// Should we use this codon?
				if (variant.intersects(pos)) useCodon = true;
				else {
					// Should we use this base? We don't use the ones that intersect with 'seqChage' (because they are deleted)
					char base = chromoBases[pos];
					currCodon[cdsCodonPos] = exon.isStrandPlus() ? base : GprSeq.wc(base); // Update current codon
				}

				// Add it?
				if (cdsCodonPos == 2) {
					if (useCodon) codonsNew += new String(currCodon);
					useCodon = false;
					currCodon[0] = currCodon[1] = currCodon[2] = ' ';
				}
			}
		}

		// Last 'codon' might be incomplete (only one or two bases)
		if (useCodon) codonsNew += new String(currCodon);

		// Remove white spaces
		return removeWhiteSpaces(codonsNew);
	}

	/**
	 * Calculate codonsOld using a naive algorithm
	 */
	String codonsOld(Variant variant) {
		int cdsBaseNum = 0;
		String codonsOld = "";
		char[] currCodon = new char[3];

		boolean useCodon = false;
		currCodon[0] = currCodon[1] = currCodon[2] = ' ';
		for (Exon exon : transcript.sortedStrand()) {
			int step = exon.isStrandPlus() ? 1 : -1;
			int beg = exon.isStrandPlus() ? exon.getStart() : exon.getEndClosed();

			for (int pos = beg; (pos >= exon.getStart()) && (pos <= exon.getEndClosed()); pos += step, cdsBaseNum++) {
				int cdsCodonPos = cdsBaseNum % 3;

				useCodon |= variant.intersects(pos); // Should we use this codon?
				char base = chromoBases[pos];
				currCodon[cdsCodonPos] = exon.isStrandPlus() ? base : GprSeq.wc(base); // Update current codon

				// Finished codon?
				if (cdsCodonPos == 2) {
					if (useCodon) codonsOld += new String(currCodon);
					useCodon = false;
					currCodon[0] = currCodon[1] = currCodon[2] = ' ';
				}
			}
		}

		// Last 'codon' might be incomplete (only one or two bases)
		if (useCodon) codonsOld += new String(currCodon);

		// Remove white spaces
		return removeWhiteSpaces(codonsOld);
	}

	@Override
	protected void init() {
		super.init();
		randSeed = 20100629;
	}

	boolean isExonDel(Transcript tr, Variant variant) {
		for (Exon ex : tr)
			if (variant.includes(ex)) return true;
		return false;
	}

	/**
	 * Remove white spaces from a string.
	 * @return A string without any white spaces. '-' resulting string is empty
	 */
	String removeWhiteSpaces(String str) {
		String strNoWs = "";
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) != ' ') strNoWs += str.charAt(i);
		}

		return strNoWs;
	}

	@Test
	public void test_01() {
		Log.debug("Test");
		CodonTable codonTable = genome.codonTable();

		// Test N times
		//	- Create a random gene transcript, exons
		//	- Create a random Insert at each position
		//	- Calculate effect
		for (int i = 0; i < N; i++) {

			initSnpEffPredictor();
			if (debug) System.out.println("DEL Test iteration: " + i + "\n" + transcript);
			else if (verbose) Log.info("DEL Test iteration: " + i + "\t" + transcript.cds());
			else Gpr.showMark(i + 1, 1);
			int cdsBaseNum = 0;

			String transcriptStr = transcript.toString() + "\n" + transcript.toStringAsciiArt(false);

			// For each exon...
			for (Exon exon : transcript.sortedStrand()) {
				int step = exon.isStrandPlus() ? 1 : -1;
				int beg = exon.isStrandPlus() ? exon.getStart() : exon.getEndClosed();

				// For each base in this exon...
				for (int pos = beg; (pos >= exon.getStart()) && (pos <= exon.getEndClosed()); pos += step, cdsBaseNum++) {
					//---
					// Create variant
					//---

					// Get a random base different from 'refBase'
					int delLen = rand.nextInt(10) + 1;

					// Variants affecting only one exon
					int start = pos;
					int end = Math.min(pos + delLen, exon.getEndClosed());

					if (transcript.isStrandMinus()) {
						start = pos - delLen;
						end = pos;
					}

					if (start < 0) start = 0;
					if (end > chromosome.getEndClosed()) end = chromosome.getEndClosed();
					delLen = end - start + 1;

					String delPlus = chromoSequence.substring(start, end + 1); // Deletion (plus strand)
					String del = delPlus;

					// Codon number
					int cdsCodonNum = cdsBaseNum / 3;
					int cdsCodonPos = cdsBaseNum % 3;

					// Create a SeqChange
					Variant variant = new Variant(chromosome, start, "", "-" + del, "");

					// Sanity checks
					assertTrue(variant.isDel()); // Is it a deletion?
					assertEquals(del.length(), variant.size()); // Does variant have the correct size?

					//---
					// Expected Effect
					//---
					String effectExpected = "", aaExpected = "";
					String codonsOld = codonsOld(variant);
					codonsOld = codonsOld.toUpperCase();
					String aaOld = codonTable.aa(codonsOld);
					String codonsNew = codonsNew(variant);
					String aaNew = codonTable.aa(codonsNew);

					// Net change
					String netChange = "";
					for (Exon ex : transcript.sortedStrand())
						netChange += variant.netChange(ex);

					// Replace empty by '-'
					if (codonsOld.isEmpty()) codonsOld = "-";
					if (codonsNew.isEmpty()) codonsNew = "-";
					if (aaOld.isEmpty()) aaOld = "-";
					if (aaNew.isEmpty()) aaNew = "-";

					if (variant.includes(transcript)) {
						effectExpected = "TRANSCRIPT_DELETED";
					} else if (isExonDel(transcript, variant)) {
						effectExpected = "EXON_DELETED";
					} else if (netChange.length() % 3 != 0) {
						effectExpected = "FRAME_SHIFT";
						aaExpected = "(" + aaOld + "/" + "-" + ")";
					} else {
						if (cdsCodonPos == 0) {
							effectExpected = "CODON_DELETION";
							aaExpected = "(" + aaOld + "/-)";
						} else {
							if (codonsOld.startsWith(codonsNew) || (codonsNew.equals("-"))) {
								effectExpected = "CODON_DELETION";
								aaExpected = "(" + aaOld + "/" + aaNew + ")";
							} else {
								effectExpected = "CODON_CHANGE_PLUS_CODON_DELETION";
								aaExpected = "(" + aaOld + "/" + aaNew + ")";
							}
						}

						if ((cdsCodonNum == 0) && codonTable.isStartFirst(codonsOld) && !codonTable.isStartFirst(codonsNew)) {
							effectExpected = "START_LOST";
							aaExpected = "(" + aaOld + "/" + aaNew + ")";
						} else if ((aaOld.indexOf('*') >= 0) && (aaNew.indexOf('*') < 0)) {
							effectExpected = "STOP_LOST";
							aaExpected = "(" + aaOld + "/" + aaNew + ")";
						} else if ((aaNew.indexOf('*') >= 0) && (aaOld.indexOf('*') < 0)) {
							effectExpected = "STOP_GAINED";
							aaExpected = "(" + aaOld + "/" + aaNew + ")";
						}
					}

					//---
					// Calculate effects
					//---
					VariantEffects effects = snpEffectPredictor.variantEffect(variant);

					//---
					// Check number of effects
					//---

					// There should be only one effect in most cases
					if (effects.isEmpty()) System.err.println("No effects for variant: " + variant + ", expecting '" + effectExpected + "'");
					assertFalse(effects.isEmpty()); // There should be at least one effect

					if (debug && (effects.size() > 1)) {
						System.out.println("Found more than one effect: " + effects.size() + "\n" + transcript);
						System.out.println("\tEffects: ");
						for (VariantEffect eff : effects)
							System.out.println("\t" + eff);
					}

					//---
					// Check effects
					//---
					boolean ok = false;
					for (VariantEffect effect : effects) {
						String effStr = effect.effect(true, false, true, false, false);
						String effFullStr = effect.effect(true, true, true, false, false);

						String line = "\tIteration: " + i + "\tPos: " + pos + "\tExpected: '" + effectExpected + "'\tEffect: '" + effStr + "'";
						StringBuilder msg = new StringBuilder();
						msg.append(line + "\n");
						msg.append("\tIteration: " + i);
						msg.append("\tPos: " + pos);
						msg.append("\n\t\tCDS base [codon] : " + cdsBaseNum + " [" + cdsCodonNum + ":" + cdsCodonPos + "]");
						msg.append("\n\t\tVariant          : " + variant + "\tsize: " + variant.size() + "\tdelPlus: " + delPlus);
						msg.append("\n\t\tNetCdsChange     : " + netChange);
						msg.append("\n\t\tEffect expected  : " + effectExpected);
						msg.append("\n\t\tEffect           : " + effStr + "\t" + effFullStr);
						msg.append("\n\t\tAA expected      : '" + aaOld + "' / '" + aaNew + "'\t" + aaExpected);
						msg.append("\n\t\tAA               : '" + effect.getAaRef() + "' / '" + effect.getAaAlt() + "'");
						msg.append("\n\t\tCodon expected   : '" + codonsOld + "' / '" + codonsNew + "'");
						msg.append("\n\t\tCodons           : '" + effect.getCodonsRef().toUpperCase() + "' / '" + effect.getCodonsAlt().toUpperCase() + "'");
						msg.append("\n\nTranscript:\n" + transcriptStr + "\n");
						msg.append(line + "\n");

						if (debug) System.out.println(msg);
						else if (verbose) Log.info(line);

						for (String e : effStr.split("\\" + EffFormatVersion.EFFECT_TYPE_SEPARATOR_OLD)) {
							if (effectExpected.equals(e)) {
								ok = true;
								// Check codons, except some cases
								if ((effect.getEffectType() != EffectType.FRAME_SHIFT) //
										&& (effect.getEffectType() != EffectType.TRANSCRIPT_DELETED) //
										&& (effect.getEffectType() != EffectType.EXON_DELETED) //
										&& (effect.getEffectType() != EffectType.SPLICE_SITE_REGION) //
										&& (effect.getEffectType() != EffectType.INTERGENIC) //
								) {
									if (codonsNew.equals("-")) codonsNew = "";

									String codonsNewEff = effect.getCodonsAlt().toUpperCase();
									if (codonsNewEff.equals("-")) codonsNewEff = "";

									assertEquals(codonsOld, effect.getCodonsRef().toUpperCase(), msg.toString()); // Check codons old
									assertEquals(codonsNew, codonsNewEff, msg.toString()); // Check codons new
								}
							}
						}
					}

					if (!ok) {
						System.err.println("Cannot find '" + effectExpected + "'");
					}

					assertTrue(ok);
				}
			}
		}

		System.err.println();
	}
}
