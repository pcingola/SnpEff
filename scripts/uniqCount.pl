#!/usr/bin/perl

while( $l = <STDIN> ) {
	chomp $l;
	$count{$l}++;
}

$tot = 0;
foreach $key ( sort keys %count ) { 
	print "$count{$key}\t$key\n"; 
	$tot += $count{$key};
}
print "$tot\tTotal\n"; 
