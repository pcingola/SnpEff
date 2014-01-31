#!/usr/bin/perl

open ID, "cdsId_trId.txt";
while( $l = <ID> ) {
	chomp $l;
	($cdsId, $trId) = split /\t/, $l;
	$trId{$cdsId} = $trId;
}
close ID;

while( $l = <STDIN> ) {
	chomp $l;
	if( $l =~ /^>(.*)/ ) {
		$cdsId = $1;
		$l = ">$trId{$cdsId}" if $trId{$cdsId} ne '';
	}

	print "$l\n";
}
