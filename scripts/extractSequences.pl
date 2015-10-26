#!/usr/bin/perl

#-------------------------------------------------------------------------------
# Extract fasta sequences matching names
#
#
#
#																Pablo Cingolani
#-------------------------------------------------------------------------------

$debug = 0;

# Check command line arguments
die "Usage: cat file.fa | ./extractSequences.pl id_1 .... id_N\n" if( $#ARGV < 0 );

# Read sequence names from command line (arguments)
foreach $name ( @ARGV ) {
	$names{$name} = 1;
	print "names{$name}\n" if $debug;
}

# Read and parse FASTA file from STDIN
$match = 0;
while( $l=<STDIN> ) {
	if( $l =~ />(.*)/ ) {
		$name = $1;

		if( $name =~ /^(.*?)\s+/ ) {
			$name = $1;
		}

		$match = ( $names{$name} ne '' );
		print "$l\tSequence name: '$name'\tmatch: $match\n" if $debug;
	}

	print $l if $match;
}
