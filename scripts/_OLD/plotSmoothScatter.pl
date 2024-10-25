#!/usr/bin/perl
#-------------------------------------------------------------------------------
#
# Plot a smooth scatter plot
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
$base = 'smoothScatter';
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

smoothLowess <- function( x, y, title, q=1.0 ) {
	# Show only this part of the data
	xmin <- quantile( x, 1-q )
	xmax <- quantile( x, q )

	ymin <- quantile( y, 1-q )
	ymax <- quantile( y, q )

	keep <- (x >= xmin) & (x <= xmax) & (y >= ymin) & (y <= ymax);
    qx <- x[ keep ]
    qy <- y[ keep ]

	smoothScatter(qx, qy, main=title, ylab='Y (column 2)', xlab='X (column 1)');
	lines( lowess(qx,qy), col='orange' );
}

png('$pngFile', width = 1024, height = 1024);
par( mfrow=c(2,1) );

data <- read.csv("$txtFile", sep='\\t', header = TRUE);
x <- data\$x
y <- data\$y

smoothLowess(x, y, "Smooth scatter plot and Lowess", 1.0);
smoothLowess(x, y, "Smooth scatter plot and Lowess: Quantile [2% - 98%]", 0.98);

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
`$show $pngFile &`;

