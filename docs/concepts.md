# Concepts
In JVC the main concepts are assets and operations.

The [complex example](complex_example.md)
illustrates just about every way to represent input assets,
output assets, operation configs.

If you find yourself getting confused with some of what follows,
maybe browse through that [example](complex_example.md) first.

## Assets
Assets are your media files: generally image, audio and video files.

All assets have a name and a path.

Input assets are defined using the `assets` array of a JVC spec.

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
* `aspectRatio`: ratio of width / height (video and image assets only)
* `samplingRate`: sampling rate in Hz (audio assets only)
* `tracks`: array of tracks. only includes audio and video tracks (video assets only)
* `audioTracks`: array of audio tracks (video assets only)
* `videoTracks`: array of video tracks (video assets only)
* `assets`: for a list asset, these are the nested sub-assets

## Operations
Operations represent transformations to perform on the inputs, and validations
to ensure correctness.

### Operation Assets
An operation requires one or more input assets. These assets are referenced
using the `source` (or for `concat`, `sources`) parameter.

An operation can produce one or more new assets, which can then be referenced in
later operations.

Learn more about [Asset References](asset_refs.md).

### Operation Configuration
Many of the operation settings can be JavaScript expressions, for example:

    "start": "someAsset.duration - 10"

The above would set the `start` value to ten seconds before the end of `someAsset`.

Learn more about [JavaScript expressions in JVC](jvc_js.md).

### Operation Validation
An operation may define validations to perform by defining a `validation` array.

Each object in the array has properties `comment` (to describe the test) and
`test` which is a JavaScript expression. If it evaluates to `true` then the
test passes. If `false`, the test fails and the `comment` is printed.

Learn more about [JavaScript expressions in JVC](jvc_js.md).
