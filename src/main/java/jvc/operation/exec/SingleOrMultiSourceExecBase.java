package jvc.operation.exec;

import jvc.model.JAsset;
import jvc.model.JFileExtension;
import jvc.model.operation.JSingleOperationContext;
import jvc.model.operation.JSingleSourceOperation;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.io.FileUtil.*;

@Slf4j
public abstract class SingleOrMultiSourceExecBase<OP extends JSingleSourceOperation> extends ExecBase<OP> {

    @Override public Map<String, Object> operate(OP op, Toolbox toolbox, AssetManager assetManager) {
        final JSingleOperationContext opCtx = op.getSingleInputContext(assetManager, toolbox);
        final JAsset source = opCtx.source;
        final JAsset output = opCtx.output;
        final JFileExtension formatType = opCtx.formatType;
        final Map<String, Object> ctx = initialContext(toolbox, source);
        addCommandContext(op, opCtx, ctx);
        return operate(op, toolbox, assetManager, source, output, formatType, ctx);
    }

    protected void addCommandContext(OP op, JSingleOperationContext opCtx, Map<String, Object> ctx) {}

    protected Map<String, Object> operate(OP op, Toolbox toolbox, AssetManager assetManager, JAsset source, JAsset output, JFileExtension formatType, Map<String, Object> ctx) {
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
                    outfile = new File(output.destDirectory(), basename(appendToFileNameBeforeExt(asset.getPath(), "_"+op.shortString())));
                    if (outfile.exists()) {
                        log.info("operate: dest exists: "+abs(outfile));
                        assetManager.addOperationAssetSlice(output, subOutput.setPath(abs(outfile)));
                        continue;
                    }
                } else {
                    outfile = defaultOutfile;
                }
                subOutput.setPath(abs(outfile));
                process(ctx, op, asset, output, subOutput, toolbox, assetManager);
                assetManager.addOperationAssetSlice(output, subOutput);
            }
        } else {
            final File defaultOutfile = assetManager.assetPath(op, source, formatType);
            final File path = resolveOutputPath(output, defaultOutfile);
            if (path == null) return null;
            output.setPath(abs(path));
            process(ctx, op, source, output, output, toolbox, assetManager);
            assetManager.addOperationAsset(output);
        }
        ctx.put("output", output);
        return ctx;
    }

    protected abstract String getProcessTemplate();

    protected void process(Map<String, Object> ctx,
                           OP op,
                           JAsset source,
                           JAsset output,
                           JAsset subOutput,
                           Toolbox toolbox,
                           AssetManager assetManager) {
        ctx.put("source", source);
        ctx.put("output", subOutput);
        final String script = renderScript(toolbox, ctx, getProcessTemplate());

        log.debug("operate: running script: "+script);
        final String scriptOutput = exec(script, op.isNoExec());
        log.debug("operate: command output: "+scriptOutput);
    }

}
