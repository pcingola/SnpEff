#!/usr/bin/perl

#------------------------------------------------------------------------------
# Split a fastq file into N sequences per file
#
# WARNING: It assumes FASTQ sequences are in four-line 
#          format. I.e. no multi-line sequences or qualities
#
#																Pablo Cingolani
#------------------------------------------------------------------------------

use strict;

die "Usage: fastqSplit.pl numSequences file.fastq prefix\n" if $#ARGV != 2;

my($numSeqs) = $ARGV[0];		# Number of sequences per file
my($fastq) = $ARGV[1];			# Original files name
my($outPrefix) = $ARGV[2];		# Output file names prefix

#------------------------------------------------------------------------------
# Main
#------------------------------------------------------------------------------

# Open file
if( $fastq =~ /\.gz$/ )	{ open FQ, "gunzip -c $fastq |" or die "Cannot open input file '$fastq'\n"; }
else 					{ open FQ, "$fastq" or die "Cannot open input file '$fastq'\n"; }

# Read sequences
my($seqName, $seq, $seqName2, $qual, $outFq, $l);
my($lineNum, $outSeqNum, $seqNum, $fileNum) = (1, 1, 1, 1);
for($seqNum=1 ; $seqName = <FQ> ; $seqNum++ ) {
	$seq = <FQ>;
	$seqName2 = <FQ>;
	$qual = <FQ>;

	# Sanity check
	die "Error parsing sequence number $seqNum (line $lineNum): Sequence name does not start with '\@'\n" if( $seqName !~ /^\@/ );
	die "Error parsing sequence number $seqNum (line $lineNum): Sequence quality name does not start with '+'\n" if( $seqName2 !~ /^\+/ );

	# Open a new output file?
	if(($outFq eq '') || ($outSeqNum > $numSeqs)) {
		# Create a files name using 'prefix'
		$outFq = sprintf( "%s.%03d.fastq", $outPrefix, $fileNum);
		print "$outFq\n";
		$fileNum++;
		$outSeqNum = 1;

		# Open new file (close old one)
		close OUT if $outFq ne '';
		open OUT,"> $outFq" or die "Cannot open output file '$outFq'\n";
	}

	# Output to file
	print OUT "$seqName$seq$seqName2$qual";

	$lineNum += 4;
	$outSeqNum++;
	$seqNum++;
}

close FQ;
close OUT;
