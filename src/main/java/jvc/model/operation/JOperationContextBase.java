package jvc.model.operation;

import jvc.model.JAsset;
import jvc.model.JStreamType;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor
public class JOperationContextBase {

    public JAsset output;
    public JStreamType streamType;
    public AssetManager assetManager;
    public Toolbox toolbox;

}
