#!/usr/bin/perl

while( $l = <STDIN> ) { 
	if( $l =~ /^#/ ) { print $l; }
	else {
		chomp $l;
		@t = split /\t/, $l;

		$t[8] = "GT";

		for( $i=9 ; $i <= $#t ; $i++ ) {
			if( $t[$i] =~ /^(.*?):/ )	{ $t[$i] = $1; }
		}

		print join("\t", @t) . "\n";
	}
}
