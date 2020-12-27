package jvc.model;

import jvc.model.js.JAssetJs;

import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.cobbzilla.util.daemon.ZillaRuntime.*;

public interface JsObjectView {

    Object toJs();

    static boolean isJsObjectCollection(Object value) {
        return (value instanceof Collection)
                && !empty(value)
                && (((Collection) value).iterator().next() instanceof JsObjectView);
    }

    static <T extends JsObjectView> Collection<JAssetJs> toJs(Collection<T> values) {
        if (empty(values)) {
            return emptyList();
        } else {
            try {
                return values.stream()
                        .map(v -> (JAssetJs) v.toJs())
                        .collect(Collectors.toList());
            } catch (Exception e) {
                return die("toJs: "+shortError(e));
            }
        }
    }

}
