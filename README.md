# Javicle - a JSON Video Composition Language

JVCL (pronounced "Javicle") is a JSON DSL for audio/video transformations.

Under the hood, it's all shell commands: `ffmpeg`, `mediainfo` and so on.

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
With JVCL, you'd write this spec file and save it to a file (for example `my-spec.jvcl`):
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
jvcl my-spec.jvcl
```
Yes, the JVCL is longer, but I think many would agree it is easier to read and maintain.

**As the number of media assets and operations grows, hand-crafted shell scripts with magical
ffmpeg incantations become ever more inscrutable.**

JVCL is designed for readability and maintainability. JVCL will continue to evolve towards greater
coverage of the full capabilities of ffmpeg.

# Who is JVCL not for?
If you like GUIs, JVCL is probably not for you.

JVCL is not a replacement for Final Cut Pro or even iMovie.

# Who is JVCL for?
JVCL is for people who like CLIs and automation.

JVCL is for people with relatively simple video composition needs (for now),
since the range of operations supported is limited.

# Concepts
In JVCL there are a few main concepts: spec files, assets and operations.

## JVCL Spec Files
A JVCL spec file is just a regular JSON file that happens to contain a single JSON object,
whose properties are `assets` and `operations`.

When you run `jvcl` on a spec file, it will load the `assets`, then perform the `operations` in order.

Unlike most JSON, comments *are* allowed in JVCL spec files:
* A line comment starts with `//` and continue to the end of the line
* A multi-line block syntax starts with `/*` and ends with `*/`

### Executing a JVCL Spec
To execute a spec stored in the file `my-spec.json`, you would run:
```shell script
jvcl my-spec.jvcl
```
or to supply a spec using stdin and pipeline:
```shell script
cat my-spec.jvcl | jvcl
```

#### Scratch Directory
Output assets will be placed in the scratch directory, unless otherwise specified
in the spec file. By default, JVCL will create a new temporary directory to use as the scratch
directory. You can set the scratch directory explicitly using the `-t` or `--temp-dir` option:

```shell script
jvcl -t /some/tempdir my-spec.json
```

#### Command Help
To view a list of all `jvcl` command-line options, run `jvcl -h` or `jvcl --help`

## Assets
Assets are your media files: generally image, audio and video files.

All assets have a name and a path.

Input assets are defined using the `assets` array of a JVCL spec.

For input assets, the path can be a file or a URL. URL-based assets will be downloaded
to the scratch directory. This can be overridden using the `dest` property on the asset.

Operations produce one or more output assets, as specified in the `creates` property of
an operation JSON object.

For output assets, the path will be within the scratch directory.
You can override this using the `dest` property on the `creates` object associated with the operation.

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

    "start": "someAsset.duration - 10"

The above would set the `start` value to ten seconds before the end of `someAsset`.

### Supported Operations
Today, JVCL supports these operations:

### [scale](src/test/resources/tests/test_scale.jvcl)
Scale a video asset from one size to another. Scaling can be proportional or anamorphic

### [split](src/test/resources/tests/test_split.jvcl)
Split an audio/video asset into multiple assets of equal time lengths

### [concat](src/test/resources/tests/test_concat.jvcl)
Concatenate audio/video assets together into one asset

### [trim](src/test/resources/tests/test_trim.jvcl)
Trim audio/video; crop a section of an asset, becomes a new asset

### [overlay](src/test/resources/tests/test_overlay.jvcl)
Overlay one asset onto another

### [ken-burns](src/test/resources/tests/test_ken_burns.jvcl)
For transforming still images into video via a fade-pan (aka Ken Burns) effect

### [letterbox](src/test/resources/tests/test_letterbox.jvcl)
Transform a video in one size to another size using black letterboxes on the sides or top/bottom. Handy for embedding mobile videos into other screen formats

# Complex Example
Here is a complex example using multiple assets and operations.

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
    },

    // Image URL
    {
      "name": "img1",
      "path": "https://live.staticflickr.com/65535/48159911972_01efa0e5ea_b.jpg",
      "dest": "src/test/resources/sources/"
    }
  ],
  "operations": [
    {
      "operation": "scale",            // name of the operation
      "creates": "vid2_scaled",        // asset it creates
      "source": "vid2",                // source asset
      "width": "1024",                 // width of scaled asset. if omitted and height is present, width will be proportional
      "height": "768"                  // height of scaled asset. if omitted and width is present, height will be proportional
    },
    {
      "operation": "scale",            // name of the operation
      "creates": "vid2_big",           // asset it creates
      "source": "vid2",                // source asset
      "factor": "2.2"                 // scale factor. if factor is set, width and height are ignored.
    },
    {
      "operation": "split",            // name of the operation
      "creates": "vid1_split_%",       // assets it creates, the '%' will be replaced with a counter
      "source": "vid1",                // split this source asset
      "interval": "10"                 // split every ten seconds
    },
    {
      "operation": "concat",           // name of the operation
      "creates": "recombined_vid1",    // assets it creates, the '%' will be replaced with a counter
      "source": ["vid1_split"]         // recombine all split assets
    },
    {
      "operation": "concat",           // name of the operation
      "creates": "combined_vid",       // asset it creates, can be referenced later
      "source": ["vid1", "vid2"]       // operation-specific: this says, concatenate these named assets
    },
    {
      "operation": "concat",           // name of the operation
      "creates": "combined_vid",       // the asset it creates, can be referenced later
      "source": ["vid1", "vid2"]       // operation-specific: this says, concatenate these named assets
    },
    {
      "operation": "trim",          // name of the operation
      "creates": {                  // create multiple files, will be prefixed with `name`, store them in `dest`
        "name": "vid1_trims",
        "dest": "src/test/resources/outputs/trims/"
      },
      "source": "vid1_split",        // trim these source assets
      "start": "1",                  // cropped region starts here, default is zero
      "end": "6"                     // cropped region ends here, default is end of video
    },
    {
      "operation": "overlay",          // name of the operation
      "creates": "overlay1",           // asset it creates
      "source": "combined_vid1",       // main video asset
      "start": "30",                   // when (on the main video timeline) to begin showing the overlay. default is 0 (beginning)
      "end": "60",                     // when (on the main video timeline) to stop showing the overlay. default is to play the entire overlay
      "overlay": {
        "source": "vid2",              // overlay this video on the main video
        "start": "0",                  // when (on the overlay video timeline) to begin playback on the overlay. default is 0 (beginning)
        "end": "overlay.duration",     // when (on the overlay video timeline) to end playback on the overlay. default is to play the entire overlay
        "width": "overlay.width / 2",  // how wide the overlay will be, in pixels. default is the full overlay width, or maintain aspect ratio if height was set
        "height": "source.height",     // how tall the overlay will be, in pixels. default is the full overlay height, or maintain aspect ratio if width was set
        "x": "source.width / 2",       // horizontal overlay position on main video. default is 0
        "y": "source.height / 2"       // vertical overlay position on main video. default is 0
      }
    },
    {
      "operation": "ken-burns",        // name of the operation
      "creates": "ken1",               // asset it creates
      "source": "img1",                // source image
      "zoom": "1.3",                   // zoom level, from 1 to 10
      "duration": "5",                 // how long the resulting video will be
      "start": "0",                    // when to start zooming, default is 0
      "end": "duration",               // when to end zooming, default is duration
      "x": "source.width * 0.6",       // pan to this x-position
      "y": "source.height * 0.4",      // pan to this y-position
      "upscale": "8",                  // upscale factor. upscaling the image results in a smoother pan, but a longer encode, default is 8
      "width": "1024",                 // width of output video
      "height": "768"                  // height of output video
    },
    {
      "operation": "letterbox",        // name of the operation
      "creates": "boxed1",             // asset it creates
      "source": "ken1",                // source asset
      "width": "source.width * 1.5",   // make it wider
      "height": "source.height * 0.9", // and shorter
      "color": "AliceBlue"             // default is black. can be a hex value (0xff0000 for red) or a color name from here: https://ffmpeg.org/ffmpeg-utils.html#color-syntax
    }
  ]
}
```

## What's up with the name?
I dunno, a cross between a javelin and an icicle? does that have any positive connotations? ok then...
