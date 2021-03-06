package jvc.operation.exec;

import jvc.model.JAsset;
import jvc.model.JStreamType;
import jvc.model.js.JAssetJs;
import jvc.model.operation.JSingleOperationContext;
import jvc.operation.OverlayOperation;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.JsEngine;

import java.io.File;
import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static org.cobbzilla.util.io.FileUtil.abs;

@Slf4j
public class OverlayExec extends ExecBase<OverlayOperation> {

    public static final String OVERLAY_TEMPLATE
            = "ffmpeg -i {{{source.path}}} "
            + "{{#if hasMainStart}}-ss {{mainStart}} {{/if}}"
            + "{{#if hasMainEnd}}-t {{mainDuration}} {{/if}}"
            + "-i {{{overlay.path}}} "

            + "-filter_complex \""
            + "[1:v] setpts=PTS-STARTPTS"
            + "{{#exists width}}, scale={{width}}x{{height}}{{/exists}}"
            + " [1v]; "
            + "[0:v][1v] overlay={{{overlayFilterConfig}}} [v] "

            + "{{#if hasAudio}}"
              + "{{#if source.hasAudio}}"
                + "{{#if overlay.hasAudio}}"
                  // source and overlay both audio -- mix them
                  + "; [1:a] setpts=PTS-STARTPTS{{#if hasOverlayStart}}-({{overlayStart}}/TB){{/if}} [1a] "
                  + "; [0:a][1a] amix=inputs=2 [merged]"
                + "{{else}}"
                  // source has audio but overlay has none, use source audio
                  + "; anullsrc=channel_layout={{source.channelLayout}}:sample_rate={{source.samplingRate}}:duration={{source.duration}} [silence] "
                  + "; [silence][0:a] amix=inputs=2 [merged]"
                + "{{/if}}"
              + "{{else}}"
                + "{{#if overlay.hasAudio}}"
                  // source has no audio -- mix null source with overlay
                  + "; anullsrc=channel_layout={{overlay.channelLayout}}:sample_rate={{overlay.samplingRate}}:duration={{source.duration}} [silence] "
                  + "; [silence][1:a] amix=inputs=2 [merged]"
                + "{{/if}}"
              + "{{/if}}"
            + "{{/if}}"

            + "\" "
            + "-map \"[v]\" "
            + "{{#if hasAudio}} -map \"[merged]\"{{else}}-an{{/if}} "
            + " -y {{{output.path}}}";

    @Override public Map<String, Object> operate(OverlayOperation op, Toolbox toolbox, AssetManager assetManager) {

        final JSingleOperationContext opCtx = op.getSingleInputContext(assetManager, toolbox);
        final JAsset source = opCtx.source;
        final JAsset output = opCtx.output;
        final JStreamType streamType = opCtx.streamType;

        final OverlayOperation.OverlayConfig overlay = op.getOverlay();
        final JAsset overlaySource = assetManager.resolve(overlay.getSource());

        final File defaultOutfile = assetManager.assetPath(op, source, streamType);
        final File path = resolveOutputPath(assetManager, output, defaultOutfile);
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
        ctx.put("overlayStart", startTime.doubleValue());
        ctx.put("hasOverlayStart", !startTime.equals(ZERO));
        ctx.put("overlayEnd", endTime.doubleValue());
        ctx.put("hasAudio", ((JAssetJs) source.toJs()).hasAudio || ((JAssetJs) overlaySource.toJs()).hasAudio);
        b.append("enable=between(t\\,")
                .append(startTime.doubleValue())
                .append("\\,")
                .append(endTime.doubleValue())
                .append(")");

        if (overlay.hasX() && overlay.hasY()) {
            b.append(":x=").append(overlay.getX(ctx, js).intValue())
             .append(":y=").append(overlay.getY(ctx, js).intValue());
        }

        return b.toString();
    }

}
