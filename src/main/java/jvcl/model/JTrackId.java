package jvcl.model;

import com.fasterxml.jackson.databind.JsonNode;
import jvcl.model.info.JTrackType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.json.JsonUtil.json;

@Accessors(chain=true)
public class JTrackId {

    @Getter @Setter private JTrackType type;
    @Getter @Setter private Integer number;
    public boolean hasNumber () { return number != null; }

    public static JTrackId createTrackId (JsonNode val) {
        if (val == null) return die("createTrackId: constructor val was null");
        if (val.isObject()) {
            return json(json(val), JTrackId.class);
        } else {
            final JTrackType trackType;
            try {
                trackType = JTrackType.fromString(val.asText());
            } catch (Exception e) {
                return die("createTrackId: not a valid track type: "+val.asText());
            }
            return new JTrackId().setType(trackType);
        }
    }

}
