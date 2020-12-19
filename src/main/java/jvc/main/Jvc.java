package jvc.main;


import jvc.model.JSpec;
import jvc.service.AssetManager;
import jvc.service.JvcEngine;
import jvc.service.Toolbox;
import org.cobbzilla.util.main.BaseMain;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.json.JsonUtil.json;

public class Jvc extends BaseMain<JvcOptions> {

    public static void main (String[] args) { main(Jvc.class, args); }

    @Override protected void run() throws Exception {
        final JvcOptions options = getOptions();
        final JSpec spec = options.getSpec();
        final boolean noExec = options.isNoExec();

        if (empty(spec.getAssets())) {
            err(">>> jvc: no assets defined in spec");
            return;
        }
        if (empty(spec.getOperations())) {
            err(">>> jvc: no operations defined in spec");
            return;
        }

        final Toolbox toolbox = Toolbox.DEFAULT_TOOLBOX;
        final AssetManager assetManager = new AssetManager(toolbox, getOptions().scratchDir());
        final JvcEngine opEngine = new JvcEngine(toolbox, assetManager, noExec);

        opEngine.runSpec(spec);

        final int opCount = spec.getOperations().length;
        err(">>> jvc: completed " + opCount + " operation"+(opCount>1?"s":""));
        out(json(assetManager.getAssets()));
    }

}
