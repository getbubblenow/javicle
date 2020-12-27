package jvc.service;

import jvc.model.JSpec;
import jvc.model.operation.JOperation;
import jvc.model.operation.JValidationResult;
import jvc.operation.exec.ExecBase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.JsEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class JvcEngine {

    private final Toolbox toolbox;
    private final AssetManager assetManager;
    @Getter private final boolean noExec;

    @Getter private final List<JOperation> completed = new ArrayList<>();

    public JvcEngine(Toolbox toolbox, AssetManager assetManager, boolean noExec) {
        this.toolbox =  toolbox;
        this.assetManager = assetManager;
        this.noExec = noExec;
    }

    public void runSpec(JSpec spec) {
        Arrays.stream(spec.getAssets()).forEach(assetManager::defineAsset);
        Arrays.stream(spec.getOperations()).forEach(op -> runOp(spec, op));
    }

    private void runOp(JSpec spec, JOperation op) {

        final ExecBase<JOperation> exec = op
                .setExecIndex(completed.size())
                .setNoExec(noExec)
                .getExec(spec);

        final Map<String, Object> ctx = exec.operate(op, toolbox, assetManager);
        if (ctx == null) {
            log.debug("runOp("+op.getOperation()+"): noop");
            return;
        }

        if (op.hasValidate()) {
            final JsEngine js = toolbox.getJs();
            final List<JValidationResult> results = Arrays.stream(op.getValidate())
                    .map(v -> v.eval(ctx, js))
                    .collect(Collectors.toList());
            if (results.stream().anyMatch(JValidationResult::failed)) {
                throw new JOperationValidationFailure(op, results);
            }
        }

        completed.add(op);
    }

}
