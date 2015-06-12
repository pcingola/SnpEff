#!/bin/sh

rsync -rlpt -v -z --delete --port=33444 rsync.wwpdb.org::ftp_data/structures/divided/pdb/ ./pdb

