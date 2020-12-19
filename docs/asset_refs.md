# Asset References
Most operations have a `source` parameter that indicates what asset is
considered the primary input to that operation.

The value of `source` is the name of an asset, either defined in `assets`
or as the output of a previous operation (named in the `creates` block).

## List Assets
The `split` operation creates multiple assets under the same name, this
is called a "list asset", in contrast to a "singular asset" that only
refers to one file.

If you want to reference a specific file within a list asset, use square
bracket notation:
```shell script
some_list_asset[0]     # first sub-asset
some_list_asset[-1]    # last sub-asset
some_list_asset[1..3]  # 2nd, 3rd and 4th assets
some_list_asset[..3]   # 1st, 2nd, 3rd and 4th assets
some_list_asset[3..]   # 4th asset and everything after
```

## Concat Operation `sources`
The `concat` operation does not take a `source` param (which would name
one asset), it takes a `sources` array of asset names.

The named assets can be either singular assets or list assets.

`concat` flattens the list of assets and concatenates them all together.
