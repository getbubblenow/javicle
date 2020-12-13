package jvcl.service;

import jvcl.model.JAsset;
import jvcl.model.JOperation;
import org.cobbzilla.util.handlebars.HandlebarsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.basename;
import static org.cobbzilla.util.json.JsonUtil.json;

public interface JOperator {

    Logger log = LoggerFactory.getLogger(JOperator.class);

    void operate(JOperation op, Toolbox toolbox, AssetManager assetManager);

    default <T> T loadConfig(JOperation op, Class<T> configClass) {
        return json(json(op.getPerform()), configClass);
    }

    default String renderScript(Toolbox toolbox, Map<String, Object> ctx, String template) {
        return HandlebarsUtil.apply(toolbox.getHandlebars(), template, ctx);
    }

    default File resolveOutputPath(JAsset output, File defaultOutfile) {
        if (output.hasDest()) {
            if (output.destExists() && !output.destIsDirectory()) {
                log.info("resolveOutputPath: dest exists: " + output.getDest());
                return null;
            } else if (output.destIsDirectory()) {
                return new File(output.destDirectory(), basename(abs(defaultOutfile)));
            } else {
                return new File(output.destPath());
            }
        } else {
            return defaultOutfile;
        }
    }


}
