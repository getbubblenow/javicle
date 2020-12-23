package jvc.operation;

import jvc.model.operation.JSingleSourceOperation;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.javascript.JsEngine;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static jvc.service.Toolbox.evalBig;
import static org.cobbzilla.util.daemon.ZillaRuntime.big;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class KenBurnsOperation extends JSingleSourceOperation
        implements HasWidthAndHeight, HasStartAndEnd {

    public static final BigDecimal DEFAULT_FPS = big(25);
    public static final BigDecimal DEFAULT_UPSCALE = big(8);

    public static final String DEFAULT_X = "source.width / 2";
    public static final String DEFAULT_Y = "source.height / 2";

    @Getter @Setter private String zoom;
    public BigDecimal getZoom(Map<String, Object> ctx, JsEngine js) {
        return evalBig(zoom, ctx, js, ONE);
    }

    @Getter @Setter private String duration;
    public boolean hasDuration () { return !empty(duration); }
    public BigDecimal getDuration(Map<String, Object> ctx, JsEngine js) {
        return evalBig(duration, ctx, js);
    }

    @Getter @Setter private String start;
    @Getter @Setter private String end;

    @Getter @Setter private String x;
    public BigDecimal getX(Map<String, Object> ctx, JsEngine js) {
        final BigDecimal defaultX = evalBig(DEFAULT_X, ctx, js);
        return evalBig(x, ctx, js, defaultX);
    }

    @Getter @Setter private String y;
    public BigDecimal getY(Map<String, Object> ctx, JsEngine js) {
        final BigDecimal defaultY = evalBig(DEFAULT_Y, ctx, js);
        return evalBig(y, ctx, js, defaultY);
    }

    @Getter @Setter private String width;
    @Getter @Setter private String height;

    @Getter @Setter private String fps;
    public boolean hasFps () { return !empty(fps); }
    public BigDecimal getFps(Map<String, Object> ctx, JsEngine js) { return evalBig(fps, ctx, js, DEFAULT_FPS); }

    @Getter @Setter private String upscale;
    public boolean hasUpscale () { return !empty(upscale); }
    public BigDecimal getUpscale(Map<String, Object> ctx, JsEngine js) { return evalBig(fps, ctx, js, DEFAULT_UPSCALE); }

}
