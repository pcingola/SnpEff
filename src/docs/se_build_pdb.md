# Building databases: PDB and AlphaFold

SnpEff supports Protein interactions calculated from [PDB](http://www.rcsb.org/) or [AlphaFold](https://alphafold.ebi.ac.uk/download).
Adding protein interaction to existing SnpEff databases is rather involved, so it's recommended you use a pre-build database if available (e.g. human).

## Requirements

In order to add protein data to a database, you need:

- A SnpEff database for the organism. The database should have been built or downloaded prior to start adding the protein data
- Protein data: One `*.pdb.gz` file for each protein. This can be downloaded from PDB (real data from cristalized proteins) or AlphaFold (predicted protein structures).
- A "mapping file" that maps transcript IDs to protein IDs

!!! warning
   Adding protein data to a SnpEff database, requires a database to be already built / downloaded

## Protein structure data

You can download protein structure data from PDB by performing executing something like:
```
rsync -rlpt -v -z --delete --port=33444 rsync.wwpdb.org::ftp_data/structures/divided/pdb/
```
See [PDB's documentation](https://www.wwpdb.org/ftp/pdb-ftp-sites).

For AlphaFold, you can download a tar file with all protein predictions from [Ensembl's AlphaFold download page](https://alphafold.ebi.ac.uk/download).
Please check the documentation on how to [cite AlphaFold](https://alphafold.ebi.ac.uk/) if you use their databases in a paper.

## Mapping file

Since SnpEff uses trasncript ID information, whereas proteins use PDB IDs (or Uniprot IDs in case of AlphaFold), we need a file to map across these different IDs.
You should provide a mapping file that maps either:
- `TranscriptID -> PdbID`: in case of PDB files
- `TranscriptID -> UniprodID`: in case of AlphaFold files

## Adding protein data


