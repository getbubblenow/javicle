package jvc.model.operation;

import jvc.model.JAsset;
import jvc.model.JFileExtension;
import jvc.model.JFormat;
import jvc.model.info.JTrackType;
import jvc.service.AssetManager;
import lombok.Getter;
import lombok.Setter;

import static jvc.model.JAsset.json2asset;
import static jvc.model.info.JTrackType.video;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;

public class JSingleSourceOperation extends JOperation {

    @Getter @Setter private String source;

    protected JTrackType outputMediaType() { return video; }

    public JSingleOperationContext getSingleInputContext(AssetManager assetManager) {
        final JAsset source = assetManager.resolve(getSource());
        final JAsset output = json2asset(getCreates());
        output.mergeFormat(source.getFormat());

        // ensure output is in the correct format
        final JFormat format = output.getFormat();
        final JTrackType type = outputMediaType();
        if (!format.hasFileExtension() || format.getFileExtension().mediaType() != type) {
            final JFileExtension ext = type.ext();
            if (ext == null) {
                return die("getSingleInputContext: no file extension found for output media type: " + type);
            }
            format.setFileExtension(ext);
        }
        final JFileExtension formatType = getFileExtension(source, output);

        return new JSingleOperationContext(source, output, formatType);
    }

    protected JFileExtension getFileExtension(JAsset source, JAsset output) {
        return output.getFormat().getFileExtension();
    }

}
