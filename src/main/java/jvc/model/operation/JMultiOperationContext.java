package jvc.model.operation;

import jvc.model.JAsset;
import jvc.model.JFileExtension;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class JMultiOperationContext extends JOperationContextBase {

    public List<JAsset> sources;

    public JMultiOperationContext(List<JAsset> sources, JAsset output, JFileExtension formatType) {
        super(output, formatType);
        this.sources = sources;
    }
}
