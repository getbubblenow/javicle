package jvcl.model.operation;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor
public class JSingleOperationContext extends JOperationContextBase {
    
    public JAsset source;

    public JSingleOperationContext(JAsset source, JAsset output, JFileExtension formatType) {
        super(output, formatType);
        this.source = source;
    }

}
