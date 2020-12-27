package jvc.model.operation;

import jvc.model.JAsset;
import jvc.model.JStreamType;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor
public class JSingleOperationContext extends JOperationContextBase {
    
    public JAsset source;

    public JSingleOperationContext(JAsset source,
                                   JAsset output,
                                   JStreamType streamType,
                                   AssetManager assetManager,
                                   Toolbox toolbox) {
        super(output, streamType, assetManager, toolbox);
        this.source = source;
    }

}
