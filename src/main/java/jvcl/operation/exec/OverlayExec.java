package jvcl.operation.exec;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
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
import static jvcl.model.JAsset.json2asset;
import static org.cobbzilla.util.daemon.ZillaRuntime.big;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.system.CommandShell.execScript;

@Slf4j
public class OverlayExec extends ExecBase<OverlayOperation> {

    public static final String OVERLAY_TEMPLATE
            = "ffmpeg -i {{{source.path}}} -vf \""
            + "        movie={{{overlay.path}}}:seek_point=0 [ovl]; "
            + "        [ovl] setpts=PTS-STARTPTS{{#exists width}}, scale={{width}}x{{height}}{{/exists}} [ovl2] ; "
            + "        [main][ovl2] overlay={{{overlayFilterConfig}}}"
            + "\" {{{output.path}}}";

    @Override public void operate(OverlayOperation op, Toolbox toolbox, AssetManager assetManager) {
        final JAsset source = assetManager.resolve(op.getSource());
        final OverlayOperation.OverlayConfig overlay = op.getOverlay();
        final JAsset overlaySource = assetManager.resolve(overlay.getSource());

        final JAsset output = json2asset(op.getCreates());
        output.mergeFormat(source.getFormat());

        final JFileExtension formatType = output.getFormat().getFileExtension();

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

        final String overlayFilter = buildOverlayFilter(op, source, overlay, ctx, js);
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

    private String buildOverlayFilter(OverlayOperation op, JAsset source, OverlayOperation.OverlayConfig overlay, Map<String, Object> ctx, StandardJsEngine js) {
        // enable=between(t\,15\,20):x=160:y=120
        final StringBuilder b = new StringBuilder();
        final BigDecimal startTime = overlay.getStartTime(ctx, js);
        if (overlay.hasEndTime()) {
            final BigDecimal endTime = overlay.getEndTime(ctx, js);
            b.append("enable=between(t\\,").append(startTime).append("\\,").append(endTime).append(")");
        } else if (startTime.intValue() > 0) {
            b.append("enable=gte(t\\,").append(startTime).append(")");
        }

        if (overlay.hasX() && overlay.hasY()) {
            if (b.length() > 0) b.append(":");
            b.append("x=").append(overlay.getX(ctx, js)).append(":y=").append(overlay.getY(ctx, js));
        }

        return b.toString();
    }

}
