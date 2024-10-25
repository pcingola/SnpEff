#!/usr/bin/perl
#-------------------------------------------------------------------------------
#
# Plot (using R) 
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
$base = 'plotXY';
if( $ARGV[0] ne '' )	{ $base = $ARGV[0]; }

$pngFile = "$base.png";
$txtFile = "$base.txt";

# Read STDIN and create an R vector
open TXT, "> $txtFile" or die "Cannot open output file '$txtFile'\n";
print TXT "x\ty\n";
for( $ln = 0 ; $l = <STDIN> ; ) {
	chomp $l;

	# Does the string contain exactly two numbers? (can be float)
	if( $l =~ /^(\S+)\s(\S+)$/ ) {
		($num1, $num2) = ($1, $2);
		if(( $num1 =~ /^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?$/ ) && ( $num2 =~ /^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?$/ )) { 
			print TXT "$num1\t$num2\n";
		}
	} 
}
close TXT;

#---
# Create an R program, save plot as PNG image
#---

open R, "| R --vanilla --slave " or die "Cannot open R program\n";
print R <<EOF;

plotChart <- function( x, y, title ) {
    plot(x, y)

    # Mean & median calculated over the whola data
    abline( v=mean(y), col='blue', lty=2, lwd=2);
    abline( v=median(y), col='green', lty=2, lwd=2);

	legend("topright",c("Mean","Median"),lty=c(1,1),col=c("blue","green"))

}

png('$pngFile', width = 1024, height = 1024);

data <- read.csv("$txtFile", sep='\\t', header = TRUE);
x <- data\$x
y <- data\$y
plotChart( x, y, "All data" );
print( summary( y ) )

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

