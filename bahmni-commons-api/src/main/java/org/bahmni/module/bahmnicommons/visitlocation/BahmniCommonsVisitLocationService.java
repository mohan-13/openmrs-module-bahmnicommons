package org.bahmni.module.bahmnicommons.visitlocation;

import org.openmrs.Location;
import org.openmrs.Visit;

import java.util.List;

public interface BahmniCommonsVisitLocationService {
   String getVisitLocationUuid(String loginLocationUuid);
   Location getVisitLocation(String loginLocationUuid);
   Visit getMatchingVisitInLocation(List<Visit> visits, String locationUuid);
}
