#!/usr/bin/perl

die "Usage: cat file.txt | swapCols.pl $#ARGV colNum1 colNum2\n" if $#ARGV < 1;

$col1 = $ARGV[0] - 1;
$col2 = $ARGV[1] - 1;

while( $l = <STDIN> ) {
	chomp $l;
	@t = split /\t/, $l;

	for( $i = 0 ; $i <= $#t ; $i++ ) {
		if( $i > 0 )			{ print "\t"; }

		if( $i == $col1 )		{ print "$t[$col2]"; }
		elsif( $i == $col2 )	{ print "$t[$col1]"; }
		else					{ print "$t[$i]"; }
	}
	print "\n";
}
