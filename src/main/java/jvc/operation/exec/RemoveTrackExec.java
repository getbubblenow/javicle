package jvc.operation.exec;

import jvc.model.JAsset;
import jvc.model.JFileExtension;
import jvc.model.JTrackId;
import jvc.model.info.JTrackType;
import jvc.model.operation.JSingleOperationContext;
import jvc.operation.RemoveTrackOperation;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RemoveTrackExec extends SingleOrMultiSourceExecBase<RemoveTrackOperation> {

    public static final String REMOVE_TRACK_TEMPLATE
            = "{{{ffmpeg}}} -i {{{source.path}}} "
            + "-map 0 -map -0:{{trackType}}{{#exists trackNumber}}:{{trackNumber}}{{/exists}} "
            + "-c copy "
            + "-y {{{output.path}}}";

    @Override protected String getProcessTemplate() { return REMOVE_TRACK_TEMPLATE; }

    @Override public void operate(RemoveTrackOperation op, Toolbox toolbox, AssetManager assetManager) {

        final JSingleOperationContext opCtx = op.getSingleInputContext(assetManager);
        final JAsset source = opCtx.source;
        final JAsset output = opCtx.output;
        final JFileExtension formatType = opCtx.formatType;

        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());
        ctx.put("source", source);

        final JTrackId trackId = op.getTrackId();
        final JTrackType trackType = trackId.getType();
        ctx.put("trackType", trackType.ffmpegType());
        if (trackId.hasNumber()) ctx.put("trackNumber", trackId.getNumber());

        operate(op, toolbox, assetManager, source, output, formatType, ctx);
    }

}
