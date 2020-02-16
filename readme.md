[![Build Status](https://travis-ci.com/rutledgepaulv/recordsets.svg?branch=master)](https://travis-ci.com/rutledgepaulv/recordsets)
[![codecov](https://codecov.io/gh/rutledgepaulv/recordsets/branch/master/graph/badge.svg)](https://codecov.io/gh/rutledgepaulv/recordsets)
 
### Installation

I wrote an installation script that assumes you already have leiningen and a JDK on your path.

```shell script
git clone --depth=1 git@github.com:RutledgePaulV/recordsets.git recordsets \ 
  && ./recordsets/install.sh \
  && rm -rf recordsets
```

This will place a shell script on your path and copy the compiled uberjars
to ~/.recordsets. The script then proxies commands through to either the 
cli or server jars.

To use the cli:

```shell script
recordsets -f file1.txt,file2.txt,file3.txt
```

To start the server:

```shell script
recordsets server
```

### CLI


Commentary:

I made the cli a separate compilation target from the server using leiningen 
profiles so that the cli doesn't include server code and have an inflated 
footprint (maintenance, security, and binary size).

Assumptions:
* Instructions really did mean want Java's interpretation of M/D/YYYY as in MONTH/DAY_OF_YEAR/YEAR.
* It's okay to use a canonical input format for dates like ISO-8601 even though it differs from the output format.
* Input files will be small enough for the entire data set to fit into the allocated heap.

Potential improvements:

* Target GraalVM to decrease time-to-interaction.
* Sort safely sized chunks into temp files and perform a streaming merge 
  sort reading from those to display the final results. This would remove
  any significant limits imposed by heap size and instead allow for 
  processing any file assuming you have enough free disk space.
* If this were something real I'd probably accept input on stdin too since 
  then it can be more easily composed with other cli tools.


### SERVER

Commentary:

I didn't use a routing library or add typical sets of middleware that enforce security headers, etc.
While I would probably do these things for a production application I tried to keep things simple here.
I maintain in-memory data with sorted sets so that reads are fast and don't do any redundant sorting 
work on each request.

Assumptions:
* Desired date display representation applies to the server as well.
* The amount and size of posted records will be small enough for the entire data set to fit into the allocated heap.



