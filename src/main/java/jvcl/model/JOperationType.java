package jvcl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jvcl.op.ConcatOperation;
import jvcl.op.SplitOperation;
import jvcl.op.TrimOperation;
import jvcl.service.AssetManager;
import jvcl.service.JOperator;
import jvcl.service.Toolbox;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum JOperationType {

    concat (new ConcatOperation()),
    split (new SplitOperation()),
    trim (new TrimOperation()),
    overlay (null),
    ken_burns (null),
    letterbox (null),
    split_silence (null);

    @JsonCreator public static JOperationType fromString(String v) { return valueOf(v.toLowerCase().replace("-", "_")); }

    private final JOperator operator;

    public void perform(JOperation op, Toolbox toolbox, AssetManager assetManager) {
        operator.operate(op, toolbox, assetManager);
    }

}
