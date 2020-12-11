package jvcl.service;

import jvcl.model.JOperation;

public interface JOperator {

    void operate(JOperation op, Toolbox toolbox, AssetManager assetManager);

}
