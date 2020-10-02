#!/usr/bin/perl

#-------------------------------------------------------------------------------
# Parse and modify NCBI's GTF (RefSeq) to make is suitable for building
# a SnpEff database
#
#
#																Pablo Cingolani
#-------------------------------------------------------------------------------

use strict;

# Debug mode?
my($debug) = 0;

# Map chromosome IDs to names
my(%chr);
my(@keys) = ();		# GTF attribute key order
my(%prot);

#-------------------------------------------------------------------------------
# Read an ID map file
#-------------------------------------------------------------------------------
sub readMap($) {
	my($mapFile) = @_;
	my($l, $name, $id, %ids);

	open IDS, $mapFile || die "Cannot open chromosome ID map file '$mapFile'\n";
	while( $l = <IDS> ) {
		chomp $l;
		($name, $id) = split /\s+/, $l;
		$ids{$id} = $name;
		print "MAP: ids{$id} = $name\n" if $debug;
	}
	close IDS;

	return (%ids);
}


#-------------------------------------------------------------------------------
# Parse GTF line
#-------------------------------------------------------------------------------
sub parseGffLine($) {
	my($l) = @_;
	my($chr, $source, $type, $start, $end, $score, $strand, $phase, $attrs);

	chomp $l;
	($chr, $source, $type, $start, $end, $score, $strand, $phase, $attrs) = split /\t/, $l;
	print STDERR "Parsing GTF line:\t$chr\t$source\t$type\t$start\t$end\t$score\t$strand\t$phase\t$attrs\n" if $debug;

	#---
	# Parse attributes as key-value pairs
	#---
	my($key, $value, $kv, %attr, @kvs);
	my(%attr) = ();
	my(@kvs) = split /\;/, $attrs;
	@keys = ();
	foreach $kv ( @kvs ) {
		if( $kv =~ /\s*(\S+)\s+"(.*)"\s*/ ) {
			($key, $value) = ($1, $2);
			$attr{$key} = $value;
			push( @keys, $key );
			print STDERR "\tattr{$key} = $value\n" if $debug;
		}
	}
	
	if( $type eq 'CDS' ) {
		my($trId, $protId);
		$trId = $attr{'transcript_id'};
		$protId = $attr{'protein_id'};
		if( $trId ne '' && $protId ne '') {
			$prot{$trId} = $protId if( $trId ne '' && $protId ne '');
			print STDERR "PROTEIN: $protId => TRANSCRIPT: $trId\n" if $debug;
		}
	}

	return ($chr, $source, $type, $start, $end, $score, $strand, $phase, $attrs, \%attr);
}

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------

# Parse command line arguments
die "Usage: $0 file.gtf chromosome_ID_map_file.txt\n" if $#ARGV <= 0;
my($gtf) = $ARGV[0];
my($chrMapFile) = $ARGV[1];
die "Missing command line argument 'file.gtf'\n" if $gtf eq '';
die "Missing command line argument 'chromosome_ID_map_file.txt'\n" if $chrMapFile eq '';

# Read chromosome ID map file
%chr = readMap($chrMapFile);

#---
# Parse GTF files from STDIN
#---

my($chr, $chrid, $source, $type, $start, $end, $score, $strand, $phase, $attrs);	# GTF parsed values
my($l, $id, $key, $value, $name, $paren, $newParent);

print STDERR "Parsing GTF file '$gtf'\n";
open GTF, "gunzip -c $gtf |" || die "Cannot open file '$gtf'\n";
while( $l = <GTF> ) {
	# Skip headers
	if( $l !~ /^#/ ) {
		($chrid, $source, $type, $start, $end, $score, $strand, $phase, $attrs) = parseGffLine($l);

		# Find chromosome name
		$chr = $chr{$chrid};
		if( $chr eq '' ) {
			print STDERR "WARNING: Cannot find chromosome name for id '$chrid'\n";
			$chr{$chrid} = $chrid;
		}

		# Show GTF line
		print "$chr\t$source\t$type\t$start\t$end\t$score\t$strand\t$phase\t$attrs\n";
	}
}

close GTF;

#---
# Write protein to transcript map file
#---

my($mapFile) = "protein_id.map.txt";
print STDERR "Creating protein IDs map file '$mapFile'\n";
open IDS, "> $mapFile";
foreach $key ( sort keys %prot ) { print IDS "$key\t$prot{$key}\n"; }
close IDS;

