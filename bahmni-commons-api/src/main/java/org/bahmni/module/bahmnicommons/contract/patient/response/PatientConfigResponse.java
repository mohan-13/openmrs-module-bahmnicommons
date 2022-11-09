package org.bahmni.module.bahmnicommons.contract.patient.response;

import org.bahmni.module.bahmnicommons.contract.patient.data.PersonAttributeTypeData;
import org.openmrs.Concept;
import org.openmrs.PersonAttributeType;

import java.util.ArrayList;
import java.util.List;

public class PatientConfigResponse {

    private List<PersonAttributeTypeData> personAttributeTypes = new ArrayList<>();


    public List<PersonAttributeTypeData> getPersonAttributeTypes() {
        return personAttributeTypes;
    }

    public void addPersonAttribute(PersonAttributeType personAttributeType, Concept concept) {
        this.personAttributeTypes.add(new PersonAttributeTypeData(personAttributeType, concept));
    }

}