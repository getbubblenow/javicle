package jvc.operation.exec;

import jvc.model.JAsset;
import jvc.model.JFileExtension;
import jvc.model.operation.JMultiOperationContext;
import jvc.operation.ConcatOperation;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cobbzilla.util.io.FileUtil.abs;

@Slf4j
public class ConcatExec extends ExecBase<ConcatOperation> {

    public static final String CONCAT_RECODE_TEMPLATE_1
            // list inputs
            = "{{ffmpeg}} {{#each sources}} -i {{{this.path}}}{{/each}} "

            // filter: list inputs
            + "-filter_complex \"{{#each sources}}[{{@index}}:v] [{{@index}}:a] {{/each}} "

            // filter: concat filter them together
            + "concat=n={{sources.length}}:v=1:a=1 [v] [a]\" "

            // output combined result
            + "-map \"[v]\" -map \"[a]\" -y {{{output.path}}}";

    @Override public Map<String, Object> operate(ConcatOperation op, Toolbox toolbox, AssetManager assetManager) {

        final JMultiOperationContext opCtx = op.getMultiInputContext(assetManager, toolbox);
        final List<JAsset> sources = opCtx.sources;
        final JAsset output = opCtx.output;
        final JFileExtension formatType = opCtx.formatType;

        final File defaultOutfile = assetManager.assetPath(op, sources, formatType);
        final File path = resolveOutputPath(output, defaultOutfile);
        if (path == null) return null;
        output.setPath(abs(path));

        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());
        ctx.put("sources", sources);
        ctx.put("output", output);
        final String script = renderScript(toolbox, ctx, CONCAT_RECODE_TEMPLATE_1);

        log.debug("operate: running script: "+script);
        final String scriptOutput = exec(script, op.isNoExec());
        log.debug("operate: command output: "+scriptOutput);
        assetManager.addOperationAsset(output);
        return ctx;
    }

}
