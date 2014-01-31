
#--------------------------------------------------------------------------------
#
# Logistic regression of coEvolutionary model
#
#															Pablo Cingolani 2013
#--------------------------------------------------------------------------------

library(epicalc)

#--------------------------------------------------------------------------------
# Histogram using non-NA values
#--------------------------------------------------------------------------------

histNA <- function( x ) {
	plot( density( x[ ! is.na(x) ] ) )
}

#--------------------------------------------------------------------------------
# Main
#--------------------------------------------------------------------------------

#---
# Parse command line args
#---

args <- commandArgs(trailingOnly = TRUE)
useNorm <- TRUE

if( length(args) > 0 ) {
	# Parse command line arguments
    resultsFileName <- args[1]	# Results from previous steps (pairs of genomic positions we want to further investigate)
    gtFileName <- args[2]		# Gentypes
    phenoFileName <- args[3]	# Phenotypes and coveriates
    PC <- args[4]				# Number of PC to use (only 3 and 10 are supported)
    cat('# Parameters:\n')
    cat('# \tResults file    :', resultsFileName, '\n')
    cat('# \tGenotypes file  :', gtFileName, '\n')
    cat('# \tPhenotypes file :', phenoFileName, '\n')
    cat('# \tNumber of principal components:', PC, '\n')
    cat('# \tUse normalized age, age^2, sex:', useNorm, '\n')
} else {
	# Default parameters
    resultsFileName	<- "coEvolution.MAX.txt"
    gtFileName		<- "coEvolution.MAX.gt.txt"
    phenoFileName	<- "coEvolution.pheno.covariates.txt"
	PC <- 3
}

#---
# Load data
#---
if( ! exists('dres') ) {
	# Read data
	cat('# Reading results from', resultsFileName, '\n')
	dres <- read.table(resultsFileName, sep="\t", header=TRUE)

	cat('# Reading phenotypes from', phenoFileName, '\n')
	dpheno <- read.table(phenoFileName, sep="\t", header=TRUE)

	numSamples <- dim(dpheno)[2] - 1

	cat('# Reading genotypes from', gtFileName, '\n')
	genotypes <- matrix( scan(gtFileName, sep="\t", what = integer()), ncol=numSamples, byrow=TRUE )
}

#---
# Parse the data file
#---
maxRow <- dim(dpheno)[1]
maxCol <- dim(dpheno)[2]
cols <- 2:maxCol

# Extract phenotypes
phenoRow <- 1			
pheno <- as.numeric( dpheno[phenoRow,cols] ) - 1

cat('# Phenotypes:\n')
cat('#     Cases    :', sum(pheno == 1), '\n' )
cat('#     Controls :', sum(pheno == 0), '\n' )
cat('#     Missing  :', sum(pheno <  0), '\n' )
cat('#     Max      :', max(pheno), '\n' )
cat('#     Min      :', min(pheno), '\n' )

phenoN <- pheno
phenoN[ pheno < 0 ] <- NA
phenoN <- as.vector( scale( phenoN ) )

# Extract sex
sexRow <- maxRow - 1
sex <- as.numeric( dpheno[sexRow,cols] ) - 1
cat('#\n# Sex:\n')
cat('#     Female  :', sum(sex == 1), '\n' )
cat('#     Male    :', sum(sex == 0), '\n' )
cat('#     Unknown :', sum(sex < 0), '\n' )

sexN <- sex
sexN[ sex < 0 ] <- NA
sexN <- as.vector( scale( sexN ) )

# Extract age
ageRow <- maxRow
age <- as.numeric( dpheno[ageRow,cols] )
cat('#\n# Age:\n')
cat('#     Unknown :', sum(age < 0), '\n' )
cat('#     Avg     :', mean(age[age>0]), '\n' )
cat('#     Max     :', max(age), '\n' )

# Normalize age
ageN <- age
ageN[age <= 0] <- NA
ageN <- as.vector( scale(ageN) )

age2N <- age^2
age2N[age <= 0] <- NA
age2N <- as.vector( scale(age2N) )

# Extract principal components (used as covariates)
pcRows <- 2:(maxRow-2)
pcs <- data.matrix( dpheno[pcRows,cols] )

# Extract p_values
pvalueCol <- 9:12
pvaluesMat <- data.matrix( dres[, pvalueCol] )
pvalues <- apply(pvaluesMat, 1, min)		# Min pvalue in each row
cat('# \n# p-values:\n')
cat('#     Min :', min(pvalues), '\n' )
cat('#     Max :', max(pvalues), '\n' )

#---
# Analyze each row (calcualte logistic regression)
#---

cat('\nLogistic regression:\n')

rowsToAnalyze <- 1:dim(dres)[1]
keepPAS <- ( pheno >= 0 ) & (age > 0) & (sex >= 0)
keepPAS <- ( pheno >= 0 ) & ( !is.na(ageN) ) & ( ! is.na(sexN) )

for( i in rowsToAnalyze ) {
	# Prepare data
	idx1 <- dres$idx1[i]
	idx2 <- dres$idx2[i]

	checksum1 <- dres$checksum1[i]
	checksum2 <- dres$checksum2[i]

	gt1 <- genotypes[idx1,]
	gt2 <- genotypes[idx2,]

	if( sum(gt1) != checksum1 ) { stop(paste("Checksum error in entry ", i, "\n\tIndex: ", idx1, "\n\tChecksum: ", sum(gt1), ' != ', checksum1, '\n')); }
	if( sum(gt2) != checksum2 ) { stop(paste("Checksum error in entry ", i, "\n\tIndex: ", idx2, "\n\tChecksum: ", sum(gt2), ' != ', checksum2, '\n')); }

	# Any missing data? Don't use the sample
	keep <- keepPAS & (gt1 >= 0) & (gt2 >= 0)

	ph <- pheno[keep]
	if( useNorm ) {
		ag <- ageN[keep]
		ag2 <- age2N[keep]
		sx <- sexN[keep]
	} else {
		ag <- age[keep]
		ag2 <- age[keep]^2
		sx <- sex[keep]
	}

	gt1 <- gt1[keep]
	gt2 <- gt2[keep]
	pc <- pcs[,keep]

	maxg12 <- pmax(gt1 - gt2, 0)
	maxg21 <- pmax(gt2 - gt1, 0)
	prod12 <- gt1 * gt1				# Note: This is not vector multiplication, is element by element multiplication (result is a vector of the same size)

	#---
	# Logistic regression
	#---
	if( PC == 10 ) {
		# PC: First 10 components
		lr0 <- glm( ph ~ maxg12 + maxg21 + gt1 + gt1 + sx + ag + ag2 + pc[1,] + pc[2,] + pc[3,] + pc[4,] + pc[5,] + pc[6,] + pc[7,] + pc[8,] + pc[9,] + pc[10,] , family=binomial)		# Full model, takes into account genotypes and PCs
		lr1 <- glm( ph ~                   gt1 + gt1 + sx + ag + ag2 + pc[1,] + pc[2,] + pc[3,] + pc[4,] + pc[5,] + pc[6,] + pc[7,] + pc[8,] + pc[9,] + pc[10,] , family=binomial)		# Reduced model: only PCs, no genotypes
	} else if ( PC == 3 ) {
		# PC: First 3 components
		#lr0 <- glm( ph ~ maxg12 + maxg21 + gt1 + gt2 + sx + ag + ag2 + pc[1,] + pc[2,] + pc[3,], family=binomial)	# Full model, takes into account genotypes and PCs
		lr0 <- glm( ph ~ prod12          + gt1 + gt2 + sx + ag + ag2 + pc[1,] + pc[2,] + pc[3,], family=binomial)	# Full model, takes into account genotypes and PCs
		lr1 <- glm( ph ~                   gt1 + gt2 + sx + ag + ag2 + pc[1,] + pc[2,] + pc[3,], family=binomial)	# Reduced model: only PCs, no genotypes
	}

	# Likelyhood ratio test
	lrt <- lrtest(lr0, lr1)
	pvalue <- lrt$p.value	# p-value from likelihood ration test

	# Show results
	pvalueOri <- pvalues[i]

	#didx <- idx + minRow - 1
	pos1 <- paste( dres[i,3] )
	gene1 <- paste( dres[i,4] )
	id1 <- paste( dres[i,5] )
	pos2 <- paste( dres[i,6] )
	gene2 <- paste( dres[i,7] )
	id2 <- paste( dres[i,8] )
	ratio <- pvalue / pvalueOri

	# Show if p-value improves
	ind = "  "
	if( pvalue < pvalueOri ) ind = "=>"
	cat( paste(ind, i, pvalue, pvalueOri, ratio, pos1, gene1, id1, pos2, gene2, id2, sep="\t") , '\n')
}

