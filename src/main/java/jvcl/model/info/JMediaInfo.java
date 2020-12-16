package jvcl.model.info;

import jvcl.model.JFormat;
import jvcl.model.JFileExtension;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static org.cobbzilla.util.daemon.ZillaRuntime.*;

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
        JTrack image = null;
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
            } else if (t.image()) {
                if (image == null) {
                    image = t;
                } else {
                    log.warn("initFormat: multiple image tracks found, only using the first one");
                }
            } else if (t.getType().equals("General") && general == null) {
                general = t;
            }
        }
        if (general == null) return die("initFormat: no general track found");

        final JFormat format = new JFormat();
        if (video != null) {
            format.setFileExtension(JFileExtension.fromString(general.getFileExtension()))
                    .setHeight(video.height())
                    .setWidth(video.width());

        } else if (audio != null) {
            format.setFileExtension(JFileExtension.fromString(general.getFileExtension()));

        } else if (image != null) {
            format.setFileExtension(JFileExtension.fromString(general.getFileExtension()))
                    .setHeight(image.height())
                    .setWidth(image.width());

        } else {
            return die("initFormat: no media tracks could be found in file");
        }
        return format;
    }

    public BigDecimal duration() {
        if (media == null || empty(media.getTrack())) return ZERO;

        // find the longest media track
        BigDecimal longest = null;
        for (JTrack t : media.getTrack()) {
            if (!t.audioOrVideo()) continue;
            if (!t.hasDuration()) continue;
            final BigDecimal d = big(t.getDuration());
            if (longest == null || longest.compareTo(d) < 0) longest = d;
        }
        return longest;
    }

    public BigDecimal width() {
        if (media == null || empty(media.getTrack())) return ZERO;
        // find the first video track
        for (JTrack t : media.getTrack()) {
            if (!t.imageOrVideo()) continue;
            if (!t.hasWidth()) continue;
            return big(t.getWidth());
        }
        return null;
    }

    public BigDecimal height() {
        if (media == null || empty(media.getTrack())) return ZERO;
        // find the first video track
        for (JTrack t : media.getTrack()) {
            if (!t.imageOrVideo()) continue;
            if (!t.hasHeight()) continue;
            return big(t.getHeight());
        }
        return null;
    }

}
