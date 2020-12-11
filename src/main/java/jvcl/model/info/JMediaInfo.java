package jvcl.model.info;

import jvcl.model.JFormat;
import jvcl.model.JFileExtension;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import static org.cobbzilla.util.daemon.ZillaRuntime.big;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@Slf4j
public class JMediaInfo {

    @Getter @Setter private JMedia media;
    @Getter(lazy=true) private final JFormat format = initFormat();
    public boolean hasFormat () { return getFormat() != null; }

    private JFormat initFormat () {
        if (media == null || empty(media.getTrack())) return null;
        JTrack general = null;
        JTrack video = null;
        JTrack audio = null;
        for (int i=0; i<media.getTrack().length; i++) {
            final JTrack t = media.getTrack()[i];
            if (t.video()) {
                if (video == null) {
                    video = t;
                } else {
                    log.warn("initFormat: multiple video tracks found, only using the first one");
                }
            } else if (t.audio()) {
                if (audio == null) {
                    audio = t;
                } else {
                    log.warn("initFormat: multiple audio tracks found, only using the first one");
                }
            } else if (t.getType().equals("General") && general == null) {
                general = t;
            }
        }
        final JFormat format = new JFormat();
        if (video != null) {
            format.setFileExtension(JFileExtension.fromString(general.getFileExtension()))
                    .setHeight(video.height())
                    .setWidth(video.width());
        } else if (audio != null) {
            format.setFileExtension(JFileExtension.fromString(audio.getFileExtension()));
        }
        return format;
    }

    public BigDecimal duration() {
        if (media == null || empty(media.getTrack())) return BigDecimal.ZERO;

        // find the longest media track
        BigDecimal longest = null;
        for (JTrack t : media.getTrack()) {
            if (!t.media()) continue;
            if (!t.hasDuration()) continue;
            final BigDecimal d = big(t.getDuration());
            if (longest == null || longest.compareTo(d) < 0) longest = d;
        }
        return longest;
    }

}
