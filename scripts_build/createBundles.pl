#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Create file bundles for ENSEMBL's BFMPP files
# Each 'bundle' is a ZIP file having at roughly '$bundleSize' MB size
#
#-------------------------------------------------------------------------------

use strict;


my($fileList) = @ARGV[0];							# Package version
my($bundleBaseName) = @ARGV[1];						# Base name for bundles

die "Usage: ./createBundles.pl genomes_list.txt bundleBaseName" if $bundleBaseName eq '';

my($debug) = 0;										# Debug mode?
my($bundleSize) = 150;								# Maximum bundle size in MB
my($dataDir) = "$ENV{'HOME'}/snpEff/data";			# Data dir
my($bundleNum) = 1;									# Bundle number
my($avgSize) = 2;									# Assume this 'average size when 'snpEffectPredictor.bin' is missing

# Read file sizes
open GEN, $fileList;
my($sum) = 0;
my($ziplist) = "";
my($gen, $bundleName, $size, $bin, %bundles);
while( $gen = <GEN> ) {
	print "GEN: $gen" if $debug;
	chomp $gen;

	$bin = "$dataDir/$gen/snpEffectPredictor.bin";

	# Calculate 'bin' file size (in MB)
	my($du) = `du -m $bin 2> /dev/null`;
	if( $du =~ /(\d+)\s+(.*)/ ) {
		$size = $1;
		print "bundles{$bundleName}\t|$gen|\t|$bin|\t|$size|\t$sum\n" if $debug;
	} else { 
		$size = $avgSize;
		print STDERR "No match for genome '$gen'" if $debug;
	}

	# Add to bundle
	if( $sum > $bundleSize ) {
		# Create bundle
		print STDERR "\tCreating $bundleName.bundle\n";
		print "$bundleName.bundle : $bundles{$bundleName}\n";

		# Prepare for new bundle
		$bundleNum++;
		$sum = 0;
		$ziplist="";
	} 
	$sum += $size;
	$ziplist .= " $bin";

	# Bundle name
	$bundleName = "$bundleBaseName\_$bundleNum";

	# Append genome to bundle
	if( $bundles{$bundleName} ne '' )	{ $bundles{$bundleName} .= " "; }
	$bundles{$bundleName} .= $gen;
}

# Create last bundle
print STDERR "\tCreating $bundleName.bundle\n";
print "$bundleName.bundle : $bundles{$bundleName}\n";
