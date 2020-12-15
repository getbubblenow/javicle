package jvcl.operation;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
import jvcl.model.JOperation;
import jvcl.service.AssetManager;
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
public class OverlayOperation extends JOperation {

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
