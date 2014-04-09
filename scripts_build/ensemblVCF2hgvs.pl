#!/usr/bin/perl

$debug = 0;

while( $l = <STDIN> ) {
	chomp $l;

	# Split line
	($chr, $pos, $id, $ref, $alt, $q, $filter, $info) = split /\t/, $l;

	# Split INFO fileds
	@f = split /;/, $info;

	# Split annotation field
	@t = split /,/,$f[4];

	# Pick annotations from ENSEMBL's fields
	$hgvs = "";
	foreach $ann ( @t ) { 
		print "\t$ann\n" if $debug; 
		@a = split /\|/, $ann;

		if( $debug ) {
			for( $i=0 ; $i <= $#a ; $i++ ) {
				print "\t\t$i\t$a[$i]\n"; 
			}
		}

		if( $a[4] =~ /intron_variant/ ) {
			$hgvs .= "," if $hgvs ne '';
			$hgvs .= $a[26]
		}
	}

	print "$chr\t$pos\t$id\t$ref\t$alt\t$q\t$filter\t$f[0];$f[1];$f[2];$f[3],HGVS=$hgvs\n" if $hgvs ne '';
}
