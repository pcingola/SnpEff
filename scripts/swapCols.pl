#!/usr/bin/perl

die "Usage: cat file.txt | swapCols.pl $#ARGV cols1 cols2\nColumns are coma separated list of one-base column indexes\n" if $#ARGV < 1;

# Parse command line arguments
@cols1 = split /,/, $ARGV[0];
@cols2 = split /,/, $ARGV[1];
die "Different number of columns in each argument" if( $#cols1 != $#cols2 );

# Add indexes to swap
for($i = 0 ; $i <= $#cols1 ; $i++ ) {
	$c1 = $cols1[$i] - 1;
	$c2 = $cols2[$i] - 1;
	$idx[$c2] = $c1;
	$idx[$c1] = $c2;
}

# Process SDTIN
while( $l = <STDIN> ) {
	chomp $l;
	@t = split /\t/, $l;

	for( $i = 0 ; $i <= $#t ; $i++ ) {
		if( $i > 0 )			{ print "\t"; }

		$j = $idx[$i];

		# Should we swap this one?
		if( $j ne '' )	{ print "$t[$j]"; }
		else 			{ print "$t[$i]"; }
	}
	print "\n";
}
