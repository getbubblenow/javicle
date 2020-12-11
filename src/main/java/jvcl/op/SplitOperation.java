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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static jvcl.model.JAsset.json2asset;
import static org.cobbzilla.util.daemon.ZillaRuntime.big;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.json.JsonUtil.json;
import static org.cobbzilla.util.system.CommandShell.execScript;
import static org.cobbzilla.util.time.TimeUtil.parseDuration;

@Slf4j
public class SplitOperation implements JOperator {

    public static final String SPLIT_TEMPLATE
            = "{{ffmpeg}} -i {{{source.path}}} -ss {{startSeconds}} -t {{endSeconds}} {{{output.path}}}";

    @Override public void operate(JOperation op, Toolbox toolbox, AssetManager assetManager) {
        final SplitConfig config = json(json(op.getPerform()), SplitConfig.class);

        final JAsset source = assetManager.resolve(config.getSplit());

        // create output object
        final JAsset output = json2asset(op.getCreates());

        // if any format settings are missing, use settings from source
        output.mergeFormat(source.getFormat());

        // get format type
        final JFileExtension formatType = output.getFormat().getFileExtension();

        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());
        ctx.put("source", source);
        final BigDecimal incr = config.getIntervalIncr();
        for (BigDecimal i = config.getStartTime();
             i.compareTo(config.getEndTime(source)) < 0;
             i = i.add(incr)) {

            final File outfile = assetManager.assetPath(op, source, formatType, new Object[]{i, incr});
            if (outfile.exists()) {
                log.info("operate: outfile exists, not re-creating: "+abs(outfile));
                return;
            }
            final JAsset slice = new JAsset(output);
            slice.setPath(abs(outfile));

            ctx.put("output", slice);
            ctx.put("startSeconds", i);
            ctx.put("endSeconds", i.add(incr));
            final String script = HandlebarsUtil.apply(toolbox.getHandlebars(), SPLIT_TEMPLATE, ctx);
            log.debug("operate: running script: "+script);
            final String scriptOutput = execScript(script);
            log.debug("operate: command output: "+scriptOutput);
            output.addAsset(slice);
        }
        assetManager.addOperationAsset(output);
    }

    @NoArgsConstructor
    private static class SplitConfig {

        @Getter @Setter private String split;

        @Getter @Setter private String interval;
        public BigDecimal getIntervalIncr() { return big(parseDuration(interval)).divide(big(1000), RoundingMode.UNNECESSARY); }

        @Getter @Setter private String start;
        public BigDecimal getStartTime() { return empty(start) ? BigDecimal.ZERO : big(start); }

        @Getter @Setter private String end;
        public BigDecimal getEndTime(JAsset source) { return empty(end) ? source.duration() : big(end); }
    }

}
