package jvcl.operation.exec;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
import jvcl.model.operation.JSingleOperationContext;
import jvcl.operation.ScaleOperation;
import jvcl.service.AssetManager;
import jvcl.service.Toolbox;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.StandardJsEngine;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.io.FileUtil.*;
import static org.cobbzilla.util.system.CommandShell.execScript;

@Slf4j
public class ScaleExec extends ExecBase<ScaleOperation> {

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

        if (source.hasList()) {
            if (output.hasDest()) {
                if (!output.destIsDirectory()) die("operate: dest is not a directory: "+output.getDest());
            }
            assetManager.addOperationArrayAsset(output);
            for (JAsset asset : source.getList()) {
                final JAsset subOutput = new JAsset(output);
                final File defaultOutfile = assetManager.assetPath(op, asset, formatType);
                final File outfile;
                if (output.hasDest()) {
                    outfile = new File(output.destDirectory(), basename(appendToFileNameBeforeExt(asset.getPath(), "_"+op.shortString(ctx, js))));
                    if (outfile.exists()) {
                        log.info("operate: dest exists: "+abs(outfile));
                        return;
                    }
                } else {
                    outfile = defaultOutfile;
                }
                subOutput.setPath(abs(outfile));
                scale(ctx, asset, output, subOutput, toolbox, assetManager);
            }
        } else {
            final File defaultOutfile = assetManager.assetPath(op, source, formatType);
            final File path = resolveOutputPath(output, defaultOutfile);
            if (path == null) return;
            output.setPath(abs(path));
            scale(ctx, source, output, output, toolbox, assetManager);
        }
    }

    private void scale(Map<String, Object> ctx,
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
