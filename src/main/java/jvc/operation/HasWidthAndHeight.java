package jvc.operation;

import jvc.model.JAsset;
import org.cobbzilla.util.javascript.JsEngine;

import java.math.BigDecimal;
import java.util.Map;

import static jvc.service.Toolbox.divideBig;
import static jvc.service.Toolbox.evalBig;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

// todo: add support for default height/width. implement defaults in subclasses.
public interface HasWidthAndHeight {

    String DEFAULT_WIDTH = "source.width";
    String DEFAULT_HEIGHT = "source.height";

    String getWidth ();
    default boolean hasWidth () { return !empty(getWidth()); }

    String getHeight ();
    default boolean hasHeight () { return !empty(getHeight()); }

    default BigDecimal defaultWidth(Map<String, Object> ctx, JsEngine js) { return evalBig(DEFAULT_WIDTH, ctx, js); }
    default BigDecimal defaultHeight(Map<String, Object> ctx, JsEngine js) { return evalBig(DEFAULT_HEIGHT, ctx, js); }

    default BigDecimal getWidth(Map<String, Object> ctx, JsEngine js) { return evalBig(getWidth(), ctx, js, defaultWidth(ctx, js)); }
    default BigDecimal getHeight(Map<String, Object> ctx, JsEngine js) { return evalBig(getHeight(), ctx, js, defaultHeight(ctx, js)); }

    default void setProportionalWidthAndHeight(Map<String, Object> ctx,
                                               JsEngine js,
                                               JAsset asset) {
        if (hasWidth()) {
            final BigDecimal width = getWidth(ctx, js);
            ctx.put("width", width.intValue());
            if (!hasHeight()) {
                final BigDecimal aspectRatio = asset.aspectRatio();
                final int height = divideBig(width, aspectRatio).intValue();
                ctx.put("height", height);
            }
        }
        if (hasHeight()) {
            final BigDecimal height = getHeight(ctx, js);
            ctx.put("height", height.intValue());
            if (!hasWidth()) {
                final BigDecimal aspectRatio = asset.aspectRatio();
                final int width = height.multiply(aspectRatio).intValue();
                ctx.put("width", width);
            }
        }
    }

}
