#!/usr/bin/perl

while( $l = <STDIN> ) {
	chomp $l;
	@t = split /\s+/, $l;

	if( $l =~ /^#/ ) {
		print "$l\n";
	} else {
		$t[2] = ".";
		$t[5] = ".";

		for( $i=0 ; $i <= $#t ; $i++ ) {
			print "\t" if $i > 0;
			$f = $t[$i];

			if( $f =~ /(.*?):/ )	{ $f = $1; }
			if( $f =~ /(.*?);/ )	{ $f = $1; }

			print $f;
		}
		print "\n";
	}

}
