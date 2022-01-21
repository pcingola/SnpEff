# Release process: Core package

1) Change version numbers:
   - Update SnpEff pom.xml
   - Update SnpSift pom.xml
   - Update `scripts_build/make.sh`, line `export VERSION=5.1`
   - Check `Config.DATABASE_COMPATIBLE_VERSIONS` for new version compatibilities added
2) Build JAR files: `./make.bds`
3) Run tests: `./make.bds -test`
4) Run `SnpEff/git/reelease` and `SnpSift/git/reelease`
5) Create distro ZIP files: `./make.bds -distroCore`
6) Upload core files to Azure: `./make.bds -uploadCore`
7) Update web page and documentation: `./make.bds -createDocs`, see [Documentation section](#documentation)

# Release process: Databases

1) Download databases: `./make.bds -download`, (see [Download Database sources](#download_database_sources)
2) Build databases: `./make.bds -db`, see section "Databases sources updates details" for details
3) Upload files to Azure: `./make.bds -uploadDbs`

**AWS instance notes**:
Downloading database sources: Can be done in a small instance (4 cores)
- Disk: 2 TB
- Memory: 32 GB (building dbNSFP loads large files in memory to sort by chr:pos)

Building databases: 64 cores recommended

# Documentation

- **NOTES**:
	- GitHub pages are published from `/docs` directory (main project's directory). This is configured in GitHub's project settings.
	- Markdown source for `mkdocs` is under `src/docs`

### Building docs

```
./make.bds -createDocs
```

This will create `site` directory (ref: <https://www.mkdocs.org/#building-the-site>) and copy the html pages to `docs` directory, so when you push to github it will be "published"

### Testing: Local

Localhost web-server starting: This will create local web-site on <http://127.0.0.1:8000> (ref: <https://www.mkdocs.org/#getting-started>)
```
# Go to snpEff's install dir, activate virtual environment containing mkdocs
cd ~/snpEff; source ./bin/activate
./bin/mkdocs serve
```

### Publishing docs

Just commit to GitHub, the updated pages in `docs` directory will be shown after a few minutes

```
./git/commit 'Documentation updated'
```

### Requirements

SnpEff uses `mkdocs`.
To build docs you need mkdocs-material theme installed.
`mkdocs` dependencies is included in `mkdocs-material` now (ref: <https://www.mkdocs.org/#installing-mkdocs>

```
# Go to snpEff's install dir, activate virtual environment containing mkdocs
cd ~/snpEff; source ./bin/activate

pip install mkdocs-material
```

# Download Database sources

### Database compatible versions

Update `make.bds`, variable `dbCompatibleVersions`. 
Add list of all database versions that are compatible with this release

### ENSEMBL vertebrates release number

1) Go to ENSEMBL's site and check the latest release number: http://ftp.ensembl.org/pub/
2) Update `make.bds` variable `ensemblRelease`
3) Create an empty config file `config/snpEff.ENSEMBL_{RELEASE_NUMBER}.config`
4) Run `./make.bds -download` will create the appropriate line for the config file `config/snpEff.ENSEMBL_{RELEASE_NUMBER}.config`
5) 

### UCSC genomes list updates

1) Download list of UCSC genomes from API
```
curl https://api.genome.ucsc.edu/list/ucscGenomes > ucscGenomes.json
```
2) Check UCSC genomes (API call + jq):
``` 
jq -r '.ucscGenomes | keys[] ucscGenomes.json'
```
3) Use the list of genomes to update `make.bds`, variable `ucscGenomes`
4) WARNING: You need to filter out repeated genomes and use the latest versions for each (maybe except `hg` and `mm`)

### ENSEMBL vertebrates release number

1) Check latest release number from http://ftp.ensemblgenomes.org/pub/ 
2) Update release number on `make.bds`, variable `ensemblBfmppRelease`
3) Create an empty config file `config/snpEff.ENSEMBL_BFMPP_{RELEASE_NUMBER}.config`
4) Run `./make.bds -download` will create the appropriate line for the config file `config/snpEff.ENSEMBL_BFMPP_{RELEASE_NUMBER}.config`

### FlyBase

**Note:** It looks like FlyBase is not providing any GTF files for other than 'dmel', this is really weird, they used to have them in the past (maybe the project is dead/dying?)

1) Check latest release from http://ftp.flybase.net/releases/
2) Update `make.bds`, variable `flybaseRelease`
3) Update the list of genomes, variable `flybaseGenomes`
4) Check which genomes have GTF files (right click on each link and look for `gtf` subdir)
5) Create / Update the config file `config/snpEff.FLYBASE_{FLYBASE_RELEASE}.config`

### MANE Genome

1) Check latest version from https://ftp.ncbi.nlm.nih.gov/refseq/MANE/MANE_human/
2) Update `make.bds`, variable `maneRelease`
3) Update file `config/snpEff.dbs.config`, look for `GRCh38.mane.*` entries

### Clinvar

Usually nothing to update, there is a stable link to the latest version

### dbSnp

Usually nothing to update

1) Check release vresion from https://ftp.ncbi.nih.gov/snp/latest_release/VCF/
2) Update `src/bds/download/downloadHumanDb.bds`
    1) GRCh37: Variable `urlDbSnpGrch37`
    2) GRCh38: Variable `urlDbSnpGrch38`

### dbNSFP

1) Check latest version from https://sites.google.com/site/jpopgen/dbNSFP
2) Update `make.bds`, variable `dbNsfpVersion` (use the version ending in "a")

### Gwas-Catalog

Usually nothing to update

1) Check the page https://www.ebi.ac.uk/gwas/docs/file-downloads
2) WARNING: If there are changes in the format, Java code might need to change

### Jaspar

1) Check the download page https://jaspar.genereg.net/downloads/
2) Download link from https://jaspar.genereg.net/download/data/2022/CORE/JASPAR2022_CORE_non-redundant_pfms_jaspar.txt 
3) Update `src/bds/download/downloadHumanDb.bds`, variable `urlJaspar` 
 
### NextProt

Usually nothing to update

1) Check release from https://download.nextprot.org/pub/current_release/
2) Update link in `src/bds/download/downloadHumanDb.bds`, variable `urlNextProt`

### PDB

1) Check download page for change https://www.rcsb.org/docs/programmatic-access/file-download-services
2) Check rsyn script

### AlphaFold



