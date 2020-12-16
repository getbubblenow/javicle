package jvcl.operation.exec;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
import jvcl.model.operation.JSingleOperationContext;
import jvcl.operation.LetterboxOperation;
import jvcl.service.AssetManager;
import jvcl.service.Toolbox;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.StandardJsEngine;

import java.util.HashMap;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.string.StringUtil.safeShellArg;
import static org.cobbzilla.util.system.CommandShell.execScript;

@Slf4j
public class LetterboxExec extends SingleOrMultiSourceExecBase<LetterboxOperation> {

    public static final String LETTERBOX_TEMPLATE
            = "{{ffmpeg}} -i {{{source.path}}} -filter_complex \""
            + "pad="
            + "width={{width}}:"
            + "height={{height}}:"
            + "x=({{width}}-iw*min({{width}}/iw\\,{{height}}/ih))/2:"
            + "y=({{height}}-ih*min({{width}}/iw\\,{{height}}/ih))/2:"
            + "color={{{color}}}"
            + "\" -y {{{output.path}}}";

    public static final String DEFAULT_LETTERBOX_COLOR = "black";

    @Override public void operate(LetterboxOperation op, Toolbox toolbox, AssetManager assetManager) {

        final JSingleOperationContext opCtx = op.getSingleInputContext(assetManager);
        final JAsset source = opCtx.source;
        final JAsset output = opCtx.output;
        final JFileExtension formatType = opCtx.formatType;

        final StandardJsEngine js = toolbox.getJs();
        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());
        ctx.put("source", source);

        if (!op.hasWidth() || !op.hasHeight()) {
            die("operate: both width and height must be set");
        }
        ctx.put("width", op.getWidth(ctx, js).intValue());
        ctx.put("height", op.getHeight(ctx, js).intValue());

        if (op.hasColor()) {
            ctx.put("color", safeShellArg(op.getColor()));
        } else {
            ctx.put("color", DEFAULT_LETTERBOX_COLOR);
        }

        operate(op, toolbox, assetManager, source, output, formatType, ctx);
    }

    @Override protected void process(Map<String, Object> ctx,
                                     LetterboxOperation op,
                                     JAsset source,
                                     JAsset output,
                                     JAsset subOutput,
                                     Toolbox toolbox,
                                     AssetManager assetManager) {

        ctx.put("source", source);
        ctx.put("output", subOutput);
        final String script = renderScript(toolbox, ctx, LETTERBOX_TEMPLATE);

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
