
# Create direcotry for genome reference files
cd ~/snpEff
mkdir -p data/puccinia_striiformis_pst-78/ORI
cd data/puccinia_striiformis_pst-78/ORI
mv ~/Downloads/puccinia_striiformis_pst-78_1_* .

# Uncompress
unzip puccinia_striiformis_pst-78_1_data.zip
unzip puccinia_striiformis_pst-78_1_transcripts.gtf.zip
unzip puccinia_striiformis_pst-78_1_transcripts.fasta.zip
unzip puccinia_striiformis_pst-78_1_proteins.fasta.zip

# Copy files using the names SnpEff expects
cp puccinia_striiformis_pst-78_1_supercontigs.fasta ~/snpEff/data/genomes/puccinia_striiformis_pst-78.fa
cp puccinia_striiformis_pst-78_1_transcripts.gtf ../genes.gtf 
cp puccinia_striiformis_pst-78_1_transcripts.fasta ../cds.fa
cp puccinia_striiformis_pst-78_1_proteins.fasta ../protein.fa

# Add entry in config file
cd ~/snpEff
echo "puccinia_striiformis_pst-78.genome : puccinia_striiformis_pst-78" >> snpEff.config

# Build
java -jar snpEff.jar build -v puccinia_striiformis_pst-78
