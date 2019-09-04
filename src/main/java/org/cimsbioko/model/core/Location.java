package org.cimsbioko.model.core;

import java.io.Serializable;

public class Location implements Serializable {

	private static final long serialVersionUID = 230186771721044764L;

    private String uuid;
	private String extId;
	private String name;
	private String latitude;
	private String longitude;
	private String hierarchyUuid;
    private String mapAreaName;
    private String sectorName;
    private int buildingNumber;
    private String description;
    private String attrs;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(int buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public String getExtId() {
		return extId;
	}

	public void setExtId(String extId) {
		this.extId = extId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getHierarchyUuid() {
		return hierarchyUuid;
	}

	public void setHierarchyUuid(String hierarchyUuid) {
		this.hierarchyUuid = hierarchyUuid;
	}

    public String getSectorName() {
        return sectorName;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

	public String getMapAreaName() {
		return mapAreaName;
	}

	public void setMapAreaName(String mapAreaName) {
		this.mapAreaName = mapAreaName;
	}

    public String getAttrs() {
        return attrs;
    }

    public void setAttrs(String attrs) {
        this.attrs = attrs;
    }
}