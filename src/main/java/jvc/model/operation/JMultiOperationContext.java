package jvc.model.operation;

import jvc.model.JAsset;
import jvc.model.JFileExtension;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class JMultiOperationContext extends JOperationContextBase {

    public List<JAsset> sources;

    public JMultiOperationContext(List<JAsset> sources,
                                  JAsset output,
                                  JFileExtension formatType,
                                  AssetManager assetManager,
                                  Toolbox toolbox) {
        super(output, formatType, assetManager, toolbox);
        this.sources = sources;
    }
}
