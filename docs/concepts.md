# Concepts
In JVCL the main concepts are assets and operations.

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

