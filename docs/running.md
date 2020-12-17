# Running JVC

## The `jvc` Script
The `jvc` script (in the `bin` directory) is the primary way to transform video
assets with JVC.

`jvc` is a swiss army knife that can read a spec file and perform any number
of operations on any number of assets.

## Operation-specific scripts
There are other scripts in [`bin`](../bin) that perform a single operation using
command-line arguments, without requiring you to write a spec file.

If you need to do a quick operation and don't feel like writing a JSON
spec file, use one of [these tools](../bin).

For example, to extract the first 5 seconds of a video:
```shell script
jtrim /tmp/input-file.mp4 /tmp/output-5s.mp4 0 5
```

There is a command-line tool for every operation except for `overlay` and
`ken-burns`, which are more complex. Pull requests welcome.

## Help
All commands accept a `-h` / `--help` option, this will print information about
how to use the command, and what arguments it takes.

To view a help for a command, just run the command with `-h` or `--help` as the
first option:
```shell script
jvc -h
jscale -h
jtrim -h
...
```

## JVC Spec Files
A JVC spec is just a regular JSON file that happens to contain a single JSON object,
whose properties are `assets` and `operations`.

When you run `jvc` on a spec file, it will load the `assets`, then perform the `operations` in order.

Learn more about [Assets and Operations](concepts.md)

Unlike most JSON, comments *are* allowed in JVC spec files:
* A line comment starts with `//` and continue to the end of the line
* A multi-line block starts with `/*` and ends with `*/`

## Writing a JVC Spec
The easiest way to write a spec is to copy one of the
[test specs](../src/test/resources/tests) and edit it.

There examples for every JVC operation.

## Executing a JVC Spec
To execute a spec stored in the file `my-spec.json`, you would run:
```shell script
jvc my-spec.jvc
```
or use stdin:
```shell script
cat my-spec.jvc | jvc
```

## Command Line Options

#### Scratch Directory
Output assets will be placed in the scratch directory, unless otherwise specified
in the spec file. By default, JVC will create a new temporary directory to use as the scratch
directory. You can set the scratch directory explicitly using the `-t` or `--temp-dir` option:
```shell script
jvc -t /some/tempdir my-spec.json
```

When using the other tools in `bin`, you can set the scratch directory via the
`JVC_SCRATCH_DIR` environment variable. If the `JVC_SCRATCH_DIR` references a
directory that does not exist, it will be created.

#### Dry Run
Use the `-n` or `--no-exec` option to print out the commands that would have been run,
but do not actually run anything.
```shell script
jvc -n my-spec.json         # will not run any ffmpeg commands
```
Note that this breaks JVC operations that require information from any assets created by
previous operations: since the command did not actually run, the intermediate asset was
never created.

When using the other tools in `bin`, you can set the "no exec" flag via the
`JVC_NO_EXEC` environment variable. If the `JVC_NO_EXEC` is non-empty, then
the script will pass the `-n` flag when it calls `jvc` and commands will
be printed out instead of actually being run.

#### Help
To view a list of all `jvc` command-line options, run `jvc -h` or `jvc --help`

# What's Next?
Learn about [Assets and Operations](concepts.md), the core concepts of JVC.
