#!/usr/bin/perl

if($ARGV[0] ne '') {
	$p = $ARGV[0]; 
	die "Argument should be a sampling probability, i.e. a number between 0.0 and 1.0 (default=0.001)" if !(($p >= 0.0) && ($p <= 1.0));
} else {
	$p = 0.001;
}

while( <STDIN> ) {
	print if (rand() <= $p);
}
