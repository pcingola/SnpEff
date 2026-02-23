# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## IMPORTANT: Documentation Policy

**DO NOT document program behavior, usage, features, or user-facing functionality in this file.**
All user-facing documentation (how the program works, command-line options, file formats, examples, etc.) belongs EXCLUSIVELY in `src/docs/`, which is the source for the user's manual published at https://pcingola.github.io/SnpEff/.
This file (CLAUDE.md) is ONLY for developer guidance: build instructions, code architecture, and development workflows. Never duplicate or paraphrase user-facing documentation here.

## Project Overview

SnpEff is a variant annotation and effect prediction tool for genomic variants. It's a Java-based bioinformatics application that analyzes VCF files and predicts the functional effects of genetic variants.

Documentation: https://pcingola.github.io/SnpEff/ (source files in `src/docs/`)

## Build System

The project uses Maven for building and BDS (BigDataScript) for automation tasks.

### Building JAR files

```bash
# Build SnpEff JAR with dependencies
mvn clean compile assembly:single jar:jar

# The output JAR will be in target/SnpEff-5.2-jar-with-dependencies.jar
```

### Running SnpEff

Use the wrapper script that handles Java memory options:

```bash
# Using the wrapper script
./scripts/snpEff [options]

# Direct JAR execution
java -Xms1g -Xmx8g -jar snpEff.jar [options] -c snpEff.config
```

The main class is `org.snpeff.SnpEff`.

## Testing

Tests use JUnit Jupiter (JUnit 5):

```bash
# Run all tests using Maven
mvn test

# Run specific test class
mvn test -Dtest=TestCasesVcf

# Run BDS-based integration tests
./src/bds/integration_test.bds
```

Test files are located in:
- `src/test/java/org/snpeff/snpEffect/testCases/unity/` - Unit tests
- `src/test/java/org/snpeff/snpEffect/testCases/integration/` - Integration tests
- `tests/` - Test data files (VCF, FASTA, etc.)

## Code Architecture

### Core Packages

- `org.snpeff` - Main entry point and top-level commands
- `org.snpeff.snpEffect` - Core effect prediction engine
  - `Config` - Configuration management for genomes and databases
  - `SnpEffectPredictor` - Main predictor that analyzes variants
  - `VariantEffect` - Represents predicted effects on genes/transcripts
  - `commandLine/` - Command-line interface implementations for all subcommands
  - `factory/` - Factories for building genome databases
- `org.snpeff.interval` - Genomic intervals (genes, transcripts, exons, chromosomes)
  - Core genomic feature classes: `Gene`, `Transcript`, `Exon`, `Chromosome`, `Genome`
  - `Marker` - Base class for genomic intervals
  - `Variant` - Represents genetic variants
- `org.snpeff.vcf` - VCF file parsing and manipulation
  - `VcfEntry`, `VcfFileIterator` - Core VCF handling
- `org.snpeff.fileIterator` - File format parsers (VCF, GFF, GTF, BED, GenBank, etc.)
- `org.snpeff.outputFormatter` - Output formatters for different formats

### Command Line Structure

The main `SnpEff` class dispatches to subcommand implementations in `org.snpeff.snpEffect.commandLine`:
- `SnpEffCmdEff` - Main annotation command (default: "ann")
- `SnpEffCmdBuild` - Build genome databases from GTF/GFF files
- `SnpEffCmdDownload` - Download pre-built databases
- `SnpEffCmdDatabases` - List available databases
- And many more specialized commands

### Configuration

Configuration is managed through `.config` files in the `config/` directory:
- `snpEff.config` - Main configuration (symlink to `config/snpEff.config`)
- `config/snpEff.core.config` - Core configuration settings
- `config/snpEff.ENSEMBL_*.config` - Ensembl genome configurations
- Configuration files define genome data directories and database locations

## Database Building

Database building is orchestrated using BDS scripts (`src/bds/make.bds`):

```bash
# Build a specific genome database (requires genome data in data/ directory)
java -jar snpEff.jar build -v <genome_name>

# Download pre-built database
java -jar snpEff.jar download <genome_name>
```

Genome data is expected in `data/<genome_name>/` directory with:
- `genes.gtf` or `genes.gff` - Gene annotations
- `sequences.fa` - Reference genome sequences

## Java Version

The project requires Java 21 (specified in pom.xml compiler configuration).

## Key Design Patterns

- Command pattern: Each SnpEff subcommand is implemented as a separate class implementing `CommandLine` interface
- Iterator pattern: File parsers use iterator pattern for memory-efficient processing of large genomic files
- Factory pattern: Genome database factories handle different source formats (GTF, GFF, GenBank, EMBL)
- Serialization: Genome databases are pre-built and serialized to `.bin` files for fast loading

## BDS Automation (make.bds)

The file `src/bds/make.bds` is the main entry point for development tasks: building, testing, downloading genomes, building databases, creating distribution packages, and uploading releases. It uses BDS (BigDataScript), a DSL for running heavyweight pipelines. Run it from `~/snpEff/`:

```bash
~/snpEff/make.bds [options]
```

With no arguments, it builds the JAR files (equivalent to `-make`).

### Build and test options

```
-make                Build JAR files (snpEff.jar and SnpSift.jar)
-createConfig        Create/regenerate the snpEff.config file
-createDocs          Build documentation web pages from markdown (uses mkdocs)
-db                  Build all genome databases
-dbs <genome ...>    Build specific genome databases by name
-dbTest              Build databases needed for test cases
-distro              Create distribution zip files (core + all database zips)
-distroCore          Create only the core distribution zip
-test                Run SnpEff and SnpSift test suites
```

### Download options

```
-download                              Download all genome datasets
-downloadSet <set>                     Download one dataset. Sets: ensembl, ensemblBfmpp, ucsc, mane, pdb, dbsnp, dbnsfp, cytobands, jaspar, gwasCatalog, nextprot, clinvar, flybase
-downloadEnsembl <genome>              Download a specific Ensembl genome (e.g. 'GRCh38.103'), requires -downloadEnsemblSpecies
-downloadEnsemblSpecies <species>      Ensembl species name (e.g. 'Homo_sapiens'), must match Ensembl capitalization
-downloadMane                          Download MANE transcripts
-downloadNcbi <genome>                 Download genome from NCBI (also requires -ncbiId)
-ncbiId <id>                           NCBI genome ID (used with -downloadNcbi)
-downloadUcsc <genome>                 Download genome from UCSC
```

### Upload options (S3)

```
-uploadCore          Upload core zip package to S3
-uploadDbs           Upload all database zips from the zip/ directory to S3
-uploadDbNsfp        Upload dbNSFP databases to S3
-uploadDev           Upload development version to S3
-zipGenome <g ...>   Create zip file for specific genome(s)
```

### AWS/S3 configuration

```
-awsProfile <name>          AWS profile (default: 'snpeff')
-s3Bucket <name>            S3 bucket (default: 'snpeff-public')
-s3DatabasesPath <path>     S3 path for databases
-s3VersionsPath <path>      S3 path for versioned releases
-s3HttpUrl <url>            S3 HTTP URL
```

### Ensembl/MANE/Flybase version options

```
-ensemblRelease <int>                  Ensembl vertebrate release number (default: 115)
-ensemblBfmppRelease <int>             Ensembl BFMPP release number (default: 57)
-maneGenome <string>                   MANE genome (default: 'GRCh38')
-maneReleases <string ...>             MANE release versions
-maneSelect                            Use MANE 'select' version
-maneTrIdTypes <string ...>            Transcript ID types: 'ensembl', 'refseq'
-flybaseRelease <string>               Flybase release (default: 'FB2022_02')
-dbCompatibleVersions <string ...>     List of database compatible versions
```

### Key directories used by make.bds

All paths are relative to `~/snpEff/`:
- `build/` -- build log files
- `data/` -- genome data and compiled databases
- `db/` -- external databases (nextProt, PDB, JASPAR, cytoBands, etc.)
- `download/` -- downloaded genome files
- `zip/` -- database zip files for distribution

## Companion Tool: SnpSift

SnpSift is a companion tool developed in a separate repository but typically distributed together with SnpEff. The build script (`scripts_build/make.sh`) builds both tools.
