package jvcl.operation;

import jvcl.model.operation.JSingleSourceOperation;
import lombok.Getter;
import lombok.Setter;

public class AddSilenceOperation extends JSingleSourceOperation {

    private static final String DEFAULT_CHANNEL_LAYOUT = "stereo";
    private static final Integer DEFAULT_SAMPLING_RATE = 48000;

    @Getter @Setter private String channelLayout = DEFAULT_CHANNEL_LAYOUT;
    @Getter @Setter private Integer samplingRate = DEFAULT_SAMPLING_RATE;

}
