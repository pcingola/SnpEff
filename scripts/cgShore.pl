#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Add shore / shelf to CpG islands:
#
#	i) Shore : 2KB up/downstream of the islands (North-shore / South-shore)
#	ii) Shelf : 2KB up/downstream of the shore:
#		ii.a) North-shelf upstream of North_Shore
#		ii.b) South-shelf downstream of South-shore
#
#
#														Pablo Cingolani 2013
#-------------------------------------------------------------------------------

$SHORE_SIZE = 2000;
$SHELF_SIZE = 2000;

while( $l = <STDIN> ) {
	chomp $l;
	($chr, $start, $end) = split /\s+/, $l;

	if( $start >= 0 ) {

		($shelfStart,$shelfEnd) = ($start - $SHORE_SIZE - $SHELF_SIZE, $start - $SHORE_SIZE);
		print "$chr\t$shelfStart\t$shelfEnd\tCpG_N_SHELF\n" if( $shelfStart >= 0);

		($shoreStart,$shoreEnd) = ($start - $SHORE_SIZE, $start);
		print "$chr\t$shoreStart\t$shoreEnd\tCpG_N_SHORE\n" if( $shoreStart >= 0);

		print "$chr\t$start\t$end\tCpG_ISLAND\n";

		($shoreStart,$shoreEnd) = ($end, $end + $SHORE_SIZE);
		print "$chr\t$shoreStart\t$shoreEnd\tCpG_S_SHORE\n";

		($shelfStart,$shelfEnd) = ($end + $SHORE_SIZE, $end + $SHORE_SIZE + $SHELF_SIZE);
		print "$chr\t$shelfStart\t$shelfEnd\tCpG_S_SHELF\n";
	}
}
