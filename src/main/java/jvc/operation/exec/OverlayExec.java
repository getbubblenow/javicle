package jvc.operation.exec;

import jvc.model.JAsset;
import jvc.model.JStreamType;
import jvc.model.operation.JSingleOperationContext;
import jvc.operation.OverlayOperation;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.JsEngine;

import java.io.File;
import java.math.BigDecimal;
import java.util.Map;

import static org.cobbzilla.util.io.FileUtil.abs;

@Slf4j
public class OverlayExec extends ExecBase<OverlayOperation> {

    public static final String OVERLAY_TEMPLATE
            = "ffmpeg -i {{{source.path}}} -i {{{overlay.path}}} "
            + "{{#if hasMainStart}}-ss {{mainStart}} {{/if}}"
            + "{{#if hasMainEnd}}-t {{mainDuration}} {{/if}}"
            + "-filter_complex \""
            + "[1:v] setpts=PTS-STARTPTS+(1/TB){{#exists width}}, scale={{width}}x{{height}}{{/exists}} [1v]; "
            + "[0:v][1v] overlay={{{overlayFilterConfig}}} "
            + "\" -y {{{output.path}}}";

    @Override public Map<String, Object> operate(OverlayOperation op, Toolbox toolbox, AssetManager assetManager) {

        final JSingleOperationContext opCtx = op.getSingleInputContext(assetManager, toolbox);
        final JAsset source = opCtx.source;
        final JAsset output = opCtx.output;
        final JStreamType streamType = opCtx.streamType;

        final OverlayOperation.OverlayConfig overlay = op.getOverlay();
        final JAsset overlaySource = assetManager.resolve(overlay.getSource());

        final File defaultOutfile = assetManager.assetPath(op, source, streamType);
        final File path = resolveOutputPath(output, defaultOutfile);
        if (path == null) return null;
        output.setPath(abs(path));

        final JsEngine js = toolbox.getJs();
        final Map<String, Object> ctx = initialContext(toolbox, source, getVars());
        ctx.put("overlay", overlaySource);

        final BigDecimal mainStart = op.getStartTime(ctx, js);
        ctx.put("mainStart", mainStart);
        ctx.put("hasMainStart", op.hasStartTime());

        final BigDecimal mainEnd = op.getEndTime(ctx, js);
        ctx.put("mainEnd", mainEnd);
        ctx.put("hasMainEnd", op.hasEndTime());
        if (op.hasEndTime()) {
            ctx.put("mainDuration", mainEnd.subtract(mainStart).doubleValue());
        }

        final String overlayFilter = buildOverlayFilter(op, source, overlaySource, overlay, ctx, js);
        ctx.put("overlayFilterConfig", overlayFilter);
        ctx.put("output", output);

        overlay.setProportionalWidthAndHeight(ctx, js, overlaySource);

        final String script = renderScript(toolbox, ctx, OVERLAY_TEMPLATE);

        log.debug("operate: running script: "+script);
        final String scriptOutput = exec(script, op.isNoExec());
        log.debug("operate: command output: "+scriptOutput);
        assetManager.addOperationAsset(output);
        return ctx;
    }

    private String buildOverlayFilter(OverlayOperation op,
                                      JAsset source,
                                      JAsset overlaySource,
                                      OverlayOperation.OverlayConfig overlay,
                                      Map<String, Object> ctx,
                                      JsEngine js) {
        final StringBuilder b = new StringBuilder();
        final BigDecimal startTime = overlay.getStartTime(ctx, js);
        final BigDecimal endTime = overlay.hasEndTime() ? overlay.getEndTime(ctx, js) : overlaySource.duration();
        b.append("enable=between(t\\,").append(startTime).append("\\,").append(endTime).append(")");

        if (overlay.hasX() && overlay.hasY()) {
            b.append(":x=").append(overlay.getX(ctx, js)).append(":y=").append(overlay.getY(ctx, js));
        }

        return b.toString();
    }

}
