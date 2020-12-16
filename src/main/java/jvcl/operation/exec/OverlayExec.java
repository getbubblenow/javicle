package jvcl.operation.exec;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
import jvcl.model.operation.JSingleOperationContext;
import jvcl.operation.OverlayOperation;
import jvcl.service.AssetManager;
import jvcl.service.Toolbox;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.StandardJsEngine;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static java.math.RoundingMode.HALF_EVEN;
import static org.cobbzilla.util.daemon.ZillaRuntime.big;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.system.CommandShell.execScript;

@Slf4j
public class OverlayExec extends ExecBase<OverlayOperation> {

    public static final String OVERLAY_TEMPLATE
            = "ffmpeg -i {{{source.path}}} -i {{{overlay.path}}} -filter_complex \""
            + "[1:v] setpts=PTS-STARTPTS+(1/TB){{#exists width}}, scale={{width}}x{{height}}{{/exists}} [1v]; "
            + "[0:v][1v] overlay={{{overlayFilterConfig}}} "
            + "\" -y {{{output.path}}}";

    @Override public void operate(OverlayOperation op, Toolbox toolbox, AssetManager assetManager) {

        final JSingleOperationContext opCtx = op.getSingleInputContext(assetManager);
        final JAsset source = opCtx.source;
        final JAsset output = opCtx.output;
        final JFileExtension formatType = opCtx.formatType;

        final OverlayOperation.OverlayConfig overlay = op.getOverlay();
        final JAsset overlaySource = assetManager.resolve(overlay.getSource());

        final File defaultOutfile = assetManager.assetPath(op, source, formatType);
        final File path = resolveOutputPath(output, defaultOutfile);
        if (path == null) return;
        output.setPath(abs(path));

        final StandardJsEngine js = toolbox.getJs();
        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());
        ctx.put("source", source);
        ctx.put("overlay", overlaySource);

        ctx.put("mainStart", op.getStartTime(ctx, js));
        ctx.put("mainEnd", op.getEndTime(ctx, js));

        final String overlayFilter = buildOverlayFilter(op, source, overlaySource, overlay, ctx, js);
        ctx.put("overlayFilterConfig", overlayFilter);
        ctx.put("output", output);

        if (overlay.hasWidth()) {
            final String width = overlay.getWidth(ctx, js);
            ctx.put("width", width);
            if (!overlay.hasHeight()) {
                final int height = big(width).divide(overlay.aspectRatio(), HALF_EVEN).intValue();
                ctx.put("height", height);
            }
        }
        if (overlay.hasHeight()) {
            final String height = overlay.getHeight(ctx, js);
            ctx.put("height", height);
            if (!overlay.hasWidth()) {
                final int width = big(height).multiply(overlay.aspectRatio()).intValue();
                ctx.put("width", width);
            }
        }

        final String script = renderScript(toolbox, ctx, OVERLAY_TEMPLATE);

        log.debug("operate: running script: "+script);
        final String scriptOutput = execScript(script);
        log.debug("operate: command output: "+scriptOutput);
        assetManager.addOperationAsset(output);
    }

    private String buildOverlayFilter(OverlayOperation op,
                                      JAsset source,
                                      JAsset overlaySource,
                                      OverlayOperation.OverlayConfig overlay,
                                      Map<String, Object> ctx,
                                      StandardJsEngine js) {
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
