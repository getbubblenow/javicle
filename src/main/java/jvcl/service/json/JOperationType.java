package jvcl.service.json;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeBase;
import com.fasterxml.jackson.databind.type.TypeBindings;

public class JOperationType extends SimpleType {

    protected JOperationType(Class<?> cls) { super(cls); }

    public JOperationType(Class<?> cls, TypeBindings bindings, JavaType superClass, JavaType[] superInts) {
        super(cls, bindings, superClass, superInts);
    }

    protected JOperationType(TypeBase base) { super(base); }

    protected JOperationType(Class<?> cls, TypeBindings bindings, JavaType superClass, JavaType[] superInts, Object valueHandler, Object typeHandler, boolean asStatic) {
        super(cls, bindings, superClass, superInts, valueHandler, typeHandler, asStatic);
    }

    protected JOperationType(Class<?> cls, TypeBindings bindings, JavaType superClass, JavaType[] superInts, int extraHash, Object valueHandler, Object typeHandler, boolean asStatic) {
        super(cls, bindings, superClass, superInts, extraHash, valueHandler, typeHandler, asStatic);
    }

}
