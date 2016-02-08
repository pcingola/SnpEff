package org.snpeff.akka.vcf;

import org.snpeff.akka.Master;
import org.snpeff.akka.msg.StartMaster;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.vcf.VcfEntry;

import akka.actor.Props;

/**
 * A master that prcess VCF files
 *
 * @author pablocingolani
 */
public class MasterVcf<T> extends Master<VcfEntry, T> {

	protected boolean parseNow = true; // Parse VCF in master process or let the workers take care of that?
	protected boolean showHeader = true; // Show VCF file header?
	protected String addHeader[]; // Add lines to VCF header before showing it
	protected VcfFileIterator vcfFileIterator;

	public MasterVcf(Props props, int numWorkers) {
		super(props, numWorkers);
	}

	@Override
	public boolean hasNext() {
		return vcfFileIterator.hasNext();
	}

	@Override
	public VcfEntry next() {
		return vcfFileIterator.next();
	}

	public void setAddHeader(String[] addHeader) {
		this.addHeader = addHeader;
	}

	public void setShowHeader(boolean showHeader) {
		this.showHeader = showHeader;
	}

	/**
	 * Start up processing
	 */
	@Override
	protected void startMaster(StartMaster startMaster) {
		try {
			super.startMaster(startMaster);
			StartMasterVcf startMasterVcf = (StartMasterVcf) startMaster;
			vcfFileIterator = new VcfFileIterator(startMasterVcf.vcfFileName, startMasterVcf.config.getGenome());
			vcfFileIterator.setParseNow(parseNow);

			// Show header
			if (showHeader) {
				// Read header
				vcfFileIterator.readHeader();

				// Add lines header?
				if (addHeader != null) {
					// Add header lines
					for (String add : addHeader)
						vcfFileIterator.getVcfHeader().addLine(add);
				}

				// Show header
				String headerStr = vcfFileIterator.getVcfHeader().toString();
				if (!headerStr.isEmpty()) System.out.println(headerStr);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			shutdown();
		}
	}
}
