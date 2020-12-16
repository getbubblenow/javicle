package jvcl.operation;

import jvcl.model.operation.JSingleSourceOperation;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.javascript.JsEngine;

import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class LetterboxOperation extends JSingleSourceOperation implements HasWidthAndHeight {

    @Getter @Setter private String width;
    @Getter @Setter private String height;

    @Getter @Setter private String color;
    public boolean hasColor () { return !empty(color); }

    public String shortString(Map<String, Object> ctx, JsEngine js) {
        return "letterbox_"+color+"_"+getWidth(ctx, js)+"x"+getHeight(ctx, js);
    }

}
