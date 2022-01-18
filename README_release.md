

# Release process

1) Change version numbers:
- Update SnpEff pom.xml
- Update SnpSift pom.xml
- Update `scripts_build/make.sh`, line `export VERSION=5.1`
- Check `Config.DATABASE_COMPATIBLE_VERSIONS` for new version compatibilities added 
 
2) Build JAR files, download databases, build databases, etc.
```
./make.bds
```

3) Run JUnit tests and integration tests
```
./make.bds -test
```

4) Download databases: ENSEMBL, NCBI, dbSnp, ClinVar, dbNSFP, PDB, Jaspar, etc.  
```
./make.bds -download
```

5) Build databases: See section "Databases sources updates details" for details
```
./make.bds -db
```

6) Upload files to sourceForge

```
./make.bds -uploadCore		# Upload core files
./make.bds -uploadDbs		# Upload databases files
```

7) Update documentation: See next section for details
```
./make.bds -createDocs
```

# SnpEff's Documentation

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

# Databases sources updates details

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

### PDB

### ClinVar

### dbNSFP

### AlphaFold



