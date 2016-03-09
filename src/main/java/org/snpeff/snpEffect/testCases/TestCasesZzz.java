package org.snpeff.snpEffect.testCases;

import org.snpeff.vcf.EffFormatVersion;

/**
 * Test case for structural variants: Translocation (fusions)
 * 
 * We create two genes (one transcript each). Each gene is in one different chromosome 
 * 
 * Transcripts:
 * 1:10-90, strand: +, id:tr1, Protein
 *      Exons:
 *      1:10-30 'exon1', rank: 1, frame: ., sequence: tatttgtatgaggatttgagt
 *      1:40-90 'exon2', rank: 2, frame: ., sequence: tactcagtgctgggcaatcccttagctgtcgcgccgcttaccctactattc
 *      CDS     :   tatttgtatgaggatttgagttactcagtgctgggcaatcccttagctgtcgcgccgcttaccctactattc
 *      Protein :   YLYEDLSYSVLGNPLAVAPLTLLF
 *
 * 2:110-190, strand: +, id:tr2, Protein
 *      Exons:
 *      2:110-125 'exon3', rank: 1, frame: ., sequence: gttaatgggatttcac
 *      2:150-190 'exon4', rank: 2, frame: ., sequence: atgggaacggagtgtcgacagcaccttatggggagctatat
 *      CDS     :   gttaatgggatttcacatgggaacggagtgtcgacagcaccttatggggagctatat
 *      Protein :   VNGISHGNGVSTAPYGELY */
public class TestCasesZzz {

	EffFormatVersion formatVersion = EffFormatVersion.FORMAT_ANN;

	boolean debug = false;
	boolean verbose = false || debug;

}
