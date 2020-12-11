package jvcl.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@NoArgsConstructor @Accessors(chain=true)
public class JSpec {

    @Getter @Setter private JAsset[] assets;
    @Getter @Setter private JOperation[] operations;
    @Getter @Setter private JArtifact[] artifacts;

}
