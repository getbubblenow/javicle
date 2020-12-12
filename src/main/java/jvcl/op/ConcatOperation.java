package jvcl.op;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
import jvcl.model.JOperation;
import jvcl.service.AssetManager;
import jvcl.service.JOperator;
import jvcl.service.Toolbox;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.handlebars.HandlebarsUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jvcl.model.JAsset.flattenAssetList;
import static jvcl.model.JAsset.json2asset;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.json.JsonUtil.json;
import static org.cobbzilla.util.system.CommandShell.execScript;

@Slf4j
public class ConcatOperation implements JOperator {

    public static final String CONCAT_RECODE_TEMPLATE_OLD
            // concat inputs
            = "{{ffmpeg}} -f concat{{#each sources}} -i {{{this.path}}}{{/each}} "
            // safely with copy codec
            + "-safe 0 -c copy {{{output.path}}}";

    public static final String CONCAT_RECODE_TEMPLATE_1
            // list inputs
            = "{{ffmpeg}} {{#each sources}} -i {{{this.path}}}{{/each}} "

            // filter: list inputs
            + "-filter_complex \"{{#each sources}}[{{@index}}:v] [{{@index}}:a] {{/each}} "

            // filter: concat filter them together
            + "concat=n={{sources.length}}:v=1:a=1 [v] [a]\" "

            // output combined result
            + "-map \"[v]\" -map \"[a]\" {{{output.path}}}";

    @Override public void operate(JOperation op, Toolbox toolbox, AssetManager assetManager) {

        final ConcatConfig config = json(json(op.getPerform()), ConcatConfig.class);

        // validate sources
        final List<JAsset> sources = flattenAssetList(assetManager.resolve(config.getConcat()));
        if (empty(sources)) die("operate: no sources");

        // create output object
        final JAsset output = json2asset(op.getCreates());
        if (output.hasDest() && output.destExists()) {
            log.info("operate: dest exists, not re-creating: "+output.destPath());
            return;
        }

        // if any format settings are missing, use settings from first source
        output.mergeFormat(sources.get(0).getFormat());

        // set the path, check if output asset already exists
        final JFileExtension formatType = output.getFormat().getFileExtension();
        final File outfile = output.hasDest()
                ? new File(output.destPath())
                : assetManager.assetPath(op, sources, formatType);
        if (outfile.exists()) {
            log.info("operate: outfile exists, not re-creating: "+abs(outfile));
            return;
        }
        if (!outfile.getParentFile().canWrite()) die("operate: cannot write file (parent directory not writeable): "+abs(outfile));

        output.setPath(abs(outfile));

        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());
        ctx.put("sources", sources);
        ctx.put("output", output);
        final String script = HandlebarsUtil.apply(toolbox.getHandlebars(), CONCAT_RECODE_TEMPLATE_1, ctx);

        log.debug("operate: running script: "+script);
        final String scriptOutput = execScript(script);
        log.debug("operate: command output: "+scriptOutput);
        assetManager.addOperationAsset(output);
    }

    @NoArgsConstructor
    private static class ConcatConfig {
        @Getter @Setter private String[] concat;
    }

}
