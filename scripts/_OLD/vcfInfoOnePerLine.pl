#!/usr/bin/perl

# Split VCF into several lines
# Show one info field per line
#

while( $l = <STDIN> ) {
	chomp $l;
	if( $l !~ /^#/ ) {
		($chrom, $pos, $id, $ref, $alt, $qual, $filter, $info) = split /\t/, $l;
		print "$chrom\t$pos\t$id\t$ref\t$alt\t$qual\t$filter\n";

		foreach $in ( split /;/, $info ) {
			if( $in =~ /^(.*?)=(.*)/ ) {
				$key = $1;
				$values = $2;

				@vals = split /,/, $values;

				if( $#vals > 0 ) {
					foreach $val ( @vals ) { print "\t\t\t\t\t\t\t$key\t$val\n"; }
				} else {
					print "\t\t\t\t\t\t\t$key\t$values\n";
				}
			} else {
				print "\t\t\t\t\t\t\t$in\n";
			}
		}
	}
}
