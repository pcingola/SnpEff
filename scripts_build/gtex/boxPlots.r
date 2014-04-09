
library(pheatmap)   
library(gplots)

#-------------------------------------------------------------------------------
# Transform into a matrix
#-------------------------------------------------------------------------------

exprList2Matrix <- function( exprList ) {
	# Length of all lists
	lengths <- unlist( lapply( 1:length(exprList), function(x) length(unlist(exprList[[x]])) ) )
	ncol <- max(lengths)
	nrow <- length(lengths)
	exprMatrix <- matrix(data = NA, nrow = nrow, ncol = ncol)

	for( i in 1:nrow ) {
		len <- lengths[i]
		cat('\ti:', i, '\tLength', len, '\n')
		exprMatrix[i,1:len] <- unlist( exprList[[i]] )
	}

	return (exprMatrix)
}

#-------------------------------------------------------------------------------
# Box plot of a matrix
#-------------------------------------------------------------------------------
boxplotMatrix <- function( exprMatrix, title ) {
	# Colorize according to values
	m <- t(exprMatrix)
	means <- colMeans(m, na.rm=T)

	numCols <- 100
	minMean <- -4.5
	maxMean <- 4.5
	cols <- cols <- heat.colors(numCols, alpha = 1)
	colIdx <- round( (numCols-1) * (means - minMean ) / (maxMean  - minMean) + 1 )

	boxplot( m, las=2, col=cols[colIdx], ylim=c(minMean, maxMean), main=title )
	abline( h=0, lty=2, col='grey' )
}

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------

usePdf <- T
if( usePdf) { pdf( width=3, height=50, "boxPlots.pdf"); }

# Read data
d <- read.table('heatMap.txt', sep='\t', header=TRUE)
dm <- as.matrix(d)
dmn <- dm

colnames(dmn) <- NULL
rownames(dmn) <- NULL

lab <-  unlist(colnames(dm)) 

tissues <- c("Brain", "Liver", "Pancreas", "Muscle_Skeletal", "Adipose_Subcutaneous", "Adipose_Visceral")
tissuesShort <- c("Brain", "Liver", "Pancreas", "Skeletal", "Subcut.", "Visceral")

par(mfcol=c(25,1) )

for( geneIdx in 1:dim(dm)[1] ) {
	gene <- rownames(dm)[geneIdx]

	cat('Gene:', gene, '\n')
	exprList <- list()

	for( tissue in tissues ) {
		cat('\tTissue:', tissue, '\n');
		keep <- unlist( lapply( lab, function(x) ! is.na( pmatch(tissue , x)) ) )

		# Append list to list of expressions
		exprList[[ length(exprList)+1 ]] <- list( unlist(dmn[geneIdx,keep]) )
	}

	# Convert to matrix (fill with NA)
	exprMatrix <- exprList2Matrix(exprList)
	# Add tissue names
	rownames(exprMatrix) <- tissuesShort
	# Boxplot
	boxplotMatrix( exprMatrix, gene )
}

if( usePdf ) { dev.off(); }

