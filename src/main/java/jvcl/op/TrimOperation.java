package jvcl.op;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
import jvcl.model.JOperation;
import jvcl.service.AssetManager;
import jvcl.service.JOperator;
import jvcl.service.Toolbox;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.handlebars.HandlebarsUtil;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static jvcl.model.JAsset.json2asset;
import static jvcl.service.Toolbox.getDuration;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.io.FileUtil.*;
import static org.cobbzilla.util.json.JsonUtil.json;
import static org.cobbzilla.util.system.CommandShell.execScript;

@Slf4j
public class TrimOperation implements JOperator {

    public static final String TRIM_TEMPLATE
            = "{{ffmpeg}} -i {{{source.path}}} -ss {{startSeconds}} {{#exists interval}}-t {{interval}} {{/exists}}{{{output.path}}}";

    @Override public void operate(JOperation op, Toolbox toolbox, AssetManager assetManager) {

        final TrimConfig config = json(json(op.getPerform()), TrimConfig.class);
        final JAsset source = assetManager.resolve(config.getTrim());

        final JAsset output = json2asset(op.getCreates());
        output.mergeFormat(source.getFormat());

        final JFileExtension formatType = output.getFormat().getFileExtension();

        if (source.hasList()) {
            if (output.hasDest()) {
                if (!output.destIsDirectory()) die("operate: dest is not a directory: "+output.getDest());
            }
            assetManager.addOperationArrayAsset(output);
            for (JAsset asset : source.getList()) {
                final JAsset subOutput = new JAsset(output);
                final File defaultOutfile = assetManager.assetPath(op, asset, formatType, new Object[]{config});
                final File outfile;
                if (output.hasDest()) {
                    outfile = new File(output.destDirectory(), basename(appendToFileNameBeforeExt(asset.getPath(), "_"+config.shortString())));
                } else {
                    outfile = defaultOutfile;
                }
                subOutput.setPath(abs(outfile));
                trim(config, asset, subOutput, toolbox, assetManager);
            }
        } else {
            if (output.hasDest() && output.destExists()) {
                log.info("operate: dest exists, not trimming: "+output.getDest());
            } else {
                trim(config, source, output, toolbox, assetManager);
            }
        }
    }

    private void trim(TrimConfig config,
                      JAsset source,
                      JAsset output,
                      Toolbox toolbox,
                      AssetManager assetManager) {
        if (output.destExists()) {
            log.info("trim: dest exists: "+output.getDest());
            return;
        }
        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());
        ctx.put("source", source);
        ctx.put("output", output);

        final BigDecimal startTime = config.getStartTime();
        ctx.put("startSeconds", startTime);
        if (config.hasEnd()) ctx.put("interval", config.getEndTime().subtract(startTime));
        final String script = HandlebarsUtil.apply(toolbox.getHandlebars(), TRIM_TEMPLATE, ctx);

        log.debug("operate: running script: "+script);
        final String scriptOutput = execScript(script);
        log.debug("operate: command output: "+scriptOutput);
        assetManager.addOperationAssetSlice(output, output);
    }

    @NoArgsConstructor @EqualsAndHashCode
    private static class TrimConfig {
        @Getter @Setter private String trim;

        @Getter @Setter private String start;
        public BigDecimal getStartTime() { return empty(start) ? BigDecimal.ZERO : getDuration(start); }

        @Getter @Setter private String end;
        public boolean hasEnd() { return !empty(end); }
        public BigDecimal getEndTime() { return getDuration(end); }

        public String shortString() { return "trim_"+getStart()+(hasEnd() ? "_"+getEnd() : ""); }
        public String toString() { return trim+"_"+getStart()+(hasEnd() ? "_"+getEnd() : ""); }
    }
}
