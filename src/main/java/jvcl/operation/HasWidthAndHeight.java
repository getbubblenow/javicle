package jvcl.operation;

import jvcl.model.JAsset;
import org.cobbzilla.util.javascript.JsEngine;
import org.cobbzilla.util.javascript.StandardJsEngine;

import java.math.BigDecimal;
import java.util.Map;

import static jvcl.service.Toolbox.divideBig;
import static jvcl.service.Toolbox.evalBig;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public interface HasWidthAndHeight {

    String getWidth ();
    default boolean hasWidth () { return !empty(getWidth()); }

    String getHeight ();
    default boolean hasHeight () { return !empty(getHeight()); }

    default BigDecimal getWidth(Map<String, Object> ctx, JsEngine js) { return evalBig(getWidth(), ctx, js); }
    default BigDecimal getHeight(Map<String, Object> ctx, JsEngine js) { return evalBig(getHeight(), ctx, js); }

    default void setProportionalWidthAndHeight(Map<String, Object> ctx,
                                               StandardJsEngine js,
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
