package jvc.operation.exec;

import jvc.model.JAsset;
import jvc.model.JFileExtension;
import jvc.model.operation.JSingleOperationContext;
import jvc.operation.SplitOperation;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.JsEngine;

import java.io.File;
import java.math.BigDecimal;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.mkdirOrDie;

@Slf4j
public class SplitExec extends ExecBase<SplitOperation> {

    public static final String SPLIT_TEMPLATE
            = "{{ffmpeg}} -i {{{source.path}}} -ss {{startSeconds}} -t {{interval}} -y {{{output.path}}}";

    @Override public void operate(SplitOperation op, Toolbox toolbox, AssetManager assetManager) {

        final JSingleOperationContext opCtx = op.getSingleInputContext(assetManager, toolbox);
        final JAsset source = opCtx.source;
        final JAsset output = opCtx.output;
        final JFileExtension formatType = opCtx.formatType;

        final JsEngine js = toolbox.getJs();
        final Map<String, Object> ctx = initialContext(toolbox, source);

        assetManager.addOperationArrayAsset(output);
        final BigDecimal incr = op.getIntervalIncr(ctx, js);
        final BigDecimal endTime = op.getEndTime(ctx, js, source.duration());
        for (BigDecimal i = op.getStartTime(ctx, js);
             i.compareTo(endTime) < 0;
             i = i.add(incr)) {

            final File outfile;
            if (output.hasDest()) {
                if (!output.destExists()) {
                    outfile = sliceFile(output, formatType, i, incr);
                } else {
                    if (output.destIsDirectory()) {
                        outfile = sliceFile(output, formatType, i, incr);
                    } else {
                        die("dest exists and is not a directory: "+output.getDest());
                        return;
                    }
                }
            } else {
                outfile = assetManager.assetPath(op, source, formatType, new Object[]{i, incr});
            }

            if (outfile.exists()) {
                log.info("operate: outfile exists, not re-creating: "+abs(outfile));
                continue;
            } else {
                mkdirOrDie(outfile.getParentFile());
            }
            final JAsset slice = new JAsset(output);
            slice.setPath(abs(outfile));
            slice.setName(source.getName()+"_"+i+"_"+incr);

            ctx.put("output", slice);
            ctx.put("startSeconds", i);
            ctx.put("interval", incr);
            final String script = renderScript(toolbox, ctx, SPLIT_TEMPLATE);

            log.debug("operate: running script: "+script);
            final String scriptOutput = exec(script, op.isNoExec());
            log.debug("operate: command output: "+scriptOutput);
            assetManager.addOperationAssetSlice(output, slice);
        }
        log.info("operate: completed");
    }

    private File sliceFile(JAsset output, JFileExtension formatType, BigDecimal i, BigDecimal incr) {
        return new File(output.destDirectory(), output.getName() + "_" + i + "_" + incr + formatType.ext());
    }

}
