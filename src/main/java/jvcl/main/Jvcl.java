package jvcl.main;


import jvcl.model.JSpec;
import jvcl.service.AssetManager;
import jvcl.service.OperationEngine;
import jvcl.service.Toolbox;
import org.cobbzilla.util.main.BaseMain;

import java.util.Arrays;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.json.JsonUtil.json;

public class Jvcl extends BaseMain<JvclOptions> {

    public static void main (String[] args) { main(Jvcl.class, args); }

    @Override protected void run() throws Exception {
        final JvclOptions options = getOptions();
        final JSpec spec = options.getSpec();

        if (empty(spec.getAssets())) {
            err(">>> jvcl: no assets defined in spec");
            return;
        }
        if (empty(spec.getOperations())) {
            err(">>> jvcl: no operations defined in spec");
            return;
        }

        final Toolbox toolbox = Toolbox.DEFAULT_TOOLBOX;

        final AssetManager assetManager = new AssetManager(toolbox, getOptions().getScratchDir());
        Arrays.stream(spec.getAssets()).forEach(assetManager::defineAsset);

        final OperationEngine opEngine = new OperationEngine(toolbox, assetManager);
        Arrays.stream(spec.getOperations()).forEach(opEngine::perform);

        final int opCount = spec.getOperations().length;
        err(">>> jvcl: completed " + opCount + " operation"+(opCount>1?"s":""));
        out(json(assetManager.getAssets()));
    }

}
