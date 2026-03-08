
# Why hg19 RefSeq annotations may not match

This page provides an in-depth technical explanation of why RefSeq transcript annotations on hg19 frequently do not match the reference genome, and what practical effect they have on variant annotation. If you are getting unexpected annotations on hg19 RefSeq transcripts, this page will help you understand why.


## The root cause: RefSeq is not derived from the reference genome

RefSeq was designed as a **consensus** of experimentally observed transcript sequences. The sequences in RefSeq come from cDNA sequencing of real biological samples. The reference genome (hg19/GRCh37) is a **mosaic** assembled from multiple individuals. There is no guarantee that the reference genome carries the same alleles as the individuals whose transcripts were sequenced for RefSeq.

When UCSC (or NCBI) maps RefSeq transcripts onto the reference genome, they are taking transcript sequences that were observed in one set of individuals and projecting them onto a genome assembled from a different set of individuals. The mapping process defines where the exons are on the genome (the coordinates in the GTF file), but the actual nucleotide sequence at those coordinates comes from the genome, not from RefSeq.

At most positions, the genome and RefSeq agree. But at some positions -- typically common polymorphisms -- they differ. And for some transcripts, the mapping itself is incorrect: the coordinates are wrong, exon boundaries are shifted, or the transcript maps to multiple loci ambiguously.


## How SnpEff builds a genome database

When you run `java -jar snpEff.jar build -v hg19`, the database goes through a multi-stage pipeline:

1. **Parse the GTF file.** SnpEff reads `data/hg19/genes.gtf` (derived from the UCSC refGene table). This file defines the genomic coordinates of each gene, transcript, exon, CDS, and UTR -- but it does not contain actual nucleotide sequences. Here is a real example showing transcript NM_015330 (gene SPECC1L, chr22, plus strand):

        chr22  refGene  transcript  24666799  24813706  .  +  .  gene_id "SPECC1L"; transcript_id "NM_015330";
        chr22  refGene  exon        24666799  24666951  .  +  .  ... exon_number "1"; ...
        chr22  refGene  5UTR        24666799  24666951  .  +  .  ... exon_number "1"; ...
        chr22  refGene  exon        24672668  24672771  .  +  .  ... exon_number "2"; ...
        chr22  refGene  5UTR        24672668  24672771  .  +  .  ... exon_number "2"; ...
        chr22  refGene  exon        24698163  24698352  .  +  .  ... exon_number "3"; ...
        chr22  refGene  5UTR        24698163  24698199  .  +  .  ... exon_number "3"; ...
        chr22  refGene  CDS         24698200  24698352  .  +  0  ... exon_number "3"; ...

    Exons 1-2 are entirely 5'UTR. Exon 3 is split: bases 24698163-24698199 are 5'UTR, bases 24698200-24698352 are CDS. The `0` in column 8 is the reading frame (0 = first base of this CDS segment is the first base of a codon).

2. **Infer UTRs from CDS boundaries** (if not explicitly defined in the GTF). Some GTF files include explicit UTR lines (as in the example above). Others only have exon and CDS lines. SnpEff infers UTRs by subtracting the CDS intervals from the exon intervals. Whatever exon sequence remains outside the CDS must be a UTR. To determine whether it is 5'UTR or 3'UTR, SnpEff checks its position relative to the CDS boundaries: for plus-strand genes, intervals before the CDS start are 5'UTR and intervals after the CDS end are 3'UTR; for minus-strand genes, intervals with higher genomic coordinates than the CDS start are 5'UTR (because the 5' end is at higher coordinates on the minus strand). This matters because the UTR boundaries directly determine where the CDS begins and ends when exon sequences are assembled.

3. **Frame correction.** The GTF carries frame information for each CDS segment (column 8: 0, 1, or 2). The frame indicates how many bases must be skipped at the start of that CDS segment to reach the first complete codon. SnpEff uses this in two passes. First, if the first coding exon has a non-zero frame, SnpEff creates a small artificial 5'UTR at the start of the coding region to compensate (shifting the effective CDS start by 1-2 bases). Second, for each subsequent coding exon, SnpEff calculates the expected frame from the cumulative CDS length and compares it to the declared frame. If they disagree, the exon boundary is adjusted by 1-2 bases until the frames match. This ensures codons are read in the correct frame across exon-exon junctions. If a CDS segment is too short to absorb the correction, a `WARNING_CDS_TOO_SHORT` is emitted (see Category F).

4. **Extract exon sequences from the reference genome.** SnpEff reads `data/genomes/hg19.fa` and extracts the nucleotide sequence at each exon's genomic coordinates. Minus-strand exons are reverse-complemented.

5. **Assemble the CDS.** All exon sequences are concatenated in transcript order (5' to 3'), then the 5'UTR and 3'UTR portions are trimmed off. The result is the "genome-derived CDS" -- the coding sequence as determined by the reference genome at the coordinates specified by the GTF. **This is the sequence SnpEff uses for all variant effect predictions.** If this sequence differs from what RefSeq expects, annotations will be wrong at the differing positions.

6. **Verify against RefSeq's own CDS sequences.** For each protein-coding transcript, SnpEff compares its genome-derived CDS against the RefSeq CDS from `data/hg19/cds.fa`. The comparison uses a cascade of checks, tried in order until one passes:
    - **Exact CDS match** -- genome-derived CDS equals RefSeq CDS exactly.
    - **mRNA match** -- the full mRNA (CDS + UTRs) matches the reference (some `cds.fa` entries contain the full mRNA, not just the CDS).
    - **mRNA + poly-A tail** -- mRNA matches if you strip a trailing poly-A tail from the reference.
    - **Substring match** -- the genome-derived CDS is fully contained within the reference (handles cases where the reference includes extra flanking sequence).
    - **Mismatch** -- none of the above passed. The transcript is marked as an error (`*`). SnpEff performs a Smith-Waterman local alignment (for sequences under 33,000 bp) with scoring +1 match, -1 mismatch, -2 gap. The result reports a `Score`, `Max. possible score` (length of shorter sequence), and `Diff` (Max - Score = number of differing positions).


## hg19 build results

During the build, SnpEff also emits warnings about structural issues in the GTF annotations:

- `WARNING_GENE_COORDINATES` -- a gene's coordinates do not fully contain one of its transcripts; SnpEff expands the gene boundaries automatically.
- `WARNING_GENE_NOT_FOUND` -- a transcript is on an alternate haplotype contig but its parent gene was defined on the primary assembly; SnpEff creates a new gene entry.
- `WARNING_CDS_TOO_SHORT` -- a CDS segment is too short to apply frame correction (see Category F below).

These warnings do not prevent the build from completing, but they indicate annotation quality issues that may affect specific transcripts.

The hg19 build produces the following summary:

```
# Genes                      : 33224
# Protein coding genes       : 22528
# Transcripts                : 77529
# Protein coding transcripts : 58834
#              Length errors  :    157 ( 0.27% )
#  STOP codons in CDS errors :    100 ( 0.17% )
#         START codon errors  :    136 ( 0.23% )
#        STOP codon warnings  :     21 ( 0.04% )
#               Total Errors  :    342 ( 0.58% )
# Exons                      : 778789
# Exons with sequence        : 778789
# Exons without sequence     : 0

CDS check:  OK: 68791  Warnings: 20112  Not found: 5015  Errors: 3723  Error percentage: 5.13%
```

This output contains results from two separate verification phases:

**Transcript sanity checks** (the upper block, checked during database construction):

- **Length errors (157):** CDS length is not a multiple of 3. The reading frame is broken (see Category D).
- **STOP codons in CDS (100):** The translated protein contains a premature stop codon before the end (see Category E).
- **START codon errors (136):** The first three CDS bases are not a valid start codon, e.g. not ATG (see Category E).

**CDS comparison** (the `CDS check` line, checked after database construction by comparing each genome-derived CDS against the RefSeq CDS from `cds.fa`):

- **OK (68,791):** The genome-derived CDS matched RefSeq's CDS. These transcripts will produce accurate annotations.
- **Warnings (20,112):** The CDS matched RefSeq, but the start or stop codon is not what the codon table expects. SnpEff checks the first 3 bases against valid start codons and the last 3 bases against valid stop codons. Each failed check increments the warning counter, so a single transcript with both a bad start and a bad stop codon contributes 2 warnings. The 20,112 is the total number of individual warnings, not transcripts. Annotations on these transcripts are still largely accurate, but effects at the very start or end of the CDS (e.g., `start_lost`, `stop_gained`) may be unreliable.
- **Not found (5,015):** The transcript ID from the GTF had no corresponding entry in `cds.fa`. This is normal for non-coding transcripts (NR_ prefix) which have no CDS by definition.
- **Errors (3,723 = 5.13%):** The genome-derived CDS differs from RefSeq's CDS. These are the problematic transcripts where variant annotations may be inaccurate (see Categories A, B, C).


## Categories of mismatches with real examples

All examples below come from the actual hg19 database build. You can reproduce them by running the build with the `-d` (debug) flag:

```bash
# Warning: The output file is huge (~700 MB), because it contains details for every transcript
java -jar snpEff.jar build -d hg19 2>&1 | tee hg19_build_debug.out
```


### Category A: Single-nucleotide polymorphisms between genome and RefSeq

This is the most common type of mismatch. The GTF coordinates are correct, the exon structure is correct, but at a handful of positions the hg19 reference genome carries a different allele than the RefSeq consensus sequence. This happens because RefSeq transcripts were sequenced from individuals whose DNA differed from the reference genome at common polymorphic sites.

#### Example: NM_000256 (MYBPC3, chr11, minus strand, 34 exons)

MYBPC3 (cardiac myosin-binding protein C) is clinically important for hypertrophic cardiomyopathy. The debug build log shows:

```
ERROR: CDSs do not match for transcript NM_000256  Strand:true  Exons: 34
    SnpEff CDS  (  3825) : 'atgcctgagccggggaagaag...'
    Reference   (  4217) : 'agtccctctttgggtgacctg...'
    Alignment.  Score: 3821  Max. possible score: 3825  Diff: 4
```

The SnpEff genome-derived CDS is 3,825 bp. The RefSeq reference is 4,217 bp because the `cds.fa` entry for this transcript includes UTR flanking sequence (55 bp of 5'UTR prefix starting with `agtccctctttgggtgacctg...` and ~337 bp of 3'UTR suffix). The important number is the alignment: 3,821 matches out of 3,825 possible, meaning **exactly 4 nucleotide positions differ** between the genome and RefSeq.

The GTF defines 34 exons spanning chr11:47352957-47374253. Here is the structure:

```
chr11  refGene  transcript  47352957  47374253  .  -  .  gene_id "MYBPC3"; transcript_id "NM_000256";
chr11  refGene  exon        47374173  47374253  .  -  .  ... exon_number "1"   <- 5'UTR
chr11  refGene  start_codon 47374196  47374198  .  -  .  ...
chr11  refGene  exon        47372789  47373055  .  -  .  ... exon_number "2"   <- partial UTR, partial CDS
chr11  refGene  CDS         47372789  47373055  .  -  2  ... exon_number "2"
...                                                          (exons 3-33: all CDS)
chr11  refGene  exon        47352957  47353266  .  -  .  ... exon_number "34"  <- partial CDS, 3'UTR
chr11  refGene  stop_codon  47353422  47353424  .  -  .  ...
```

SnpEff extracts the sequence at each exon's coordinates from hg19.fa, reverse-complements (minus strand), concatenates, trims the UTR, and obtains the CDS starting with `ATGCCTGAGCCGGGGAAGAAG...`.

The RefSeq CDS for this transcript (from `cds.fa`) starts with `agtccctctttgggtgacctgtgcctgcttcgtgcctggtgtgacgtctctcagg` -- 55 bases of UTR -- then `ATGCCTGAGCCGGGGAAGAAG...` matching the genome. The two sequences are nearly identical, but 4 positions differ. Here is what the alignment looks like at the two mismatch regions:

**Mismatch region 1** (around CDS position ~867):

```
SnpEff (genome):    ...CACTGCTGAAAAAGAGGGACAGTTTCCGG...
                                         *
RefSeq (cds.fa):    ...CACTGCTGAAAAAGAGAGACAGTTTCCGG...
```

At this position, the hg19 genome has `G` while RefSeq has `A`. This is a known common polymorphism (SNP). The RefSeq transcript was sequenced from an individual who carried the `A` allele. The hg19 reference genome carries the `G` allele.

**Mismatch region 2** (around CDS position ~1125):

```
SnpEff (genome):    ...GCAGCAGGTACATCTTTGAGTCC...
                            *
RefSeq (cds.fa):    ...GCAGCAAGTACATCTTTGAGTCC...
```

Again, a G-vs-A difference at a single position.

**What this means for variant annotation:** Suppose a patient has a variant at one of these mismatch positions that changes the genome's `G` to `A` (matching RefSeq). SnpEff will annotate this as a missense (or synonymous) variant because from the genome's perspective, the base changed. But from RefSeq's perspective, `A` is the normal allele -- the variant is actually restoring the "correct" RefSeq base. This creates a false positive for anyone using RefSeq as their ground truth for the normal protein sequence.

For a clinically important gene like MYBPC3, even a single false-positive missense call could trigger unnecessary follow-up. The 4-position difference means there are 4 genomic sites where this transcript will produce potentially misleading annotations.


### Category B: CDS boundary disagreement (UCSC vs RefSeq)

In this category, the GTF coordinates place the CDS start (or end) at a different position than RefSeq expects. The result is that a large chunk of what RefSeq considers coding sequence is treated as UTR by SnpEff, or vice versa. This typically happens when the UCSC mapping pipeline and RefSeq disagree on where the translation initiation site is.

#### Example: NM_001351299 (ABCF3, chr3, plus strand, 21 exons)

ABCF3 is an ATP-binding cassette transporter. The debug build log shows:

```
ERROR: CDSs do not match for transcript NM_001351299  Strand:false  Exons: 21
    SnpEff CDS  (  1401) : 'atgacactcctgccctgcag...'
    Reference   (  2579) : 'gcactccgctcctttcctgcg...'
    Alignment.  Score: 1397  Max. possible score: 1401  Diff: 4
```

SnpEff's genome-derived CDS is only 1,401 bp, while RefSeq's CDS is 2,579 bp -- nearly twice as long. The difference is 1,178 bp of coding sequence that RefSeq includes but the UCSC GTF treats as UTR.

The GTF defines 21 exons spanning chr3:183903863-183911795. Here is how the exons are classified:

```
Exon  1:  chr3:183903863-183904067   frame: .   <- entirely 5'UTR
Exon  2:  chr3:183904294-183904441   frame: .   <- entirely 5'UTR
Exon  3:  chr3:183904583-183904662   frame: .   <- entirely 5'UTR
Exon  4:  chr3:183905184-183905230   frame: .   <- entirely 5'UTR
Exon  5:  chr3:183905451-183905548   frame: .   <- entirely 5'UTR
Exon  6:  chr3:183905648-183905770   frame: .   <- entirely 5'UTR
Exon  7:  chr3:183905928-183906194   frame: .   <- partially 5'UTR, CDS starts at 183906096
Exon  8:  chr3:183906541-183906628   frame: 2   <- CDS
Exon  9:  chr3:183906716-183906775   frame: 1   <- CDS
...
Exon 21:  chr3:183911327-183911795   frame: 0   <- partial CDS, then 3'UTR from 183911487
```

The GTF places the start codon at chr3:183906096-183906098 (within exon 7). Everything upstream -- exons 1 through 6 and the first part of exon 7 -- is classified as 5'UTR.

But the RefSeq CDS for NM_001351299 starts with `gcactccgctcctttcctgcgtcaccgctacacatgcgccttgcaaggaa...` -- this corresponds to sequence from exon 1, which the GTF calls UTR. RefSeq considers translation to begin 1,178 bp upstream of where the UCSC GTF places it.

Within the 1,401 bp overlap (from the UCSC-defined CDS start to the end), there are also 4 single-nucleotide differences (Score 1397 vs Max 1401). Around CDS position ~100:

```
SnpEff (genome):    ...CCCAGATTGCTGCTGGCAGCCTCCAGGGCGGAGGGCTCTGAAG...
                                               **
RefSeq (cds.fa):    ...CCCAGATTGCTGCTGGCAGCCACAAGGGCGGAGGGCTCTGAAG...
```

SnpEff has `CTCCAG` where RefSeq has `CACAAG` -- two nucleotides differ at this junction.

**What this means for variant annotation:** The protein predicted by SnpEff is truncated: it is missing the N-terminal ~392 amino acids that RefSeq includes. Any variant falling in exons 1-6 (chr3:183903863-183905770) or the 5'UTR portion of exon 7 (chr3:183905928-183906095) will be annotated as `5_prime_UTR_variant` instead of a coding variant (missense, synonymous, etc.). A pathogenic missense variant in the N-terminal domain of this protein would be completely invisible to coding-level annotation.


### Category C: Completely wrong mapping (total sequence mismatch)

In the worst cases, the genomic coordinates in the GTF are entirely wrong for a given transcript isoform. The exon positions map to a region of the genome that has nothing to do with the expected gene product. The genome-derived CDS and the RefSeq CDS share zero similarity.

#### Example: NM_001267550 (TTN, chr2, minus strand, 363 exons)

TTN is the largest known human gene, with hundreds of exons and multiple complex isoforms. The debug build log shows:

```
ERROR: CDSs do not match for transcript NM_001267550  Strand:true  Exons: 363
    SnpEff CDS  (107976) : 'atgacaactcaagcaccgacgtttacgcag...'
    Reference   (109224) : 'gagcagtcgtgcattcccagcctcgcctcg...'
    Alignment.  Score: 0  Max. possible score: 107976  Diff: 107976
```

The alignment score is **zero**. In Smith-Waterman alignment, a score of 0 means no local alignment was found that scores above zero -- the two sequences share no significant similarity at all. The genome-derived CDS (107,976 bp) and RefSeq's CDS (109,224 bp) are both over 100 kb long, yet they are completely unrelated.

Notice the telling sign in the sequences: the SnpEff CDS starts with `atgacaactcaagcaccgacg...` (a proper ATG start codon), while the RefSeq CDS starts with `gagcagtcgtgcattcccagc...` (no ATG). This strongly suggests the `cds.fa` entry for this transcript contains the wrong sequence or is in the wrong orientation, or that the UCSC coordinates for this particular isoform map to an entirely wrong region of chromosome 2.

The 363 exons span chr2:179390717-179672150, covering ~281 kb of genomic sequence:

```
Exon   1:  chr2:179671938-179672149   frame: .    <- 3'UTR + CDS (minus strand: higher coord = 5' end)
Exon   2:  chr2:179669278-179669381   frame: .    <- UTR
...
Exon  18:  chr2:179647532-179647790   frame: 0    <- CDS
Exon  19:  chr2:179647265-179647328   frame: 2    <- CDS
...
Exon 362:  chr2:179392172-179392474   frame: 2    <- CDS
Exon 363:  chr2:179390716-179392033   frame: 2    <- CDS + 3'UTR
```

When SnpEff extracts the genome sequence at these 363 exon positions, reverse-complements (minus strand), and concatenates, the result is a 107,976 bp CDS that codes for a protein. But it is the **wrong** protein -- the one encoded by this genomic region is not the TTN isoform that NM_001267550 is supposed to represent.

**What this means for variant annotation:** Every single variant annotated on this transcript will produce incorrect results. The amino acid predictions, the HGVS notation, the effect classification -- all of it is meaningless because the underlying CDS is completely wrong. This is the worst-case scenario for a mismatch. Any variant in the TTN region annotated using this specific transcript isoform on hg19 should be treated with extreme skepticism.


### Category D: Broken reading frame (CDS length not a multiple of 3)

The hg19 build reports 157 protein-coding transcripts whose CDS length is not a multiple of 3. Since codons are triplets, a CDS of, say, 1,000 bp (instead of 999 or 1,002) means there is an extra or missing base somewhere. The reading frame is broken and the protein cannot be reliably translated.

This typically happens when the GTF exon boundaries are slightly off: an exon is one base too long or too short compared to the real splice site. Since the GTF coordinates come from mapping RefSeq transcripts to the genome (an imperfect process, as we have seen), small coordinate errors accumulate. A single misplaced exon boundary shifts the reading frame for the entire downstream CDS.

These 157 transcripts are detected during database construction as part of the transcript sanity checks. The check is simple: after assembling the CDS from exon sequences (step 5 of the build pipeline), SnpEff checks whether CDS length is divisible by 3. If not, the transcript is counted in the "Length errors" summary line. Unlike the CDS comparison errors (Categories A-C), these errors are not logged per-transcript in the build output -- they appear only as the aggregate count `Length errors: 157`.

**How SnpEff behaves at annotation time:** When a variant falls on one of these transcripts, SnpEff's sanity check detects the broken reading frame and adds a `WARNING_TRANSCRIPT_INCOMPLETE` flag to the annotation. The HGVS protein notation becomes `p.?` (unknown effect) instead of a specific amino acid change like `p.Arg123Cys`. SnpEff does not attempt to translate a broken-frame CDS to protein, so all protein-level information (amino acid change, protein position, domain) is lost for that transcript. The variant effect is still reported (e.g., `missense_variant`), but without reliable protein-level detail.


### Category E: Premature stop codons and missing start codons

Like Category D, these are detected during the transcript sanity checks at build time and reported as aggregate counts. They are not individually logged in the build output.

**Premature stop codons (100 transcripts):** After assembling the CDS and translating it to protein, SnpEff checks whether the protein contains a stop codon (`*`) before the last position. If it does, the transcript has a premature termination. This typically happens when a single-nucleotide difference between the genome and RefSeq falls within a codon and converts it to a stop. For example, if RefSeq has `CAG` (Gln) at a position but the hg19 genome has `TAG` (stop), the genome-derived protein will terminate prematurely at that point. All downstream amino acids are lost -- the protein predicted by SnpEff is truncated compared to the real RefSeq protein.

SnpEff allows one internal stop codon (to accommodate selenocysteine and other rare amino acids encoded by stop codons in special contexts). Only transcripts with more than one internal stop are flagged.

**How SnpEff behaves at annotation time:** The transcript is flagged with `WARNING_TRANSCRIPT_MULTIPLE_STOP_CODONS`. Any variant on this transcript will carry this warning. SnpEff still attempts to predict the variant effect, but the protein sequence it uses is wrong (truncated at the premature stop), so the amino acid change, protein position, and HGVS protein notation may all be incorrect. For variants downstream of the premature stop, the prediction is meaningless.

**Missing start codons (136 transcripts):** After assembling the CDS, SnpEff checks whether the first three bases are a valid start codon according to the organism's codon table. For the standard genetic code this means `ATG`. For mitochondrial genes, alternative start codons like `ATA`, `ATC`, `ATT`, `GTG` are also valid. If the first codon is not a valid start, the transcript is flagged.

This happens when the CDS start coordinate in the GTF is wrong by one or more bases, or when a polymorphism at the start codon position changes the `ATG` to something else in the hg19 genome. For example, if RefSeq has `ATG` but the genome has `ACG` at the CDS start, SnpEff will see a non-start codon.

**How SnpEff behaves at annotation time:** The transcript is flagged with `WARNING_TRANSCRIPT_NO_START_CODON`. Annotations are still produced, but the protein translation starts from whatever codon is there (not necessarily methionine), which may differ from the real RefSeq protein. Variants near the start codon may be annotated as synonymous when they should be `start_lost`, or vice versa.

There are also 21 transcripts where the CDS **does not end** with a valid stop codon. These are reported as "STOP codon warnings" (not errors) in the build summary. SnpEff flags these with `WARNING_TRANSCRIPT_NO_STOP_CODON`. The debug build log shows these explicitly, for example:

```
WARNING: CDS for transcript 'NR_102370' does not start with a start codon:  TGG  TGGAAAAGACAATGATGTTTTATTTC...
WARNING: CDS for transcript 'NR_102370' does not end with a stop codon:     CCA  ...TCTGAGTTGTATGTGTGGACAGCACTGAGACTGAGTCTTTCCA
```

Note that these particular debug-log warnings appear for all transcripts (including NR_ non-coding transcripts that SnpEff treats as coding for verification purposes). The 136 START errors and 100 STOP errors in the build summary count only protein-coding transcripts (NM_ prefix).


### Category F: Frame correction failures (CDS too short)

During the database build, SnpEff adjusts exon and CDS boundaries to match the reading frame declared in the GTF (step 3 of the build pipeline). For each CDS segment after the first, SnpEff calculates what the frame should be based on the cumulative CDS length so far (frame = (3 - (length % 3)) % 3). If this calculated frame does not match the frame declared in the GTF, SnpEff adjusts the exon's start (or end, for minus-strand genes) by 1 base at a time until the frames agree, shifting the corresponding CDS marker in tandem.

When a CDS segment is very short (e.g., only 1 base long), the required frame correction may be larger than the CDS itself. For instance, if a 1-bp CDS needs a 1-base correction, there is nothing left after the correction. In that case, the correction cannot be applied and the build emits a warning. The hg19 build log contains 27 instances of this warning across 5 distinct genomic loci:

```
WARNING_CDS_TOO_SHORT: CDS too short, cannot correct frame: frame size 1,
    frame correction 1, CDS: 3  147106645-147106645
    CDS 'STOP_CODON_STOP_CODON_3_147106646_147106646', frame: 0
```

```
WARNING_CDS_TOO_SHORT: CDS too short, cannot correct frame: frame size 1,
    frame correction 1, CDS: 16  89865486-89865486
    CDS 'STOP_CODON_STOP_CODON_16_89865487_89865487', frame: 0
```

The 5 affected loci are on chr3 (position 147106645), chr10 (126449071), chr13 (99447001), chr16 (89865486), and chr22 (44645564). All show the same pattern: a 1-bp CDS segment (start == end coordinate) derived from a stop codon that cannot absorb even a 1-base frame correction. The warning fires multiple times per locus because multiple transcripts share the same stop codon coordinates.

**How SnpEff behaves at annotation time:** The affected transcripts have uncorrected frame errors. This means the reading frame for exons downstream of the failed correction may be shifted by 1 or 2 bases, producing a completely wrong protein translation from that point onward. Variant effect predictions on these transcripts (amino acid changes, protein position, HGVS notation) will be incorrect for any variant downstream of the failed frame correction. Unlike Categories D and E, there is no specific warning flag added at annotation time -- the transcript appears normal but produces wrong results silently.


## Summary: how many transcripts are affected

To put the numbers in perspective, here is the full breakdown for the hg19 database. Note that there are two separate verification phases, each catching different problems. A transcript can fail both (e.g., a CDS that does not match RefSeq AND has a broken reading frame), or fail just one.

**Phase 1: Transcript sanity checks** (during database construction). These check the internal consistency of each transcript model built from the GTF coordinates and genome sequence:

| Check | Count | % of protein-coding |
|---|---|---|
| CDS length not a multiple of 3 (Category D) | 157 | 0.27% |
| Premature stop codons in CDS (Category E) | 100 | 0.17% |
| Missing start codon (Category E) | 136 | 0.23% |
| **Total transcript-level errors** | **342** | **0.58%** |

**Phase 2: CDS comparison** (after database construction). Each transcript's genome-derived CDS is compared against the RefSeq CDS from `cds.fa`:

| Category | Count | % of protein-coding |
|---|---|---|
| CDS matches genome perfectly | 68,791 | ~70% |
| CDS matches but minor start/stop issues | 20,112 | ~20% |
| Non-coding (no CDS expected) | 5,015 | N/A |
| **CDS does NOT match genome** | **3,723** | **~5.1%** |

The 3,723 CDS-check errors are the transcripts where variant annotations are most likely to be inaccurate. Most are Category A (small SNP differences), but some are Category B (CDS boundary disagreement) or Category C (complete mismatch). Even a single nucleotide difference can affect clinical interpretation at that position.


## The solution: use MANE on GRCh38

All of these problems are resolved by using MANE (Matched Annotation from NCBI and EMBL-EBI) transcripts on the GRCh38 genome. SnpEff provides these databases as `GRCh38.mane.1.2.ensembl` and `GRCh38.mane.1.2.refseq`.

MANE transcripts are specifically designed to solve the genome/transcript mismatch problem. Every MANE transcript's CDS perfectly matches the GRCh38 reference genome (0% error rate). RefSeq and Ensembl agree 100% on coding sequence and UTRs for every MANE transcript. There is one well-supported transcript per protein-coding locus, eliminating ambiguity from multiple isoforms with different mapping quality. The transcript set is jointly curated by NCBI and EMBL-EBI, manually reviewed, versioned, and stable. Both Ensembl and RefSeq transcript IDs are available, so you can use whichever naming convention your pipeline requires.

To switch:

```bash
# Download the database (Ensembl IDs)
java -jar snpEff.jar download GRCh38.mane.1.2.ensembl

# Annotate
java -jar snpEff.jar ann GRCh38.mane.1.2.ensembl input.vcf > annotated.vcf
```

Or with RefSeq IDs:

```bash
java -jar snpEff.jar download GRCh38.mane.1.2.refseq
java -jar snpEff.jar ann GRCh38.mane.1.2.refseq input.vcf > annotated.vcf
```

If you must use hg19 for legacy reasons, be aware that approximately 5% of RefSeq transcripts will have CDS mismatches and annotations at those loci may be inaccurate. For clinical-grade annotation, MANE on GRCh38 is strongly recommended.


## How to investigate a specific transcript

If you suspect a particular transcript has a mismatch, you can check it yourself.

### Step 1: Check the CDS verification result

Rebuild with debug mode to see the CDS comparison for every transcript:

```bash
# This produces ~700 MB of output; redirect to a file
java -jar snpEff.jar build -d hg19 2>&1 | tee hg19_build_debug.out

# Then search for your transcript
grep -A 10 "NM_YOUR_TRANSCRIPT" hg19_build_debug.out
```

The output will show the genome-derived CDS, the RefSeq CDS, the alignment score, and the exact number of mismatches. For example:

```
ERROR: CDSs do not match for transcript NM_000256  Strand:true  Exons: 34
    SnpEff CDS  (  3825) : 'atgcctgagccggggaagaag...'
    Reference   (  4217) : 'agtccctctttgggtgacctg...'
    Alignment.  Score: 3821  Max. possible score: 3825  Diff: 4
```

The `Diff` value tells you how many positions differ. `Diff: 4` means 4 mismatches. `Diff: 107976` (as in the TTN example) means total mismatch.

### Step 2: Check the GTF coordinates

```bash
zcat data/hg19/genes.gtf.gz | grep "NM_000256"
```

This shows you the exon structure, CDS boundaries, start/stop codon positions, and strand. Check whether the exon count and CDS boundaries match what RefSeq expects.

### Step 3: Check the RefSeq CDS directly

```bash
zcat data/hg19/cds.fa.gz | grep -A 5 "NM_000256"
```

This shows the RefSeq CDS sequence. Compare the first and last few bases with the genome-derived CDS from the debug output to see where they diverge.

### Step 4: Use `snpeff show` for a visual summary

```bash
java -jar snpEff.jar show hg19 NM_000256
```

This displays the transcript structure, exon sequences, CDS, and protein translation in a human-readable format.

By comparing the GTF coordinates, the genome sequence at those coordinates, and RefSeq's own CDS, you can determine exactly where and why a mismatch occurs for any transcript.

For more details on the different human genome versions available in SnpEff, see the [Human Genomes](human_genomes.md) page.
