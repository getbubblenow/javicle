package jvcl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jvcl.model.info.JMediaInfo;
import jvcl.service.AssetManager;
import jvcl.service.Toolbox;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.cobbzilla.util.collection.ArrayUtil;
import org.cobbzilla.util.http.HttpUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static jvcl.service.Toolbox.divideBig;
import static org.cobbzilla.util.daemon.ZillaRuntime.*;
import static org.cobbzilla.util.http.HttpSchemes.isHttpOrHttps;
import static org.cobbzilla.util.io.FileUtil.*;
import static org.cobbzilla.util.io.StreamUtil.loadResourceAsStream;
import static org.cobbzilla.util.json.JsonUtil.json;
import static org.cobbzilla.util.reflect.ReflectionUtil.copy;
import static org.cobbzilla.util.system.CommandShell.execScript;

@NoArgsConstructor @AllArgsConstructor @Accessors(chain=true) @Slf4j
public class JAsset implements JsObjectView {

    public static final JAsset NULL_ASSET = new JAsset().setName("~null asset~").setPath("/dev/null");
    public static final String PREFIX_CLASSPATH = "classpath:";

    public static final String[] COPY_EXCLUDE_FIELDS = {"list"};

    public JAsset(JAsset other) { copy(this, other, null, COPY_EXCLUDE_FIELDS); }

    @Getter @Setter private String name;
    @Getter @Setter private String path;
    public boolean hasPath() { return !empty(path); }

    // an asset can specify where its file should live
    // if the file already exists, it is used and not overwritten
    @Getter @Setter private String dest;

    public static List<JAsset> flattenAssetList(JAsset[] assets) {
        final List<JAsset> list = new ArrayList<>();
        return _flatten(assets, list);
    }

    private static List<JAsset> _flatten(JAsset[] assets, final List<JAsset> list) {
        if (assets != null) {
            for (final JAsset a : assets) {
                if (a.hasList()) {
                    _flatten(a.getList(), list);
                } else {
                    list.add(a);
                }
            }
        }
        return list;
    }

    public boolean hasDest() { return !empty(dest); }
    public boolean destExists() { return new File(destPath()).exists(); }

    public String destPath() {
        if (destIsDirectory()) {
            if (hasPath()) {
                return abs(new File(destDirectory(), basename(getPath())));
            } else {
                return abs(destDirectory());
            }
        } else {
            return abs(new File(dest));
        }

    }

    public boolean destIsDirectory() {
        return hasDest() && (dest.endsWith("/") || new File(dest).isDirectory());
    }
    public File destDirectory() {
        final String dir = destIsDirectory() ? dest : dirname(dest);
        return mkdirOrDie(new File(dir.endsWith("/") ? dir.substring(0, dir.length()-1) : dir));
    }

    // if path was not a file, it got resolved to a file
    // the original value of 'path' is stored here
    @Getter @Setter private String originalPath;
    public boolean hasOriginalPath () { return !empty(originalPath); }

    @Getter @Setter private JAsset[] list;
    public boolean hasList () { return list != null; }
    public boolean hasListAssets () { return !empty(getList()); }
    public void addAsset(JAsset slice) { list = ArrayUtil.append(list, slice); }
    @JsonIgnore public Integer getLength () { return hasList() ? list.length : null; }

    @Getter @Setter private JFormat format;
    public boolean hasFormat() { return format != null; }

    @Getter private JMediaInfo info;
    public JAsset setInfo (JMediaInfo info) {
        this.info = info;
        setFormat(info.getFormat());
        return this;
    }
    public boolean hasInfo() { return info != null; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final JAsset a = (JAsset) o;
        return Objects.equals(path, a.path) && Arrays.equals(list, a.list);
    }

    @Override public int hashCode() {
        int result = Objects.hash(path);
        result = 31 * result + Arrays.hashCode(list);
        return result;
    }

    public static JAsset json2asset(JsonNode node) {
        if (node.isObject()) return json(node, JAsset.class);
        if (node.isTextual()) return new JAsset().setName(node.textValue());
        return die("json2asset: node was neither a JSON object nor a string: "+json(node));
    }

    public void mergeFormat(JFormat format) {
        if (format == null) {
            log.warn("mergeFormat: cannot merge null");
            return;
        }
        if (this.format == null) {
            this.format = new JFormat(format);
        } else {
            this.format.merge(format);
        }
    }

    public BigDecimal duration() { return hasInfo() ? getInfo().duration() : null; }
    @JsonIgnore public BigDecimal getDuration () { return duration(); }

    public BigDecimal width() { return hasInfo() ? getInfo().width() : null; }
    @JsonIgnore public BigDecimal getWidth () { return width(); }

    public BigDecimal height() { return hasInfo() ? getInfo().height() : null; }
    @JsonIgnore public BigDecimal getHeight () { return height(); }

    public BigDecimal aspectRatio() {
        final BigDecimal width = width();
        final BigDecimal height = height();
        return width == null || height == null ? null : divideBig(width, height);
    }

    public JAsset init(AssetManager assetManager, Toolbox toolbox) {
        final JAsset asset = initPath(assetManager);
        if (!asset.hasListAssets()) {
            setInfo(toolbox.getInfo(this));
        } else {
            setInfo(toolbox.getInfo(list[0]));
            for (JAsset a : asset.getList()) a.setInfo(getInfo());
        }
        return this;
    }

    private JAsset initPath(AssetManager assetManager) {
        final String path = getPath();
        if (empty(path)) return die("initPath: no path!");

        // if dest already exists, use that
        if (hasDest()) {
            if (destExists() && !destIsDirectory()) {
                setOriginalPath(path);
                setPath(destPath());
                return this;
            }
        }

        final File sourcePath;
        if (hasDest()) {
            if (destIsDirectory()) {
                sourcePath = new File(getDest(), basename(getPath()));
            } else {
                sourcePath = new File(getDest());
            }
        } else {
            sourcePath = assetManager.sourcePath(getName());
        }

        if (sourcePath.exists()) {
            setOriginalPath(path);
            setPath(abs(sourcePath));
            return this;
        }

        if (path.startsWith(PREFIX_CLASSPATH)) {
            // it's a classpath resource
            final String resource = path.substring(PREFIX_CLASSPATH.length());
            try {
                @Cleanup final InputStream in = loadResourceAsStream(resource);
                @Cleanup final OutputStream out = new FileOutputStream(sourcePath);
                IOUtils.copyLarge(in, out);
                setOriginalPath(path);
                setPath(abs(sourcePath));
            } catch (Exception e) {
                return die("initPath: error loading classpath resource "+resource+" : "+shortError(e));
            }

        } else if (isHttpOrHttps(path)) {
            // it's a URL
            try {
                @Cleanup final InputStream in = HttpUtil.get(path);
                @Cleanup final OutputStream out = new FileOutputStream(sourcePath);
                IOUtils.copyLarge(in, out);
                setOriginalPath(path);
                setPath(abs(sourcePath));
            } catch (Exception e) {
                return die("initPath: error loading URL resource "+path+" : "+shortError(e));
            }

        } else {
            // must be a file
            // does it contain a wildcard?
            if (path.contains("*")) {
                // find matching files
                final int starPos = path.indexOf("*");
                final int lastSlash = path.substring(0, starPos).lastIndexOf("/");
                final String[] parts = path.split("\\*");
                final StringBuilder b = new StringBuilder();
                Arrays.stream(parts).forEach(p -> {
                    if (b.length() > 0) b.append(".+");
                    b.append(Pattern.quote(p));
                });
                final Pattern regex = Pattern.compile(b.toString());
                final File dir = new File(path.substring(0, lastSlash));
                if (dir.exists()) {
                    final String filesInDir = execScript("find " + dir + " -type f");
                    final Set<String> matches = Arrays.stream(filesInDir.split("\n"))
                            .filter(f -> regex.matcher(f).matches())
                            .collect(Collectors.toCollection(() -> new TreeSet<>(comparing(String::toString))));
                    for (String f : matches) {
                        addAsset(new JAsset(this).setPath(f));
                    }
                } else {
                    return die("initPath: no files matched: "+path);
                }

            } else {
                final File f = new File(path);
                if (!f.exists()) return die("initPath: file path does not exist: "+path);
                if (!f.canRead()) return die("initPath: file path is not readable: "+path);
            }
        }
        return this;
    }

    @Override public Object toJs() { return new JAssetJs(this); }

    public static class JAssetJs {
        public Integer duration;
        public Integer width;
        public Integer height;
        public JAssetJs (JAsset asset) {
            final BigDecimal d = asset.duration();
            this.duration = d == null ? null : d.intValue();

            final BigDecimal w = asset.width();
            this.width = w == null ? null : w.intValue();

            final BigDecimal h = asset.height();
            this.height = h == null ? null : h.intValue();
        }
    }
}
