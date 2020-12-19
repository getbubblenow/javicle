package jvc.model.operation;

import jvc.model.JAsset;
import jvc.model.JFileExtension;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static jvc.model.JAsset.flattenAssetList;
import static jvc.model.JAsset.json2asset;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public abstract class JMultiSourceOperation extends JOperation {

    @Getter @Setter private String[] sources;

    public JMultiOperationContext getMultiInputContext(AssetManager assetManager, Toolbox toolbox) {
        // validate sources
        final List<JAsset> sources = flattenAssetList(assetManager.resolve(getSources()));
        if (empty(sources)) die("operate: no sources");

        // create output object
        final JAsset output = json2asset(getCreates());

        // if any format settings are missing, use settings from first source
        output.mergeFormat(sources.get(0).getFormat());

        // set the path, check if output asset already exists
        final JFileExtension formatType = output.getFormat().getFileExtension();

        return new JMultiOperationContext(sources, output, formatType, assetManager, toolbox);
    }
}
