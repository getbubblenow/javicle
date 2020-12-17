package jvc.operation;

import jvc.model.operation.JSingleSourceOperation;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.javascript.JsEngine;

import java.math.BigDecimal;
import java.util.Map;

import static jvc.service.Toolbox.evalBig;
import static org.cobbzilla.util.daemon.ZillaRuntime.big;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class KenBurnsOperation extends JSingleSourceOperation
        implements HasWidthAndHeight, HasStartAndEnd {

    public static final BigDecimal DEFAULT_FPS = big(25);
    public static final BigDecimal DEFAULT_UPSCALE = big(8);

    @Getter @Setter private String zoom;
    public BigDecimal getZoom(Map<String, Object> ctx, JsEngine js) {
        return evalBig(zoom, ctx, js);
    }

    @Getter @Setter private String duration;
    public BigDecimal getDuration(Map<String, Object> ctx, JsEngine js) {
        return evalBig(duration, ctx, js);
    }

    @Getter @Setter private String start;
    @Getter @Setter private String end;

    @Getter @Setter private String x;
    public boolean hasX () { return !empty(x); }
    public BigDecimal getX(Map<String, Object> ctx, JsEngine js) { return evalBig(x, ctx, js); }

    @Getter @Setter private String y;
    public boolean hasY () { return !empty(y); }
    public BigDecimal getY(Map<String, Object> ctx, JsEngine js) { return evalBig(y, ctx, js); }

    @Getter @Setter private String width;
    @Getter @Setter private String height;

    @Getter @Setter private String fps;
    public boolean hasFps () { return !empty(fps); }
    public BigDecimal getFps(Map<String, Object> ctx, JsEngine js) { return evalBig(fps, ctx, js, DEFAULT_FPS); }

    @Getter @Setter private String upscale;
    public boolean hasUpscale () { return !empty(upscale); }
    public BigDecimal getUpscale(Map<String, Object> ctx, JsEngine js) { return evalBig(fps, ctx, js, DEFAULT_UPSCALE); }

}
