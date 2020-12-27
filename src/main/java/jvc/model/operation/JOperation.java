package jvc.model.operation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import jvc.model.JAsset;
import jvc.model.JSpec;
import jvc.operation.exec.ExecBase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static jvc.service.json.JOperationFactory.getOperationExecClass;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.daemon.ZillaRuntime.hashOf;
import static org.cobbzilla.util.json.JsonUtil.json;
import static org.cobbzilla.util.reflect.ReflectionUtil.instantiate;
import static org.cobbzilla.util.security.ShaUtil.sha256_hex;
import static org.cobbzilla.util.string.StringUtil.safeShellArg;

@NoArgsConstructor @Accessors(chain=true) @Slf4j
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "operation",
        visible = true
)
public abstract class JOperation {

    @Getter @Setter private String operation;
    @Getter @Setter private JsonNode creates;
    @Getter @Setter private boolean noExec = false;

    @Getter @Setter private JValidation[] validate;
    public boolean hasValidate() { return !empty(validate); }

    @Getter @Setter private String comment;
    public boolean hasComment () { return !empty(comment); }

    @JsonIgnore @Getter @Setter private Integer execIndex;
    public String execIndex() { return execIndex != null ? ""+execIndex : "(null)"; }

    public String hash(JAsset[] sources) { return hash(sources, null); }

    public String hash(JAsset[] sources, Object[] args) {
        return hashOf(operation, json(this), sources, args);
    }

    private static final Map<String, ExecBase<?>> execMap = new HashMap<>();
    public <OP extends JOperation> ExecBase<OP> getExec(JSpec spec) {
        final String cacheKey = hashOf(getClass().getName(), spec.getVars());
        return (ExecBase<OP>) execMap.computeIfAbsent(cacheKey,
                c -> ((ExecBase<?>) instantiate(getOperationExecClass(getClass()))).setSpec(spec));
    }

    public String shortString() { return safeShellArg(operation+"_"+sha256_hex(json(this))); }

}
