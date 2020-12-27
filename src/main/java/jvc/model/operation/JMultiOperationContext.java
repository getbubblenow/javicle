package jvc.model.operation;

import jvc.model.JAsset;
import jvc.model.JStreamType;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class JMultiOperationContext extends JOperationContextBase {

    public List<JAsset> sources;

    public JMultiOperationContext(List<JAsset> sources,
                                  JAsset output,
                                  JStreamType streamType,
                                  AssetManager assetManager,
                                  Toolbox toolbox) {
        super(output, streamType, assetManager, toolbox);
        this.sources = sources;
    }
}
