package jvcl.service.json;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import jvcl.model.JOperation;
import jvcl.operation.exec.ExecBase;

import java.io.IOException;
import java.util.StringTokenizer;

import static com.fasterxml.jackson.databind.type.TypeBindings.emptyBindings;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.shortError;
import static org.cobbzilla.util.reflect.ReflectionUtil.forName;

public class JOperationFactory extends DeserializationProblemHandler {

    @Override public JavaType handleUnknownTypeId(DeserializationContext ctxt,
                                                  JavaType baseType,
                                                  String subTypeId,
                                                  TypeIdResolver idResolver,
                                                  String failureMsg) throws IOException {
        try {
            return new JOperationType(getOperationClass(subTypeId), emptyBindings(), baseType, null);
        } catch (Exception e) {
            throw new IOException("handleUnknownTypeId: '"+subTypeId+"' is not a valid operation type: "+shortError(e));
        }
    }

    public static final String OPERATION_DEFAULT_PACKAGE = "jvcl.operation";
    public static final String OPERATION_CLASSNAME_SUFFIX = "Operation";

    public static Class<?> getOperationClass(String id) {
        final String className;
        if (id.contains(".")) {
            className = id;
        } else {
            className = OPERATION_DEFAULT_PACKAGE
                    + "."
                    + (id.contains("-") ? replaceHyphens(id) : capitalize(id))
                    + OPERATION_CLASSNAME_SUFFIX;
        }
        return forName(className);
    }

    private static String replaceHyphens(String id) {
        final StringBuilder b = new StringBuilder();
        final StringTokenizer st = new StringTokenizer(id, "-");
        while (st.hasMoreTokens()) b.append(capitalize(st.nextToken()));
        return b.toString();
    }

    public static <OP extends JOperation> Class<? extends ExecBase<OP>> getOperationExecClass(Class<? extends JOperation> opClass) {
        final String name = opClass.getSimpleName();
        if (!name.endsWith(OPERATION_CLASSNAME_SUFFIX)) {
            return die("getOperationExecClass: expected JOperation class to end with '" + OPERATION_CLASSNAME_SUFFIX + "'");
        }
        final String execClassName
                = opClass.getPackageName()
                + ".exec."
                + name.substring(0, name.length() - OPERATION_CLASSNAME_SUFFIX.length())
                + "Exec";
        return forName(execClassName);
    }

}
