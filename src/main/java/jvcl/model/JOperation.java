package jvcl.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.cobbzilla.util.daemon.ZillaRuntime.hashOf;
import static org.cobbzilla.util.json.JsonUtil.json;

@NoArgsConstructor @Accessors(chain=true)
public class JOperation {

    @Getter @Setter private JOperationType operation;
    @Getter @Setter private JsonNode creates;
    @Getter @Setter private JsonNode perform;

    public String hash(JAsset[] sources) { return hash(sources, null); }

    public String hash(JAsset[] sources, Object[] args) {
        return hashOf(getOperation(), json(creates), json(perform), json(sources), args);
    }

}
