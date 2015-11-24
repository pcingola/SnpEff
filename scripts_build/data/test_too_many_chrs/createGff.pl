#!/usr/bin/perl

$N = 1000;

for( $chr=1 ; $chr < $N ; $chr++ ) {
	print "chr$chr\t.\tgene\t10\t40\t.\t+\t.\tID=gene$chr\n";
	print "chr$chr\t.\tmRNA\t10\t40\t.\t+\t.\tID=tr$chr;Parent=gene$chr\n";
	print "chr$chr\t.\tCDS\t10\t40\t.\t+\t0\tID=cds$chr;Parent=tr$chr\n";
}

print "##FASTA\n";
for( $chr=1 ; $chr < $N ; $chr++ ) {
	print ">chr$chr\n";
	print "AGTAGATGGAACATCTAGGAATCATAATTTAGGTATGATTATCTAAAATTGAAAAATGTG\n";
	print "GTGGCTATCAAAGAAACTTAATGATCAAGCAAGAGGACCAAGATCGACGTCGTAGTTAGA\n";

}
