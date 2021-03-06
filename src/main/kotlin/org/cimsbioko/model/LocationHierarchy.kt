package org.cimsbioko.model

import org.cimsbioko.data.DataWrapper
import org.cimsbioko.navconfig.UsedByJSConfig
import java.io.Serializable

@UsedByJSConfig
data class LocationHierarchy(
        var uuid: String = "",
        var extId: String = "",
        var name: String = "",
        var parentUuid: String? = null,
        override var level: String = "",
        var attrs: String? = null
) : HierarchyItem, Serializable {

    override val wrapped: DataWrapper
        get() = DataWrapper(
                uuid = uuid,
                category = level,
                extId = extId,
                name = name
        )
    override val hierarchyId: String
        get() = "$level:$uuid"
}