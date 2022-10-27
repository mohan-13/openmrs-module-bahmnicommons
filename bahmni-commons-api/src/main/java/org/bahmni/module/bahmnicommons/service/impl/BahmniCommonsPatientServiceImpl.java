package org.bahmni.module.bahmnicommons.service.impl;

import org.bahmni.module.bahmnicommons.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicommons.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicommons.dao.BahmniCommonsPatientDao;
import org.bahmni.module.bahmnicommons.service.BahmniCommonsPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

;

@Service
public class BahmniCommonsPatientServiceImpl implements BahmniCommonsPatientService {
    private BahmniCommonsPatientDao bahmniCommonsPatientDao;

    @Autowired
    public BahmniCommonsPatientServiceImpl(BahmniCommonsPatientDao bahmniCommonsPatientDao) {
        this.bahmniCommonsPatientDao = bahmniCommonsPatientDao;
    }

    @Override
    public List<PatientResponse> search(PatientSearchParameters searchParameters) {
        return bahmniCommonsPatientDao.getPatients(searchParameters.getIdentifier(),
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

    @Override
    public List<PatientResponse> luceneSearch(PatientSearchParameters searchParameters) {
        return bahmniCommonsPatientDao.getPatientsUsingLuceneSearch(searchParameters.getIdentifier(),
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
