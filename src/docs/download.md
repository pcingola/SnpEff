## Downloading SnpEff &amp; SnpSift

SnpEff and SnpSift are bundled together.

> [Download SnpEff](https://snpeff.blob.core.windows.net/versions/snpEff_latest_core.zip)

> Old versions [here](https://sourceforge.net/projects/snpeff/files/).


### License

SnpEff is open source, released as "MIT".

### System requirements

SnpEff requires that you have Java v1.11 or later installed (any modern operating system has it).

The amount of memory used can vary significantly depending on genome size and data analysis type you are doing.
For large genomes, such as the human genome, you'll probably need at least 4Gb of memory.


## Installing SnpEff

Installing SnpEff is very easy, you just have to uncompress the ZIP file.

**How to install using command line (unix systems)**

It is better if you install SnpEff in `snpEff` directory in your home directory (`$HOME/snpEff` in unix systems).
```
# Go to home dir
cd

# Download latest version
wget https://snpeff.blob.core.windows.net/versions/snpEff_latest_core.zip

# Unzip file
unzip snpEff_latest_core.zip
```

### Configuration
In most cases you **DO NOT** need to configure anything.

The only configuration file is `snpEff.config`.
Most configuration parameters, are explained in the comments in the same config file, so I won't repeat the explanation here :-)

Usually you do NOT need to change the configuration.
Some peoeple may need to change the location of the databases (`data.dir` parameter).
By default, this parameter points to the `data` directory where you installed the tool (i.e. in unix systems, this is `./data`).
If you want to change this, you can edit the `snpEff.config` file and change the `data_dir` entry:
```
#---
# Databases are stored here
# E.g.: Information for 'hg19' is stored in data_dir/hg19/
#
# You can use tilde ('~') as first character to refer to your home directory.
# Also, a non-absolute path will be relative to config's file dir
#
#---
data.dir = ./data/
```

## Downloading SnpEff databases

In order to perform annotations, SnpEff automatically downloads and installs genomic database.

!!! info
    By default SnpEff automatically downloads and installs the database for you, so you don't need to do it manually.

Databases can be downloaded in three different ways:

* The easiest way is to let SnpEff download and install databases automatically
* You can pre-install databases manually using the `SnpEff download` command (once SnpEff is installed).
      E.g. to download the human genome database:

         java -jar snpEff.jar download GRCh38.76

      Note: Current human genome version at the time of writing is GRCh38.76.

## Available databases
There are over 20,000 databases available.

A list of databases is available in snpEff.config file.
You can also see all available databases by running the following command (once SnpEff has been installed):

    java -jar snpEff.jar databases

## Source code

### Getting the source
The source code is in GitHub (although we keep the binary distribution is at SourceForge).
Here is the `git` command to check out the development version of the code:

```
# Get SnpEff
git clone https://github.com/pcingola/SnpEff.git

# Get SnpSift as well
git clone https://github.com/pcingola/SnpSift.git
```
###  Building from the source
Most libraries should be install using Maven, so you just need to run `mvn` command.

* Java (JDK)
* ANT
* Maven

Some libraries are not available through maven, so you have to install them into via Maven manually (these libraries are in `SnpEff/lib`)
```
# Go to 'lib' dir
cd SnpEff/lib

# Antlr
mvn install:install-file \
    -Dfile=antlr-4.5.1-complete.jar \
    -DgroupId=org.antlr \
    -DartifactId=antlr \
    -Dversion=4.5.1 \
    -Dpackaging=jar

# BioJava core
mvn install:install-file \
    -Dfile=biojava3-core-3.0.7.jar \
    -DgroupId=org.biojava \
    -DartifactId=biojava3-core \
    -Dversion=3.0.7 \
    -Dpackaging=jar

# BioJava structure
mvn install:install-file \
    -Dfile=biojava3-structure-3.0.7.jar \
    -DgroupId=org.biojava \
    -DartifactId=biojava3-structure \
    -Dversion=3.0.7 \
    -Dpackaging=jar
```
Once the libraries are installed, you can use `make.sh` to build the code
```
cd $HOME/snpEff

# Create link to scripts_build directory if it doesn't exist
ln -s $HOME/workspace/SnpEff/scripts_build

# Invoke the build script
./scripts_build/make.sh
```

### Installing test cases
Test cases require special "test cases databases and genome", you can find them here:

```
# Install test databases in SnpEff's development directory (not the soruce code dir!)
cd $HOME/snpEff

# Download databases and genome for test cases
wget https://snpeff.blob.core.windows.net/databases/test_cases.tgz

# Uncompress
tar -xvzf test_cases.tgz

# Go to Eclipse's workspace directory (where the source code is)
cd $HOME/workspace/SnpEff

# Create a link to the 'data' dir, so that we can run test cases within Eclipse
ln -s $HOME/snpEff/data

# Add data dir to 'gitignore'
echo "/data" >> .gitignore
```
