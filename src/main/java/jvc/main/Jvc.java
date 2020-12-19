package jvc.main;


import jvc.model.JSpec;
import jvc.model.operation.JValidationResult;
import jvc.service.AssetManager;
import jvc.service.JOperationValidationFailure;
import jvc.service.JvcEngine;
import jvc.service.Toolbox;
import org.cobbzilla.util.main.BaseMain;

import java.util.List;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

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

        try {
            opEngine.runSpec(spec);
            printCompleted(opEngine);

        } catch (JOperationValidationFailure e) {
            printCompleted(opEngine);
            final List<JValidationResult> results = e.getResults();
            err(">>> jvc: operation (index="+e.getOperation().execIndex()+") failed: ");
            for (JValidationResult r : results) {
                err(r.toString());
            }
        }
    }

    private void printCompleted(JvcEngine opEngine) {
        final int opCount = opEngine.getCompleted().size();
        err(">>> jvc: completed " + opCount + " operation" + (opCount > 1 ? "s" : ""));
    }

}
