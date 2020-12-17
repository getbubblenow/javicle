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

    public static final String ADD_SILENCE_TEMPLATE = "";

    @Override public void operate(AddSilenceOperation op, Toolbox toolbox, AssetManager assetManager) {
        final JSingleOperationContext opCtx = op.getSingleInputContext(assetManager);
        final JAsset source = opCtx.source;
        final JAsset output = opCtx.output;
        final JFileExtension formatType = opCtx.formatType;

        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());
        ctx.put("source", source);

        operate(op, toolbox, assetManager, source, output, formatType, ctx);
    }

    @Override protected void process(Map<String, Object> ctx, AddSilenceOperation addSilenceOperation, JAsset source, JAsset output, JAsset asset, Toolbox toolbox, AssetManager assetManager) {
        // todo
    }

}
