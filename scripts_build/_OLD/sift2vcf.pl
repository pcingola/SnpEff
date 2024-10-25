#!/usr/bin/perl


print "##INFO=<ID=SIFT_SCORE,Number=1,Type=Float,Description=\"SIFT score, 0 = Damaging, 1=Tolerated\">\n";
print "##INFO=<ID=SIFT_CONS,Number=1,Type=Float,Description=\"SIFT median conservation value, as log2. 0=High confidence, 4.32=Low confidence\">\n";
print "##INFO=<ID=SIFT_SEQS,Number=1,Type=Integer,Description=\"SIFT number of sequences at position\">\n";
print "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\n";

while( $l = <STDIN> ) {
	chomp $l;
	($chr, $coord1, $nt1, $nt2, $score, $median, $seqs_rep) = split /\t/, $l;

	# Trim spaces
	if( $seqs_rep =~ /\s(.*)/ )	{ $seqs_rep = $1; }

	$coord1++;	# Get it in one-based coordinates

	print "$chr\t$coord1\t.\t$nt1\t$nt2\t.\t.\tSIFT_SCORE=$score;SIFT_CONS=$median;SIFT_SEQS=$seqs_rep\n";
}
