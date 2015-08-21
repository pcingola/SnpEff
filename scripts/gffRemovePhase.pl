#!/usr/bin/perl

while ( $l = <STDIN> ) {
	chomp $l;
	if( $l =~ /^#/ ) { print "$l\n"; }
	else {
		@t = split /\t/,$l;
		$t[7] = ".";
		print join("\t", @t) . "\n";
	}
}
