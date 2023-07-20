# Help, New databases and Bugs

This project is maintained by [**Pablo Cingolani**](http://www.linkedin.com/in/pablocingolani)

## Asking for help

Please send any questions by creating issues in repositories:

* [SnpEff - https://github.com/pcingola/SnpEff/issues](https://github.com/pcingola/SnpEff/issues)
* [SnpSift - https://github.com/pcingola/SnpSift/issues](https://github.com/pcingola/SnpSift/issues)

!!!warning
    **Important:** In order to assess and your problem precisely, I must be able to reproduce your example.

Any request for help should include at least the following items:

Item         | Explanation | Example
------------ | ----------- | ---------
What?        | A clear explanation of what you are trying to achieve | "I want to annotate variants... and then filter them to obtain..."
How?         | A minimal demonstration of how you are trying to do it | "we run the following command...".<br> Make sure you send the exact command lines you are using.
Which?       | Which organism and genome version you are using | "we are analyzing human samples using GRCh37.72 reference genome"
Version      | Which SnpEFf version and sub-version you are using?<br> Make sure to try the **latest version** since the issue might be already solved. | "we are using SnpEFf 3.3H").<br> Hint: Running `java -jar snpEff.jar` shows version information.
Command line | The full command line that shows the issue. | `java -Xmx8g -jar snpEff.jar GRCh37.75 sample.vcf ...`
Data         | Sample data enough to reproduce the conditions<br>**Always attach the data files** such as VCF lines (even if you added some snippets in the email's body)  | "find attached a sample of the VCF file..."
Results      | What you expect to obtain | "we wanted to get information on variants that affect splcie regions..."

**Note:** A few guidelines when asking for help...

* **Use the latest version.** May be the issue has already been corrected.

* **Read the documentation first.** Please make sure you've read the documentation before asking fo help.
        I know this is obvious, but a lot of people ask for questions in that are answered, or shown as examples in the documentation.

* **If the documentation did not help.** I'm aware that the documentation is not always "easy reading".
        So let me know which part of the manual is confusing, outdated or plain wrong.
        I'll do my best to update and improve the docs.

* **Be polite.** This one should be obvious, but there is a reason it's in the list...

* **I'm too busy.** I always try to help people, but oftentimes I'm busy and I won't be able to answer your emails & requests in due time and manner.
     Apologies in advanced.

* **If I don't get back to you...** Do remind me if I don't get back to you within a few days.
      Sometimes I'm swamped with work or just out of town, a polite reminder helps.

* **Ask for too much advice.** I don't mind helping, I've been doing it forever.
      However I won't be able to help you with all the details (due to limited bamdwith), so most times, the best I can do is to provide some pointers and general advise you can follow up on.

## Bug reports

Please send any bug reports by creating issues in repositories:

* SnpEff:  [https://github.com/pcingola/SnpEff/issues](https://github.com/pcingola/SnpEff/issues)
* SnpSift: [https://github.com/pcingola/SnpSift/issues](https://github.com/pcingola/SnpSift/issues)

!!!warning
    **Important:** In order to assess and fix the issue, I must be able to reproduce exactly your error condition.

This means that I need the following information:

Item         | Explanation     | Example
------------ | --------------- | ---------
What?        | A clear explanation of the bug condition.<br>Please copy the full stack trace if one is available.| "a RuntimeException occurss when trying to annotate..."
How?         | A minimal bug-demonstrating test case | "when running the following command/s...".<br> Make sure you send the exact command lines you are using.
Which?       | Which organism and genome version you are using (if applicable) | "we use GRCh37.72"
Version      | Which SnpEFf version and sub-version you are using?<br> Make sure to try the **latest version** since the bug might be already fixed. | "we are using SnpEFf 3.3H").<br> Hint: Running `java -jar snpEff.jar` shows version information.
Command line | The full command line that shows the issue. | `java -Xmx8g -jar snpEff.jar GRCh37.75 sample.vcf ...`
Data         | Sample data enough to reproduce the conditions<br>**Always attach the data files** such as VCF lines (even if you added some snippets in the email's body)  | "find attached a sample of the VCF file..."
Results      | What you expect to obtain (if applicable)| "we expected result X, but we obtained Y instead..."

**Important tips:**

* Make sure you **attached** a minimal dataset to reproduce the error condition.
    For instance, a few VCF lines showing the problem are enough (sometimes one VCF line is enough), most of the times I don't need the whole VCF file.

* Always send the data **attached**, even if it's only one VCF line. Many times the problem is a malformed line, which I can only asses if the data is attached, and

* If you have to send large files, as sometimes happens with genome references, try uploading them to a server (e.g. Google Drive) and sending me a link. Remember to always compress them.

* **Try SnpEff's latest version.**
    SnpEff is updated often, may be the bug has already been corrected (e.g you are using version 5.0A, but I've already fixed it in version 5.0B).

## Asking for new features

I always take into account features request.
So don't hesitate to send me your ideas by creating issues in repositories:

* SnpEff:  [https://github.com/pcingola/SnpEff/issues](https://github.com/pcingola/SnpEff/issues)
* SnpSift: [https://github.com/pcingola/SnpSift/issues](https://github.com/pcingola/SnpSift/issues)

Of course, it  doesn't mean I'll get all suggestions implemented as soon as I get a request.
But if a lot of people are asking for the same feature, it is a strong indicator that I should try to implement it.

Please, don't be frustrated if I tell you I cannot implement your suggestion / idea. Keep in mind that some features are too difficult to implement for non-obvious reasons, e.g.:

* SnpEff uses some standard (e.g. VCF) and new features cannot break those standards.
* SnpEff is used by a LOT of people in MANY different pipelines. New features must be compatible with current infrastructure and cannot break operational production pipelines.
* Some feature requests are just too difficult to code, requiring large changes.

## Asking for new database / genome

I'm willing to help you out if you need to build a new database (a.k.a. add a new reference genome).
Building a new database (i.e. adding a new reference genome) is relatively easy and the procedure is described in detail [in the documentation](snpeff/build_db.md).
If you are unable to build the database, I can try to help you out. Please, create issue in repository: <https://github.com/pcingola/SnpEff>.

In order to do so, I need you to send me the following information:

* Name of the species
* Reference genome version
* A link to the reference genome sequence (i.e. a link to the FASTA file)
* A link to a gene and transcript definition file (either GTF, GFF, GenBank, etc.)
* At least one of the following (preferably, both):
     * A link to the CDS sequences (FASTA file)
     * A link to the protein sequences (FASTA files)
* Codon table information (take a look a the codon tables in `snpEff.config` file):
     * Which codon table does the organism use? (e.g. `codon.Standard` or `codon.Mycoplasma`).
     * Specific codon tables used by some chromosomes (e.g. chromosome `'MT'` uses `codon.Vertebrate_Mitochondrial` table)

I'm well aware that I could try to google that information myself, but I need you to send the information because I want to be absolutely sure we are using exactly the reference genome version, sub-version, and data files you need.

## Contact information

<strong>[Pablo Cingolani](http://www.linkedin.com/in/pablocingolani)</strong>
