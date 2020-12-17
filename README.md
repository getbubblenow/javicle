# Javicle - a JSON Video Composition Language
JVCL (pronounced "Javicle") is a JSON DSL for audio/video transformations.

Under the hood, it's all shell commands: `ffmpeg`, `mediainfo` and so on.

JVCL provides higher-level semantics for working with these lower level tools.

# Motivation
I don't do much video editing, so I've never bothered to learn iMovie or any
graphical video editor. My editing needs are usually pretty simple, so I bust
out ffmpeg and get it done.

But it seems like every time, there is at least one wrinkle in my requirements
that requires some deep research into
[ffmpeg filter arcana](https://ffmpeg.org/ffmpeg-filters.html)
and before I know it, the day is done.

I created JVCL to make it really easy to do the most common things people
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
With JVCL, you'd write this spec file and save it to a file
(for example `my-spec.jvcl`):
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
jvcl my-spec.jvcl
```
Yes, the JVCL is longer, but I think many would agree it is easier to read
and maintain.

**As the number of media assets and operations grows, hand-crafted shell
scripts with magical ffmpeg incantations become ever more inscrutable.**

JVCL is designed for readability and maintainability. JVCL will continue to
evolve towards greater coverage of the full capabilities of ffmpeg.

# Who is JVCL not for?
If you like GUIs, JVCL is probably not for you.

JVCL is not a replacement for Final Cut Pro or even iMovie.

# Who is JVCL for?
JVCL is for people who like CLIs and automation.

JVCL is for people with relatively simple video composition needs (for now),
since the range of operations supported is limited.

# Requirements
 * Java 11
 * Maven 3

The first time you run `jvcl`, it will automatically build the JVCL jar file
from sources, using maven and javac. This takes a little time but only needs
to be done once.

# Running JVCL
Learn more about [running `jvcl`](docs/running.md) and other useful tools.

# JVCL Concepts
Learn about [Assets and Operations](docs/concepts.md), the core concepts
of JVCL.

# Supported Operations
Today, JVCL supports several basic operations.

For each operation listed below, the header links to an example from the JVCL
test suite.

### [add-silence](src/test/resources/tests/test_add_silence.jvcl)
Add a silent audio track to a video asset.

### [concat](src/test/resources/tests/test_concat.jvcl)
Concatenate audio/video assets together into one asset.

### [ken-burns](src/test/resources/tests/test_ken_burns.jvcl)
Transform still images into video via a fade-pan (aka Ken Burns) effect.

### [letterbox](src/test/resources/tests/test_letterbox.jvcl)
Transform a video from one size to another size, maintaining the aspect ratio
of the video and adding letterboxes on the sides or top/bottom.
Handy for embedding mobile videos into other screen formats.

### [merge-audio](src/test/resources/tests/test_merge_audio.jvcl)
Merge an audio asset into the audio track of a video asset.

### [overlay](src/test/resources/tests/test_overlay.jvcl)
Overlay one asset onto another.

### [remove-track](src/test/resources/tests/test_remove_track.jvcl)
Remove a track from a video asset.

### [scale](src/test/resources/tests/test_scale.jvcl)
Scale a video asset from one size to another. Scaling can be proportional
or anamorphic.

### [split](src/test/resources/tests/test_split.jvcl)
Split an audio/video asset into multiple assets of equal time lengths.

### [trim](src/test/resources/tests/test_trim.jvcl)
Trim audio/video; crop a section of an asset, becomes a new asset.

## Complex Example
Here is a [long, complex example](docs/complex_example.md) that uses
every operation.

## What's with the name?
A cross between a javelin and an icicle?
Does that have any positive connotations?
I really don't like naming things.
