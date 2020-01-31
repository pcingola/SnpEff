#!/usr/bin/env python3


import argparse
import sys


class GtfLine:
    " A GTF line "
    def __init__(self, line_num, line):
        self.line_num, self.line = line_num, line
        self.parse()

    def __getitem__(self, key):
        return self.attribute[key]

    def has(self, key):
        return key in self.attribute

    def parse(self):
        """ Parse GTF line.
        Simple parsing: Does not take into account escaped characters or
        semicolons in quoted values
        """
        fields = self.line.rstrip().split('\t')
        self.chr, self.source, self.feature = fields[0:3]
        self.start, self.end = int(fields[3]), int(fields[4])
        self.score, self.strand, self.frame, self.attribute_raw = fields[5:9]
        self.attribute = dict()
        for sub in self.attribute_raw.split(';'):
            if not sub:
                continue
            try:
                k, v = sub.strip().split(' ', 1)
                if v.startswith('"') and v.endswith('"'):
                    v = v[1:-1]
                self.attribute[k] = v
            except ValueError:
                pass  # print(f"WARNING: Could not parse attrivute in line '{self.line_num}': '{sub}'")


class Gtf:
    " A GTF file "
    def __init__(self, file_name):
        self.file_name = file_name
        self.tr2protein = dict()
        self.protein2tr = dict()

    def add_tr_protein(self, gtfline):
        " Add trainscript_id <-> protein_id mapping "
        if gtfline.has('transcript_id') and gtfline.has('protein_id'):
            self.tr2protein[gtfline['transcript_id']] = gtfline['protein_id']
            self.protein2tr[gtfline['protein_id']] = gtfline['transcript_id']

    def read(self):
        " Read and parse GTF file "
        with open(self.file_name) as f:
            for line_num, line in enumerate(f):
                if line.startswith('#'):
                    continue
                gtfline = GtfLine(line_num, line)
                self.add_tr_protein(gtfline)


class FixFasta:
    " Fix sequence identifiers in a FASTA file "
    def __init__(self, file_name, gtf):
        self.file_name = file_name
        self.gtf = gtf

    def __call__(self):
        with open(self.file_name) as f:
            for line_num, line in enumerate(f):
                line = line.rstrip()
                if line.startswith('>'):
                    line = self.fix_line(line_num, line)
                print(line)

    def get_protein_id(self, text):
        " Parse a protein_id from a text "
        if text.startswith('"') and text.endswith('"'):
            text = text[1:-1]
        if text.startswith('[') and text.endswith(']'):
            text = text[1:-1]
        if text.startswith('(') and text.endswith(')'):
            text = text[1:-1]
        if '=' in text:
            text = text.split('=', 1)[1]
        if text in self.gtf.protein2tr:
            return text
        return None

    def fix_line(self, line_num, line):
        " Change a line having a protein_id to transcript_id "
        words = line[1:].split(' ')
        for i, w in enumerate(words):
            prid = self.get_protein_id(w)
            if prid:
                return '>' + self.gtf.protein2tr[prid]
        return line


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("gtf_file", help="GTF file")
    parser.add_argument("fasta_file", help="FASTA file")
    parser.add_argument('-d', '--debug', help="Debug mode", action='store_true')
    parser.add_argument('-v', '--verbose', help="Verbose mode", action='store_true')
    args = parser.parse_args(sys.argv[1:])
    gtf = Gtf(args.gtf_file)
    gtf.read()
    ff = FixFasta(args.fasta_file, gtf)
    ff()
