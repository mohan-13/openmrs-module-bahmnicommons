package org.bahmni.module.bahmnicommons.api.visitlocation;

import org.openmrs.Location;
import org.openmrs.Visit;

import java.util.List;

public interface BahmniVisitLocationService {
   String getVisitLocationUuid(String loginLocationUuid);
   Location getVisitLocation(String loginLocationUuid);
   Visit getMatchingVisitInLocation(List<Visit> visits, String locationUuid);
}
