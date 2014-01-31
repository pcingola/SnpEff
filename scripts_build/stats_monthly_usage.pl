#!/usr/bin/perl

while( $l = <STDIN> ) {
	@t = split /\t/, $l;
	$date = $t[1];

	if( $date =~/^(20..\-..)/ ) {
		$month = $1;
		$count{$month}++;
		# print "$t[1]\t$month\t$count{$month}\n";
	}
}

foreach $m ( sort keys %count ) {
	print "$count{$m}\t$m\n";
}
