package jvcl.model;

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

import static org.cobbzilla.util.daemon.ZillaRuntime.*;
import static org.cobbzilla.util.http.HttpSchemes.isHttpOrHttps;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.StreamUtil.loadResourceAsStream;
import static org.cobbzilla.util.json.JsonUtil.json;
import static org.cobbzilla.util.reflect.ReflectionUtil.copy;

@NoArgsConstructor @AllArgsConstructor @Accessors(chain=true) @Slf4j
public class JAsset {

    public static final JAsset NULL_ASSET = new JAsset().setName("~null asset~").setPath("/dev/null");
    public static final String PREFIX_CLASSPATH = "classpath:";

    public static final String[] COPY_EXCLUDE_FIELDS = {"list"};

    public JAsset(JAsset other) { copy(this, other, null, COPY_EXCLUDE_FIELDS); }

    @Getter @Setter private String name;
    @Getter @Setter private String path;

    // an asset can specify where its file should live
    // if the file already exists, it is used and not overwritten
    @Getter @Setter private String dest;
    public boolean hasDest() { return !empty(dest); }
    public boolean destExists() { return new File(dest).exists(); }

    // if path was not a file, it got resolved to a file
    // the original value of 'path' is stored here
    @Getter @Setter private String originalPath;

    @Getter @Setter private JAsset[] list;
    public boolean hasList () { return list != null; }
    public void addAsset(JAsset slice) { list = ArrayUtil.append(list, slice); }

    @Getter @Setter private JFormat format;
    public boolean hasFormat() { return format != null; }

    @Getter private JMediaInfo info;
    public JAsset setInfo (JMediaInfo info) {
        this.info = info;
        setFormat(info.getFormat());
        return this;
    }
    public boolean hasInfo() { return info != null; }

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

    public BigDecimal duration() { return getInfo().duration(); }

    public JAsset init(AssetManager assetManager, Toolbox toolbox) {
        initPath(assetManager);
        setInfo(toolbox.getInfo(this));
        return this;
    }

    private JAsset initPath(AssetManager assetManager) {
        final String path = getPath();
        if (empty(path)) return die("initPath: no path!");

        // if dest already exists, use that
        if (hasDest() && destExists()) {
            setOriginalPath(path);
            setPath(getDest());
            return this;
        }

        final File sourcePath = hasDest() ? new File(getDest()) : assetManager.sourcePath(getName());
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
            final File f = new File(path);
            if (!f.exists()) return die("initPath: file path does not exist: "+path);
            if (!f.canRead()) return die("initPath: file path is not readable: "+path);
        }
        return this;
    }

}
