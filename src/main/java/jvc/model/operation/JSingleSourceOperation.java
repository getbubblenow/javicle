package jvc.model.operation;

import jvc.model.JAsset;
import jvc.model.JStreamType;
import jvc.model.JFormat;
import jvc.model.info.JTrackType;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.Getter;
import lombok.Setter;

import static jvc.model.JAsset.json2asset;
import static jvc.model.info.JTrackType.video;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;

public class JSingleSourceOperation extends JOperation {

    @Getter @Setter private String source;

    protected JTrackType outputMediaType() { return video; }

    public JSingleOperationContext getSingleInputContext(AssetManager assetManager, Toolbox toolbox) {
        final JAsset source = assetManager.resolve(getSource());
        final JAsset output = json2asset(getCreates());
        output.mergeFormat(source.getFormat());

        // ensure output is in the correct format
        final JFormat format = output.getFormat();
        final JTrackType type = outputMediaType();
        if (!format.hasFileExtension() || format.getStreamType().mediaType() != type) {
            final JStreamType streamType = type.streamType();
            if (streamType == null) {
                return die("getSingleInputContext: no file extension found for output media type: " + type);
            }
            format.setStreamType(streamType);
        }
        final JStreamType streamType = getStreamType(source, output);

        return new JSingleOperationContext(source, output, streamType, assetManager, toolbox);
    }

    protected JStreamType getStreamType(JAsset source, JAsset output) {
        return output.getFormat().getStreamType();
    }

}
