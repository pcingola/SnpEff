#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Split a WIG file by chromosome
#
#
#															Pablo Cingolani
#-------------------------------------------------------------------------------

$chrPrev = "";

# Parse STDIN
while( $l = <STDIN> ) {
	$chr = "";

	if( $l =~ /^variableStep\s+chrom=(\S+)/ ) {
		$chr = $1;

		# Open new chromosome file?
		if( $chr ne $chrPrev ) {
			# Close old file
			close OUT if $chrPrev ne "";

			# Open new file
			$file = "$chr.wig";
			print STDERR "Creating new file '$file'\n";
			open OUT, ">$file";

			$chrPrev = $chr;
		}
	}

	print OUT $l;
}

close OUT;
