#!/usr/bin/perl
#-------------------------------------------------------------------------------
#
# Create an MA-plot
# Data is feed as two column of numbers 
#
# Note: Any line that does not match a numeric regular expression, is filtered out).
#
#														Pablo Cingolani
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------

# Parse command line option (file base name)
$base = 'maPlot';
if( $ARGV[0] ne '' )	{ $base = $ARGV[0]; }

$pngFile = "$base.png";
$txtFile = "$base.txt";

# Read STDIN and create an R table
open TXT, "> $txtFile" or die "Cannot open output file '$txtFile'\n";
print TXT "x\ty\n";
for( $ln = 0 ; $l = <STDIN> ; ) {
	chomp $l;
	($x, $y) = split /\t/, $l;

	# Does the string contain exactly one number? (can be float)
	if(( $x =~ /^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?$/ ) && ( $y =~ /^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?$/ )) { print TXT "$x\t$y\n"; }
}
close TXT;

#---
# Create an R program, save histogram plot as PNG image
#---

open R, "| R --vanilla --slave " or die "Cannot open R program\n";
print R <<EOF;

maPlot <- function( logr, logg, title ) {
	m <- logr - logg;
	a <- 1/2 * (logr + logg);
	smoothScatter(a, m, main=title, ylab='M', xlab='A');
	lines( lowess(a, m), col='orange' );
}

png('$pngFile', width = 1024, height = 1024);

data <- read.csv("$txtFile", sep='\\t', header = TRUE);
x <- data\$x
y <- data\$y

maPlot(x, y, "MA plot");

dev.off();
quit( save='no' )
EOF

close R;

#---
# Show figure
#---

$os = `uname`;
$show = "eog"; 
if( $os =~ "Darwin" )	{ $show = "open"; }
`$show $pngFile`;

