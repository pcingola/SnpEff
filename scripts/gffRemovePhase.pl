#!/usr/bin/perl

while ( $l = <STDIN> ) {
	chomp $l;
	if( $l =~ /^#/ ) { 
		print "$l\n"; 
	} else {
		@t = split /\t/,$l;
		if( $#t > 7 ) {
			$t[7] = ".";
			print join("\t", @t) . "\n";
		} else {
			print "$l\n"; 
		}
	}
}
