#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Create file bundles for ENSEMBL's BFMPP files
# Each 'bundle' is a ZIP file having at roughly '$bundleSize' MB size
#
#-------------------------------------------------------------------------------

use strict;


my($version) = @ARGV[0];							# Package version
die "Usage: ./createBundles.pl versionNumber" if $version eq '';

my($debug) = 0;										# Debug mode?
my($bundleBaseName) = "ENSEMBL_BFMPP_$version";		# Base name for bundles
my($bundleSize) = 150;								# Maximum bundle size in MB
my($bundleNum) = 1;									# Bundle number

# Read file sizes
open DU, "du -sm GCA*$version/snpEffectPredictor.bin |";
my($sum) = 0;
my($ziplist) = "";
my($l, $bundleName, %bundles);
while( $l = <DU> ) {
	chomp $l;
	if( $l =~ /(\d+)\s+(.*?)\/(.*)/ ) {
		my($size, $genome, $bin) = ($1, $2, $3);
		

		if( $sum > $bundleSize ) {
			# New bundle
			$bundleNum++;
			$sum = 0;

			# Create bundle
			print "$bundleName.bundle : $bundles{$bundleName}\n";
			`zip $bundleName.zip $ziplist`;
		} 
		$sum += $size;
		$ziplist .= " $genome/$bin";

		# Bundle name
		$bundleName = "$bundleBaseName\_$bundleNum";

		# Append file to bundle
		if( $bundles{$bundleName} ne '' )	{ $bundles{$bundleName} .= " "; }
		$bundles{$bundleName} .= $genome;

		print "bundles{$bundleName}\t|$genome|\t|$bin|\t|$size|\t$sum\n" if $debug;
	} else {
		die "No match!";
	}
}

# Create last bundle
print "$bundleName.bundle : $bundles{$bundleName}\n";
`zip $bundleName.zip $ziplist`;
