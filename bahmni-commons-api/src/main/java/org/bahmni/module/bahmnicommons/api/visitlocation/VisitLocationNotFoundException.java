package org.bahmni.module.bahmnicommons.api.visitlocation;

import org.openmrs.api.APIException;

public class VisitLocationNotFoundException extends APIException {
    public VisitLocationNotFoundException(String message) {
        super(message);
    }
}