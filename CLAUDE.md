# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SnpEff is a variant annotation and effect prediction tool for genomic variants. It's a Java-based bioinformatics application that analyzes VCF files and predicts the functional effects of genetic variants.

Documentation: https://pcingola.github.io/SnpEff/

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

## Companion Tool: SnpSift

SnpSift is a companion tool developed in a separate repository but typically distributed together with SnpEff. The build script (`scripts_build/make.sh`) builds both tools.
