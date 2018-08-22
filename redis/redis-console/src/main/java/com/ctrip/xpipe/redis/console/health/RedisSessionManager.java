package com.ctrip.xpipe.redis.console.health;

import com.ctrip.xpipe.api.endpoint.Endpoint;
import com.ctrip.xpipe.endpoint.HostPort;

/**
 * @author marsqing
 *
 * Dec 1, 2016 6:40:43 PM
 */
public interface RedisSessionManager {
	RedisSession findOrCreateSession(HealthCheckEndpoint endpoint);

	RedisSession findOrCreateSession(HostPort hostPort);
}
