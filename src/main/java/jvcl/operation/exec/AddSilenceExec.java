package jvcl.operation.exec;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
import jvcl.model.operation.JSingleOperationContext;
import jvcl.operation.AddSilenceOperation;
import jvcl.service.AssetManager;
import jvcl.service.Toolbox;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AddSilenceExec extends SingleOrMultiSourceExecBase<AddSilenceOperation> {

    public static final String ADD_SILENCE_TEMPLATE
            = "{{{ffmpeg}}} -i {{{source.path}}} -i {{{silence.path}}} "
            + "-map 0:v -map 1:a -c:v copy -shortest "
            + "-y {{{output.path}}}";

    @Override protected String getProcessTemplate() { return ADD_SILENCE_TEMPLATE; }

    @Override public void operate(AddSilenceOperation op, Toolbox toolbox, AssetManager assetManager) {
        final JSingleOperationContext opCtx = op.getSingleInputContext(assetManager);
        final JAsset source = opCtx.source;
        final JAsset output = opCtx.output;
        final JFileExtension formatType = opCtx.formatType;

        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());
        ctx.put("source", source);

        final JAsset silence = createSilence(op, toolbox, assetManager, source.duration(), source);
        ctx.put("silence", silence);

        operate(op, toolbox, assetManager, source, output, formatType, ctx);
    }

}
