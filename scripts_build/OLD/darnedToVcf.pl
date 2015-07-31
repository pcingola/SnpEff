#!/usr/bin/perl

#--------------------------------------------------------------------------------
#
# Convert a TXT file (from Darned) to a VCF file that we can use to annotate
#
# Reference: http://beamish.ucc.ie/
#
# Usage: cat hg19.txt | ./darnedToVcf.pl > darned.hg19.vcf
#
#															Pablo Cingolani
#--------------------------------------------------------------------------------

# WC-complement
%rwc = ( 
		"A" => "T"
		, "C" => "G"
		, "G" => "C"
		, "T" => "A"
		);

# Show header
print "##INFO=<ID=RNA_ED,Number=0,Type=Flag,Description=\"Rna editig site (putative)\">\n";
print "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\n";

# Read all lines from stdin
while( $l = <STDIN> ) {
	chomp $l;
	($chrom, $pos, $strand, $inchr, $inrna, $gene, $seqReg, $exReg, $source, $pubMedID) = split /\t/, $l;

	# Discard title line
	if( $pos > 0 ) {
		$ref = $inchr;
		$alt = $inrna;

		# Base 'I' gets translated as 'G'
		if( $alt eq "I" )	{ $alt = "G"; }

		# Negative strand?
		if( $strand eq "-" ) {
			$ref = $rwc{$ref};
			$alt = $rwc{$alt};
		}

		# Show VCF-like line
		print "$chrom\t$pos\t.\t$ref\t$alt\t.\t.\tRNA_ED=$inchr>$inrna\n";
	}
}

