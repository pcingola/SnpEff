#!/usr/bin/perl

$sum = 0;

for( $count = $lines = 0 ; $l = <STDIN> ; $lines++ ) {
	chomp $l;
	if( $l ne '' ) {
		if( $count == 0 ) {
			$max = $l;
			$min = $l;
		} else {
			$max = $l if $max < $l;
			$min = $l if $min > $l;
		}

		$sum += $l;
		$count++;
	}
}

print "lines: $lines\tnumber of values: $count\tmin: $min\tmax: $max\tsum: $sum\n";
