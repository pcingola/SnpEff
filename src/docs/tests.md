# SnpEff and SnpSift: Test cases

SnpEff and SnpSift have a comprehensive set of unit and integration test cases.

Some of these test cases require genome databases or other large files, see instructions below on how to download and install the test files.

### Nomenclature

In this document we assume you have the following variables properly set in your environment

```
# Change this to where you installed the SnpEff project from GitHub
export SNPEFF_PROJECT_DIR="$HOME/workspace/SnpEff"

# Change this to your SnpEff install dir
export SNPEFF_DIR="$HOME/snpEff"
```

### Install test datasets

- Note that the download link includes a SnpEff version, so it might change in future releases

- Download the [test dataset here](https://snpeff.blob.core.windows.net/databases/data_test_5.0.tar) 

- Untar and move the files to your SnpEff's `data` directory
```
# Here we assume that your 'data' directory is in '$SNPEFF_DIR/data'
cd $SNPEFF_DIR/data
tar -cvf path/to/data_test.5.0.tar
mv data_test/* .
```

## SnpEff Unit tests

To run unit test suite, you can run the following commands:

```
cd $SNPEFF_PROJECT_DIR

java -Xmx4g \
    -cp $SNPEFF_DIR/snpEff.jar \
    org.junit.runner.JUnitCore \
    org.snpeff.snpEffect.testCases.TestSuiteUnity \
    2>&1 \
    | tee testcases.snpeff.unity.txt
```

## SnpEff integration tests

To run integration test suite, you can run the following commands:

```
cd $SNPEFF_PROJECT_DIR

java -Xmx4g \
    -cp $SNPEFF_DIR/snpEff.jar \
    org.junit.runner.JUnitCore \
    org.snpeff.snpEffect.testCases.TestSuiteUnity \
    2>&1 \
    | tee testcases.snpeff.unity.txt
```

## SnpSift tests

To run SnpSift test suite, you can run the following commands:

```
cd $SNPEFF_PROJECT_DIR

java -Xmx4g \
    -cp $SNPEFF_DIR/SnpSift.jar \
    org.junit.runner.JUnitCore \
    org.snpsift.testCases.TestSuiteAll \
    2>&1 \
    | tee testcases.snpsift.all.txt
```
