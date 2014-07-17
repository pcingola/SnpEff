#!/usr/bin/perl

while( $l = <STDIN> ) {
	chomp $l;
	@t = split /\t/, $l;

	if( $t[2] eq 'exon' ) {
		$t[7] = ".";
		for( $i=0 ; $i <= $#t ; $i++ ) {
			print "\t" if $i > 0;
			print $t[$i];
		}
		print "\n";
	} else {
		print "$l\n";
	}
}
