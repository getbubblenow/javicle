package jvc.service;

import jvc.model.operation.JOperation;
import jvc.model.operation.JValidationResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public class JOperationValidationFailure extends RuntimeException {

    @Getter private final JOperation operation;
    @Getter private final List<JValidationResult> results;

}
