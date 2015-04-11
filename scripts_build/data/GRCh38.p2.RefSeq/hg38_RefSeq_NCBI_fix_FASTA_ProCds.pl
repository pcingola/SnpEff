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
sub show($$) {
	my($seq, $ids)  = @_;
	my(@t) = split /\t/, $ids;
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
while( $l = <IDMAP> ) {
	chomp $l;
	($id, $name) = split /\s+/, $l;
	$id2name{$id} = $name;

	if( $name2id{$name} eq '' ) { $name2id{$name} = $id; }
	else						{ $name2id{$name} .= "\t$id"; }

	print "MAP: name2id{$name} = '$name2id{$name}'\n" if $debug;
	$count++;
}
close IDMAP;
print STDERR "Done\n";
die "Empty chromosome ID map file '$mapFile'\n" if $count <= 0;

#---
# Parse FASTA files from STDIN
#---
$id = $seq = "";
while( $l = <STDIN> ) {
	if( $l =~ /^>/ ) {
		# Show previous sequence
		show($seq, $name2id{$id}) if $id ne '';

		# Parse sequence header lines
		chomp $l;
		if( $l =~ />(.*)/ ) { $idNew = $1; }
	
		$id = $idNew;
		$seq = "";
	} else {
		# Append sequence
		$seq .= $l;
	}
}

show($seq, $name2id{$id}) if $id ne '';
