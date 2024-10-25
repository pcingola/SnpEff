#!/usr/bin/perl
#-------------------------------------------------------------------------------
#
# Plot a histogram (using R) 
# Data is feed as a 1 column of numbers 
#
# Note: Any line that does not match a numeric regular expression, is filtered out).
#
#														Pablo Cingolani
#-------------------------------------------------------------------------------

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------

# Parse command line option (file base name)
$base = 'hist';
if( $ARGV[0] ne '' )	{ $base = $ARGV[0]; }

$pngFile = "$base.png";
$txtFile = "$base.txt";

# Read STDIN and create an R vector
open TXT, "> $txtFile" or die "Cannot open output file '$txtFile'\n";
print TXT "x\n";
for( $ln = 0 ; $l = <STDIN> ; ) {
	chomp $l;

	# Does the string contain exactly one number? (can be float)
	if( $l =~ /^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?$/ ) { print TXT "$l\n"; }
}
close TXT;

#---
# Create an R program, save histogram plot as PNG image
#---

open R, "| R --vanilla --slave " or die "Cannot open R program\n";
print R <<EOF;

histDens <- function( x, title, q=1.0, breaks = 50 ) {
	# Show only this part of the data
	xmin <- quantile( x, 1-q )
	xmax <- quantile( x, q )
    data <- x[ (x >= xmin) & (x <= xmax) ];

    dens <- density(data)

    h <- hist(data, main=title, xlab = "data", ylab = "Frequency", freq = T, breaks=breaks);

    # Adjust density height to 'frecuency'
	dens\$y <- max(h\$counts) * dens\$y/max(dens\$y)
    lines(dens, col='red')

    # Mean & median calculated over the whola data
    abline( v=mean(x), col='blue', lty=2, lwd=2);
    abline( v=median(x), col='green', lty=2, lwd=2);

	legend("topright",c("Mean","Median"),lty=c(1,1),col=c("blue","green"))
}

png('$pngFile', width = 1024, height = 1024);
par( mfrow=c(2,1) );

data <- read.csv("$txtFile", sep='\\t', header = TRUE);
x <- data\$x

histDens( x, "Histogram: All data", 1.0 );
histDens( x, "Histogram: Quantile [2% - 98%]", 0.98 );

print( summary( x ) )
cat('std:', sd(x), '\\n')

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

