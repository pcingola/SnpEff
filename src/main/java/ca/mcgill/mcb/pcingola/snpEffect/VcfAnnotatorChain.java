package ca.mcgill.mcb.pcingola.snpEffect;

import java.util.LinkedList;
import java.util.List;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Maintains a list of VcfAnnotators and applies them one by one
 * in the specified order
 */
public class VcfAnnotatorChain implements VcfAnnotator {

	List<VcfAnnotator> annotators;

	public VcfAnnotatorChain() {
		annotators = new LinkedList<>();
	}

	/**
	 * Add a new annotator
	 */
	public void add(VcfAnnotator vcfAnnotator) {
		annotators.add(vcfAnnotator);
	}

	@Override
	public boolean addHeaders(VcfFileIterator vcfFile) {
		boolean error = false;

		for (VcfAnnotator vcfAnnotator : annotators)
			error |= vcfAnnotator.addHeaders(vcfFile);

		return error;
	}

	@Override
	public void annotate(VcfEntry vcfEntry) {
		for (VcfAnnotator vcfAnnotator : annotators)
			vcfAnnotator.annotate(vcfEntry);
	}

	@Override
	public boolean annotateFinish() {
		boolean error = false;

		for (VcfAnnotator vcfAnnotator : annotators)
			error |= vcfAnnotator.annotateFinish();

		return error;
	}

	@Override
	public boolean annotateInit(VcfFileIterator vcfFile) {
		boolean error = false;

		for (VcfAnnotator vcfAnnotator : annotators)
			error |= vcfAnnotator.annotateInit(vcfFile);

		return error;
	}

}
