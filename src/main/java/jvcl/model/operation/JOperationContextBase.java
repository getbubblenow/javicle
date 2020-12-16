package jvcl.model.operation;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor
public class JOperationContextBase {

    public JAsset output;
    public JFileExtension formatType;

}
