package org.snpeff.nextProt;

/**
 * Mimics a node in NextProt XML file
 *
 * @author Pablo Cingolani
 *
 */
public class NextProtXmlNode {

	String accession;

	public NextProtXmlNode(String accession) {
		this.accession = accession;
	}

	public String getAccession() {
		return accession;
	}
}
