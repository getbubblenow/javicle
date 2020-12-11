package jvcl.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Optional;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.json.JsonUtil.json;
import static org.cobbzilla.util.reflect.ReflectionUtil.copy;

@NoArgsConstructor @Accessors(chain=true)
public class JFormat {

    @Getter @Setter private Integer height;
    public boolean hasHeight () { return height != null; }

    @Getter @Setter private Integer width;
    public boolean hasWidth () { return width != null; }

    @Getter @Setter private JFileExtension fileExtension;
    public boolean hasFileExtension() { return fileExtension != null; }

    public JFormat(JFormat format) { copy(this, format); }

    public void merge(JFormat other) {
        if (!hasHeight()) setHeight(other.getHeight());
        if (!hasWidth()) setWidth(other.getWidth());
        if (!hasFileExtension()) setFileExtension(other.getFileExtension());
    }

    public static JFormat getFormat(JsonNode formatNode, JAsset[] sources) {
        if (formatNode == null) {
            // no format supplied, use format from first source
            return new JFormat().setFileExtension(sources[0].getFormat().getFileExtension());
        }
        if (formatNode.isObject()) {
            return json(formatNode, JFormat.class);

        } else if (formatNode.isTextual()) {
            final JFileExtension formatType;
            final String formatTypeString = formatNode.textValue();
            if (!empty(formatTypeString)) {
                // is the format the name of an input?
                final Optional<JAsset> asset = Arrays.stream(sources).filter(s -> s.getName().equals(formatTypeString)).findFirst();
                if (asset.isEmpty()) {
                    // not the name of an asset, must be the name of a format
                    formatType = JFileExtension.valueOf(formatTypeString);
                } else {
                    // it's the name of an asset, use that asset's format
                    formatType = asset.get().getFormat().getFileExtension();
                }
                return new JFormat().setFileExtension(formatType);
            } else {
                // is the format a valid format type?
                if (JFileExtension.isValid(formatTypeString)) return new JFormat().setFileExtension(JFileExtension.valueOf(formatTypeString));
                return die("getFormat: invalid format type: "+formatTypeString);
            }
        } else {
            return die("getFormat: invalid format node: "+json(formatNode));
        }
    }

}
