Sisyphus
========

A high-performance data processing framework in java.

Motivation
----------

A common pattern when processing data is to read, modify and update some entries
in a large data set. This kind of processing generally requires to scan the
data set entirely to find the entries that need updates (or deletion). For many
applications, several modifications will be needed, sometimes for different
parts of the data set. Updating a large subset (<10%) of a data set this way
can be very expensive in a database.

Another common pattern is join operations, where you merge two or more data sets
by completing the entries in one data set with data from the other data sets,
for the entries that have common parts.

Sisyphus is a java library that tries to address data processing problems where
a full scan of data sets is required in order to extract or modify the data. It
is, in principle, similar to "sed" or "awk", with some modern, powerful tool
such as hashtables, joins, and user-defined functions.

Sisyphus specifically targets problems where a full file scan is required, and
multiple operations must occur. In this class of problems, I/O is the
bottleneck, and CPU is not an issue. Having a lot of memory can help, but it is
not necessary.

Other frameworks, such as MySql or Hadoop with or without Pig or Hive, have
several drawbacks for this class of problems:
- perform only one operation at a time during a given scan
- processes must have low-memory requirements
- generate large amount of temporary data (x10)
- storage is uncompressed
- uncommon languages, low-level concepts, no libraries
- shared environment can make production processes unreliable

Sisyphus is not suitable for problems where CPU is the bottleneck, or to process
terabytes of data. A distributed solution such as Map-reduce is better suited
for these types of problems.

Sisyphus is not suitable for data exploration, where the modifications to apply
are not known in advance. For such problems, SQL databases or Mathlab are better.

Sisyphus is best suited for problems that include:
- tabular data with a defined schema
- small to very large files (up to 500M rows on 16GB)
- read-modify-write of many rows
- filtering and aggregating by columns
- joining files
- sorting rows in large files
- use of persistent lookup tables
- repeatable process needs to happen reliably
- process needs predictable scalability
- custom input and output formats
- custom processing functions

The development of Sisyphus was sponsored by TheFind.com. TheFind.com uses
Sisyphus to daily add, update, delete and prioritize some attributes of its
500M products index.

Overview
--------

Sisyphus processes files in tabular form with a defined schema: each column
must have a name. By default, the files are in gzipped TSV format: values are
separated by a tab character, and cannot contain tabs or line breaks; each
row is terminated with a newline character.

A Sisyphus program is a java program that defines what the input files are,
what the schema is for each input, what operations will be applied to each row
(optionally depending on an if condition), and what the output files are.

All input files in Sisyphus are read only, and all output files are write only.
It is sometimes necessary to create intermediate files when filtering or sorting.
Thus, the typical usage pattern is to have an "input" directory containing all
your current files for input, a "tmp" directory for all the temporary files,
and an "output" directory where all the final outputs will be created.

For programs that need to run daily, the "output" directory can replace this
run's "input" directory, and the "tmp" directory can be deleted. (The previous
"input" directory can be archived if needed.)

Example
-------

A file dvd.tsv.gz containing a list of DVDs, with the following schema:
- "title": the title of the movie
- "category": the category of the movie (ie: comedy, action, ...)
- "msrp": the original price of the DVD
- "price": my price for the DVD
- "num_rented": the number of times this DVD was rented.

If I want to update my file to change the price of all the comedies to the sale
price of msrp-10%, I will create a Sisyphus program with the following
actions (written in pseudo-code instead of the Java API of Sisyphus):

    for each row of file(dvd.tsv.gz):
        if category=="comedy":
            price = msrp * 0.9
        output row to file(dvd_new.tsv.gz)

If, in addition, I want to update the number of times each DVD was rented by
adding the data from this other file rented.txt, which contains only one column
with the list of titles, one for each rental, I will change my Sisyphus program
as follows:

    # first push: read rented.txt into a hashtable
    rented = new hashtable
    for each row of file(rented.txt):
        increment row(title) in rented

    # second push: do the same loop as before, with one more operation
    for each row of file(dvd.tsv.gz):
        if category=="comedy":
            price = msrp * 0.9
        if row(title) in rented:
            row(num_rented) += rented.get(title)
        output row to file(dvd_new.tsv.gz)

As you can see, the entire process only does one scan of each file. The memory
size of the hashtable is not a concern on modern hardware, because Sisyphus uses
very compact hashtables that doesn't store the keys and values as Objects,
and can hold 85M entries in per GB of memory.

I can further improve the program by adding actions that would still only
require a single scan. For instance:
- print statistics at the end: aggregate statistics about prices and/or num_rented
- delete obsolete DVDs: skip the row if the movie is in file deleted.txt
- insert new DVDs: process both files dvd.tsv.gz and new.txt

All these improvement will only have a marginal impact on performance because
I/O is the bottleneck, and each file is only scanned once still.

Licenses
--------

- Sisyphus is licensed under the terms of the GPL2, check the LICENSE file.

