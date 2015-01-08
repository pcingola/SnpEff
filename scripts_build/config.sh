#!/bin/sh

export VERSION_SNPEFF=4.1
export SUBVERSION_SNPEFF=""
export VERSION_SNPSIFT=$VERSION_SNPEFF

export ENSEMBL_RELEASE=78
export ENSEMBL_BFMPP_RELEASE=24

# Version values using underscores ('3_2' instead of '3.2')
export SNPEFF_VERSION=`echo $VERSION_SNPEFF | tr "." "_"`
export SNPSIFT_VERSION=`echo $VERSION_SNPSIFT | tr "." "_"`

export SNPEFF_VERSION_REV=$SNPEFF_VERSION"$SUBVERSION_SNPEFF"

# HUman and mouse versions
GRCH=GRCh38
GRCM=GRCm38
