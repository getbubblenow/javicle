# Complex Example
Here is a complex example using multiple assets and operations.

Note that comments, which are not usually legal in JSON, are allowed in
JVC files.

If you have other JSON-aware tools that need to read JVLC files, you may not
want to use this comment syntax. The `asset` and `operation` JSON objects also
support a `comment` field, which can be used as well.

```js
{
  "assets": [
    // file -- will be referenced directory
    {
      "comment": "first video, already local",
      "name": "vid1",
      "path": "/tmp/path/to/video1.mp4"
    },

    // URL -- will be downloaded to scratch directory and referenced from there
    {
      "comment": "second video, will be downloaded",
      "name": "vid2",
      "path": "https://archive.org/download/gov.archives.arc.1257628/gov.archives.arc.1257628_512kb.mp4"
    },

    // URL -- will be downloaded to `dest` directory and referenced from there
    {
      "comment": "third video, will be downloaded",
      "name": "vid3",
      "path": "https://archive.org/download/gov.archives.arc.49442/gov.archives.arc.49442_512kb.mp4",
      "dest": "src/test/resources/sources/"
    },

    // Image URL
    {
      "comment": "JPEG image, will be downloaded",
      "name": "img1",
      "path": "https://live.staticflickr.com/65535/48159911972_01efa0e5ea_b.jpg",
      "dest": "src/test/resources/sources/"
    },

    // Audio clip
    {
      "name": "bull-roar",
      "path": "http://soundbible.com/grab.php?id=2073&type=mp3",
      "dest": "src/test/resources/sources/"
    }
  ],
  "operations": [
    // scale examples
    {
      "comment": "scale using explicity height x width",
      "operation": "scale",            // name of the operation
      "creates": "vid2_scaled",        // asset it creates, can be referenced later
      "source": "vid2",                // source asset, from `assets` above
      "width": "1024",                 // width of scaled asset. if omitted and height is present, width will be proportional
      "height": "768"                  // height of scaled asset. if omitted and width is present, height will be proportional
    },
    {
      "comment": "scale proportionally by a scale factor",
      "operation": "scale",            // name of the operation
      "creates": "vid2_big",           // asset it creates
      "source": "vid2",                // source asset, from `assets` above
      "factor": "2.2"                  // scale factor. if factor is set, width and height are ignored.
    },

    // split example
    {
      "comment": "split one asset into many",
      "operation": "split",            // name of the operation
      "creates": "vid1_splits",        // assets it creates, 'vid1_splits' will be a list of assets
      "source": "vid1",                // split this asset
      "interval": "10"                 // split every ten seconds
    },

    // concat examples
    {
      "comment": "re-combine previously split assets back together",
      "operation": "concat",           // name of the operation
      "creates": "recombined_vid1",    // asset it creates
      "sources": ["vid1_split"]        // recombine all split assets
    },
    {
      "comment": "append vid2 to the end of vid1 and create a new asset",
      "operation": "concat",           // name of the operation
      "creates": "combined_vid2",      // asset it creates
      "sources": ["vid1", "vid2"]      // operation-specific: this says, concatenate these named assets
    },
    {
      "comment": "re-combine only some of the previously split assets",
      "operation": "concat",           // name of the operation
      "sources": ["vid1_splits[1..2]"],// concatentate these sources -- the 2nd and 3rd files only
      "creates": "combined_vid3"       // name of the output asset, will be written to scratch directory
    },

    // trim example
    {
      "comment": "trim all of the assets that were split above",
      "operation": "trim",             // name of the operation
      "creates": {                     // create multiple files, will be prefixed with `name`, store them in `dest`
        "name": "vid1_trims",
        "dest": "src/test/resources/outputs/trims/"
      },
      "source": "vid1_split",          // trim these source assets
      "start": "1",                    // cropped region starts here, default is zero
      "end": "6"                       // cropped region ends here, default is end of video
    },

    // overlay example
    {
      "comment": "overlay one video onto another",
      "operation": "overlay",          // name of the operation
      "creates": "overlay1",           // asset it creates
      "source": "combined_vid1",       // main video asset, `combined_vid1` was the output of an earlier operation
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

    // ken-burns example
    {
      "comment": "apply zoom-pan effect to image, creates video",
      "operation": "ken-burns",        // name of the operation
      "creates": "ken1",               // asset it creates
      "source": "img1",                // source image, `img1` is defined above in `assets`
      "zoom": "1.3",                   // zoom level, from 1 to 10
      "duration": "5.5",               // how long the resulting video will be
      "start": "0",                    // when to start zooming, default is 0
      "end": "duration",               // when to end zooming, default is duration
      "x": "source.width * 0.6",       // pan to this x-position, default is center (no horizontal pan)
      "y": "source.height * 0.4",      // pan to this y-position, default is center (no vertical pan)
      "upscale": "8",                  // upscale factor. upscaling the image results in a smoother pan, but a longer encode, default is 8
      "width": "1024",                 // width of output video, default is source width
      "height": "768"                  // height of output video, default is source height
    },

    // letterbox example
    {
      "comment": "increase video size proportionally and add letterboxes as needed",
      "operation": "letterbox",        // name of the operation
      "creates": "boxed1",             // asset it creates
      "source": "ken1",                // source asset
      "width": "source.width * 1.5",   // make it wider
      "height": "source.height * 0.9", // and shorter
      "color": "AliceBlue"             // default is black. can be a hex value (0xff0000 for red) or a color name from here: https://ffmpeg.org/ffmpeg-utils.html#color-syntax
    },

    // remove-track examples
    {
      "comment": "remove all audio tracks",
      "operation": "remove-track",     // name of the operation
      "creates": "vid2_video_only",    // name of the output asset
      "source": "vid2",                // main video asset
      "track": "audio"                 // remove all audio tracks
    },
    {
      "comment": "remove all video tracks",
      "operation": "remove-track",     // name of the operation
      "creates": "vid2_audio_only",    // name of the output asset
      "source": "vid2",                // main video asset
      "track": "video"                 // remove all video tracks
    },
    {
      "comment": "remove a specific audio track",
      "operation": "remove-track",     // name of the operation
      "creates": "vid2_video_only2",   // name of the output asset
      "source": "vid2",                // main video asset
      "track": {
        // only remove the first audio track
        "type": "audio",               // track type to remove
        "number": "0"                  // track number to remove
      }
    },

    // merge-audio example
    {
      "operation": "merge-audio",      // name of the operation
      "creates": "with_roar",          // output asset name
      "source": "vid2",                // main video asset
      "insert": "bull-roar",           // audio asset to insert
      "at": "5"                        // when (on the video timeline) to start playing the audio. default is 0 (beginning)
    },

    // add-silence example
    {
      "operation": "add-silence",      // name of the operation
      "creates": "v2_silent",          // output asset name
      "source": "v2",                  // main video asset
      "channelLayout": "stereo",       // optional channel layout, usually 'mono' or 'stereo'. Default is 'stereo'
      "samplingRate": 48000            // optional sampling rate, in Hz. default is 48000
    },

    // adjust-speed example
    {
        "operation": "adjust-speed",     // name of the operation
        "creates": "quickened",          // output asset name
        "source": "v2",                  // main video asset
        "factor": "4",                   // factor=1 is no change, factor>1 is faster, factor<1 is slower
        "audio": "silent"                // audio: silent (default), unchanged, match
                                         // if audio is match, then factor must be between 0.5 and 100
    }
  ]
}
```
