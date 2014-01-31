#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Count unmber of bioType for transcript (second column in GTF file) and 
# for gene ('/gene_biotype' info field)
#
#																Pablo Cingolani
#-------------------------------------------------------------------------------

use strict;

my($count, %count, $countMatch) = (0, 0);

#---
# Process input (GTF file)
#---
my($l, $key, $field, @t, %bioType);
while( $l = <STDIN> ) {
	chomp $l;
	@t = split /\t/, $l;

	# Get transcript biotype
	my($bioTypeTr) = $t[1];
	die "Cannot find biotype" if( $bioTypeTr eq '' );

	# Get gene biotype
	@t = split /;/, $t[8];
	my($bioTypeGene) = '';
	foreach $field ( @t ) {
		# Parse 'name value' fields
		if( $field =~/\s*(.*)\s\"(.*)\"/ )	{
			my($name, $value) = ($1, $2); 
			if( $name eq 'gene_biotype' )	{ $bioTypeGene = $value; }
		}
	}
	die "Cannot find biotype" if($bioTypeGene eq '');

	$bioType{$bioTypeTr} = 1;
	$bioType{$bioTypeGene} = 1;

	$key = "$bioTypeTr\t$bioTypeGene";
	$count{$key}++;

	# Count
	if( $bioTypeTr eq $bioTypeGene )	{ $countMatch++; }
	$count++;
}

#---
# Show results
#---
foreach $key ( sort keys %count ) { print "$count{$key}\t$key\n"; }
print "Count\t$count\n";
print "Count match\t$countMatch\n";

#---
# Show as table
#---
my($bt, $bg);
print "\t";
foreach $bg ( sort keys %bioType ) { print "$bg\t"; }
print "\n";
foreach $bt ( sort keys %bioType ) {
	print "$bt\t";
	foreach $bg ( sort keys %bioType ) {
		$key = "$bt\t$bg";
		print "$count{$key}\t";
	}
	print "\n";
}
