package jvc.service;

import jvc.model.JAsset;
import jvc.model.JStreamType;
import jvc.model.operation.JOperation;
import lombok.Getter;
import org.cobbzilla.util.handlebars.HandlebarsUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Integer.parseInt;
import static jvc.model.JAsset.NULL_ASSET;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.string.StringUtil.isOnlyDigits;

public class AssetManager {

    public static final String OUTFILE_PREFIX = AssetManager.class.getSimpleName() + ".output.";
    public static final String RANGE_SEP = "..";

    private final Toolbox toolbox;
    @Getter private final File scratchDir;
    private final Map<String, JAsset> assets = new ConcurrentHashMap<>();

    public AssetManager(Toolbox toolbox, File scratchDir) {
        this.toolbox = toolbox;
        this.scratchDir = scratchDir;
    }

    public File sourcePath(String name) { return new File(scratchDir, OUTFILE_PREFIX + "source." + name); }

    public File assetPath(JOperation op, JAsset source, JStreamType streamType) {
        return assetPath(op, source, streamType, null);
    }

    public File assetPath(JOperation op, JAsset source, JStreamType streamType, Object[] args) {
        return assetPath(op, new JAsset[]{source}, streamType, args);
    }

    public File assetPath(JOperation op, JAsset[] sources, JStreamType streamType) {
        return assetPath(op, sources, streamType, null);
    }

    public File assetPath(JOperation op, List<JAsset> sources, JStreamType streamType) {
        return assetPath(op, sources.toArray(JAsset[]::new), streamType, null);
    }

    public File assetPath(JOperation op, JAsset[] sources, JStreamType streamType, Object[] args) {
        return new File(scratchDir, OUTFILE_PREFIX
                + op.hash(sources, args)
                + (!empty(args) ? "_" + args[0] + (args.length > 1 ? "_" + args[1] : "") : "")
                + streamType.ext());
    }

    public Map<String, JAsset> getAssets () {
        final Map<String, JAsset> map = new HashMap<>();
        for (Map.Entry<String, JAsset> entry : assets.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    private String checkName(JAsset asset) {
        final String name = asset.getName();
        if (assets.containsKey(name)) die("defineAsset: name already defined: "+ name);
        return name;
    }

    public void defineAsset(JAsset asset) {
        final String name = checkName(asset);
        assets.put(name, asset.init(this, toolbox));
    }

    public void addOperationAsset(JAsset asset) {
        if (asset == null || asset == NULL_ASSET) return;
        final String name = checkName(asset);
        assets.put(name, asset.init(this, toolbox));
    }

    public void addOperationArrayAsset(JAsset asset) {
        if (asset == null || asset == NULL_ASSET) return;
        final String name = checkName(asset);
        assets.put(name, asset);
    }

    public void addOperationAssetSlice(JAsset asset, JAsset slice) {
        if (!assets.containsKey(asset.getName())) die("asset not found: "+asset.getName());
        final JAsset found = assets.get(asset.getName());
        found.addAsset(slice.init(this, toolbox));
    }

    public JAsset[] resolve(String[] assets) {
        final JAsset[] resolved = new JAsset[assets.length];
        for (int i=0; i<assets.length; i++) {
            resolved[i] = resolve(assets[i]);
        }
        return resolved;
    }

    public JAsset resolve(String name) {
        final JAsset asset;
        if (name.contains("[") && name.contains("]")) {
            final int openPos = name.indexOf("[");
            final int closePos = name.indexOf("]");
            final String assetName = name.substring(0, openPos);
            asset = assets.get(assetName);
            if (asset == null) return die("resolve("+name+"): asset not found: "+assetName);
            if (!asset.hasList()) return die("resolve("+name+"): "+assetName+" is not a list asset");
            final JAsset[] list = asset.getList();

            final String indexExpr = name.substring(openPos+1, closePos);
            if (indexExpr.contains(RANGE_SEP)) {
                final int dotPos = indexExpr.indexOf(RANGE_SEP);
                final String from = indexExpr.substring(0, dotPos);
                final String to = indexExpr.substring(dotPos+RANGE_SEP.length());
                final int fromIndex = parseIndexExpression(from, list, JIndexType.from);
                final int toIndex = parseIndexExpression(to, list, JIndexType.to);
                if (toIndex < fromIndex) return die("parseIndexExpression("+indexExpr+"): 'to' index ("+toIndex+") < 'from' index ("+fromIndex+")");
                final int len = 1 + (toIndex - fromIndex);
                final JAsset[] subList = new JAsset[len];
                System.arraycopy(list, fromIndex, subList, 0, len);
                return new JAsset(asset).setList(subList);

            } else {
                // single element
                final int index = parseIndexExpression(indexExpr, list, JIndexType.single);
                if (Math.abs(index) >= list.length) return die("parseIndexExpression("+indexExpr+"): index out of range: "+index);
                return index < 0 ? list[list.length + index] : list[index];
            }

        } else {
            asset = assets.get(name);
        }
        return asset == null ? die("resolve("+name+")") : asset;
    }

    private int parseIndexExpression(String indexExpr, JAsset[] list, JIndexType type) {
        if (empty(indexExpr)) {
            switch (type) {
                case from: return 0;
                case to: return list.length-1;
                case single: return die("parseIndexExpression(): no expression provided!");
                default: return die("parseIndexExpression(): invalid type: "+type);
            }
        }
        final int index;
        if (isOnlyDigits(indexExpr)) {
            try {
                index = parseInt(indexExpr);
            } catch (Exception e) {
                return die("parseIndexExpression("+indexExpr+"): not an integer: "+indexExpr);
            }

        } else {
            // not all digits, evaluate as handlebars expression
            final Map<String, Object> ctx = new HashMap<>(assets);
            final String indexString = HandlebarsUtil.apply(toolbox.getHandlebars(), "{{{" + indexExpr + "}}}", ctx);
            if (empty(indexString)) return die("parseIndexExpression("+indexExpr+"): index expression resolved to empty string: "+indexExpr);
            try {
                index = parseInt(indexString);
            } catch (Exception e) {
                return die("parseIndexExpression("+indexExpr+"): index expression did not evaluate to an integer: "+indexString);
            }
        }
        if (index < 0 || index > list.length) return die("parseIndexExpression("+indexExpr+"): '"+type+"' index out of range: "+index);
        return index;
    }

}
