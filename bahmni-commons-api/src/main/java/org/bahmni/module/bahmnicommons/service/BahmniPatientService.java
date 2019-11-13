package org.bahmni.module.bahmnicommons.service;

import org.bahmni.module.bahmnicommons.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicommons.contract.patient.response.PatientResponse;
import org.openmrs.Patient;
import org.openmrs.RelationshipType;

import java.util.List;

public interface BahmniPatientService {

    public List<PatientResponse> search(PatientSearchParameters searchParameters);

}
