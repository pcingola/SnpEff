#!/usr/bin/perl

#-------------------------------------------------------------------------------
# Parse and modify NCBI's GFF (RefSeq) to make is suitable for building
# a SnpEff database
#
#
#																Pablo Cingolani
#-------------------------------------------------------------------------------

use strict;

# Debug mode?
my($debug) = 0;

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------

# Parse command line arguments
my($chrMapFile) = $ARGV[0];
die "Missing command line argument 'chromosome_ID_map_file.txt'\n" if $chrMapFile eq '';

#---
# Read chromosome ID map file
#---
open CHR, $chrMapFile || die "Cannot open chromosome ID map file '$chrMapFile'\n";
my($l, $chr, $id, %chr);
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
my($chr, $chrid, $source, $type, $start, $end, $score, $strand, $phase, $attrs, $id, $key, $value, $kv, $paren);
my(%attr, @kvs, @keys, $name, %id, %trid, %prot);
while( $l = <STDIN> ) {

	if( $l =~ /^#/ ) {
		# Show header / comment lines
		print $l;
	} else {
		chomp $l;
		($chrid, $source, $type, $start, $end, $score, $strand, $phase, $attrs) = split /\t/, $l;
		print STDERR "ORI:\t$chr\t$source\t$type\t$start\t$end\t$score\t$strand\t$phase\t$attrs\n" if $debug;

		#---
		# Find chromosome name
		#---
		$chr = $chr{$chrid};
		if( $chr eq '' ) {
			print STDERR "WARNING: Cannot find chromosome name for id '$chrid'\n";
			$chr{$chrid} = $chrid;
		}

		#---
		# Translate types
		#---
		if( $type eq 'primary_transcript' ) { $type = 'transcript'; }

		#---
		# Parse attributes as key-value pairs
		#---
		%attr = ();
		@kvs = split /\;/, $attrs;
		@keys = ();
		foreach $kv ( @kvs ) {
			($key, $value) = split /=/, $kv;
			$attr{$key} = $value;
			push( @keys, $key );
			print STDERR "\tattr{$key} = $value\n" if $debug;
		}

		$name = $attr{'Name'};
		$id = $attr{'ID'};
		$paren = $attr{'Parent'};

		#---
		# Replace ID
		#---
		my($oldId, $newId);
		$oldId = $attr{'ID'};
		if( $attr{'Name'} ne '' )	{ $newId = $attr{'Name'} . "." . $oldId; }
		else 						{ $newId = $oldId; }

		$id{$oldId} = $newId;
		$attr{'ID'} = $newId;
		$trid{$newId} = $name if $name ne '';

		#---
		# Replace 'Parent'
		#---
		if( $attr{'Parent'} ne '' ) {
			my($oldParent, $newParent);
			$oldParent = $attr{'Parent'};
			$newParent = $id{$oldParent};
			if( $newParent ne '' ) { $attr{'Parent'} = $newParent; }
			else { print STDERR "WARNING: Cannot find ID for parent '$oldParent' in line: $l\n"; }

			#---
			# Protein - Transcript map
			#---
			if( $type eq 'CDS' ) {
				my($protId) = $name;
				my($trId) = $paren;
				if( $protId ne '' ) {
					$prot{$trId} = $protId;
					$prot{$newParent} = $protId;
				}
			}

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

#---
# Write IDs map file
#---
my($mapFile) = "ids.map.txt";
print STDERR "Creating IDs transcript map file '$mapFile'\n";
open IDS, "> $mapFile";
foreach $key ( sort keys %id ) { print IDS "$key\t$id{$key}\n"; }
close IDS;

$mapFile = "protein_id.map.txt";
print STDERR "Creating protein IDs map file '$mapFile'\n";
open IDS, "> $mapFile";
foreach $key ( sort keys %prot ) { print IDS "$key\t$prot{$key}\n"; }
close IDS;

$mapFile = "transcript_id.map.txt";
print STDERR "Creating transcript IDs map file '$mapFile'\n";
open IDS, "> $mapFile";
foreach $key ( sort keys %trid ) { print IDS "$key\t$trid{$key}\n"; }
close IDS;
