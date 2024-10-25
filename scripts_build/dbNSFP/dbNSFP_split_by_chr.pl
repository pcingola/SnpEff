#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Split dbNSFP by chromosome and replace genomic coordinates
# Note: By default use the first two columns as 'chr' and 'pos'
#       If you want to sort dbNSFP 3.X using hg19 genomic coordinates, you
#       have to specify columns 7 and 8. E.g.:
#
#			cat dbNSFP3.2a_variant.chr* \
#				| $HOME/snpEff/scripts_build/dbNSFP_split_by_chr.pl 7 8
#
#
#															Pablo Cingolani 2018
#-------------------------------------------------------------------------------

# By default use the first two columns as 'chr' and 'pos'
$chrCol = 0;
$posCol = 1;

# Two arguments? Use them as columns for chr and pos respectively
if( $#ARGV == 1 ) {
	$chrCol = $ARGV[0];
	$posCol = $ARGV[1];
} else {
	die "Usage cat dbNSFP*.txt | dbNSFP_split_by_chr.pl chrCol posCol\n";
}

# Maximum column to split
$splitCols = ( $chrCol > $posCol ? $chrCol : $posCol ) + 2;

# Read all lines
$title = '';
for( $i=1 ; $l = <STDIN> ; $i++ ) {
	if( $l =~ /^#chr/ ) {
		$title = $l;
		next;
	}

	# Parse line (we only use the first columns)
	@t = split /\t/, $l, $splitCols;

	# Genomic coordinates
	$chr = $t[$chrCol];
	$pos = $t[$posCol];

	# Show progress
	print STDERR "$i\t$chr\t$pos\n" if( $i % 100000 == 0 );

	# Skip line if missing data
	if(($chr eq '') || ($chr eq '.') || ($pos eq '')) { next; }

	# Replace coordinates
	$t[0] = $chr;
	$t[1] = $pos;
	$line =  join("\t", @t);

	# Store lines by chromosome
	$fh = $files{$chr};
	if( ! defined $fh ) {
		$fn = "dbNSFP.split.$chr.txt";
		print STDERR "Creating file '$fn'\n";
		open($fh, ">", $fn);
		$files{$chr} = $fh;
	}
	print $fh $line;
}

# Close all files
foreach $chr (keys %files) {
	print STDERR "Closing file for chromosome '$chr'\n";
	$files{$chr}.close();
}
