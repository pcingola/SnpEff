#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Convert sample names to sample number in a "SnpSift filter" expression
#
# It is easier to write expressions to depend on sample names 
# instead of sample numbers. This allows to replace the names by 
# numbers, so you can use it in SnpSift filter.
#
#
#															Pablo Cingolani
#-------------------------------------------------------------------------------

#---
# Parse command line 
#---
$vcf=$ARGV[0];
$filter=$ARGV[1];
die "Usage: snpSift_filter_sample_to_number.pl file.vcf expression\n" if $filter eq '';

#---
# Read VCF header and extract sample names to sample numbers mapping
#---
$sampleNamesFound = 0;
open VCF, $vcf or die "Cannot open VCF file '$vcf'\n";
while(($l = <VCF>) && ($l =~ /^#/)) {
	# Parse header lines having sample names
	if( $l =~ /#CHROM/ ) {
		$sampleNamesFound = 1;

		chomp $l;
		@t = split /\t/, $l;

		for( $i=0 ; $i <= $#t ; $i++ ) {
			($sname, $snum) = ($t[$i+9], $i);
			$sample{$sname} = $snum if $sname ne '';
		}
	}
}

die "Errro: VCF header not found. Unable to  parse sample names!" if ! $sampleNamesFound;

#---
# Replace all sample names by sample numbers
#---
foreach $sname ( sort keys %sample ) {
	$snum = $sample{$sname};
	$filter =~ s/$sname/$snum/g;
	# print "Replacing $sname by $snum:\t$filter\n";
}
print "$filter\n";
