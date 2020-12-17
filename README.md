# Javicle - a JSON Video Composition Language
Javicle (JVC for short) is a JSON DSL for audio/video transformations.

Under the hood, it's all shell commands: `ffmpeg`, `mediainfo` and so on.

JVC provides higher-level semantics for working with these lower level tools.

# Motivation
I infrequently find myself doing video editing, so I've never bothered to
learn iMovie or any graphical video editor.

My editing needs are usually pretty simple, so I bust out ffmpeg and get the
job done.

But it seems like every time, there is at least one wrinkle in the requirements
that requires some deep research into
[ffmpeg filter arcana](https://ffmpeg.org/ffmpeg-filters.html).

Hours later, before I know it, the day has gone by.

I created JVC to make it really easy to do the most common things people
usually do to videos: splitting, concatenating, letterboxing, overlaying
one video onto another, and so on.

# A Quick Example
Suppose you have one video that is five minutes long,
and you want to split it into five videos, each one minute long.

With ffmpeg and bash, you might do something like this:
```shell script
    INCR=60
    for ((i=0;i<300;i=(i+INCR))); do
      ffmpeg -i /tmp/my/source.mp4 -ss ${i} -t $((i+INCR)) /tmp/my/slice_${i}_$((i+INCR)).mp4
    done
```
With JVC, you'd write this spec file and save it to a file
(for example `my-spec.jvc`):
```json
{
  "assets": [ {"name": "src", "path": "/tmp/my/source.mp4"} ],
  "operations": [{
      "operation": "split",
      "creates": "src_split_files",
      "source": "src",
      "interval": "60"
  }]
}
```
and then run it like this:
```shell script
jvc my-spec.jvc
```
Yes, the JVC is longer, but I think many would agree it is easier to read
and maintain.

**As the number of media assets and operations grows, hand-crafted shell
scripts with magical ffmpeg incantations become ever more inscrutable.**

JVC is designed for readability and maintainability. JVC will continue to
evolve towards greater coverage of the full capabilities of ffmpeg.

# Who is JVC not for?
If you like GUIs, JVC is probably not for you.

JVC is not a replacement for Final Cut Pro or even iMovie.

# Who is JVC for?
JVC is for people who like CLIs and automation.

JVC is for people with relatively simple video composition needs (for now),
since the range of operations supported is limited.

JVC is for people who have used ffmpeg filters before and had flashbacks
of editing Sendmail configs and debugging PostScript.

##### Caveat Emptor
Obligatory Disclaimer: JVC is still relatively new software and lots of stuff might break.
It should never overwrite your source files, all output goes to new files.

# Requirements
 * Java 11
 * Maven 3

The first time you run `jvc`, it will automatically build the JVC jar file
from sources, using maven and javac. This takes a little time but only needs
to be done once.

# Running JVC
Learn more about **[running `jvc`](docs/running.md)** and other useful tools.

# JVC Concepts
Learn about **[Assets and Operations](docs/concepts.md)**, the core concepts
of JVC.

# Supported Operations
Today, JVC supports several basic operations.

For each operation listed below, the header links to an example from the JVC
test suite.

### [add-silence](src/test/resources/tests/test_add_silence.jvc)
Add a silent audio track to a video asset.

### [concat](src/test/resources/tests/test_concat.jvc)
Concatenate audio/video assets together into one asset.

### [ken-burns](src/test/resources/tests/test_ken_burns.jvc)
Transform a still image into video via a fade-pan (aka Ken Burns) effect.

### [letterbox](src/test/resources/tests/test_letterbox.jvc)
Transform a video from one size to another size, maintaining the aspect ratio
of the video and adding letterboxes on the sides or top/bottom.
Handy for embedding mobile videos into other screen formats.

### [merge-audio](src/test/resources/tests/test_merge_audio.jvc)
Merge an audio asset into the audio track of a video asset.

### [overlay](src/test/resources/tests/test_overlay.jvc)
Overlay one asset onto another.

### [remove-track](src/test/resources/tests/test_remove_track.jvc)
Remove a track from a video asset.

### [scale](src/test/resources/tests/test_scale.jvc)
Scale a video asset from one size to another. Scaling can be proportional
or anamorphic.

### [split](src/test/resources/tests/test_split.jvc)
Split an audio/video asset into multiple assets of equal time lengths.

### [trim](src/test/resources/tests/test_trim.jvc)
Trim audio/video; crop a section of an asset, becomes a new asset.

## Complex Example
Here is a [long, complex example](docs/complex_example.md) that uses
every operation.

## What's with the name?
A cross between a javelin and an icicle? JSON and a miracle?
Something with positive connotations?
I really don't like naming things.
