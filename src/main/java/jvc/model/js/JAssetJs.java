package jvc.model.js;

import jvc.model.JAsset;
import jvc.model.info.JMediaInfo;
import jvc.model.info.JTrack;
import org.cobbzilla.util.collection.ArrayUtil;

import java.math.BigDecimal;

import static jvc.model.js.JTrackJs.EMPTY_TRACKS;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class JAssetJs {

    public static final JAssetJs[] EMPTY_ASSETS = new JAssetJs[0];

    public final Double duration;
    public final Integer width;
    public final Integer height;
    public final Double aspectRatio;
    public final Integer samplingRate;
    public JTrackJs[] allTracks = EMPTY_TRACKS;
    public JTrackJs[] tracks = EMPTY_TRACKS;
    public JTrackJs[] videoTracks = EMPTY_TRACKS;
    public JTrackJs[] audioTracks = EMPTY_TRACKS;
    public JAssetJs[] assets = EMPTY_ASSETS;
    public final boolean hasAudio;
    public final boolean hasVideo;

    public JAssetJs(JAsset asset) {
        final BigDecimal d = asset.duration();
        this.duration = d == null ? null : d.doubleValue();

        final BigDecimal w = asset.width();
        this.width = w == null ? null : w.intValue();

        final BigDecimal h = asset.height();
        this.height = h == null ? null : h.intValue();

        this.aspectRatio = asset.aspectRatio() == null ? Double.NaN : asset.aspectRatio().doubleValue();
        this.samplingRate = asset.hasSamplingRate() ? asset.samplingRate().intValue() : 0;

        if (asset.hasInfo()) {
            final JMediaInfo info = asset.getInfo();
            for (JTrack track : info.getMedia().getTrack()) {

                final JTrackJs trackJs = new JTrackJs(track.type().name());
                allTracks = ArrayUtil.append(allTracks, trackJs);

                if (!track.audioOrVideo()) continue;

                tracks = ArrayUtil.append(tracks, trackJs);
                switch (track.type()) {
                    case audio:
                        audioTracks = ArrayUtil.append(audioTracks, trackJs);
                        break;
                    case video:
                        videoTracks = ArrayUtil.append(videoTracks, trackJs);
                        break;
                }
            }
        }

        if (asset.hasListAssets()) {
            final JAsset[] list = asset.getList();
            this.assets = new JAssetJs[list.length];
            for (int i = 0; i < assets.length; i++) {
                this.assets[i] = new JAssetJs(list[i]);
            }
        }

        hasAudio = !empty(audioTracks);
        hasVideo = !empty(videoTracks);
    }
}
