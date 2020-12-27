package jvc.model;

import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public interface JsObjectView {

    Object toJs();

    static boolean isJsObjectCollection(Object value) {
        return (value instanceof Collection)
                && !empty(value)
                && (((Collection) value).iterator().next() instanceof JsObjectView);
    }

    static <T extends JsObjectView> Collection<T> toJs(Collection<T> values) {
        if (empty(values)) {
            return emptyList();
        } else {
            return values.stream()
                    .map(v -> (T) v.toJs())
                    .collect(Collectors.toList());
        }
    }

}
