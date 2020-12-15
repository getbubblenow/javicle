package jvcl.operation.exec;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
import jvcl.operation.OverlayOperation;
import jvcl.service.AssetManager;
import jvcl.service.Toolbox;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.StandardJsEngine;

import java.io.File;
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
            = "{{ffmpeg}} -i {{{source.path}}} -filter_complex \"" +
            "movie={{{overlay.path}}}{{#exists overlayStart}}:seek_point={{overlayStart}}{{/exists}} [ovl]; " +
            "[v:0] setpts=PTS-(STARTPTS+{{#exists offset}}{{offset}}{{else}}0{{/exists}}) [main]; " +
            "[ovl] setpts=PTS-STARTPTS{{#exists width}}, scale={{width}}x{{height}}{{/exists}} ; " +
            "[main][ovl] overlay=shortest=1{{#exists x}}:x={{x}}{{/exists}}{{#exists y}}:y={{y}}{{/exists}} " +
            "\" {{{output.path}}}";

    @Override public void operate(OverlayOperation op, Toolbox toolbox, AssetManager assetManager) {
        final JAsset source = assetManager.resolve(op.getSource());
        final JAsset overlay = assetManager.resolve(op.getOverlay());

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
        ctx.put("overlay", overlay);
        ctx.put("output", output);
        ctx.put("offset", op.getOffsetSeconds(ctx, js));
        ctx.put("overlayStart", op.getOverlayStartSeconds(ctx, js));
        if (op.hasOverlayEnd()) ctx.put("overlayEnd", op.getOverlayEndSeconds(ctx, js));
        if (op.hasWidth()) {
            final String width = op.getWidth(ctx, js);
            ctx.put("width", width);
            if (!op.hasHeight()) {
                final int height = big(width).divide(overlay.aspectRatio(), HALF_EVEN).intValue();
                ctx.put("height", height);
            }
        }
        if (op.hasHeight()) {
            final String height = op.getHeight(ctx, js);
            ctx.put("height", height);
            if (!op.hasWidth()) {
                final int width = big(height).multiply(overlay.aspectRatio()).intValue();
                ctx.put("width", width);
            }
        }
        if (op.hasX()) ctx.put("x", op.getX(ctx, js));
        if (op.hasY()) ctx.put("y", op.getY(ctx, js));

        final String script = renderScript(toolbox, ctx, OVERLAY_TEMPLATE);

        log.debug("operate: running script: "+script);
        final String scriptOutput = execScript(script);
        log.debug("operate: command output: "+scriptOutput);
        assetManager.addOperationAsset(output);
    }

}
