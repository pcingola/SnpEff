#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Sort dbNSFP by hg19 coordinates
#
#
#															Pablo Cingolani 2016
#-------------------------------------------------------------------------------

#---
# Read all data into memory (we need a lot of memory)
#---
$showTitle = 1;
for( $i=1 ; $l = <STDIN> ; $i++ ) {
	if( $l =~ /^#chr/ ) {
		print $l if $showTitle;
		$showTitle = 0;
		next;
	}

	# Parse line (we only use the first 9 columns)
	@t = split /\t/, $l, 10;

	# Show progress
	print STDERR "$i\t$t[0]\t$t[1]\n" if( $i % 100000 == 0 );

	# Position in hg19 coordinates
	$chr = $t[7];
	$pos = $t[8];

	# Store lines by position
	if(($chr ne '') && ($pos ne '')) {
		# Replace coordinates (hg19)
		$t[0] = $t[7];
		$t[1] = $t[8];
		$lines{$chr}->[$pos] .=  join("\t", @t);
	}
}

#---
# Print sorted by chr:pos in hg19 coordinates
#---
print STDERR "Printing results\n";
foreach $chr (sort keys %lines) {
	print STDERR "\tChr: $chr\n";

	# Print all lines within this chromosome, sorted by hg19 position
	$lpos = $lines{$chr};
	$len = scalar @{ $lpos };

	for( $i=0 ; $i <= $#{$lpos} ; $i++ ) {
		$lines = $lpos->[$i];
		print $lines if $lines ne '';
		print STDERR "\t\t$i\n" if( $i % 1000000 == 0 );
	}
}

