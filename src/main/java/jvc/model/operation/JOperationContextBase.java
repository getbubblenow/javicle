package jvc.model.operation;

import jvc.model.JAsset;
import jvc.model.JFileExtension;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor
public class JOperationContextBase {

    public JAsset output;
    public JFileExtension formatType;
    public AssetManager assetManager;
    public Toolbox toolbox;

}
