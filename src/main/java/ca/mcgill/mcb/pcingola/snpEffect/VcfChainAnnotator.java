package ca.mcgill.mcb.pcingola.snpEffect;

import java.util.LinkedList;
import java.util.List;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import ca.mcgill.mcb.pcingola.vcf.VcfHeader;

/**
 * Maintains a list of VcfAnnotators and applies them one by one
 * in the specified order
 */
public class VcfChainAnnotator implements VcfAnnotator {

	List<VcfAnnotator> annotators;

	public VcfChainAnnotator() {
		annotators = new LinkedList<>();
	}

	/**
	 * Add a new annotator
	 */
	public void add(VcfAnnotator vcfAnnotator) {
		annotators.add(vcfAnnotator);
	}

	@Override
	public void annotate(VcfEntry vcfEntry) {
		for (VcfAnnotator vcfAnnotator : annotators)
			vcfAnnotator.annotate(vcfEntry);
	}

	@Override
	public void finishAnnotate() {
		for (VcfAnnotator vcfAnnotator : annotators)
			vcfAnnotator.finishAnnotate();
	}

	@Override
	public List<VcfHeader> header() {
		List<VcfHeader> headers = new LinkedList<>();

		for (VcfAnnotator vcfAnnotator : annotators)
			headers.addAll(vcfAnnotator.header());

		return headers;
	}

	@Override
	public void initAnnotate(VcfFileIterator vcfFile) {
		for (VcfAnnotator vcfAnnotator : annotators)
			vcfAnnotator.initAnnotate(vcfFile);
	}

}
