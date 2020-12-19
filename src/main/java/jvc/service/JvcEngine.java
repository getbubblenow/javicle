package jvc.service;

import jvc.model.JSpec;
import jvc.model.operation.JOperation;
import lombok.Getter;

import java.util.Arrays;

public class JvcEngine {

    private final Toolbox toolbox;
    private final AssetManager assetManager;
    @Getter private final boolean noExec;

    public JvcEngine(Toolbox toolbox, AssetManager assetManager, boolean noExec) {
        this.toolbox =  toolbox;
        this.assetManager = assetManager;
        this.noExec = noExec;
    }

    public void runSpec(JSpec spec) {
        Arrays.stream(spec.getAssets()).forEach(assetManager::defineAsset);
        Arrays.stream(spec.getOperations()).forEach(this::runOp);
    }

    private void runOp(JOperation op) {
        op.setNoExec(noExec).getExec().operate(op, toolbox, assetManager);
    }

}
