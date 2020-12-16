package jvcl.model.operation;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
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
