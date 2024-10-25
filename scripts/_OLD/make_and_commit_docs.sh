#!/bin/bash -eu
set -o pipefail

rm -rvf site/
mkdocs build
cp -vf src/docs/index.html site/

cd ~/workspace/SnpEff
rm -rvf docs/*
cp -rvf ~/snpEff/site/* docs/

./git/commit 'Documentation updated'

