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
# Show all sequences
#-------------------------------------------------------------------------------
sub show($$$) {
	my($seq, $ids, $name)  = @_;
	my(@t) = split /\t/, $ids;
	if( $debug && $#t > 1 ) { print STDERR "Duplicated name '$name' ($#t): $ids\n"; }
	foreach $id ( @t ) { print ">$id\n$seq"; }
}

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------

# Parse command line arguments
$mapFile = $ARGV[0];
die "Missing command line argument 'map_file.txt'\n" if $mapFile eq '';

#---
# Read ID map file
#---
print STDERR "Reading file $mapFile\n";
open IDMAP, $mapFile || die "Cannot open chromosome ID map file '$mapFile'\n";
$count = 0;
for($ln=1 ; $l = <IDMAP> ; $ln++) {
	chomp $l;
	($id, $name) = split /\s+/, $l;
	$id2name{$id} = $name;

	if( $name2id{$name} eq '' ) { $name2id{$name} = $id; }
	else						{ $name2id{$name} .= "\t$id"; }

	print STDERR "MAP: id2name{$id} = '$id2name{$id}'\n" if $debug;
	$count++;
}
close IDMAP;
print STDERR "Done, $ln lines\n";
die "Empty chromosome ID map file '$mapFile'\n" if $count <= 0;

#---
# Parse FASTA files from STDIN
#---
$name = $seq = "";
while( $l = <STDIN> ) {
	if( $l =~ /^>/ ) {
		# Show previous sequence
		show($seq, $name2id{$name}, $name) if $name ne '';

		# Parse sequence header lines
		chomp $l;
		if( $l =~ />(.*)/ ) { $nameNew = $1; }
	
		$name = $nameNew;
		$seq = "";
	} else {
		# Append sequence
		$seq .= $l;
	}
}

show($seq, $name2id{$name}, $name) if $name ne '';

