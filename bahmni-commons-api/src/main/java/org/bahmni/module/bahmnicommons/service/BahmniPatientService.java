package org.bahmni.module.bahmnicommons.service;

import org.bahmni.module.bahmnicommons.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicommons.contract.patient.response.PatientConfigResponse;
import org.bahmni.module.bahmnicommons.contract.patient.response.PatientResponse;
import org.openmrs.Patient;
import org.openmrs.RelationshipType;
import org.openmrs.annotation.Authorized;

import java.util.List;

public interface BahmniPatientService {
    @Authorized({"Get Patients"})
    List<PatientResponse> search(PatientSearchParameters searchParameters);
    @Authorized({"Get Patients"})
    List<PatientResponse> luceneSearch(PatientSearchParameters searchParameters);

    @Authorized({"Get Patients"})
    public List<Patient> get(String partialIdentifier, boolean shouldMatchExactPatientId);

    public PatientConfigResponse getConfig();

    public List<RelationshipType> getByAIsToB(String aIsToB);

}
