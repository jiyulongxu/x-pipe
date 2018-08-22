package com.ctrip.xpipe.redis.console.health.redisconf.version;

import com.ctrip.xpipe.endpoint.HostPort;
import com.ctrip.xpipe.redis.console.alert.ALERT_TYPE;
import com.ctrip.xpipe.redis.console.health.*;
import com.ctrip.xpipe.redis.console.health.redisconf.Callbackable;
import com.ctrip.xpipe.redis.core.entity.ClusterMeta;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author chen.zhu
 * <p>
 * Sep 25, 2017
 */

@Component
@Lazy
@ConditionalOnProperty(name = { HealthChecker.ENABLED }, matchIfMissing = true)
public class VersionMonitor extends AbstractRedisConfMonitor<VersionInstanceResult> {

    @Autowired
    private List<VersionCollector> collectors;

    @Override
    protected void notifyCollectors(Sample<VersionInstanceResult> sample) {
        collectors.forEach(collector->collector.collect(sample));
    }

    @Override
    protected List<ALERT_TYPE> alertTypes() {
        return Lists.newArrayList(ALERT_TYPE.XREDIS_VERSION_NOT_VALID);
    }

    @Override
    protected void doStartSample(BaseSamplePlan<VersionInstanceResult> plan) {
        long startNanoTime = recordSample(plan);
        sampleVersionCheck(startNanoTime, plan);
    }

    private void sampleVersionCheck(long startNanoTime, BaseSamplePlan<VersionInstanceResult> plan) {
        for (Map.Entry<HealthCheckEndpoint, VersionInstanceResult> entry : plan.getHostPort2SampleResult().entrySet()) {

            HealthCheckEndpoint endpoint = entry.getKey();
            try{
                RedisSession redisSession = findRedisSession(endpoint);
                redisSession.infoServer(new Callbackable<String>() {
                    @Override
                    public void success(String message) {
                        addInstanceSuccess(startNanoTime, endpoint, message);
                    }

                    @Override
                    public void fail(Throwable throwable) {
                        addInstanceFail(startNanoTime, endpoint, throwable);
                    }
                });
            }catch (Exception e){
                addInstanceFail(startNanoTime, endpoint, e);
            }
        }
    }

    @Override
    protected BaseSamplePlan<VersionInstanceResult> createPlan(String dcId, String clusterId, String shardId) {
        return new VersionSamplePlan(clusterId, shardId);
    }

    @Override
    protected void addRedis(BaseSamplePlan<VersionInstanceResult> plan, String dcId, HealthCheckEndpoint endpoint) {
        log.debug("[addRedis]{}", endpoint.getHostPort());
        plan.addRedis(dcId, endpoint, new VersionInstanceResult());
    }


    @Override
    protected boolean addCluster(String dcName, ClusterMeta clusterMeta) {
        return true;
    }
}
