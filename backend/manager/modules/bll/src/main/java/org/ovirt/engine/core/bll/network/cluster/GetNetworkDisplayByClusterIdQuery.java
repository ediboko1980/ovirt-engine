package org.ovirt.engine.core.bll.network.cluster;

import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.queries.VdsGroupQueryParamenters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class GetNetworkDisplayByClusterIdQuery<P extends VdsGroupQueryParamenters> extends QueriesCommandBase<P> {
    public GetNetworkDisplayByClusterIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid vdsgroupid = getParameters().getVdsGroupId();
        List<NetworkCluster> all = getDbFacade().getNetworkClusterDao().getAllForCluster(vdsgroupid);
        final NetworkCluster nc = LinqUtils.firstOrNull(all, new Predicate<NetworkCluster>() {
            @Override
            public boolean eval(NetworkCluster networkCluster) {
                return networkCluster.isDisplay();
            }
        });
        if (nc != null) {
            getQueryReturnValue().setReturnValue(
                    LinqUtils.firstOrNull(getDbFacade().getNetworkDao().getAllForCluster(vdsgroupid),
                            new Predicate<Network>() {
                                @Override
                                public boolean eval(Network network) {
                                    return network.getId().equals(nc.getNetworkId());
                                }
                            }));
        }
    }
}
