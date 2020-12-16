package jvcl.model.operation;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
import jvcl.service.AssetManager;
import lombok.Getter;
import lombok.Setter;

import static jvcl.model.JAsset.json2asset;

public class JSingleSourceOperation extends JOperation {

    @Getter @Setter private String source;

    public JSingleOperationContext getSingleInputContext(AssetManager assetManager) {
        final JAsset source = assetManager.resolve(getSource());
        final JAsset output = json2asset(getCreates());
        output.mergeFormat(source.getFormat());
        final JFileExtension formatType = output.getFormat().getFileExtension();

        return new JSingleOperationContext(source, output, formatType);
    }

}
