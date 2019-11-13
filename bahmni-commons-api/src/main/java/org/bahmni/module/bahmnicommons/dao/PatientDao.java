package org.bahmni.module.bahmnicommons.dao;

import org.bahmni.module.bahmnicommons.contract.patient.response.PatientResponse;
import org.openmrs.Patient;
import org.openmrs.RelationshipType;

import java.util.List;

public interface PatientDao {

    public List<PatientResponse> getPatients(String identifier, String name, String customAttribute,
                                             String addressFieldName, String addressFieldValue, Integer length, Integer offset,
                                             String[] patientAttributes, String programAttribute, String programAttributeField,
                                             String[] addressSearchResultFields, String[] patientSearchResultFields, String loginLocationUuid, Boolean filterPatientsByLocation, Boolean filterOnAllIdentifiers);
}
