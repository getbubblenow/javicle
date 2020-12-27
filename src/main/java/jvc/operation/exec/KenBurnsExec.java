package jvc.operation.exec;


import jvc.model.JAsset;
import jvc.model.JStreamType;
import jvc.model.operation.JSingleOperationContext;
import jvc.operation.KenBurnsOperation;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.JsEngine;

import java.io.File;
import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static jvc.service.Toolbox.TWO;
import static jvc.service.Toolbox.divideBig;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.json.JsonUtil.COMPACT_MAPPER;
import static org.cobbzilla.util.json.JsonUtil.json;

@Slf4j
public class KenBurnsExec extends ExecBase<KenBurnsOperation> {

    public static final String KEN_BURNS_TEMPLATE
            = "{{{ffmpeg}}} -i {{{source.path}}} -filter_complex \""
            + "scale={{expr width '*' upscale}}x{{expr height '*' upscale}}, "
            + "zoompan="
            + "{{#if hasZoom}}"
              + "z='{{#exists startFrame}}"
                + "if(between(in,{{startFrame}},{{endFrame}}),min(zoom+{{zoomIncrementFactor}},{{zoom}}),{{zoom}})"
              + "{{else}}"
                + "z='min(zoom+{{zoomIncrementFactor}},{{zoom}})':"
              + "{{/exists}}':"
            + "{{/if}}"
            + "d={{totalFrames}}:"
            + "fps={{fps}}:"
            + "x='if(gte(zoom,{{zoom}}),x,x{{deltaXSign}}{{deltaX}}/a)':"
            + "y='if(gte(zoom,{{zoom}}),y,y{{deltaYSign}}{{deltaY}})':"
            + "s={{width}}x{{height}}"
            + "\" -y {{{output.path}}}";

    @Override public Map<String, Object> operate(KenBurnsOperation op, Toolbox toolbox, AssetManager assetManager) {

        final JSingleOperationContext opCtx = op.getSingleInputContext(assetManager, toolbox);
        final JAsset source = opCtx.source;
        final JAsset output = opCtx.output;
        final JStreamType streamType = opCtx.streamType;

        final File defaultOutfile = assetManager.assetPath(op, source, streamType);
        final File path = resolveOutputPath(output, defaultOutfile);
        if (path == null) return null;
        output.setPath(abs(path));

        final JsEngine js = toolbox.getJs();
        final Map<String, Object> ctx = initialContext(toolbox, source, getVars());
        ctx.put("output", output);
        ctx.put("width", op.getWidth(ctx, js));
        ctx.put("height", op.getHeight(ctx, js));
        ctx.put("upscale", op.getUpscale(ctx, js));

        final BigDecimal fps = op.getFps(ctx, js);
        if (!op.hasDuration()) return die("operate: no duration defined: "+json(op, COMPACT_MAPPER));
        final BigDecimal duration = op.getDuration(ctx, js);
        ctx.put("duration", duration);

        final BigDecimal start = op.getStartTime(ctx, js);
        final BigDecimal end = op.getEndTime(ctx, js, duration.subtract(start));
        if (op.hasStartTime() || op.hasEndTime()) {
            ctx.put("startFrame", start.multiply(fps).intValue());
            ctx.put("endFrame", end.multiply(fps).intValue());
        }

        final BigDecimal zoom = op.getZoom(ctx, js);
        final BigDecimal totalFrames = duration.multiply(fps);
        final BigDecimal zoomIncrementFactor = zoom.equals(ONE) ? ZERO : divideBig(zoom.subtract(ONE), totalFrames);

        ctx.put("zoom", zoom);
        ctx.put("fps", fps.intValue());
        ctx.put("totalFrames", totalFrames.intValue());
        ctx.put("zoomIncrementFactor", zoomIncrementFactor);
        ctx.put("hasZoom", !zoomIncrementFactor.equals(ZERO));

        final BigDecimal midX = divideBig(source.getWidth(), TWO);
        final BigDecimal midY = divideBig(source.getHeight(), TWO);
        final BigDecimal destX = op.getX(ctx, js);
        final BigDecimal destY = op.getY(ctx, js);
        final BigDecimal deltaX = divideBig(destX.subtract(midX), totalFrames);
        final BigDecimal deltaY = divideBig(destY.subtract(midY), totalFrames);

        ctx.put("deltaXSign", deltaX.compareTo(ZERO) < 0 ? "-" : "+");
        ctx.put("deltaX", deltaX.abs());
        ctx.put("deltaYSign", deltaY.compareTo(ZERO) < 0 ? "-" : "+");
        ctx.put("deltaY", deltaY.abs());

        final String script = renderScript(toolbox, ctx, KEN_BURNS_TEMPLATE);

        log.debug("operate: running script: "+script);
        final String scriptOutput = exec(script, op.isNoExec());
        log.debug("operate: command output: "+scriptOutput);
        assetManager.addOperationAsset(output);
        return ctx;
    }

}
