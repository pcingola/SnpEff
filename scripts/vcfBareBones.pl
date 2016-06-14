#!/usr/bin/perl

while( $l = <STDIN> ) {
	chomp $l;
	@t = split /\t/, $l;

	if( $l =~ /^#/ ) {
		if( $l =~ /^#CHROM/ ) { print "$l\n"; } # Only use title line
	} else {
		$t[2] = ".";	# ID
		$t[5] = ".";	# QUALITY
		$t[6] = ".";	# FILTER

		# INFO
		#if( $t[7] =~ /[\t;](END=\d+)[;\t]/ ) {
		if( $t[7] =~ /(END=\d+)/ ) {
			# If there is an 'END' field, keep it
			$t[7] = $1;
		} else {
			$t[7] = ".";
		}

		# Show fields
		for( $i=0 ; $i < 8 ; $i++ ) {
			print "\t" if $i > 0;
			print $t[$i];
		}

		# Show sample fields (only show genotypes)
		for( $i=8 ; $i <= $#t ; $i++ ) {
			$f = $t[$i];

			if( $f =~ /(.*?):/ )	{ $f = $1; }
			if( $f =~ /(.*?);/ )	{ $f = $1; }

			print "\t$f";
		}
		print "\n";
	}

}
