# Building databases: PDB and AlphaFold

SnpEff supports Protein interactions calculated from [PDB](http://www.rcsb.org/) or [AlphaFold](https://alphafold.ebi.ac.uk/download).
Adding protein interaction to existing SnpEff databases is rather involved, so it's recommended you use a pre-built database if available (e.g. human).

## Requirements

In order to add protein data to a database, you need:

- A SnpEff database for the organism. The database should have been built or downloaded prior to start adding the protein data
- Protein data: One `*.pdb.gz` file for each protein. This can be downloaded from PDB (real data from crystallized proteins) or AlphaFold (predicted protein structures).
- A "mapping file" that maps protein IDs to transcript IDs

!!! warning
   Adding protein data to a SnpEff database, requires a database to be already built / downloaded

## Command line reference

```
Usage: snpEff pdb [options] genome_version

Options:
    -aaSep <num>              : Minimum amino acid separation within the sequence. Default: 20
    -idMap <file>             : File mapping protein IDs to transcript IDs (tab-separated).
    -maxDist <num>            : Maximum distance in Angstrom for amino acids to be considered 'in contact'. Default: 3.0
    -maxErr <num>             : Maximum amino acid sequence mismatch rate (0.0 to 1.0). Default: 0.05
    -org <name>               : Organism common name (case-insensitive). Default: HUMAN
    -orgScientific <name>     : Organism scientific name (case-insensitive). Default: HOMO SAPIENS
    -pdbDir <path>            : Path to directory containing PDB files (searched recursively).
    -res <num>                : Maximum PDB file resolution in Angstrom. Default: 3.0
```

## Usage example

```
java -jar snpEff.jar pdb \
    -v \
    -pdbDir /path/to/pdb_files/ \
    -idMap /path/to/id_mapping.txt \
    GRCh38.mane.1.0.ensembl
```

## Output

The command produces an `interactions.bin` file in the genome's data directory (e.g. `data/GRCh38.mane.1.0.ensembl/interactions.bin`).
This file contains tab-separated records of amino acid pairs that are in close proximity within protein structures, along with their genomic coordinates.
Once created, SnpEff will automatically use this file when annotating variants.

## Protein structure data

You can download protein structure data from PDB by executing something like:
```
rsync -rlpt -v -z --delete --port=33444 rsync.wwpdb.org::ftp_data/structures/divided/pdb/
```
See [PDB's documentation](https://www.wwpdb.org/ftp/pdb-ftp-sites).

For AlphaFold, you can download a tar file with all protein predictions from [Ensembl's AlphaFold download page](https://alphafold.ebi.ac.uk/download).
Please check the documentation on how to [cite AlphaFold](https://alphafold.ebi.ac.uk/) if you use their databases in a paper.

**PDB file filters**: By default SnpEff will filter PDB files using the following criteria

- SnpEff will search PDB formatted file in all subdirectories.
- The file names must end with any of the extensions (case-sensitive): `*.ent`, `*.ent.gz`, `*.pdb`, or `*.pdb.gz` (as usual, files ending in `.gz` are expected to be gzip-compressed)
- The protein ID must be parsed from the file name. Currently accepted file name formats are "PDB-style" (e.g. `pdb7daa.ent.gz`, where PDBID is `7daa`) or AlphaFold-style (e.g. `AF-Q9Y6V0-F9-model_v2.pdb.gz`, where Uniprot-ID is `Q9Y6V0`)
- The PDB file must have a resolution of at least 3 Angstrom. This value can be changed using the `-res` command line option

!!! info
    If the PDB file has a missing resolution value, or the resolution is over `99.0`, then it is assumed to be 'missing value' and not filtered.
    For instance, AlphaFold PDB files usually don't have resolution values since they are predictions, not measurements.

## Mapping file

Since SnpEff uses transcript ID information, whereas proteins use PDB IDs (or Uniprot IDs in case of AlphaFold), we need a file to map across these different IDs.
You should provide a mapping file that maps `proteinId` to `transcriptId`:

The format is: tab-separated, one line per entry:
```
proteinID	transcriptID
```
