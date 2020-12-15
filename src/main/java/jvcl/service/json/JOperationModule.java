package jvcl.service.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;

public class JOperationModule extends Module {

    @Override public String getModuleName() { return "JOperationFactoryModule"; }
    @Override public Version version() { return new Version(1, 0, 0, "", "", ""); }

    @Override public void setupModule(SetupContext context) {
        context.addDeserializationProblemHandler(new JOperationFactory());
    }

}
