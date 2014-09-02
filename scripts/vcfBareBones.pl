#!/usr/bin/perl

while( $l = <STDIN> ) {
	chomp $l;
	@t = split /\s+/, $l;

	if( $l =~ /^#/ ) {
		if( $l =~ /^#CHROM/ ) { print "$l\n"; } # Only use title line
	} else {
		$t[2] = ".";	# ID
		$t[5] = ".";	# QUALITY
		$t[6] = ".";	# FILTER
		$t[7] = ".";	# INFO

		# Cut INFO and GENOTYPES
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
