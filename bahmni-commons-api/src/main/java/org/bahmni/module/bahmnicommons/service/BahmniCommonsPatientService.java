package org.bahmni.module.bahmnicommons.service;

import org.bahmni.module.bahmnicommons.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicommons.contract.patient.response.PatientResponse;

import java.util.List;

public interface BahmniCommonsPatientService {

    List<PatientResponse> search(PatientSearchParameters searchParameters);

    List<PatientResponse> luceneSearch(PatientSearchParameters searchParameters);

}
