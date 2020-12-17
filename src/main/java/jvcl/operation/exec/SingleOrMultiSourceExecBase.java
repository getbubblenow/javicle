package jvcl.operation.exec;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
import jvcl.model.operation.JOperation;
import jvcl.service.AssetManager;
import jvcl.service.Toolbox;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.io.FileUtil.*;

@Slf4j
public abstract class SingleOrMultiSourceExecBase<OP extends JOperation> extends ExecBase<OP> {

    protected void operate(OP op, Toolbox toolbox, AssetManager assetManager, JAsset source, JAsset output, JFileExtension formatType, Map<String, Object> ctx) {
        if (source.hasList()) {
            if (output.hasDest()) {
                if (!output.destIsDirectory()) die("operate: dest is not a directory: "+output.getDest());
            }
            assetManager.addOperationArrayAsset(output);
            for (JAsset asset : source.getList()) {
                final JAsset subOutput = new JAsset(output);
                final File defaultOutfile = assetManager.assetPath(op, asset, formatType);
                final File outfile;
                if (output.hasDest()) {
                    outfile = new File(output.destDirectory(), basename(appendToFileNameBeforeExt(asset.getPath(), "_"+op.shortString())));
                    if (outfile.exists()) {
                        log.info("operate: dest exists: "+abs(outfile));
                        return;
                    }
                } else {
                    outfile = defaultOutfile;
                }
                subOutput.setPath(abs(outfile));
                process(ctx, op, asset, output, subOutput, toolbox, assetManager);
                assetManager.addOperationAssetSlice(output, subOutput);
            }
        } else {
            final File defaultOutfile = assetManager.assetPath(op, source, formatType);
            final File path = resolveOutputPath(output, defaultOutfile);
            if (path == null) return;
            output.setPath(abs(path));
            process(ctx, op, source, output, output, toolbox, assetManager);
            assetManager.addOperationAsset(output);
        }
    }

    protected abstract void process(Map<String, Object> ctx,
                                    OP op,
                                    JAsset source,
                                    JAsset output,
                                    JAsset asset,
                                    Toolbox toolbox,
                                    AssetManager assetManager);

}
