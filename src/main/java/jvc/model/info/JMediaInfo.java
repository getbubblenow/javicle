package jvc.model.info;

import jvc.model.JStreamType;
import jvc.model.JFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

import static java.math.BigDecimal.ZERO;
import static org.cobbzilla.util.daemon.ZillaRuntime.*;

@NoArgsConstructor @Slf4j
public class JMediaInfo {

    public JMediaInfo (JMediaInfo other, JFormat format) {
        this.media = other.getMedia();
        this.formatRef.set(format);
    }

    @Getter @Setter private JMedia media;
    public boolean emptyMedia() { return media == null || empty(media.getTrack()); }

    private final AtomicReference<JFormat> formatRef = new AtomicReference<>();

    public JFormat getFormat () {
        if (formatRef.get() == null) formatRef.set(initFormat());
        return formatRef.get();
    }

    private JFormat initFormat () {
        if (emptyMedia()) return null;
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
            format.setStreamType(video.hasFormat()
                    ? JStreamType.fromTrack(video)
                    : JStreamType.fromString(general.getFileExtension()))
                    .setHeight(video.height())
                    .setWidth(video.width());

        } else if (audio != null) {
            format.setStreamType(audio.hasFormat()
                    ? JStreamType.fromTrack(audio)
                    : JStreamType.fromString(general.getFileExtension()));

        } else if (image != null) {
            format.setStreamType(JStreamType.fromString(general.getFileExtension()))
                    .setHeight(image.height())
                    .setWidth(image.width());

        } else {
            return die("initFormat: no media tracks could be found in file: "+ref());
        }
        return format;
    }

    public String ref() {
        return emptyMedia() ? "(null media)" : empty(getMedia().getRef()) ? "(null ref)" : getMedia().getRef();
    }

    public BigDecimal duration() {
        if (emptyMedia()) return ZERO;

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

    public BigDecimal samplingRate() {
        if (emptyMedia()) return null;
        for (JTrack t : media.getTrack()) {
            if (!t.audioOrVideo()) continue;
            if (t.hasSamplingRate()) return big(t.getSamplingRate());
        }
        return null;
    }

    public String channelLayout() {
        if (emptyMedia()) return null;
        for (JTrack t : media.getTrack()) {
            if (!t.audioOrVideo()) continue;
            final String channelLayout = t.channelLayout();
            if (!empty(channelLayout)) return channelLayout;
        }
        return null;
    }

    public JStreamType audioExtension() {
        final JTrack audio = firstTrack(JTrackType.audio);
        return audio == null ? null : JStreamType.fromTrack(audio);
    }

    public BigDecimal width() {
        if (emptyMedia()) return ZERO;
        // find the first video track
        for (JTrack t : media.getTrack()) {
            if (!t.imageOrVideo()) continue;
            if (!t.hasWidth()) continue;
            return big(t.getWidth());
        }
        return null;
    }

    public BigDecimal height() {
        if (emptyMedia()) return ZERO;
        // find the first video track
        for (JTrack t : media.getTrack()) {
            if (!t.imageOrVideo()) continue;
            if (!t.hasHeight()) continue;
            return big(t.getHeight());
        }
        return null;
    }

    public int numTracks(JTrackType type) {
        if (emptyMedia()) return 0;
        int count = 0;
        for (JTrack t : media.getTrack()) {
            if (t.type() == type) count++;
        }
        return count;
    }

    public JTrack firstTrack(JTrackType type) {
        if (emptyMedia()) return null;
        for (JTrack t : media.getTrack()) {
            if (t.type() == type) return t;
        }
        return null;
    }

}
