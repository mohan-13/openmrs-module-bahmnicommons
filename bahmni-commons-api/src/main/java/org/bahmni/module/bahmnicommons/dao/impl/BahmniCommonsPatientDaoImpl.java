package org.bahmni.module.bahmnicommons.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.bahmni.module.bahmnicommons.contract.patient.mapper.PatientResponseMapper;
import org.bahmni.module.bahmnicommons.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicommons.contract.patient.search.PatientSearchBuilder;
import org.bahmni.module.bahmnicommons.dao.BahmniCommonsPatientDao;
import org.bahmni.module.bahmnicommons.visitlocation.BahmniCommonsVisitLocationServiceImpl;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.ProgramWorkflowServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Repository
public class BahmniCommonsPatientDaoImpl implements BahmniCommonsPatientDao {

    public static final int MAX_NGRAM_SIZE = 20;

    private SessionFactory sessionFactory;

    @Autowired
    public BahmniCommonsPatientDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<PatientResponse> getPatients(String identifier, String name, String customAttribute,
                                             String addressFieldName, String addressFieldValue, Integer length,
                                             Integer offset, String[] customAttributeFields, String programAttributeFieldValue,
                                             String programAttributeFieldName, String[] addressSearchResultFields,
                                             String[] patientSearchResultFields, String loginLocationUuid, Boolean filterPatientsByLocation, Boolean filterOnAllIdentifiers) {

        validateSearchParams(customAttributeFields, programAttributeFieldName, addressFieldName);

        ProgramAttributeType programAttributeType = getProgramAttributeType(programAttributeFieldName);

        SQLQuery sqlQuery = new PatientSearchBuilder(sessionFactory)
                .withPatientName(name)
                .withPatientAddress(addressFieldName, addressFieldValue, addressSearchResultFields)
                .withPatientIdentifier(identifier, filterOnAllIdentifiers)
                .withPatientAttributes(customAttribute, getPersonAttributeIds(customAttributeFields), getPersonAttributeIds(patientSearchResultFields))
                .withProgramAttributes(programAttributeFieldValue, programAttributeType)
                .withLocation(loginLocationUuid, filterPatientsByLocation)
                .buildSqlQuery(length, offset);
        return sqlQuery.list();
    }

    @Override
    public List<PatientResponse> getPatientsUsingLuceneSearch(String identifier, String name, String customAttribute,
                                                              String addressFieldName, String addressFieldValue, Integer length,
                                                              Integer offset, String[] customAttributeFields, String programAttributeFieldValue,
                                                              String programAttributeFieldName, String[] addressSearchResultFields,
                                                              String[] patientSearchResultFields, String loginLocationUuid,
                                                              Boolean filterPatientsByLocation, Boolean filterOnAllIdentifiers) {

        validateSearchParams(customAttributeFields, programAttributeFieldName, addressFieldName);

        List<PatientIdentifier> patientIdentifiers = getPatientIdentifiers(identifier, filterOnAllIdentifiers, offset, length);
        List<Integer> patientIds = patientIdentifiers.stream().map(patientIdentifier -> patientIdentifier.getPatient().getPatientId()).collect(toList());
        List<PersonName> pNames = null;
        List<PatientResponse> patientResponses = new ArrayList<>();

        if (StringUtils.isNotBlank(name)) {
            pNames = getPatientsByName(name, offset, length);
            patientIds.addAll(pNames.stream().map(pName -> pName.getPerson().getPersonId()).collect(toList()));
        }

        Map<Object, Object> programAttributes = Context.getProgramWorkflowService().getPatientProgramAttributeByAttributeName(patientIds, programAttributeFieldName);
        PatientResponseMapper patientResponseMapper = new PatientResponseMapper(Context.getVisitService(),new BahmniCommonsVisitLocationServiceImpl(Context.getLocationService()));
        Set<Integer> uniquePatientIds = new HashSet<>();
        if(pNames != null && pNames.size() > 0) {
            patientResponses = pNames.stream().filter(pName -> pName.getPerson().getIsPatient())
                    .map(pName -> {
                        Person person = pName.getPerson();
                        Patient patient = Context.getPatientService().getPatient(person.getPersonId());
                        if ( patient !=null && patient.getPatientId() != null) {
                            if (!uniquePatientIds.contains(patient.getPatientId())) {
                                PatientResponse patientResponse = patientResponseMapper.map(patient, loginLocationUuid, patientSearchResultFields,
                                        addressSearchResultFields, programAttributes.get(patient.getPatientId()));
                                uniquePatientIds.add(patient.getPatientId());
                                return patientResponse;
                            } else
                                return null;
                        } else
                            return null;
                    }).filter(Objects::nonNull)
                    .collect(toList());
        }
        patientResponses .addAll(patientIdentifiers.stream()
                .map(patientIdentifier -> {
                    Patient patient = patientIdentifier.getPatient();
                    if (patient!= null && patient.getPatientId()!= null && !uniquePatientIds.contains(patient.getPatientId())) {
                        PatientResponse patientResponse = patientResponseMapper.map(patient, loginLocationUuid, patientSearchResultFields, addressSearchResultFields,
                                programAttributes.get(patient.getPatientId()));
                        uniquePatientIds.add(patient.getPatientId());
                        return patientResponse;
                    } else
                        return null;
                }).filter(Objects::nonNull)
                .collect(toList()));
        return patientResponses;
    }

    private void validateSearchParams(String[] customAttributeFields, String programAttributeFieldName, String addressFieldName) {
        List<Integer> personAttributeIds = getPersonAttributeIds(customAttributeFields);
        if (customAttributeFields != null && personAttributeIds.size() != customAttributeFields.length) {
            throw new IllegalArgumentException(String.format("Invalid Attribute In Patient Attributes [%s]", StringUtils.join(customAttributeFields, ", ")));
        }

        ProgramAttributeType programAttributeTypeId = getProgramAttributeType(programAttributeFieldName);
        if (programAttributeFieldName != null && programAttributeTypeId == null) {
            throw new IllegalArgumentException(String.format("Invalid Program Attribute %s", programAttributeFieldName));
        }


        if (!isValidAddressField(addressFieldName)) {
            throw new IllegalArgumentException(String.format("Invalid Address Field %s", addressFieldName));
        }
    }

    private boolean isValidAddressField(String addressFieldName) {
        if (addressFieldName == null) return true;
        String query = "SELECT DISTINCT COLUMN_NAME FROM information_schema.columns WHERE\n" +
                "LOWER (TABLE_NAME) ='person_address' and LOWER(COLUMN_NAME) IN " +
                "( :personAddressField)";
        Query queryToGetAddressFields = sessionFactory.getCurrentSession().createSQLQuery(query);
        queryToGetAddressFields.setParameterList("personAddressField", Arrays.asList(addressFieldName.toLowerCase()));
        List list = queryToGetAddressFields.list();
        return list.size() > 0;
    }

    private ProgramAttributeType getProgramAttributeType(String programAttributeField) {
        if (StringUtils.isEmpty(programAttributeField)) {
            return null;
        }

        return (ProgramAttributeType) sessionFactory.getCurrentSession().createCriteria(ProgramAttributeType.class).
                add(Restrictions.eq("name", programAttributeField)).uniqueResult();
    }

    private List<Integer> getPersonAttributeIds(String[] patientAttributes) {
        if (patientAttributes == null || patientAttributes.length == 0) {
            return new ArrayList<>();
        }

        String query = "select person_attribute_type_id from person_attribute_type where name in " +
                "( :personAttributeTypeNames)";
        Query queryToGetAttributeIds = sessionFactory.getCurrentSession().createSQLQuery(query);
        queryToGetAttributeIds.setParameterList("personAttributeTypeNames", Arrays.asList(patientAttributes));
        List list = queryToGetAttributeIds.list();
        return (List<Integer>) list;
    }

    private List<PatientIdentifier> getPatientIdentifiers(String identifier, Boolean filterOnAllIdentifiers, Integer offset, Integer length) {
        FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
        QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(PatientIdentifier.class).get();
        identifier = identifier.replace('%','*');
        org.apache.lucene.search.Query identifierQuery;
        if(identifier.length() <= MAX_NGRAM_SIZE) {
            identifierQuery = queryBuilder.keyword()
                    .wildcard().onField("identifierAnywhere").matching("*" + identifier.toLowerCase() + "*").createQuery();
        } else {
            identifierQuery = queryBuilder.keyword()
                    .onField("identifierExact").matching(identifier.toLowerCase()).createQuery();
        }
        org.apache.lucene.search.Query nonVoidedIdentifiers = queryBuilder.keyword().onField("voided").matching(false).createQuery();
        org.apache.lucene.search.Query nonVoidedPatients = queryBuilder.keyword().onField("patient.voided").matching(false).createQuery();

        List<String> identifierTypeNames = getIdentifierTypeNames(filterOnAllIdentifiers);

        BooleanJunction identifierTypeShouldJunction = queryBuilder.bool();
        for (String identifierTypeName: identifierTypeNames) {
            org.apache.lucene.search.Query identifierTypeQuery = queryBuilder.phrase().onField("identifierType.name").sentence(identifierTypeName).createQuery();
            identifierTypeShouldJunction.should(identifierTypeQuery);
        }

        org.apache.lucene.search.Query booleanQuery = queryBuilder.bool()
                .must(identifierQuery)
                .must(nonVoidedIdentifiers)
                .must(nonVoidedPatients)
                .must(identifierTypeShouldJunction.createQuery())
                .createQuery();
        Sort sort = new Sort( new SortField( "identifierExact", SortField.Type.STRING, false ) );
        FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(booleanQuery, PatientIdentifier.class);
        fullTextQuery.setSort(sort);
        fullTextQuery.setFirstResult(offset);
        fullTextQuery.setMaxResults(length);
        return (List<PatientIdentifier>) fullTextQuery.list();
    }

    private List<PersonName> getPatientsByName(String name, Integer offset, Integer length) {
        FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
        QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(PersonName.class).get();
        name = name.replace('%','*');

        org.apache.lucene.search.Query nonVoidedNames = queryBuilder.keyword().onField("voided").matching(false).createQuery();
        org.apache.lucene.search.Query nonVoidedPersons = queryBuilder.keyword().onField("person.voided").matching(false).createQuery();

        List<String> patientNames = getPatientNames();

        BooleanJunction nameShouldJunction = queryBuilder.bool();
        for (String patientName: patientNames) {
            org.apache.lucene.search.Query nameQuery = queryBuilder.keyword().wildcard()
                    .onField(patientName).matching("*" + name.toLowerCase() + "*").createQuery();
            nameShouldJunction.should(nameQuery);
        }

        org.apache.lucene.search.Query booleanQuery = queryBuilder.bool()
                .must(nonVoidedNames)
                .must(nonVoidedPersons)
                .must(nameShouldJunction.createQuery())
                .createQuery();
        FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(booleanQuery, PersonName.class);
        fullTextQuery.setFirstResult(offset);
        fullTextQuery.setMaxResults(length);
        return (List<PersonName>) fullTextQuery.list();
    }

    private List<String> getIdentifierTypeNames(Boolean filterOnAllIdentifiers) {
        List<String> identifierTypeNames = new ArrayList<>();
        addIdentifierTypeName(identifierTypeNames,"bahmni.primaryIdentifierType");
        if(filterOnAllIdentifiers){
            addIdentifierTypeName(identifierTypeNames,"bahmni.extraPatientIdentifierTypes");
        }
        return identifierTypeNames;
    }

    private List<String> getPatientNames() {
        List<String> patientNames = new ArrayList<>();
        patientNames.add("givenNameAnywhere");
        patientNames.add("middleNameAnywhere");
        patientNames.add("familyNameAnywhere");
        return patientNames;
    }

    private void addIdentifierTypeName(List<String> identifierTypeNames,String identifierProperty) {
        String identifierTypes = Context.getAdministrationService().getGlobalProperty(identifierProperty);
        if(StringUtils.isNotEmpty(identifierTypes)) {
            String[] identifierUuids = identifierTypes.split(",");
            for (String identifierUuid :
                    identifierUuids) {
                PatientIdentifierType patientIdentifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(identifierUuid);
                if (patientIdentifierType != null) {
                    identifierTypeNames.add(patientIdentifierType.getName());
                }
            }
        }
    }
}
