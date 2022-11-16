Openmrs-module-bahmnicommons
==
This repository contains the extracted APIs from Bahmni Core, which are being used by [bahmni appointments module](https://github.com/Bahmni/openmrs-module-appointment-frontend).

## APIs Extracted

* patient-search 
  * `/openmrs/ws/rest/v1/bahmni/search/patient` 
  * `/openmrs/ws/rest/v1/bahmni/search/patient/lucene`
* location-search-handler-bytags 
  * `/openmrs/ws/rest/v1/location?operator=ALL&s=byTags`
  
## Setup
```
git clone git@github.com:Bahmni/openmrs-module-bahmnicommons.git
cd openmrs-module-bahmnicommons
mvn clean install  #runs the unit tests and generates omod

mvn clean install -P IT #runs the unit and Integration tests and generates omod 
```