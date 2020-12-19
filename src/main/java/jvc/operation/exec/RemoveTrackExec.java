package jvc.operation.exec;

import jvc.model.JTrackId;
import jvc.model.info.JTrackType;
import jvc.model.operation.JSingleOperationContext;
import jvc.operation.RemoveTrackOperation;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class RemoveTrackExec extends SingleOrMultiSourceExecBase<RemoveTrackOperation> {

    public static final String REMOVE_TRACK_TEMPLATE
            = "{{{ffmpeg}}} -i {{{source.path}}} "
            + "-map 0 -map -0:{{trackType}}{{#exists trackNumber}}:{{trackNumber}}{{/exists}} "
            + "-c copy "
            + "-y {{{output.path}}}";

    @Override protected String getProcessTemplate() { return REMOVE_TRACK_TEMPLATE; }

    @Override protected void addCommandContext(RemoveTrackOperation op,
                                               JSingleOperationContext opCtx,
                                               Map<String, Object> ctx) {
        final JTrackId trackId = op.getTrackId();
        final JTrackType trackType = trackId.getType();
        ctx.put("trackType", trackType.ffmpegType());
        if (trackId.hasNumber()) ctx.put("trackNumber", trackId.getNumber());
    }

}
