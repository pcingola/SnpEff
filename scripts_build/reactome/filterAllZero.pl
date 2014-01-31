#!/usr/bin/perl

while( $l = <STDIN> ) {
	chomp $l;
	@t = split /\t/, $l;

	$show=0;
	for( $i=2 ; (! $show) && ($i <= $#t); $i++ ) {
		if( $t[$i] != 0.0 ) { $show=1; }
	}

	print "$l\n" if $show;
}
