package ca.mcgill.mcb.pcingola.snpEffect;

import java.util.List;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import ca.mcgill.mcb.pcingola.vcf.VcfHeader;

/**
 * Annotate a VCF file: E.g. add information to INFO column
 *
 */
public interface VcfAnnotator {

	/**
	 * Annotate a VCF file entry
	 */
	public void annotate(VcfEntry vcfEntry);

	/**
	 * This method is called after all annotations have been performed.
	 * The vcfFile might have already been closed by this time
	 * (i.e. the VcfFileIterator reached the end).
	 */
	public void finishAnnotate();

	/**
	 * Provide a list of header lines to be added to annotated VCF header
	 */
	public List<VcfHeader> header();

	/**
	 * Initialize annotator: This method is called after vcfFile
	 * is opened, but before the header is output.
	 * The first vcfEntry might have (and often has) already been
	 * read from the file.
	 */
	public void initAnnotate(VcfFileIterator vcfFile);

}
