# JVC JavaScript Expressions
JavaScript expressions are used in a couple of places in JVC.

  * Operation configuration
  * Operation validations

## Operation Configuration
When you're writing the JSON for an operation, many of the operation's
config parameters can be JS expressions that will be evaluated
when the operation is run.

For example, say you wanted to use `trim` to just chop a video in half.

The `start` and `end` parameters of the `trim` operation support JavaScript
expressions, so you could write:
```js
{
  "comment": "cut video at midpoint",
  "operation": "trim",
  "creates": "chopped"
  "source": "some_vid",
  "end": "source.duration / 2"
}
```
or run:
```shell script
jtrim some_vid.mp4 output_vid.mp4 0 "source.duration / 2"
```
In the above, `source` is referring to the source asset, `some_vid`.

When JVC loads an asset (like `some_vid`), or an operation creates a new asset,
the asset metadata is read using `mediainfo`. The metadata is stored in an object
that is then put into the JavaScript context using the variable name `source`.

## Operation Validations
Once an operation completes, the validations run to ensure that the output
assets pass whatever tests you want to run.

Each test is a JavaScript expressions, which is evaluated to a boolean value.
If the expression is true, the test passes. If false, the test fails and the
`comment` associated with the failing test is printed.

## JavaScript Context
What variables are available in the JS context? And what properties do they have?

### Assets
Assets can be defined in the JVC's `assets` array, or as the output of an
operation.

Assets are referenced by their asset name. Assets have some useful properties:

#### `duration`
Duration in seconds of the asset (audio or video).

#### `width`
Resolution width in pixes (video or image).

#### `height`
Resolution height in pixes (video or image).

#### `tracks`
An array of the tracks in a video. Only includes audio and video tracks.

#### `audioTracks`
An array of the audio tracks in a video.

#### `videoTracks`
An array of the video tracks in a video.

#### `assets`
If an asset is a list asset, this is an array of the sub-assets. Each sub-asset
has the same properties described above.

#### more properties
The list of asset properties supported today is fairly limited.
More properties will be exposed in the future.

### JS Functions
The JavaScript environment that JVC sets up includes some useful built-in
functions.

#### `is_within`
Check if a variable is "close" to another value by comparing against a delta.
```js
x = 10.2
is_within(x, 10, 1);     // true
is_within(x, 9.5, 0.5);  // false
is_within(x, 10.3, 0.1); // true
```

#### `is_within_pct`
Check if a variable is "close" to another value by percentage. The percentage
argument is an integer where 1 == 1% and 100 == 100%
```js
x = 10.2
is_within_pct(x, 10, 2);     // true
is_within_pct(x, 9, 10);     // false (9 + (10% of 9) only gets you to 9.9, x is 10.2) 
is_within_pct(x, 10.3, 10);  // true
```

