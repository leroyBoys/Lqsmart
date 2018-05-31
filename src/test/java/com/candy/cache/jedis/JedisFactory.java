package com.candy.cache.jedis;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

/**
 * PoolableObjectFactory custom impl.
 */
public class JedisFactory implements PooledObjectFactory<Jedis> {
	private final AtomicReference<HostAndPort> hostAndPort = new AtomicReference<HostAndPort>();
	private final int timeout;
	private final String password;
	private final int database;
	private final String clientName;
	private final JedisSentinelPool pool;

	public JedisFactory(final HostAndPort hostAndPort, final int timeout, final String password,
			final JedisSentinelPool pool) {
		this.hostAndPort.set(hostAndPort);
		this.timeout = timeout;
		this.password = password;
		this.database = 0;
		this.clientName = null;
		this.pool = pool;
	}

	public void setHostAndPort(final HostAndPort hostAndPort) {
		this.hostAndPort.set(hostAndPort);
	}

	@Override
	public void activateObject(PooledObject<Jedis> pooledJedis) throws Exception {
		final BinaryJedis jedis = pooledJedis.getObject();
		if (jedis.getDB() != database) {
			jedis.select(database);
		}

	}

	@Override
	public void destroyObject(PooledObject<Jedis> pooledJedis) throws Exception {
		final BinaryJedis jedis = pooledJedis.getObject();
		if (jedis.isConnected()) {
			try {
				try {
					jedis.quit();
				} catch (Exception e) {
				}
				jedis.disconnect();
			} catch (Exception e) {

			}
		}

	}

	@Override
	public PooledObject<Jedis> makeObject() throws Exception {
		final HostAndPort hostAndPort = this.hostAndPort.get();
		final Jedis jedis = new Jedis(hostAndPort.getHost(), hostAndPort.getPort(), this.timeout);

		jedis.connect();
		if (null != this.password) {
			jedis.auth(this.password);
		}
		if (database != 0) {
			jedis.select(database);
		}
		if (clientName != null) {
			jedis.clientSetname(clientName);
		}
		if (pool != null) {
			jedis.setDataSource(pool);
		}

		return new DefaultPooledObject<Jedis>(jedis);
	}

	@Override
	public void passivateObject(PooledObject<Jedis> pooledJedis) throws Exception {
		// TODO maybe should select db 0? Not sure right now.
	}

	@Override
	public boolean validateObject(PooledObject<Jedis> pooledJedis) {
		final BinaryJedis jedis = pooledJedis.getObject();
		try {
			HostAndPort hostAndPort = this.hostAndPort.get();

			String connectionHost = jedis.getClient().getHost();
			int connectionPort = jedis.getClient().getPort();

			return hostAndPort.getHost().equals(connectionHost) && hostAndPort.getPort() == connectionPort
					&& jedis.isConnected() && jedis.ping().equals("PONG");
		} catch (final Exception e) {
			return false;
		}
	}
}
