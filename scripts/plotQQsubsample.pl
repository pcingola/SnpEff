#!/usr/bin/perl
#-------------------------------------------------------------------------------
#
# Plot a QQ plot (using R) 
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
if( $ARGV[0] eq '' )	{ die "Usage: cat data.txt | qqplot_subsample.pl title pvalueTh sub_sample_rate\n"; }

$base = $ARGV[0];
$pvalueTh = $ARGV[1];
$pvalueSubsample = $ARGV[2];

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
# Create an R program, save QQ-plot as PNG image
#---

open R, "| R --vanilla --slave " or die "Cannot open R program\n";
print R <<EOF;

#-------------------------------------------------------------------------------
# Create a QQ-plot form values in 'x'
#
# If a sample is more than pvalueTh, then we only have one sample every 'pvalueSample'
#-------------------------------------------------------------------------------
qqplot <- function( x, title, pvalueTh=1.0, pvalueSample=1 ) {
    keep <- (x > 0) & (x <= 1) & ( ! is.na(x) );
    x <- x[keep]

    s <- sort(x);
    ly <- -log10(s);

	# Expected p-values assuming uniform districution is just the rank
	# So we have to calculate the rank
    n <- length(s);
	rank <- 1:n

	# Do we need to counter effect subsampling of high p-values?
	if(( pvalueTh < 1.0 ) && ( pvalueSample > 1)) {
		# We must correct rank for pvalues over threshold
		n.overTh <- sum( s > pvalueTh)
		n.underTh <- sum( s <= pvalueTh)

		# Create a sequence of 'expected pvalues'
		rank.undetTh <- 1:n.underTh;		# These are not sub-sampled, so we are OK
		rank.overTh <- n.underTh + 1 + ( seq(0, n.overTh-1) * pvalueSample );	# These are sub-sampled 1/pvalueSample, so we compensate it
		rank <- c( rank.undetTh, rank.overTh )
	}

	# Now that we have the rank, we can calculate the expected p-value
	expected.pvalue <- rank / ( max(rank) + 1 )
    lx <- -log10( expected.pvalue )

	# Show full range in both plots
	range <- c(0 , max(lx, ly) );
	lyTh <- ly[n.underTh + 1]
	lxTh <- lx[n.underTh + 1]

	plot( lx, ly, xlim=range, ylim=range, main=title, xlab="-Log[ rank / (N+1) ]", ylab="-Log[ p ]" );
	col <- rgb( 0.8, 0.8, 0.8, 0.5 )
	rect(0, 0, lxTh, lyTh, col=col, border=NA)
	abline( 0 , 1 , col='red');
	s <- sort(x);
	ly <- -log10(s);
}

png('$pngFile', width = 1024, height = 1024);

data <- read.csv("$txtFile", sep='\t', header = TRUE);
qqplot( data\$x, "$base", $pvalueTh, $pvalueSubsample );

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

