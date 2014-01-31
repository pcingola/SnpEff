#!/usr/bin/perl

#------------------------------------------------------------------------------
# Split a fasta file (create one file per sequence)
#
#																Pablo Cingolani
#------------------------------------------------------------------------------

use strict;

#------------------------------------------------------------------------------
# Write fasta file
#------------------------------------------------------------------------------
sub writeSeq($$) {
	my($name, $seq) = @_;
	if( $name !~ /^chr/ ) { $name = "chr$name"; }
	$name = "$name.fa";
	print "Writing to $name\n";
	open OUT, "> $name";
	print OUT $seq;
	close OUT;
}

#------------------------------------------------------------------------------
# Main
#------------------------------------------------------------------------------

my($seq, $name) = ('', '');
my($lineNum, $l, $newName);
#---
# Read fasta file
#---
for($lineNum=0 ; $l = <STDIN> ; $lineNum++ ) {
	if( $l =~/^>\s*(.*?)\s+.*/ ) {
		$newName = $1;
		if( $seq ne "" ) { writeSeq($name, $seq); } 
		# New sequence
		$name = $newName;
		$seq = $l;
	} else { $seq .= $l; }
}

if( $seq ne "" ) { writeSeq($name, $seq); } 
