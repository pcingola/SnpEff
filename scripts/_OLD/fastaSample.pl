#!/usr/bin/perl

#---
# Initialize parameters
#---
$sampleStart = $ARGV[0];
$sampleEnd = $ARGV[1];
if(( $ARGV[0] eq '' ) || ($ARGV[1] eq ''))	{ die "Usage: fastaSample sampleStart sampleEnd\n"; }

$sampleStart--;
$sampleEnd--;
$sampleLen = $sampleEnd - $sampleStart + 1;

#---
# Read fasta file
#---
for($lineNum=0 ; $l = <STDIN> ; $lineNum++ ) {
	if( $l =~/^>/ ) {
		# Sample if not empty
		if( $seq ne "" ) { 
			$s = substr( $seq, $sampleStart, $sampleLen);
			print "$s\n";
		}
		# New sequence
		$seq = "";
	} else {
		chomp($l);
		$seq .= $l;
	}
}

# Sample if not empty
if( $seq ne "" ) { 
	$s = substr( $seq, $sampleStart, $sampleLen);
	print "$s\n";
}

$len = length($seq);
print STDERR "Lines: $lineNum\n";
print STDERR "Sequence size: $len\n";
print STDERR "Sample size: $sampleLen\n";
