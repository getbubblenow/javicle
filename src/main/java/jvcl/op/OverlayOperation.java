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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static jvcl.model.JAsset.json2asset;
import static jvcl.service.Toolbox.getDuration;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.system.CommandShell.execScript;

@Slf4j
public class OverlayOperation implements JOperator {

    public static final String OVERLAY_TEMPLATE
            = "{{ffmpeg}} ";

    @Override public void operate(JOperation op, Toolbox toolbox, AssetManager assetManager) {
        final OverlayConfig config = loadConfig(op, OverlayConfig.class);
        final JAsset source = assetManager.resolve(config.getSource());
        final JAsset overlay = assetManager.resolve(config.getOverlay());

        final JAsset output = json2asset(op.getCreates());
        output.mergeFormat(source.getFormat());

        final JFileExtension formatType = output.getFormat().getFileExtension();

        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("source", source);
        ctx.put("overlay", overlay);
        ctx.put("output", output);
        ctx.put("offset", config.getOffsetSeconds());
        ctx.put("overlayStart", config.getOverlayStartSeconds());
        if (config.hasOverlayEnd()) ctx.put("overlayEnd", config.getOverlayEndSeconds());
        if (config.hasWidth()) ctx.put("width", config.getWidth());
        if (config.hasHeight()) ctx.put("height", config.getHeight());

        final String script = renderScript(toolbox, ctx, OVERLAY_TEMPLATE);

        log.debug("operate: running script: "+script);
        final String scriptOutput = execScript(script);
        log.debug("operate: command output: "+scriptOutput);
        assetManager.addOperationAsset(output);
    }

    private static class OverlayConfig {
        @Getter @Setter private String source;
        @Getter @Setter private String overlay;

        @Getter @Setter private String offset;
        public BigDecimal getOffsetSeconds () { return empty(offset) ? BigDecimal.ZERO : getDuration(offset); }

        @Getter @Setter private String overlayStart;
        public BigDecimal getOverlayStartSeconds () { return empty(overlayStart) ? BigDecimal.ZERO : getDuration(overlayStart); }

        @Getter @Setter private String overlayEnd;
        public boolean hasOverlayEnd () { return !empty(overlayEnd); }
        public BigDecimal getOverlayEndSeconds () { return getDuration(overlayEnd); }

        @Getter @Setter private String width;
        public boolean hasWidth () { return !empty(width); }

        @Getter @Setter private String height;
        public boolean hasHeight () { return !empty(height); }

        @Getter @Setter private String x;
        public boolean hasX () { return !empty(x); }

        @Getter @Setter private String y;
        public boolean hasY () { return !empty(y); }

        @Getter @Setter private String outputWidth;
        public boolean hasOutputWidth () { return !empty(outputWidth); }

        @Getter @Setter private String outputHeight;
        public boolean hasOutputHeight () { return !empty(outputHeight); }
    }
}
