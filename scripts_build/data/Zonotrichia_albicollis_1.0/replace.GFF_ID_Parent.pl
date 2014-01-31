#!/usr/bin/perl

$debug = 0;

# Read list of IDs to replace
open LIST,"replace_list.txt";
while( $l = <LIST> ) {
	chomp $l;
	($id, $name) = split /\t/, $l;
	$replace{$name} = $id;
}

# Translate input (GFF file)
for( $line=1 ; $l = <STDIN> ; $line++ ) {
	chomp $l;

	$name = "";
	if( $l =~ /Parent=(rna.*?);/ ) { $name = $1; }
	elsif( $l =~ /ID=(rna.*?);/ ) { $name = $1; }

	if(($name ne '') && ($replace{$name} ne '')) {
		$id = $replace{$name};
		if( $l =~ s/=$name;/=$id;/ ) { print STDERR "Line $line\tReplace: '$name'\t'$id'\n" if $debug; }
	}

	print "$l\n";
}
