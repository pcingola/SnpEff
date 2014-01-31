
library(pheatmap)   
library(gplots)

pdfSize <- 15

# Read data
d <- read.table('heatMap.txt', sep='\t', header=TRUE)
dm <- as.matrix(d)

# Replace NA by 0, otherwise R stops
dm[ is.na(dm) ] <- 0

# PDF output
pdf( width=pdfSize, height=pdfSize, "heatMap_small.pdf")
pheatmap(dm) # Create heatmap
dev.off()

ratio <- dim(dm)[2] / dim(dm)[1]
pdfSizeX <- ratio * pdfSize
pdf( width=pdfSizeX, height=pdfSize, "heatMap_large.pdf")
pheatmap(dm) # Create heatmap
dev.off()
