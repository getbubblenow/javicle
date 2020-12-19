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

Assets are referenced by their asset name.