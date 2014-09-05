
#-------------------------------------------------------------------------------
# Mean non-zero value 
#-------------------------------------------------------------------------------

lnz <- function(x) {
	log2( x[(x > 0)] );
}

mainName <- function(n) {
	idx = regexpr(".SM.", n, fixed=T)[1]
	if( idx > 1 ) {
		return( substr(n, 1, idx-1) )
	}
	return(n)
}

#-------------------------------------------------------------------------------
# Main
#-------------------------------------------------------------------------------

# Load data
if( ! exists('gt') ) {
	cat("Loading data from gtex.txt\n");
	gt = read.table("gtex.txt", sep="\t", header=TRUE);
}

minCol = 3

# Experiment names
colExpNames = as.vector( sapply(colnames(gt), mainName) )
expNames = unique( sort( colExpNames[3:ncol(gt)] ) )

# Plot histogram
plot(0, 0, xlim = c(-15,15), ylim = c(0, 0.15) )

#---
# Perform
#	i) Log2( colum )
#	ii) Column wise mean correction
#---
cat("Step 1:\n");
L = matrix( 0 , nrow = nrow(gt), ncol = ncol(gt)-2 )
for( idx in minCol:ncol(gt) ) {
	cat('\t', colExpNames[idx], "\n");
	x = gt[,idx]
	l = log2( x )	

	k = ( x > 0 )
	l[!k] = NA

	mu = mean( l, na.rm = TRUE )
	l = l - mu

	L[,idx-minCol+1] = l
}

#---
# Difference to average
#---
cat("Step 2:\n");
lmu = rowMeans(L, na.rm = TRUE)
plot( density( lmu[k] ) , xlim = c(-15,15) )
for( idx in 1:ncol(L) ) {
	l = L[, idx] - lmu

	k = ! is.na(l)
	mu = mean(l , na.rm = TRUE )
	l = (l - mu) / sd(l,  na.rm = TRUE)

	lines( density( l[k] ), col=idx )

	L[,idx] = l

	cat('\t', colExpNames[idx], "\n");
}

colnames(L) = colnames(gt)[minCol:ncol(gt)]
norm = data.frame( geneId = gt[,1], geneName = gt[,2], L)

write.table(norm, file="gtex_norm.txt", quote = FALSE, sep = "\t", row.names = FALSE)

