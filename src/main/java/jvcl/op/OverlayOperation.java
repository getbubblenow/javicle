package jvcl.op;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
import jvcl.model.JOperation;
import jvcl.service.AssetManager;
import jvcl.service.JOperator;
import jvcl.service.Toolbox;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.JsEngine;
import org.cobbzilla.util.javascript.StandardJsEngine;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static java.math.RoundingMode.HALF_EVEN;
import static jvcl.model.JAsset.json2asset;
import static jvcl.service.Toolbox.getDuration;
import static org.cobbzilla.util.daemon.ZillaRuntime.big;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.system.CommandShell.execScript;

@Slf4j
public class OverlayOperation implements JOperator {

    public static final String OVERLAY_TEMPLATE
            = "{{ffmpeg}} -i {{{source.path}}} -filter_complex \"" +
            "movie={{{overlay.path}}}{{#exists overlayStart}}:seek_point={{overlayStart}}{{/exists}} [ovl]; " +
            "[v:0] setpts=PTS-(STARTPTS+{{#exists offset}}{{offset}}{{else}}0{{/exists}}) [main]; " +
            "[ovl] setpts=PTS-STARTPTS{{#exists width}}, scale={{width}}x{{height}}{{/exists}} ; " +
            "[main][ovl] overlay=shortest=1{{#exists x}}:x={{x}}{{/exists}}{{#exists y}}:y={{y}}{{/exists}} " +
            "\" {{{output.path}}}";

    @Override public void operate(JOperation op, Toolbox toolbox, AssetManager assetManager) {
        final OverlayConfig config = loadConfig(op, OverlayConfig.class);
        final JAsset source = assetManager.resolve(config.getSource());
        final JAsset overlay = assetManager.resolve(config.getOverlay());

        final JAsset output = json2asset(op.getCreates());
        output.mergeFormat(source.getFormat());

        final JFileExtension formatType = output.getFormat().getFileExtension();

        final File defaultOutfile = assetManager.assetPath(op, source, formatType, new Object[]{config});
        final File path = resolveOutputPath(output, defaultOutfile);
        if (path == null) return;
        output.setPath(abs(path));

        final StandardJsEngine js = toolbox.getJs();
        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());
        ctx.put("source", source);
        ctx.put("overlay", overlay);
        ctx.put("output", output);
        ctx.put("offset", config.getOffsetSeconds(ctx, js));
        ctx.put("overlayStart", config.getOverlayStartSeconds(ctx, js));
        if (config.hasOverlayEnd()) ctx.put("overlayEnd", config.getOverlayEndSeconds(ctx, js));
        if (config.hasWidth()) {
            final String width = config.getWidth(ctx, js);
            ctx.put("width", width);
            if (!config.hasHeight()) {
                final int height = big(width).divide(overlay.aspectRatio(), HALF_EVEN).intValue();
                ctx.put("height", height);
            }
        }
        if (config.hasHeight()) {
            final String height = config.getHeight(ctx, js);
            ctx.put("height", height);
            if (!config.hasWidth()) {
                final int width = big(height).multiply(overlay.aspectRatio()).intValue();
                ctx.put("width", width);
            }
        }
        if (config.hasX()) ctx.put("x", config.getX(ctx, js));
        if (config.hasY()) ctx.put("y", config.getY(ctx, js));

        final String script = renderScript(toolbox, ctx, OVERLAY_TEMPLATE);

        log.debug("operate: running script: "+script);
        final String scriptOutput = execScript(script);
        log.debug("operate: command output: "+scriptOutput);
        assetManager.addOperationAsset(output);
    }

    private static class OverlayConfig {
        @Getter @Setter private String source;
        @Getter @Setter private String overlay;

        private String eval(String val, Map<String, Object> ctx, JsEngine js) {
            final Map<String, Object> jsCtx = Toolbox.jsContext(ctx);
            final Object result = js.evaluate(val, jsCtx);
            return result == null ? null : result.toString();
        }

        @Getter @Setter private String offset;
        public BigDecimal getOffsetSeconds (Map<String, Object> ctx, JsEngine js) {
            return empty(offset) ? BigDecimal.ZERO : getDuration(eval(offset, ctx, js));
        }

        @Getter @Setter private String overlayStart;
        public BigDecimal getOverlayStartSeconds (Map<String, Object> ctx, JsEngine js) {
            return empty(overlayStart) ? BigDecimal.ZERO : getDuration(eval(overlayStart, ctx, js));
        }

        @Getter @Setter private String overlayEnd;
        public boolean hasOverlayEnd () { return !empty(overlayEnd); }
        public BigDecimal getOverlayEndSeconds (Map<String, Object> ctx, JsEngine js) {
            return getDuration(eval(overlayEnd, ctx, js));
        }

        @Getter @Setter private String width;
        public boolean hasWidth () { return !empty(width); }
        public String getWidth(Map<String, Object> ctx, JsEngine js) { return eval(width, ctx, js); }

        @Getter @Setter private String height;
        public boolean hasHeight () { return !empty(height); }
        public String getHeight(Map<String, Object> ctx, JsEngine js) { return eval(height, ctx, js); }

        @Getter @Setter private String x;
        public boolean hasX () { return !empty(x); }
        public String getX(Map<String, Object> ctx, JsEngine js) { return eval(x, ctx, js); }

        @Getter @Setter private String y;
        public boolean hasY () { return !empty(y); }
        public String getY(Map<String, Object> ctx, JsEngine js) { return eval(y, ctx, js); }

        @Getter @Setter private String outputWidth;
        public boolean hasOutputWidth () { return !empty(outputWidth); }
        public String getOutputWidth(Map<String, Object> ctx, JsEngine js) { return eval(outputWidth, ctx, js); }

        @Getter @Setter private String outputHeight;
        public boolean hasOutputHeight () { return !empty(outputHeight); }
        public String getOutputHeight(Map<String, Object> ctx, JsEngine js) { return eval(outputHeight, ctx, js); }
    }
}
