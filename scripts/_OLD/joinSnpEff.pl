#!/usr/bin/perl

#------------------------------------------------------------------------------
#
# Mark snps as X1, X2 or 'Both'
#
#------------------------------------------------------------------------------

use strict;

my($debug) = 0;

#------------------------------------------------------------------------------
# Read a file and index lines by SNP
#------------------------------------------------------------------------------
sub readSnps($) {
	my($file) = (@_);
	my($l, %snps);
	
	open SNP, $file || die "Cannot open file '$file'\n"; 
	while( $l = <SNP> ) {
		my($chr, $pos, $ref, $var) = split /\t/, $l;
		my($snp) = "$chr:$pos\_$ref/$var";
		$snps{$snp} .= $l;
	}
	close SNP;
	return %snps;
}

#------------------------------------------------------------------------------
# Print SNP info and quals
#------------------------------------------------------------------------------
sub printLine($$$$) {
	my($snp, $lines, $quals, $q) = (@_);
	my($line, @lines);
	(@lines) = split '\n', $lines;
	foreach $line ( @lines ) { 
		my($l) = replaceSnpQ($line, $q);
		print "$l\t$quals\n"; 
	}
}

#------------------------------------------------------------------------------
# Parse snp quality parameter
#------------------------------------------------------------------------------
sub parseSnpQ($) {
	my($l) = @_;
	my(@t);
	(@t) = split /\t/,$l;
	return $t[6];
}

#------------------------------------------------------------------------------
# Replace a quality
#------------------------------------------------------------------------------
sub replaceSnpQ($$) {
	my($line, $q) = @_;
	my(@t);
	(@t) = split /\t/, $line;
	$t[1] = $q;
	return join("\t", @t);
}

#------------------------------------------------------------------------------
# Main
#------------------------------------------------------------------------------
# Read arguments
my(@file);
(@file) = @ARGV;
if( $#file <= 0 )	{ die "Usage: ./joinSnpEff.pl tag1 file1 tag2 file2 ... tagN fileN\n"; }

# Parse arguments
print STDERR "Reading files:\n";
my($i, $j, $file, $tag, $snp, @snpsAll, %snps, @tags);
for( $i=0 , $j=0 ; $i < $#ARGV ; $i+=2, $j++ ) {
	# Read tag
	$tags[$j] = $tag = $ARGV[$i];

	# Read file
	$file = $ARGV[$i+1];
	if( $file eq '' )	{ die "Missing file for tag '$tag'\n"; }
	print STDERR "\tTags[$j]: $tag\t'$file'\n";
	%snps = readSnps($file);

	# Add all snps
	foreach $snp ( keys %snps ) { $snpsAll[$j]->{$snp} = $snps{$snp}; }
}

#---
# Print SNPS
#---
my($snp, %done, %snpsi);
my($j, $jj);
$i = 0;
print STDERR "Joining SNP from all files\n";
for( $i=0 ; $i <= $#tags ; $i++ ) { # For all tags
	print "TAG:\ttags[$i] = '$tags[$i]'\n" if $debug;
	my($uniq, $shared) = (0, 0);
	%snpsi = %{$snpsAll[$i]};
	foreach $snp (sort keys %snpsi) { # For all snps...
		if( ! $done{$snp} ) { # Not done yet?

			# Get qualities from all SNPs
			my($quals, $qSum, $qCount) = ("", 0, 0);
			my($all) = "ALL ";
			for( $j=0, $jj=1 ; $j <= $#snpsAll ; $j++ , $jj++ ) {
				if( exists $snpsAll[$j]{$snp} ) { 
					my($q) = parseSnpQ($snpsAll[$j]->{$snp});
					$quals .= "$tags[$j]:$q "; 
					$qSum += $q;
					$qCount++;
				} else { $all = ""; }
			}

			if( $qCount <= 1 )	{ $uniq++; } # Count unique SNPs for this file
			else				{ $shared++; }

			$done{$snp} = 1;
			my($qAvg) = ( $qCount > 0 ? int($qSum/$qCount) : 0);
			printLine($snp, $snpsi{$snp}, "$all $quals", $qAvg);
		} else { $shared++; }
	}
	print STDERR "\tTags[$i]: $tags[$i]\tUnique / Shared snps: $uniq / $shared\n";
}

