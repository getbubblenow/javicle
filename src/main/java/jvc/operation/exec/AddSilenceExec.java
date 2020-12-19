package jvc.operation.exec;

import jvc.model.JAsset;
import jvc.model.operation.JSingleOperationContext;
import jvc.operation.AddSilenceOperation;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class AddSilenceExec extends SingleOrMultiSourceExecBase<AddSilenceOperation> {

    public static final String ADD_SILENCE_TEMPLATE
            = "{{{ffmpeg}}} -i {{{source.path}}} -i {{{silence.path}}} "
            + "-map 0:v -map 1:a -c:v copy -shortest "
            + "-y {{{output.path}}}";

    @Override protected String getProcessTemplate() { return ADD_SILENCE_TEMPLATE; }

    @Override protected void addCommandContext(AddSilenceOperation op,
                                               JSingleOperationContext opCtx,
                                               Map<String, Object> ctx) {
        final JAsset source = opCtx.source;
        final JAsset silence = createSilence(op, opCtx.toolbox, opCtx.assetManager, source.duration(), source);
        ctx.put("silence", silence);
    }

}
