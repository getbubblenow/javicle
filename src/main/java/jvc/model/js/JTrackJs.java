package jvc.model.js;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@NoArgsConstructor @AllArgsConstructor @Accessors(chain=true)
public class JTrackJs {

    public static final JTrackJs[] EMPTY_TRACKS = new JTrackJs[0];

    @Getter @Setter public String type;

}
