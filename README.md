# Javicle - a JSON Video Composition Language

Javicle is a JSON DSL for audio/video transformations.

Under the hood, it's all shell commands: ffmpeg, mediainfo, sox, and so on.
JVCL provides higher-level semantics for working with these lower level tools.

# A Quick Example
Say you want to split a portion of a video into ten-second chunks. With ffmpeg
and bash, you might do something like this:
```shell script
    START=10
    END=130
    INCR=10
    for ((i=START;i<END;i=(i+INCR))); do
      ffmpeg -i $i /tmp/my/source.mp4 -ss $((i)) -t $((i+INCR)) /tmp/my/slice_$((i))_$((i+INCR)).mp4
    done
```
With JVCL, you would use write this spec:
```json
{
  "assets": [ {"name": "src", "path": "/tmp/my/source.mp4"} ],
  "operations": [{
    "operation": "split",
    "creates": "src_splits",
    "perform": {
      "split": "src",
      "interval": "10s",
      "start": "10s",
      "end": "130s"
    }
  }]
}
```
And then run `jvcl spec-file.json` on your spec, and it would run essentially the same ffmepg commands.

# Who is this not for?
If you like GUIs, Javicle is probably not for you.

Javicle is not a replacement for Final Cut Pro or even iMovie.

# Who is this for?
If you like CLIs, Javicle might be for you.

You might enjoy Javicle if your video composition needs are relatively simple and/or
you enjoy capturing repeatable processes in source control.

# Concepts
In JVCL there are two main concepts: assets and operations.

## Assets
Assets are the inputs - generally image, audio and video files. Assets have a name and a path.
The path can be a file or a URL.

## Operations
Operations are transformations to perform on the inputs.
An operation can produce a new intermediate asset.
Intermediate assets have names, and special paths that indicate how to reconstruct them from their assets, such that if you have the path of an intermediate asset, you can recreate its content, assuming you supply the same input assets.

The operations that JVCL either supports or intends to support are:

### split
Split an audio/video asset into multiple assets

### concat
Concatenate audio/video assets together into one asset

### trim
Trim audio/video - crop from beginning, end, or both

### overlay
Overlay one video file onto another

### ken-burns
For transforming still images into video via a fade-pan (aka Ken Burns) effect

### letterbox
Transform a video in one size to another size using black letterboxes on the sides or top/bottom. Handy for embedding mobile videos into other screen formats

### split-silence
Split an audio file according to silence

# Complex Example
Here is a complex example using multiple assets and operations:

```json
{
  "assets": [
    {"name": "vid1", "path": "/tmp/path/to/video1.mp4"},
    {"name": "vid2", "path": "/tmp/path/to/video2.mp4"}
  ],
  "operations": [
    {
      "operation": "split",            // name of the operation
      "creates": "vid1_split_%",       // assets it creates, the '%' will be replaced with a counter
      "perform": {
        "split": "vid1",               // split this source asset
        "interval": "10s"              // split every ten seconds
      }
    },
    {
      "operation": "concat",           // name of the operation
      "creates": "recombined_vid1",    // assets it creates, the '%' will be replaced with a counter
      "perform": {
        "concat": ["vid1_split"]       // recombine all split assets
      }
    },
    {
      "operation": "concat",           // name of the operation
      "creates": "combined_vid",       // asset it creates, can be referenced later
      "perform": {
        "concat": ["vid1", "vid2"]     // operation-specific: this says, concatenate these named assets
      }
    },
    {
      "operation": "concat",           // name of the operation
      "creates": "combined_vid",       // the asset it creates, can be referenced later
      "perform": {
        "concat": ["vid1", "vid2"]     // operation-specific: this says, concatenate these named assets
      }
    },
    {
      "operation": "overlay",          // name of the operation
      "creates": "overlay1",           // asset it creates
      "perform": {
        "source": "combined_vid1",     // main video asset
        "overlay": "vid1",             // overlay this video on the main video

        "start": "vid1.end_ts",        // when (on the main video timeline) to start the overlay. default is 0 (beginning)
        "duration": "vid1.duration",   // how long to play the overlay. default is to play the entire overlay asset

        "width": 400,                  // how wide the overlay will be, in pixels. default is "overlay.width"
        "height": 300,                 // how tall the overlay will be, in pixels. default is "overlay.height"

        "x": "source.width / 2",       // horizontal overlay position. default is 0
        "y": "source.height / 2",      // vertical overlay position. default is 0
 
        "out": "1080p",                // this is a shortcut to the two lines below, and is the preferred way of specifying the output resolution
        "out_width": 1920,             // output width in pixels. default is source width
        "out_height": 1024            // output height in pixes. default is source height
      }
    }
  ]
}
```
