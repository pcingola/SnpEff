package org.snpeff.stats;

import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.VariantBnd;
import org.snpeff.vcf.VcfEffect;

/**
 * Pojo for translocation reports
 * @author pcingola
 */
public class TranslocationReport {

	Transcript tr1, tr2;
	VariantBnd var;
	VcfEffect veff;
	String svgPlot;

	public TranslocationReport(VariantBnd var, VcfEffect veff, Transcript tr1, Transcript tr2) {
		this.tr1 = tr1;
		this.tr2 = tr2;
		this.var = var;
		this.veff = veff;
	}

	public String getChr1() {
		return var.getChromosomeName();
	}

	public String getChr2() {
		return var.getEndPoint().getChromosomeName();
	}

	public String getGeneName1() {
		return ((Gene) tr1.getParent()).getGeneName();
	}

	public String getGeneName2() {
		return ((Gene) tr2.getParent()).getGeneName();
	}

	public String getHgvsC() {
		return veff.getHgvsC();
	}

	public String getHgvsP() {
		return veff.getHgvsP();
	}

	public String getImpact() {
		return veff.getImpact().toString();
	}

	public String getIndex() {
		return getChr1() + ":" + getPos1OneBased() //
				+ "-" + getChr2() + ":" + getPos2OneBased() //
				+ " " + getTrId1() + "-" + getTrId2() //
		;
	}

	/**
	 * Get position (as one-based coordinates)
	 */
	public int getPos1OneBased() {
		return var.getStart() + 1;
	}

	/**
	 * Get position (as one-based coordinates)
	 */
	public int getPos2OneBased() {
		return var.getEndPoint().getStart() + 1;
	}

	public String getSvgPlot() {
		return svgPlot;
	}

	public String getTrId1() {
		return tr1.getId();
	}

	public String getTrId2() {
		return tr2.getId();
	}

	public String getVariantEffect() {
		return veff.getEffString();
	}

	public String getVcfEffect() {
		return veff.toString();
	}

	public void setSvgPlot(String svgPlot) {
		this.svgPlot = svgPlot;
	}
}
