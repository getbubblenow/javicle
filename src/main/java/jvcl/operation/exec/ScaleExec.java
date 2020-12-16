package jvcl.operation.exec;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
import jvcl.model.operation.JSingleOperationContext;
import jvcl.operation.ScaleOperation;
import jvcl.service.AssetManager;
import jvcl.service.Toolbox;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.StandardJsEngine;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.cobbzilla.util.system.CommandShell.execScript;

@Slf4j
public class ScaleExec extends SingleOrMultiSourceExecBase<ScaleOperation> {

    public static final String SCALE_TEMPLATE
            = "{{ffmpeg}} -i {{{source.path}}} -filter_complex \""
            + "scale={{width}}x{{height}}" +
            "\" -y {{{output.path}}}";

    @Override public void operate(ScaleOperation op, Toolbox toolbox, AssetManager assetManager) {

        final JSingleOperationContext opCtx = op.getSingleInputContext(assetManager);
        final JAsset source = opCtx.source;
        final JAsset output = opCtx.output;
        final JFileExtension formatType = opCtx.formatType;

        final StandardJsEngine js = toolbox.getJs();
        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());

        if (op.hasFactor()) {
            final BigDecimal factor = op.getFactor(ctx, js);
            ctx.put("width", factor.multiply(source.getWidth()).intValue());
            ctx.put("height", factor.multiply(source.getHeight()).intValue());
        } else {
            op.setProportionalWidthAndHeight(ctx, js, source);
        }

        operate(op, toolbox, assetManager, source, output, formatType, ctx);
    }

    @Override protected void process(Map<String, Object> ctx,
                                     ScaleOperation op,
                                     JAsset source,
                                     JAsset output,
                                     JAsset subOutput,
                                     Toolbox toolbox,
                                     AssetManager assetManager) {

        ctx.put("source", source);
        ctx.put("output", subOutput);
        final String script = renderScript(toolbox, ctx, SCALE_TEMPLATE);

        log.debug("operate: running script: "+script);
        final String scriptOutput = execScript(script);
        log.debug("operate: command output: "+scriptOutput);
        if (output == subOutput) {
            assetManager.addOperationAsset(output);
        } else {
            assetManager.addOperationAssetSlice(output, subOutput);
        }
    }

}
