#!/usr/bin/perl

#-------------------------------------------------------------------------------
# Parse and modify NCBI's GFF (RefSeq) to make is suitable for building
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
while( $l = <CHR> ) {
	chomp $l;
	($chr, $id) = split /\s+/, $l;
	$chr{$id} = $chr;
	print "MAP: chr{$id} = $chr\n" if $debug;
}
close CHR;

#---
# Parse GFF files from STDIN
#---
while( $l = <STDIN> ) {

	if( $l =~ /^#/ ) {
		# Show header / comment lines
		print $l;
	} else {
		chomp $l;
		($chrid, $source, $type, $start, $end, $score, $strand, $phase, $attrs) = split /\t/, $l;

		# Find chromo ID
		$chr = $chr{$chrid};
		if( $chr eq '' ) {
			print STDERR "WARNING: Cannot find chromosome name for id '$chrid'\n";
			$chr{$chrid} = $chrid;
		}

		print "ORI:\t$chr\t$source\t$type\t$start\t$end\t$score\t$strand\t$phase\t$attrs\n" if $debug;

		# Parse attributes as key-value pairs
		%attr = ();
		@kvs = split /\;/, $attrs;
		@keys = ();
		foreach $kv ( @kvs ) {
			($key, $value) = split /=/, $kv;
			$attr{$key} = $value;
			push( @keys, $key );
			print "\tattr{$key} = $value\n" if $debug;
		}

		#if( $type eq 'gene' || $type eq 'transcript' || $type eq 'primary_transcript' || $type eq 'ncRNA' || $type eq 'mRNA' || $type eq 'tRNA' || $type eq 'rRNA' ) {
		if( $type eq 'exon' || $type eq 'CDS' ) {
			# Replace parent ID
			$oldId = $attr{'Parent'};
			$newId = $trId{$oldId};
			if( $newId ne '' ) { $attr{'Parent'} = $newId; }
			else { print STDERR "WARNING: Cannot find ID for parent '$oldId' in line: $l\n"; }
		} else {
			# Replace ID
			$oldId = $attr{'ID'};
			if( $attr{'Name'} ne '' )	{ $newId = $attr{'Name'} . "." . $oldId; }
			else 						{ $newId = $oldId; }
			$trId{$oldId} = $newId;
			$attr{'ID'} = $newId;
		}

		# Rebuild attributes
		$attrs = "";
		foreach $key ( @keys ) { 
			$attrs .= ";" if $attrs ne ''; 
			$attrs .= "$key=$attr{$key}";
		}

		# Show GFF line
		print "$chr\t$source\t$type\t$start\t$end\t$score\t$strand\t$phase\t$attrs\n";
	}
}
