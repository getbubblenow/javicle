package jvcl.service;

import jvcl.model.JOperation;
import org.cobbzilla.util.handlebars.HandlebarsUtil;

import java.util.Map;

import static org.cobbzilla.util.json.JsonUtil.json;

public interface JOperator {

    void operate(JOperation op, Toolbox toolbox, AssetManager assetManager);

    default <T> T loadConfig(JOperation op, Class<T> configClass) {
        return json(json(op.getPerform()), configClass);
    }

    default String renderScript(Toolbox toolbox, Map<String, Object> ctx, String template) {
        return HandlebarsUtil.apply(toolbox.getHandlebars(), template, ctx);
    }

}
