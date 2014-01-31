#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Replace un-versioned transcript names with versioned ones
#
#
#																Pablo Cingolani
#-------------------------------------------------------------------------------

$refSeq = $ARGV[0];
die "Usage: cat protein.noVersion.fa | hg19_proteinFastaReplaceName.pl genes.txt.gz > protein.fa " if $refSeq eq'';

#---
# Read IDs from genes file
#---

# Use gzip?
$cmd = "cat";
if( $refSeq =~ /.*\.gz$/ ) { $cmd = "zcat"; }

open REFSEQ,"$cmd $refSeq |";
while( $l = <REFSEQ> ) {
	(@t) = split /\t/, $l;
	$geneId = $t[1];
	if( $geneId =~ /(.*)\./ )	{ $geneIdNoVer = $1; }
	# print "$geneId\t$geneIdNoVer\n";

	$id{ $geneIdNoVer } = $geneId;
}
close REFSEQ;

#---
# Read STDIN
#---
while( $l = <STDIN> ) {
	chomp $l;
	if( $l =~ /^>(.*)/ ) {
		$geneIdNoVer = $1;
		if( $id{$geneIdNoVer} ne '' )	{ print ">$id{$geneIdNoVer}\n"; }
		else 							{ print "$l\n"; }
	} else {
		print "$l\n";
	}
}
