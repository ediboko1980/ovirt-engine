package org.ovirt.engine.core.bll.storage.pool;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.AddClusterCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.QuotaDao;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;

public class AddEmptyStoragePoolCommand<T extends StoragePoolManagementParameter> extends
        StoragePoolManagementCommandBase<T> {

    @Inject
    private ManagementNetworkUtil managementNetworkUtil;

    @Inject
    private NetworkFilterDao networkFilterDao;

    @Inject
    private QuotaDao quotaDao;

    public AddEmptyStoragePoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected void addStoragePoolToDb() {
        getStoragePool().setId(Guid.newGuid());
        getStoragePool().setStatus(StoragePoolStatus.Uninitialized);

        getStoragePoolDao().save(getStoragePool());
    }

    @Override
    protected void executeCommand() {
        setDataCenterDetails();
        addStoragePoolToDb();
        addDefaultQuotaToDb();
        getReturnValue().setActionReturnValue(getStoragePool().getId());
        addDefaultNetworks();
        setSucceeded(true);
    }

    private void addDefaultQuotaToDb() {
        Quota quota = new Quota();
        quota.setId(Guid.newGuid());
        quota.setQuotaName("Default");
        quota.setDescription("Default unlimited quota");
        quota.setStoragePoolId(getStoragePool().getId());
        quota.setDefault(true);

        QuotaCluster quotaCluster = new QuotaCluster();
        quotaCluster.setMemSizeMB(QuotaCluster.UNLIMITED_MEM);
        quotaCluster.setVirtualCpu(QuotaCluster.UNLIMITED_VCPU);
        quota.setGlobalQuotaCluster(quotaCluster);

        QuotaStorage quotaStorage = new QuotaStorage();
        quotaStorage.setStorageSizeGB(QuotaStorage.UNLIMITED);
        quota.setGlobalQuotaStorage(quotaStorage);

        quotaDao.save(quota);
    }

    private void setDataCenterDetails() {
        StoragePool dc = getParameters().getStoragePool();
        setCompatibilityVersion(dc.getCompatibilityVersion().toString());
        setQuotaEnforcementType(dc.getQuotaEnforcementType().name());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_STORAGE_POOL : AuditLogType.USER_ADD_STORAGE_POOL_FAILED;
    }

    private void addDefaultNetworks() {
        Network net = new Network();
        net.setId(Guid.newGuid());
        net.setName(managementNetworkUtil.getDefaultManagementNetworkName());
        net.setDescription(AddClusterCommand.DefaultNetworkDescription);
        net.setDataCenterId(getStoragePool().getId());
        net.setVmNetwork(true);
        getNetworkDao().save(net);
        NetworkHelper.addPermissionsOnNetwork(getCurrentUser().getId(), net.getId());
        VnicProfile profile = NetworkHelper.createVnicProfile(net, networkFilterDao);
        getVnicProfileDao().save(profile);
        NetworkHelper.addPermissionsOnVnicProfile(getCurrentUser().getId(), profile.getId(), true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__CREATE);
    }

    @Override
    protected boolean validate() {
        boolean result = true;
        // set version to latest supported version if not given
        if (getStoragePool().getCompatibilityVersion().isNotValid()) {
            getStoragePool().setCompatibilityVersion(Version.getLast());
        }
        if (result && !isStoragePoolUnique(getStoragePool().getName())) {
            result = false;
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NAME_ALREADY_EXIST);
        } else if (!checkStoragePoolNameLengthValid()) {
            result = false;
        } else if (!VersionSupport.checkVersionSupported(getStoragePool().getCompatibilityVersion())) {
            addValidationMessage(VersionSupport.getUnsupportedVersionMessage());
            result = false;
        }
        return result;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(
                new PermissionSubject(Guid.SYSTEM, VdcObjectType.System, getActionType().getActionGroup())
        );
    }

}
