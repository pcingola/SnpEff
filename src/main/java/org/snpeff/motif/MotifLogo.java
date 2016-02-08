package org.snpeff.motif;

import java.util.ArrayList;
import java.util.Collections;

import org.snpeff.snpEffect.EffectType;

/**
 * Store a base, size and compare them
 *
 * @author pcingola
 */
class BaseSize implements Comparable<BaseSize> {

	char base;
	double size;

	BaseSize(char base, double size) {
		this.base = base;
		this.size = size;
	}

	@Override
	public int compareTo(BaseSize baseScore) {
		if (size < baseScore.size) return -1;
		if (size > baseScore.size) return 1;
		return 0;
	}
}

/**
 * Create a DNA logo for a PWM
 *
 * References:
 * 	- See WebLogo http://weblogo.berkeley.edu/
 * 	- "WebLogo: A Sequence Logo Generator"
 *
 *
 * @author pcingola
 */
public class MotifLogo {

	static final int FAKE_COUNT_BASE = 1; // Fake count per base
	static final int FAKE_COUNT_TOTAL = 4 * FAKE_COUNT_BASE; // One for each base type
	static final double LOG2 = Math.log(2.0);

	Pwm pwm;

	/**
	 * Log base 2
	 */
	public static double log2(double p) {
		return Math.log(p) / LOG2;
	}

	public MotifLogo(Pwm pwm) {
		this.pwm = pwm;
	}

	/**
	 * Frequency of a given base and position
	 */
	double baseFrecuency(char base, int position) {
		int total = sumCount(position);
		int count = pwm.getCount(base, position);

		count += FAKE_COUNT_BASE; // Add a fake count of 1
		total += FAKE_COUNT_TOTAL; // Add a fake count of 4 (1 per base)

		return ((double) count) / total;
	}

	/**
	 * Sequence conservation. Measured as the difference between max entropy and observed entropy
	 */
	double seqConserv(int position) {
		// Max entropy
		double sMax = 2.0; // log2(N), where N is the number of symbols (N=4 for DNA)

		// Observed entropy
		double sObs = 0;
		for (char base : Pwm.BASES) {
			double p = baseFrecuency(base, position);
			sObs += -p * log2(p);
		}

		return (sMax - sObs) / sMax;
	}

	/**
	 * Sum all counts at a position
	 */
	int sumCount(int position) {
		int total = 0;
		for (char b : Pwm.BASES)
			total += pwm.getCount(b, position);
		return total;
	}

	/**
	 * Return an HTML string that represents the motif.
	 */
	public String toStringHtml(int width, int maxHeight, EffectType efectType) {
		StringBuffer sb = new StringBuffer();
		sb.append("<table border=0>\n\t</tr>\n");
		for (int pos = 0; pos < pwm.size(); pos++) {
			// Sequence conservation
			double seqConserv = seqConserv(pos);

			// Calculate all base sizes and sort by size
			ArrayList<BaseSize> bases = new ArrayList<BaseSize>();
			for (char base : Pwm.BASES) {
				double p = baseFrecuency(base, pos);
				double size = seqConserv * p;

				BaseSize baseSize = new BaseSize(base, size);
				bases.add(baseSize);
			}

			// Sort by size (biggest first)
			Collections.sort(bases, Collections.reverseOrder());

			// Color exons as grey
			int ppos = pos - pwm.size() / 2;
			String bgcolor = "";
			if (efectType == EffectType.SPLICE_SITE_DONOR && ppos <= 0) bgcolor = "bgcolor=#cccccc";
			if (efectType == EffectType.SPLICE_SITE_ACCEPTOR && ppos >= 0) bgcolor = "bgcolor=#cccccc";

			// Show
			sb.append("\t\t<td valign=bottom " + bgcolor + ">\n\t\t\t<center>\n");
			for (BaseSize baseSize : bases) {
				int height = (int) (maxHeight * baseSize.size);
				sb.append("\t\t\t<img border=0 src=\"" + baseSize.base + ".png\" width=" + width + " height=" + height + "\"><br>\n");
			}
			sb.append("\t\t\t" + ppos + "<br>\n\t\t\t</center>\n");
			sb.append("\t\t</td>\n");
		}
		sb.append("\t</tr>\n</table>\n");
		return sb.toString();
	}

}
