#!/usr/bin/perl

#-------------------------------------------------------------------------------
# Add 'Parent' option in mRNA lines
#
# 								Pablo Cingolani
#-------------------------------------------------------------------------------

while( $l = <STDIN> ) {
	chomp $l;
	@t = split /\t/,$l;

	$type = $t[2];

	# Is it an mRNA?
	if( $type eq 'mRNA' ) {
		# Parse ID in options 
		$opts = $t[8];
		if( $opts =~ /ID=(.*?)-\d+;/ ) {
			$pid = $1;
			$pid =~ tr/t/g/;
			# Add 'Parent' option
			$l .= ";Parent=$pid";
		}
	}

	print "$l\n";
}
