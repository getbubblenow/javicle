package jvc.operation.exec;

import jvc.model.JAsset;
import jvc.model.JSpec;
import jvc.model.JStreamType;
import jvc.model.operation.JOperation;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.collection.NameAndValue;
import org.cobbzilla.util.handlebars.HandlebarsUtil;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static jvc.service.Toolbox.jsContext;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.basename;
import static org.cobbzilla.util.system.CommandShell.execScript;

@Slf4j @Accessors(chain=true)
public abstract class ExecBase<OP extends JOperation> {

    @Getter @Setter private JSpec spec;

    public NameAndValue[] getVars () {
        return spec == null || empty(spec.getVars())
                ? NameAndValue.EMPTY_ARRAY
                : spec.getVars();
    }

    public abstract Map<String, Object> operate(OP operation, Toolbox toolbox, AssetManager assetManager);

    protected String renderScript(Toolbox toolbox, Map<String, Object> ctx, String template) {
        return HandlebarsUtil.apply(toolbox.getHandlebars(), template, jsContext(ctx));
    }

    protected File resolveOutputPath(JAsset output, File defaultOutfile) {
        if (output.hasDest()) {
            if (output.destExists() && !output.destIsDirectory()) {
                log.info("resolveOutputPath: dest exists: " + output.getDest());
                return null;
            } else if (output.destIsDirectory()) {
                return new File(output.destDirectory(), basename(abs(defaultOutfile)));
            } else {
                return new File(output.destPath());
            }
        } else {
            return defaultOutfile;
        }
    }

    protected Map<String, Object> initialContext(Toolbox toolbox, JAsset source, NameAndValue[] vars) {
        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());
        ctx.put("source", source);
        if (!empty(vars)) {
            for (NameAndValue var : vars) {
                final String name = var.getName();
                if (ctx.containsKey(name)) {
                    log.warn("initialContext: spec variable "+ name +" will mask existing value: "+ctx.get(name));
                }
                ctx.put(name, var.getValue());
            }
        }
        return ctx;
    }

    public String exec(String script, boolean noExec) {
        if (noExec) {
            System.out.println(script);
            return "";
        } else {
            return execScript(script);
        }
    }

    public static final String CREATE_SILENCE_TEMPLATE
            = "{{{ffmpeg}}} -f lavfi "
            + "-i anullsrc=channel_layout={{channelLayout}}:sample_rate={{samplingRate}} "
            + "-t {{duration}} "
            + "-y {{{silence.path}}}";

    protected JAsset createSilence(OP op,
                                   Toolbox toolbox,
                                   AssetManager assetManager,
                                   BigDecimal duration,
                                   JAsset asset) {
        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());
        ctx.put("duration", duration);

        if (!asset.hasSamplingRate()) return die("createSilence: no sampling rate could be determined: "+asset);
        ctx.put("samplingRate", asset.samplingRate());

        if (!asset.hasChannelLayout()) return die("createSilence: no channel layout could be determined: "+asset);
        ctx.put("channelLayout", asset.channelLayout());

        final JStreamType streamType = asset.audioExtension();
        final File silenceFile = assetManager.assetPath(op, asset, streamType, new Object[]{duration});
        final JAsset silence = new JAsset().setPath(abs(silenceFile));
        ctx.put("silence", silence);

        final String script = renderScript(toolbox, ctx, CREATE_SILENCE_TEMPLATE);

        log.debug("createSilence: running script: "+script);
        final String scriptOutput = exec(script, op.isNoExec());
        log.debug("createSilence: command output: "+scriptOutput);

        return silence;
    }

}
