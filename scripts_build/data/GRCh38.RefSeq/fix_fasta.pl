#!/usr/bin/perl

#-------------------------------------------------------------------------------
# Parse and modify NCBI's FASTA (RefSeq) to make is suitable for building
# a SnpEff database
#
#
#																Pablo Cingolani
#-------------------------------------------------------------------------------

# Debug mode?
$debug = 0;

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------

# Parse command line arguments
$chrMapFile = $ARGV[0];
die "Missing command line argument 'chromosome_ID_map_file.txt'\n" if $chrMapFile eq '';

#---
# Read chromosome ID map file
#---
open CHR, $chrMapFile || die "Cannot open chromosome ID map file '$chrMapFile'\n";
$count = 0;
while( $l = <CHR> ) {
	chomp $l;
	($chr, $id) = split /\s+/, $l;
	$chr{$id} = $chr;
	print STDERR "MAP: chr{$id} = $chr\n" if $debug;
	$count++;
}
close CHR;
die "Empty chromosome ID map file '$chrMapFile'\n" if $count <= 0;

#---
# Parse GFF files from STDIN
#---
while( $l = <STDIN> ) {
	if( $l =~ /^>/ ) {
		# Parse sequence header lines
		chomp $l;
		print STDERR "New sequence:\t$l\n" if $debug;
		# Format: ">NC_000001.10 Homo sapiens chromosome 1, GRCh37.p13 Primary Assembly"
		if( $l =~ /^>(\S+).*/ ) {
			$chrId = $1;
		
			# Map ID to name
			$chr = $chr{$chrId};
			if( $chr eq '' ) {
				print STDERR "Not found '$chrId'\n";
				$chr = $chrId;
			} else {
				print STDERR "OK '$chrId' => '$chr'\n"; 
			}

			print ">$chr\n";
		} else {
			die "ERROR matching chromosome name for line: $l\n";
		}
	} else {
		# Print all other lines (sequence)
		print $l;
	}
}
