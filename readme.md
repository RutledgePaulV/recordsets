[![Build Status](https://travis-ci.com/rutledgepaulv/recordsets.svg?branch=master)](https://travis-ci.com/rutledgepaulv/recordsets)
[![codecov](https://codecov.io/gh/rutledgepaulv/recordsets/branch/master/graph/badge.svg)](https://codecov.io/gh/rutledgepaulv/recordsets)
 
### Installation

I wrote an installation script that builds from source and assumes you already have leiningen and a JDK on your path.

```shell script
git clone --depth=1 git@github.com:RutledgePaulV/recordsets.git recordsets && cd recordsets && ./install.sh && cd .. && rm -rf recordsets
```

This will place a shell script on your path and copy the compiled uberjars
to ~/.recordsets. The script then proxies commands through to either the 
cli or server jars.

To use the cli:

```shell script
recordsets -f file1.txt,file2.txt,file3.txt -s birthdate
```

To start the server:

```shell script
recordsets server
```

### CLI


Commentary:

I made the cli a separate compilation target from the server using a leiningen 
profile so that the cli doesn't include server code and have an inflated footprint 
(maintenance, security, and binary size). In this case it probably would have been
fine to compile them together, but typically I would distribute them separately.

Assumptions:
* It's acceptable to mix and match delimiters within a row and include leading / trailing whitespace.
* Instructions were literal / intended Java's interpretation of M/D/YYYY as in MONTH/DAY_OF_YEAR/YEAR.
* It's okay to use a standard input format for dates like ISO-8601 even though it differs from the output format.
* Input files will be small enough for the entire data set to fit into the default heap.

Potential improvements:

* Compile to a native binary using GraalVM to improve cli responsiveness / remove jvm dependency.
* Sort safely sized chunks into temp files and perform a streaming merge 
  sort reading from those to display the final results. This would remove
  any significant limits imposed by heap size and instead allow for 
  processing any file assuming you have enough free disk space.
* If this were something real I'd want to accept input on stdin too since 
  then it can be more easily composed with other cli tools.
* Support structured data output formats (edn, json, csv, etc).


### SERVER

Commentary:

I didn't use a routing library or add typical sets of middleware that enforce security headers, etc.
While I would likely do these things for a production application I was keeping things barebones.
I maintain data in-memory with sorted sets so that reads are very fast and don't do any redundant sorting 
work on each request.

Assumptions:
* Desired date display representation applies to the json output as well.
* The amount and size of posted records will be small enough for the entire data set to fit into the default heap.
* It should be possible to create multiple records with the exact same data.

Potential improvements:
* Enforce a limit on max size of posted record.


