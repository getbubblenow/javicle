package jvcl.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

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

}
