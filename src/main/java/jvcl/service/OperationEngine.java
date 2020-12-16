package jvcl.service;

import jvcl.model.operation.JOperation;

public class OperationEngine {

    private final Toolbox toolbox;
    private final AssetManager assetManager;

    public OperationEngine(Toolbox toolbox, AssetManager assetManager) {
        this.toolbox =  toolbox;
        this.assetManager = assetManager;
    }

    public void perform(JOperation op) {
        op.getExec().operate(op, toolbox, assetManager);
    }
}
