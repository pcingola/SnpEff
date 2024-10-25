#!/usr/bin/perl

#------------------------------------------------------------------------------
#
# Joins the lines of file_big and file_small by the common 
# values in column_big_file and column_file_small.
#
# Note: It is assumed the there column_file_small is a primery key in file_small. 
#------------------------------------------------------------------------------

use strict;

my(%linesByKey); # Lines from fileSmall, indexed by key

# Command line arguments
my($all) = 0;
if( $ARGV[0] eq '-all' ) { $all = 1; shift @ARGV; }

my($diff) = 0;
if( $ARGV[0] eq '-diff' ) { $diff = 1; shift @ARGV; }

my($fileBig, $colFileBig, $fileSmall, $colFileSmall) = ($ARGV[0], $ARGV[1], $ARGV[2], $ARGV[3]);
die "Usage: join.pl [-all|-diff] file_big.txt column_big_file file_small.txt column_file_small\n" if( $colFileSmall eq '');
$colFileBig--; $colFileSmall--; # Transform to zero-based

#---
# Read small file
#---
my($l, @t, $key, $i, $newLine);
open SF, $fileSmall;
while( $l = <SF> ) {
	chomp $l;
	@t = (); # Empty array
	@t = split /\t/, $l;
	$key = $t[$colFileSmall];
	$linesByKey{$key} = $l;
}
close SF;

#---
# Read fileBig
#---
open BF, $fileBig;
while( $l = <BF> ) {
	chomp $l;
	@t = split /\t/, $l;
	$key = $t[$colFileBig];

	if( $all )	{ print "$l\t$linesByKey{$key}\n"; }
	elsif( $diff ) {
		if( ! exists $linesByKey{$key} )	{ print "$l\t$linesByKey{$key}\n"; }
	} else {
		if( exists $linesByKey{$key} )		{ print "$l\t$linesByKey{$key}\n"; }
	}
}
close FB;

