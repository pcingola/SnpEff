package ca.mcgill.mcb.pcingola.snpEffect;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Annotate a VCF file: E.g. add information to INFO column
 *
 */
public interface VcfAnnotator {

	/**
	 * Add annotation headers to VCF file
	 *
	 * @return true if OK, false on error
	 */
	public boolean addHeaders(VcfFileIterator vcfFile);

	/**
	 * Annotate a VCF file entry
	 *
	 * @return true if OK, false on error
	 */
	public void annotate(VcfEntry vcfEntry);

	/**
	 * This method is called after all annotations have been performed.
	 * The vcfFile might have already been closed by this time
	 * (i.e. the VcfFileIterator reached the end).
	 *
	 * @return true if OK, false on error
	 */
	public boolean annotateFinish();

	/**
	 * Initialize annotator: This method is called after vcfFile
	 * is opened, but before the header is output.
	 * The first vcfEntry might have (and often has) already been
	 * read from the file.
	 *
	 * @return true if OK, false on error
	 */
	public boolean annotateInit(VcfFileIterator vcfFile);

}
