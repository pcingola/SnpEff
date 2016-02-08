package org.snpeff.interval;

/**
 * A Marker that has 'frame' information (Exon and Cds)
 * 
 * @author pcingola
 */
public interface MarkerWithFrame {

	public int getFrame();

	public void setFrame(int frame);

}
