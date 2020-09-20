

# Release process

0) Install required libraries

```
# ANTRL
wget http://www.antlr.org/download/antlr-4.5.1-complete.jar
mvn install:install-file -Dfile=antlr-4.5.1-complete.jar -DgroupId=org.antlr -DartifactId=antlr -Dversion=4.5.1 -Dpackaging=jar
```

1) Change version numbers:
- Update SnpEff pom.xml
- Update SnpSift pom.xml

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

5) Build databases
```
./make.bds -db
```

6) Upload files to sourceForge

```
./make.bds -uploadCore		# Upload core files
./make.bds -uploadDbs		# Upload databases files
./make.bds -uploadHtml		# Upload web pages and manual
```

7) Update documentation
```

```

# SnpEff's Documentation

SnpEff uses `mkdocs`.
To build docs you need mkdocs-material theme installed.
`mkdocs` dependencies is included in `mkdocs-material` now (ref: <https://www.mkdocs.org/#installing-mkdocs>

```
# Go to snpEff's install dir, activate virtual environment containing mkdocs
cd ~/snpEff; source ./bin/activate

pip install mkdocs-material==5.2
```

- Building docs: This will create `site` directory (ref: <https://www.mkdocs.org/#building-the-site>)
```
# Go to snpEff's install dir, activate virtual environment containing mkdocs
cd ~/snpEff; source ./bin/activate

mkdocs build
```

- Localhost web-server starting: This will create local web-site on <http://127.0.0.1:8000> (ref: <https://www.mkdocs.org/#getting-started>)
```
# Go to snpEff's install dir, activate virtual environment containing mkdocs
cd ~/snpEff; source ./bin/activate

mkdocs serve
```

- Deploying docs: You just need to copy the `site` directory to `docs` and push to GitHub
```
cd ~/workspace/SnpEff
cp -rvf ~/snpEff/site/* docs/

./git/commit 'Documentation updated'
```
