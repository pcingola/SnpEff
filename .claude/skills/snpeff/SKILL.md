---
name: snpeff
description: Run SnpEff, a variant annotation and effect prediction tool for genomic variants. Use when the user wants to annotate VCF files, build genome databases, or use SnpEff utilities.
allowed-tools: Bash, Read, Grep, Glob
---

# SnpEff

SnpEff is a variant annotation and effect prediction tool. It analyzes VCF files and predicts the functional effects of genetic variants (such as amino acid changes) on known genes.

Full documentation: https://pcingola.github.io/SnpEff/

## Running

Use the wrapper script at `.claude/skills/snpeff/snpeff.sh`. It handles JVM memory defaults and argument passthrough. The JAR file is expected at `$HOME/snpEff/snpEff.jar`.

**IMPORTANT: SnpEff output is typically very large (thousands to millions of lines). ALWAYS redirect output to a file. NEVER let output print to stdout, as it will fill the context window and make the conversation unusable.**

```bash
# Correct: redirect to file
.claude/skills/snpeff/snpeff.sh <command> [options] [arguments] > output.vcf 2> snpeff.log

# WRONG: never do this
.claude/skills/snpeff/snpeff.sh <command> [options] [arguments]
```

## Commands

Command | Description | Docs
------- | ----------- | ----
`ann` / `eff` | Annotate variants (default command) | [commandline](docs/commandline.md), [running](docs/running.md), [input/output](docs/inputoutput.md)
`build` | Build a SnpEff database from reference genome files | [build_db](docs/build_db.md), [build_db_gff_gtf](docs/build_db_gff_gtf.md)
`buildNextProt` | Build NextProt database from XML files | [commands](docs/commands.md)
`cds` | Compare CDS sequences (database check) | [commands](docs/commands.md)
`closest` | Annotate closest genomic region | [commands](docs/commands.md)
`count` | Count reads/bases overlapping genomic intervals | [commands](docs/commands.md)
`databases` | List available databases | [commands](docs/commands.md)
`download` | Download a pre-built database | [commands](docs/commands.md)
`dump` | Dump database contents (BED/TXT) | [commands](docs/commands.md)
`genes2bed` | Create BED file from gene list | [commands](docs/commands.md)
`len` | Calculate genomic length per marker type | [commands](docs/commands.md)
`pdb` | Build interaction database from PDB/AlphaFold data | [build_pdb](docs/build_pdb.md)
`protein` | Compare protein sequences (database check) | [commands](docs/commands.md)
`seq` | Translate DNA sequence to protein | [commands](docs/commands.md)
`show` | Show gene/transcript text representation | [commands](docs/commands.md)
`translocReport` | Create translocation report with SVG | [commands](docs/commands.md)

## Additional Documentation

Topic | File
----- | ----
Introduction | [introduction](docs/introduction.md)
Additional annotations | [additionalann](docs/additionalann.md)
Building regulation databases | [build_reg](docs/build_reg.md)
Cancer samples | [cansersamples](docs/cansersamples.md)
FAQ | [faq](docs/faq.md)
Human genomes | [human_genomes](docs/human_genomes.md)
Integration (GATK, Galaxy) | [integration](docs/integration.md)
Output summary | [outputsummary](docs/outputsummary.md)
Troubleshooting | [troubleshooting](docs/troubleshooting.md)
Download & Install | [download](docs/download.md)
Examples | [examples](docs/examples.md)
Help | [help](docs/help.md)
