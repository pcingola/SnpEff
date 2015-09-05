#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Create a VCF subset using some samples.
# Sample IDs are provided in a TXT file (format: one sample ID per line).
#
#
#															Pablo Cingolani
#-------------------------------------------------------------------------------

$debug = 0;			# Debug mode?
$SAMPLE_COLUMN = 9;	# Column where sample information starts in VCF 
%sample2col = {};	# Sample names to column number
@samplesKeep = ();	# Samples to keep

#-------------------------------------------------------------------------------
# Select some samples in a VCF line
#-------------------------------------------------------------------------------
sub selectSamples($) {
	my($i, $l, $out, @fields);
	($l) = @_;
	@fields = split /\t/, $l;

	$out = "";
	for( $i=0 ; $i < $SAMPLE_COLUMN ; $i++ ) {
		$out .= "\t" if( $out ne '' );
		$out .= $fields[$i];
	}

	for( $i=0 ; $i <= $#samplesKeep ; $i++ ) {
		$col = $sample2col{ $samplesKeep[$i] };
		$out .= "\t$fields[$col]";
	}

	print "$out\n";
}

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------

#---
# Parse command line 
#---
$samplesFile = @ARGV[0];
die "Usage: cat file.vcf | ./vcfFilterSamples.pl sample_IDs.txt\n" if $samplesFile eq '';

#---
# Read samples file
#---
open SAMPLES, $samplesFile or die "Cannot open samples file '$samplesFile'\n";
for( $i=0 ; $l = <SAMPLES> ; $i++ ) {
	chomp $l;
	$samplesKeep[$i] = $l;
	print "samplesKeep[$i] = '$samplesKeep[$i]'\n" if $debug;
}
close SAMPLES;

#---
# Read VCF from STDIN
#---
$sampleNamesFound = 0;
while($l = <STDIN>) {
	chomp $l;

	if($l =~ /^#/) {
		# VCF header
		if( $l =~ /#CHROM/ ) {
			@t = split /\t/, $l;

			# Create sample name to sample number mapping
			# VCF sample names start at column 9
			for( $i=$SAMPLE_COLUMN ; $i <= $#t ; $i++ ) {
				$sname = $t[$i];
				print STDERR "Sample[$i] = '$sname'\n" if $debug;
				$sample2col{$sname} = $i if $sname ne '';
			}

			# Check that all samples 'to keep' are found
			foreach $sname ( @samplesKeep ) {
				die "Error: Cannot find sample '$sname' in VCF header\n" if( $sample2col{$sname} eq '' );
			}

			# Print header line
			selectSamples($l);
		} else {
			print "$l\n";
		}
	} else {
		# VCF body
		selectSamples($l);
	}
}

