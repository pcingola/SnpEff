#-------------------------------------------------------------------------------
#
# Show simmulated values from Reactome + GTEx "circuit" simmulation
#
#																Pablo Cingolani
#-------------------------------------------------------------------------------

savePdf <- FALSE
savePdf <- TRUE

maxPval <- 1E-3
minNameCount <- 5

if( ! exists('d') ) {
	# Simaltion data
	d <- read.table('circuits.non_zero.txt', sep='\t', header=TRUE)

	# Experiment names
	names <- read.table('expNames_full.txt', sep='\t', header=TRUE)
	names <- read.table('expNames.txt', sep='\t', header=TRUE)
}

# Experiment indexes
minExp <- 3
maxExp <- dim(d)[2]

# Column names
cnames <- colnames(d)
rnames <- d$entityName

# Experiment name
# Remove low counts and 'NA'
nameShort <- as.vector( names$nameShort[minExp:maxExp] )
nameShort[ is.na(nameShort) ] <- "Etc"
nshort <- sort(unique(nameShort))
for( ns in nshort ) {
	keep <- (nameShort == ns)
	if( sum(nameShort == ns) <= minNameCount ) { 
		nameShort[ nameShort == ns ] <- "Etc" 
	}
}

minColor = 1

if( savePdf ) { pdf(width=20, height=20); }

# Plot values
for( i in 1:length(rnames) ) {
	#if( length( grep("insulin", rnames[i], fixed=TRUE) ) > 0 ) {
	if( (d$enityId[i] == 74695) || (d$enityId[i] == 165690) || (d$enityId[i] == 373676) ) {
		x <- as.numeric(d[i,minExp:maxExp])

		# Kruskal-Wallis test
		kw <- kruskal.test( list( x, nameShort ) )
		pval.kw <- kw$p.value

		# Annova test
		anv <- oneway.test(x ~ nameShort)
		pval <- anv$p.value

		# Density plots
		if( !is.na(pval) && (pval < maxPval)) {
			name = as.character(d$entityName[i])

			# Boxplots
			par( mfrow=c(2,1) ) 
			boxplot(x ~ nameShort, main=name )

			cat('  \t', i,'\tp-value:', pval, '\tNode:', i, '\tId:', d$enityId[i], '\tName:', name, '\n');
			pvalStr <- paste('p-value(ANNOVA):', pval, '  p-value(Kruskal):', pval.kw)
			dens <- density(x)
			plot( dens, xlim=c(-1,1), main=rnames[i], sub=pvalStr, xlab="All" )

			if( F ) {

			# Plot chrts for each tissue
			par( mfrow=c(3,3) ) 
			col <- minColor
			for( ns in nshort ) {
				# Only keep matching experiments
				keep <- (nameShort == ns) & (! is.na(x))
				xns <- x[keep]
				xns <- xns[ ! is.na(xns) ]

				# At least 'minNameCount' samples?
				if( length(xns) > minNameCount ) {
					# Plot this density and overall density
					main <- paste(ns, '(#samples:',length(xns), ')')
					plot( density(xns), xlim=c(-1,1), main=main, xlab=rnames[i], sub=col, col=col )
					lines( dens, lty=2, col='gray' )

					col <- col + 1
				}
			}

			}
		} else {
			cat('NO\t', i,'\tp-value:', pval, '\tNode:', i, d$enityId[i], d$entityName[i],as.character(rnames[i]), '\n');
		}
	}
}

if( savePdf ) { dev.off(); }
