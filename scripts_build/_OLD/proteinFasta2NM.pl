#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Convert fasta headers from protein ID (NP_XXXX) into transcript ID NM_XXXX
#
#
#															Pablo Cingolani
#-------------------------------------------------------------------------------

# Parse command line arguments
$refLink = $ARGV[0];
die "Usage: cat file.fasta | ./proteinFasta2NM.pl refLink.txt > protein_NM.fasta" if $refLink eq '';

# Read refLink file
open RL, $refLink or die "Cannot opne file '$refLink'\n";
while( <RL> ) {
	chomp;
	@t = split /\t/;
	($nm, $np) = ($t[2], $t[3]);

	if( $np ne '' ) {
		if( $trId{$np} ne '' )	{ print STDERR "Error: Non empty entry '$np' = $trId{$np}\n"; }
		else					{ $trId{$np} = $nm; }
	}
}

# Read fasta file
while( <STDIN> ) {
	chomp;

	if( /^>(.*)/ ) {	# Header? => change form protein NP_XXX to transcript NM_XXXX
		# Get NM_ field
		@t = split /\|/;
		$np = $t[3];

		# Remove anything after the dot
		if( $np =~ /(.*)\./ )	{ $np = $1; }

		# Found a transcript ID? 
		if( $trId{$np} ne '' )	{ print ">$trId{$np}\n"; }
		else					{ print "$l\n"; }
	} else { print "$_\n"; }	# Show line
}

