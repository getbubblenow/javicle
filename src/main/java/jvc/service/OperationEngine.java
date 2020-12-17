package jvc.service;

import jvc.model.operation.JOperation;
import lombok.Getter;

public class OperationEngine {

    private final Toolbox toolbox;
    private final AssetManager assetManager;
    @Getter private final boolean noExec;

    public OperationEngine(Toolbox toolbox, AssetManager assetManager, boolean noExec) {
        this.toolbox =  toolbox;
        this.assetManager = assetManager;
        this.noExec = noExec;
    }

    public void perform(JOperation op) {
        op.setNoExec(noExec).getExec().operate(op, toolbox, assetManager);
    }
}
