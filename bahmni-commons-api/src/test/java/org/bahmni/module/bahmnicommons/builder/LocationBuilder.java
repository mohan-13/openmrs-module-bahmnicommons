package org.bahmni.module.bahmnicommons.builder;

import org.bahmni.module.bahmnicommons.visitlocation.BahmniVisitLocationServiceImpl;
import org.openmrs.Location;
import org.openmrs.LocationTag;

public class LocationBuilder {
    private Location location;

    public LocationBuilder() {
        this.location = new Location();
    }

    public LocationBuilder withVisitLocationTag() {
        location.addTag(new LocationTag(BahmniVisitLocationServiceImpl.LOCATION_TAG_SUPPORTS_VISITS, "Visit Location"));
        return this;
    }

    public LocationBuilder withParent(Location parentLocation) {
        location.setParentLocation(parentLocation);
        return this;
    }

    public Location build() {
        return location;
    }
}