package org.snpeff.annotate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.snpeff.collections.AutoHashMap;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEntry;

/**
 * Command line program: Predict variant effects
 *
 * @author Pablo Cingolani
 */
public class AnnotateVcfHaplotypes extends AnnotateVcf {

	boolean saveResults;
	Queue<VcfEntry> queue;
	VcfEntry latestVcfEntry;
	AutoHashMap<VcfEntry, VariantEffects> effsByVcfEntry;
	SameCodonHaplotype sameCodonHaplotype;
	List<VcfEntry> vcfEntries;

	public AnnotateVcfHaplotypes() {
		super();
		queue = new LinkedList<>();
		effsByVcfEntry = new AutoHashMap<>(new VariantEffects());
		sameCodonHaplotype = new SameCodonHaplotype();
		vcfEntries = new ArrayList<>();
	}

	/**
	 * Add entry
	 */
	void add(VcfEntry ve) {
		latestVcfEntry = ve;
		queue.add(ve);
	}

	@Override
	protected void addVariantEffect(VcfEntry ve, Variant variant, VariantEffect variantEffect) {
		if (debug) Gpr.debug("Adding:" + ve.toStr() + "\t" + variantEffect);
		effsByVcfEntry.getOrCreate(ve).add(variantEffect);

		// Detection strategies
		sameCodonHaplotype.add(ve, variant, variantEffect);
	}

	/**
	 * Annotate a VCF entry
	 */
	@Override
	public boolean annotate(VcfEntry vcfEntry) {
		add(vcfEntry);
		super.annotate(vcfEntry);
		if (saveResults) vcfEntries.add(vcfEntry);
		return true;
	}

	@Override
	public boolean annotateFinish(VcfFileIterator vcfFile) {
		flush(); // Make sure we output all VcfEntries
		return super.annotateFinish(vcfFile);
	}

	/**
	 * Can we print this VcfEntry?
	 * I.e. is this entry free from any restrictions?
	 */
	boolean canPrint(VcfEntry ve) {
		return (!sameChromosome(ve)) // Difference chromosomes? No restrictions
				|| (!sameCodon(ve) // Different codons?
						&& !compensatingFrameShift(ve) // Different genes?
				);
	}

	/**
	 * Could this entry be a compensating Is this entry at the same codon
	 * (and transcript) than the latest entry processed?
	 */
	public boolean compensatingFrameShift(VcfEntry ve) {
		return false;
	}

	/**
	 * Remove first entry from the queue
	 */
	VcfEntry dequeue() {
		VcfEntry ve = queue.remove();
		if (latestVcfEntry == ve) latestVcfEntry = null;
		effsByVcfEntry.remove(ve);
		sameCodonHaplotype.remove(ve);
		return ve;
	}

	/**
	 * Flush the whole queue
	 */
	void flush() {
		while (!queue.isEmpty())
			print(queue.remove());
	}

	public List<VcfEntry> getVcfEntries() {
		return vcfEntries;
	}

	VcfEntry peek() {
		return queue.peek();
	}

	/**
	 * Print as many items in the queue as possible
	 */
	@Override
	protected void print() {
		// If so, print is and remove it from the list
		// until we wether cannot print or the queue is at
		// least one element
		// Note: If the queue has only one element, we don't
		// know whether the next entry will create a restriction
		// or not, so we cannot remove it
		while (size() > 1 && canPrint(peek())) {
			print(peek());
			dequeue(); // Remove all information (queue, variant effects, etc)
		}
	}

	protected void print(VcfEntry ve) {
		vcfOutputFormatter.setVcfEntry(ve);
		VariantEffects effs = effsByVcfEntry.get(ve);
		vcfOutputFormatter.add(effs);
		vcfOutputFormatter.print();
	}

	/**
	 * Is this entry at the same chromosome as the latest VcfEntry processed?
	 */
	boolean sameChromosome(VcfEntry ve) {
		return ve.getChromosomeName().equals(latestVcfEntry.getChromosomeName());
	}

	/**
	 * Is this entry at the same codon (and transcript) than the latest entry processed?
	 *
	 */
	public boolean sameCodon(VcfEntry ve) {
		return sameCodonHaplotype.hasSameCodon(ve);
	}

	public void setKeepAll(boolean keepAll) {
		sameCodonHaplotype.setKeepAll(keepAll);
	}

	public void setSaveResults(boolean saveResults) {
		this.saveResults = saveResults;
	}

	int size() {
		return queue.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName() //
				+ ", queue size: " + queue.size() //
				+ ", map(vcfentry => effs) size: " + effsByVcfEntry.size() //
				+ "\n" //
		);

		int num = 0;
		for (VcfEntry ve : queue) {
			sb.append(num //
					+ "\tsameChr: " + sameChromosome(ve) //
					+ "\tsameCodon: " + sameCodon(ve) //
					+ "\tcompFs: " + compensatingFrameShift(ve) //
					+ "\t" + ve //
					+ "\n");
			num++;
		}
		return sb.toString();
	}

}
