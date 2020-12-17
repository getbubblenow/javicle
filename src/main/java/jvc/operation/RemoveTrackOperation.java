package jvc.operation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jvc.model.JAsset;
import jvc.model.JFileExtension;
import jvc.model.JFormat;
import jvc.model.JTrackId;
import jvc.model.info.JMediaInfo;
import jvc.model.info.JTrack;
import jvc.model.info.JTrackType;
import jvc.model.operation.JSingleSourceOperation;
import lombok.Getter;
import lombok.Setter;

import static jvc.model.JTrackId.createTrackId;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;

public class RemoveTrackOperation extends JSingleSourceOperation {

    @Getter @Setter private JsonNode track;

    @JsonIgnore @Getter(lazy=true) private final JTrackId trackId = initTrackId();
    private JTrackId initTrackId() {
        final JTrackId trackId = createTrackId(getTrack());
        final JTrackType trackType = trackId.getType();
        if (!trackType.hasFfmpegType()) die("initTrackType: cannot remove tracks of type "+ trackType);
        return trackId;
    }

    @Override protected JFileExtension getFileExtension(JAsset source, JAsset output) {

        final JTrackId trackId = getTrackId();
        final JTrackType trackType = trackId.getType();

        // if we are removing all video tracks, the output will be an audio asset
        final int trackCount = source.numTracks(trackType);
        if (trackCount == 0) return die("getFileExtension: no tracks of type "+ trackType +" found in source: "+source);

        if (wouldRemoveAllVideoTracks(trackId, trackCount)) {
            // find the format of the first audio track
            final JTrack audio = source.firstTrack(JTrackType.audio);
            if (audio == null) return die("getFileExtension: no audio tracks found!");
            final JFileExtension ext = JFileExtension.fromTrack(audio);
            source.setInfo(new JMediaInfo(source.getInfo(), new JFormat().setFileExtension(ext)));
            return ext;
        }

        return super.getFileExtension(source, output);
    }

    private boolean wouldRemoveAllVideoTracks(JTrackId trackId, int trackCount) {
        if (trackId.getType() != JTrackType.video) return false;
        if (!trackId.hasNumber()) return true;
        return trackCount == 1;
    }

}
