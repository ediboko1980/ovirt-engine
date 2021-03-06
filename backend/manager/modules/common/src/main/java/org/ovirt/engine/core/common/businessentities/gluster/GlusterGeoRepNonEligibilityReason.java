package org.ovirt.engine.core.common.businessentities.gluster;

public enum GlusterGeoRepNonEligibilityReason {
        SLAVE_AND_MASTER_VOLUMES_SHOULD_NOT_BE_IN_SAME_CLUSTER,
        SLAVE_VOLUME_SIZE_SHOULD_BE_GREATER_THAN_MASTER_VOLUME_SIZE,
        SLAVE_CLUSTER_AND_MASTER_CLUSTER_COMPATIBILITY_VERSIONS_DO_NOT_MATCH,
        SLAVE_VOLUME_SHOULD_NOT_BE_SLAVE_OF_ANOTHER_GEO_REP_SESSION,
        SLAVE_VOLUME_SHOULD_BE_UP,
        SLAVE_VOLUME_SIZE_TO_BE_AVAILABLE,
        SLAVE_VOLUME_TO_BE_EMPTY,
        NO_UP_SLAVE_SERVER,
        MASTER_VOLUME_SIZE_TO_BE_AVAILABLE;
}
