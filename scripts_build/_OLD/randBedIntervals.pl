#!/usr/bin/perl

# Number of intervals per chromosome
$intsPerChr = 10;

# Max interval len
$maxLen = 1000;

# Chromosome length
$len{'chr2L'} = 23299195;
$len{'chr2LHet'} = 373492;
$len{'chr2R'} = 21411048;
$len{'chr2RHet'} = 3329880;
$len{'chr3L'} = 24850358;
$len{'chr3LHet'} = 2587444;
$len{'chr3R'} = 28253873;
$len{'chr3RHet'} = 2548985;
$len{'chr4'} = 1368761;
$len{'chrdmel_mitochondrion_genome'} = 19790;
$len{'chrUextra'} = 29367225;
$len{'chrU'} = 10174655;
$len{'chrX'} = 22703118;
$len{'chrXHet'} = 206671;
$len{'chrYHet'} = 351384;

foreach $chr ( sort keys %len ) {
	$max = $len{$chr} - $maxLen - 1000;

	for( $i=0 ; $i < $intsPerChr ; $i++ ) {
		$start = int( rand() * $max );
		$end = int( rand() * $maxLen ) + $start;

		print "$chr\t$start\t$end\n";
	}
}

