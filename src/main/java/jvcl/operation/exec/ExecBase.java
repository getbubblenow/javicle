package jvcl.operation.exec;

import jvcl.model.JAsset;
import jvcl.model.JOperation;
import jvcl.service.AssetManager;
import jvcl.service.Toolbox;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.handlebars.HandlebarsUtil;

import java.io.File;
import java.util.Map;

import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.basename;

@Slf4j
public abstract class ExecBase<OP extends JOperation> {

    public abstract void operate(OP operation, Toolbox toolbox, AssetManager assetManager);

    protected String renderScript(Toolbox toolbox, Map<String, Object> ctx, String template) {
        return HandlebarsUtil.apply(toolbox.getHandlebars(), template, ctx);
    }

    protected File resolveOutputPath(JAsset output, File defaultOutfile) {
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
