package com.socialplatform.cache

import com.socialplatform.config.AppConfig
import redis.clients.jedis.JedisPooled
import kotlin.time.Duration

class RedisCache(config: AppConfig.Redis) {
    private val jedis = JedisPooled(config.url)
    fun get(key: String): String? = runCatching { jedis.get(key) }.getOrNull()
    fun set(key: String, value: String, ttl: Duration? = null) { if (ttl == null) jedis.set(key, value) else jedis.setex(key, ttl.inWholeSeconds, value) }
    fun del(key: String) { jedis.del(key) }
    fun incr(key: String): Long = jedis.incr(key)
    fun publish(channel: String, payload: String) { jedis.publish(channel, payload) }
    fun sadd(key: String, member: String) { jedis.sadd(key, member) }
    fun srem(key: String, member: String) { jedis.srem(key, member) }
    fun smembers(key: String): Set<String> = jedis.smembers(key)
}
