package jvcl.operation.exec;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
import jvcl.model.operation.JSingleOperationContext;
import jvcl.operation.TrimOperation;
import jvcl.service.AssetManager;
import jvcl.service.Toolbox;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.StandardJsEngine;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.cobbzilla.util.system.CommandShell.execScript;

@Slf4j
public class TrimExec extends SingleOrMultiSourceExecBase<TrimOperation> {

    public static final String TRIM_TEMPLATE
            = "{{ffmpeg}} -i {{{source.path}}} " +
            "-ss {{startSeconds}} " +
            "{{#exists interval}}-t {{interval}} {{/exists}}" +
            "-y {{{output.path}}}";

    @Override public void operate(TrimOperation op, Toolbox toolbox, AssetManager assetManager) {

        final JSingleOperationContext opCtx = op.getSingleInputContext(assetManager);
        final JAsset source = opCtx.source;
        final JAsset output = opCtx.output;
        final JFileExtension formatType = opCtx.formatType;

        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());

        operate(op, toolbox, assetManager, source, output, formatType, ctx);
    }

    @Override protected void process(Map<String, Object> ctx,
                                     TrimOperation op,
                                     JAsset source,
                                     JAsset output,
                                     JAsset subOutput,
                                     Toolbox toolbox,
                                     AssetManager assetManager) {

        ctx.put("source", source);
        ctx.put("output", subOutput);

        final StandardJsEngine js = toolbox.getJs();
        final BigDecimal startTime = op.getStartTime(ctx, js);
        ctx.put("startSeconds", startTime);
        if (op.hasEnd()) ctx.put("interval", op.getEndTime(ctx, js).subtract(startTime));
        final String script = renderScript(toolbox, ctx, TRIM_TEMPLATE);

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
