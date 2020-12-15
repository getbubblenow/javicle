# Javicle - a JSON Video Composition Language

JVCL (pronounced "Javicle") is a JSON DSL for audio/video transformations.

Under the hood, it's all shell commands: `ffmpeg`, `mediainfo`, `sox`, and so on.

JVCL provides higher-level semantics for working with these lower level tools.

# A Quick Example
Say you want to split a portion of a video into ten-second chunks. With ffmpeg
and bash, you might do something like this:
```shell script
    INCR=10
    for ((i=10;i<130;i=(i+INCR))); do
      ffmpeg -i /tmp/my/source.mp4 -ss ${i} -t $((i+INCR)) /tmp/my/slice_${i}_$((i+INCR)).mp4
    done
```
With JVCL, you'd write this spec file:
```json
{
  "assets": [ {"name": "src", "path": "/tmp/my/source.mp4"} ],
  "operations": [{
      "operation": "split",
      "creates": "src_split_files",
      "split": "src",
      "interval": "10s",
      "start": "10s",
      "end": "130s"
  }]
}
```
and then run it like this:
```shell script
jvcl my-spec.json
```
Yes, the JVCL is longer, but I think many would agree it is easier to read and maintain.

**As the number of input assets and operations grows, hand-crafted shell scripts with magical
ffmpeg incantations become ever more inscrutable.**

JVCL is designed for readability and maintainability. JVCL will continue to evolve towards greater
coverage of the full capabilities of ffmpeg. We also plan to introduce "function" concepts
to create reusable compound operations, further increasing reusability and lowering long-term
maintenance.

# Who is JVCL not for?
If you like GUIs, JVCL is probably not for you.

JVCL is not a replacement for Final Cut Pro or even iMovie.

# Who is JVCL for?
JVCL is for people who like CLIs and automation.

JVCL is for people whose your video composition needs are relatively simple (for now),
since the range of operations supported is limited.

# Concepts
In JVCL there are a few main concepts: spec files, assets and operations.

## JVCL Spec Files
A JVCL spec file is just a regular JSON file that happens to contain a single JSON object,
whose properties are `assets` and `operations`.

When you run `jvcl` on a spec file, JVCL will read the `assets`, then perform the `operations` in order.

## Assets
Assets are the inputs: generally image, audio and video files. Assets have a name and a path.

The path can be a file or a URL.

Input assets are defined using the `assets` array of a JVCL spec.

Operations produce one or more assets, as specified in the `creates` property of
an operation JSON object.

### Asset Properties
Assets expose properties that can be referenced in operations. The properties currently exposed are:

  * `duration`: duration of the audio/video in seconds (audio and video assets only)
  * `width`: width of the video in pixels (video and image assets only)
  * `height`: width of the video in pixels (video and image assets only)

## Operations
Operations are transformations to perform on the inputs.

An operation can produce one or more new assets, which can then be referenced in
later operations.

Most of the operation settings can be JavaScript expressions, for example:

    "startTime": "someAsset.duration - 10"

The above would set the `startTime` value to ten seconds before the end of `someAsset`.

### Supported Operations
Today, JVCL supports these operations:

### split
Split an audio/video asset into multiple assets

### concat
Concatenate audio/video assets together into one asset

### trim
Trim audio/video; crop a section of an asset, becomes a new asset

### overlay
Overlay one audio or video file onto another

### ken-burns
For transforming still images into video via a fade-pan (aka Ken Burns) effect

### letterbox
Transform a video in one size to another size using black letterboxes on the sides or top/bottom. Handy for embedding mobile videos into other screen formats

### split-silence
Split an audio file according to silence

# Complex Example
Here is a complex example using multiple assets and operations.

Note that in JVCL json files, comments are allowed:
 * A line comment starts with `//` and continue to the end of the line (`// comment`)
 * A multi-line block syntax starts with `/*` and ends with `*/` (`/* comment that may span multiple lines */`)

```json
{
  "assets": [
    // file -- will be referenced directory
    {
      "name": "vid1",
      "path": "/tmp/path/to/video1.mp4"
    },

    // URL -- will be downloaded to scratch directory and referenced from there
    {
      "name": "vid2",
      "path": "https://archive.org/download/gov.archives.arc.1257628/gov.archives.arc.1257628_512kb.mp4"
    },

    // URL -- will be downloaded to `dest` directory and referenced from there
    {
      "name": "vid3",
      "path": "https://archive.org/download/gov.archives.arc.49442/gov.archives.arc.49442_512kb.mp4",
      "dest": "src/test/resources/sources/"
    }
  ],
  "operations": [
    {
      "operation": "split",            // name of the operation,
      "creates": "vid1_split_%",       // assets it creates, the '%' will be replaced with a counter
      "split": "vid1",                 // split this source asset
      "interval": "10s"                // split every ten seconds
    },
    {
      "operation": "concat",           // name of the operation,
      "creates": "recombined_vid1",    // assets it creates, the '%' will be replaced with a counter
      "concat": ["vid1_split"]         // recombine all split assets
    },
    {
      "operation": "concat",           // name of the operation,
      "creates": "combined_vid",       // asset it creates, can be referenced later
      "concat": ["vid1", "vid2"]       // operation-specific: this says, concatenate these named assets
    },
    {
      "operation": "concat",           // name of the operation,
      "creates": "combined_vid",       // the asset it creates, can be referenced later
      "concat": ["vid1", "vid2"]       // operation-specific: this says, concatenate these named assets
    },
    {
      "operation": "overlay",          // name of the operation,
      "creates": {
        "name": "overlay1",            // asset it creates
        "width": "1920",               // output width in pixels. default is source width
        "height": "1024"               // output height in pixes. default is source height
      },
      "main": "combined_vid1",         // main video asset
      "startTime": "30",               // when (on the main video timeline) to begin showing the overlay. default is 0 (beginning)
      "endTime": "60",                 // when (on the main video timeline) to stop showing the overlay. default is to play the entire overlay
      "overlay": {
        "source": "vid2",              // overlay this video on the main video
        "startTime": "0",              // when (on the overlay video timeline) to begin playback on the overlay. default is 0 (beginning)
        "endTime": "overlay.duration", // when (on the overlay video timeline) to end playback on the overlay. default is to play the entire overlay
        "width": "overlay.width / 2",  // how wide the overlay will be, in pixels. default is the full overlay width, or maintain aspect ratio if height was set
        "height": "source.height",     // how tall the overlay will be, in pixels. default is the full overlay height, or maintain aspect ratio if width was set
        "x": "source.width / 2",       // horizontal overlay position on main video. default is 0
        "y": "source.height / 2"       // vertical overlay position on main video. default is 0
      }
    }
  ]
}
```

## What's up with the name?
I dunno, a cross between a javelin and an icicle? does that have any positive connotations? ok then...
