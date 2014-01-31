#!/usr/bin/perl

#-------------------------------------------------------------------------------
#
# Converts one column to deciBel (Db)
# 		db = -10 * log(x) / log(10) 
#
#-------------------------------------------------------------------------------

# By default, change the first column
$colNum = 0;
if( $ARGV[0] ne '' )	{ $colNum = $ARGV[0] - 1; }		# Use argument as column number (transform to zero-based)

$k = -10 / log(10);

# Parse STDIN
while( $l = <STDIN> ) {
	chomp $l;
	@t = ();
	@t = split /\t/, $l;

	# Convert column to DB
	if( $t[$colNum] > 0 )	{ $t[$colNum] = $k * log( $t[$colNum] ); }
	else 					{ $t[$colNum] = "NA"; }

	# Show all fields in this line
	for( $i=0 ; $i < $#t ; $i++ ) {	print "$t[$i]\t"; }
	print "$t[$i]\n";
}
