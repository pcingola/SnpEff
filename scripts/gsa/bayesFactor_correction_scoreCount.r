#-------------------------------------------------------------------------------
#
# Bayes-factor correction for gene set analysis (SnpEff gsa)
#
#
#															Pablo Cingolani 2013
#-------------------------------------------------------------------------------


#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------

show = TRUE		# Used for debugging
show = FALSE	# Used for production

pvalCoefThreshold = 10^-12	# Adjust only if linearmodel p-value is less than this

#---
# Parse command line arguments
#---
if( ! exists('fileName') ) {
    args <- commandArgs(trailingOnly = TRUE)
    fileName = args[1]
    outFileName = args[2]
    minScoreCount = args[3]
}

#---
# Read data
#---
d = read.table(fileName, sep="\t", header=TRUE)

# p-values or scores. 
# Note: the conversion 'as.numeric(as.character())' is necesary 
# because R reads '0.6049666666666666' as a number, 
# but reads       '0.60496666666666666' as a string (yes, R is amazing!)
p = as.numeric(as.character(d$score))	
c = d$scoreCount						# number of variants 

keep = (c > 0)

p = p[keep]
mu = mean(p)
sigma = sd(p)
lp = as.vector(scale(p[keep]))		# Scale to mean=0 and var=1 => Convert to z-scores using an inverse normal distribution

c = c[keep]
lc = log10(c)

#---
# Linear model
#---
lmfit = lm( lp ~ lc )
sumLmfit =  summary(lmfit)
pvalCoef = sumLmfit$coefficients[2,4]

res = lmfit$residuals		# Residuals
padj = sigma * res + mu	# Adjusted bayesian factor

# Don't adjust under 'minScoreCount'
padj[ c <= minScoreCount ] = p[ c <= minScoreCount ]

if( show ) {
	print(sumLmfit)
	cat('Slope:\t', sumLmfit$coefficients[2], '\tp-value:\t', pvalCoef, 'File:\t', fileName, '\n');

	par(mfrow=c(2,2))
	smoothScatter( lc, p, main="Z-Scores", xlab="Number of scores", ylab="Z-Score" )
	lines(lowess(lc, p ), col='orange')

	smoothScatter( lc, padj, main="Adjusted Z-Scores", xlab="Number of scores", ylab="Z-Score" )
	lines(lowess(lc, padj ), col='orange')

	plot( density(p) , main="Bayes-Factors distribution", xlab="red:Adjusted black:Unadjusted")
	lines( density(padj) , col='red')

	plot( density(lc) , main="log10(Number of scores)", xlab="")
}

#---
# Decide whether to use corrected scores or not
#---
if( pvalCoef < pvalCoefThreshold ) { 
	so = padj		# Significant? Then use correction
	so[ c <= minScoreCount ] = p[ c <= minScoreCount ]
} else {
	so = d$score	# Not very significant? Use original scores
}

#---
# Create output file
#---
dout = data.frame( geneId = d$geneId[keep], score = so )
write.table( dout, file=outFileName, quote=FALSE, row.names = FALSE, col.names = FALSE, sep="\t")

