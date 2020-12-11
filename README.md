# Javicle - a JSON Video Composition Language

Javicle is not a replacement for Final Cut Pro or even iMovie.

Javicle might be right for you if your video composition and manipulation needs are relatively simple
and you enjoy doing things with the command line and some JSON config instead of a GUI.

This also give you the ability to track more of your workflow in source control - if you commit all
the original assets and the .jvcl files that describe how to create the output assets, you don't need
to save/archive the output assets anywhere.

Note, for those who want truly 100% re-creatable builds, you would also need to record the versions
of the various tools used (ffmpeg, etc) and reuse those same versions when recreating a build. This is
generally overkill though, since the options we use on the different tools have been stable for a while
and I see a low likelihood of a significant change in behavior, or a bug being introduced.

In JVCL there are two main concepts: assets and operations.

Assets are the inputs - generally image, audio and video files. Assets have a name and a path.
The path can be a file or a URL.

Operations are transformations to perform on the inputs.
An operation can produce a new intermediate asset.
Intermediate assets have names, and special paths that indicate how to reconstruct them from their assets, such that if you have the path of an intermediate asset, you can recreate its content, assuming you supply the same input assets.

## Operations

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

## Example
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