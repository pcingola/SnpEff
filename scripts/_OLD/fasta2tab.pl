#!/usr/bin/perl

#------------------------------------------------------------------------------
# Transform a fasta file to a TXT (tab-separated) table
#
#																Pablo Cingolani
#------------------------------------------------------------------------------

use strict;

#------------------------------------------------------------------------------
# Main
#------------------------------------------------------------------------------

my($seq, $name) = ('', '');
my($lineNum, $l, $newName);
#---
# Read fasta file
#---
for($lineNum=0 ; $l = <STDIN> ; $lineNum++ ) {
	chomp $l;
	if( $l =~/^>\s*(.*)\s*$/ ) {
		$newName = $1;
		if( $seq ne "" ) { print "$name\t$seq\n"; } 
		# New sequence
		$name = $newName;
		$seq = "";
	} else { $seq .= $l; }
}

if( $seq ne "" ) { print "$name\t$seq\n"; }

