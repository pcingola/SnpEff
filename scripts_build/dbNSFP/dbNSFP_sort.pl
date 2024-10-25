#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Sort dbNSFP by genomic coordinates
# Note: By default use the first two columns as 'chr' and 'pos'
#       If you want to sort dbNSFP 3.X using hg19 genomic coordinates, you
#       have to specify columns 7 and 8. E.g.:
#
#			cat dbNSFP3.2a_variant.chr* \
#				| $HOME/snpEff/scripts_build/dbNSFP_sort.pl 7 8 \
#				> dbNSFP3.2a_hg19.txt
#
#
#															Pablo Cingolani 2016
#-------------------------------------------------------------------------------

# By default use the first two columns as 'chr' and 'pos'
$chrCol = 0;
$posCol = 1;

# Two arguments? Use them as columns for chr and pos respectively
if( $#ARGV < 0 ) {
	# No arguments? Use defaults
} elsif( $#ARGV == 1 ) {
	$chrCol = $ARGV[0];
	$posCol = $ARGV[1];
} else {
	die "Usage cat dbNSFP*.txt | dbNSFP_sort.pl chrCol posCol\n";
}

# Maximum column to split
$splitCols = ( $chrCol > $posCol ? $chrCol : $posCol ) + 2;

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

	# Parse line (we only use the first columns)
	@t = split /\t/, $l, $splitCols;

	# Genomic coordinates
	$chr = $t[$chrCol];
	$pos = $t[$posCol];

	# Show progress
	print STDERR "Read\t$i\t$chr\t$pos\n" if( $i % 100000 == 0 );

	# Skip line if missing data
	if(($chr eq '') || ($pos eq '')) { next; }

	# Replace coordinates
	$t[0] = $chr;
	$t[1] = $pos;

	# Store lines by position
	$lines{$chr}->[$pos] .=  join("\t", @t);
}

#---
# Print sorted by chr:pos coordinates
#---
foreach $chr (sort keys %lines) {
	# Print all lines within this chromosome, sorted by hg19 position
	$lpos = $lines{$chr};
	$len = scalar @{ $lpos };

	for( $i=0 ; $i <= $#{$lpos} ; $i++ ) {
		$lines = $lpos->[$i];
		print $lines if $lines ne '';
		print STDERR "Print\t$chr\t$i\n" if( $i % 1000000 == 0 );
	}
}
