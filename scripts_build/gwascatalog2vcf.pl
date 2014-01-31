#!/usr/bin/perl

$l = <STDIN>;	# Ignore title line

while( $l = <STDIN> ) {
	@t = split /\t/, $l;

	for( $i=0 ; $i <= $#t ; $i++ ) {
		$s = $t[$i];
		if( $s =~ /^\s+(.*)$/ )	{ $s = $1; }
		if( $s =~ /^(.*)\s+$/ )	{ $s = $1; }
		$s =~ s/, /,/g;
		$s =~ tr/ ;=/___/;
		$t[$i] = $s;
	}

	$vcf = "";
	$vcf .= "$t[11]\t";			# CHR
	$vcf .= "$t[12]\t";			# POS
	$vcf .= "$t[21]\t";			# ID
	$vcf .= "A\t";				# REF (Amzingly, GwasCatalog doen't have REF/ALT fields)
	$vcf .= "T\t";				# ALT
	$vcf .= ".\t";				# QUAL
	$vcf .= "PASS\t";			# FILTER

	# Info
	$vcf .= "trait=$t[7]";	
	$vcf .= ";gene=$t[13]";
	$vcf .= ";date=$t[3]";
	$vcf .= ";pubmedId=$t[1]";
	$vcf .= ";p_value=$t[27]";
	$vcf .= ";OR_beta=$t[30]";

	print "$vcf\n";
}
