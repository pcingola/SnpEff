# Integration: Claude Code

SnpEff and SnpSift are available as [Claude Code](https://claude.ai/code) skills.
This means you can run variant annotation and filtering directly from a conversational AI interface, without memorizing command-line syntax.

Claude Code is Anthropic's CLI tool for AI-assisted software development.
With the SnpEff and SnpSift skills, Claude Code can run SnpEff commands, build databases, annotate VCF files, filter variants, and more, all through natural language.

### How it works

The skills teach Claude Code how to invoke SnpEff and SnpSift, including the correct command-line options, memory settings, and output handling.
You describe what you want in plain English, and Claude Code translates that into the appropriate SnpEff or SnpSift command.

For example, you can say things like:

* "Annotate my variants.vcf file using the GRCh38.105 genome"
* "Filter the annotated VCF to keep only HIGH impact variants"
* "Extract the gene name, variant type, and HGVS notation into a tab-separated file"
* "Build a SnpEff database for my custom genome"
* "Annotate my VCF with dbNSFP scores"

### Setup

1. Install [Claude Code](https://docs.anthropic.com/en/docs/claude-code/overview)
2. The SnpEff and SnpSift skills are included in the SnpEff repository under `.claude/skills/`
3. Run `claude` from the SnpEff directory to start using the skills

### Available skills

**SnpEff skill (`/snpeff`)** -- Runs any SnpEff command: variant annotation (`ann`), database building (`build`), database download (`download`), and all utility commands (`closest`, `count`, `dump`, `genes2bed`, `len`, `seq`, `show`, etc.).

**SnpSift skill (`/snpsift`)** -- Runs any SnpSift command: filtering (`filter`), annotation with databases (`annotate`, `dbnsfp`, `gwasCat`), field extraction (`extractFields`), interval operations (`intervals`, `intersect`, `join`), and all other SnpSift utilities.
