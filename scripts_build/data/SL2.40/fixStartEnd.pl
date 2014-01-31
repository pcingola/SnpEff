#!/usr/bin/perl

#
# If start > end swap values
#
while( $l = <STDIN> ) {
	chomp $l;
	@t = ();
	@t = split /\t/, $l;

	($start, $end) = ($t[3], $t[4]);
	if( $start > $end )	{ 
		#print STDERR "ERROR:\t$l\n"; 
		($t[3], $t[4]) = ($t[4], $t[3]);
		$l = join "\t", @t;
	}

	print "$l\n";
}
