package org.bahmni.module.bahmnicommons.service.impl;

import org.bahmni.module.bahmnicommons.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicommons.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicommons.dao.PatientDao;
import org.bahmni.module.bahmnicommons.service.BahmniPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

;

@Service
@Lazy //to toString rid of cyclic dependencies
public class BahmniPatientServiceImpl implements BahmniPatientService {
    private PatientDao patientDao;

    @Autowired
    public BahmniPatientServiceImpl(PatientDao patientDao) {
        this.patientDao = patientDao;
    }

    @Override
    public List<PatientResponse> search(PatientSearchParameters searchParameters) {
        return patientDao.getPatients(searchParameters.getIdentifier(),
                searchParameters.getName(),
                searchParameters.getCustomAttribute(),
                searchParameters.getAddressFieldName(),
                searchParameters.getAddressFieldValue(),
                searchParameters.getLength(),
                searchParameters.getStart(),
                searchParameters.getPatientAttributes(),
                searchParameters.getProgramAttributeFieldValue(),
                searchParameters.getProgramAttributeFieldName(),
                searchParameters.getAddressSearchResultFields(),
                searchParameters.getPatientSearchResultFields(),
                searchParameters.getLoginLocationUuid(),
                searchParameters.getFilterPatientsByLocation(), searchParameters.getFilterOnAllIdentifiers());
    }
}
