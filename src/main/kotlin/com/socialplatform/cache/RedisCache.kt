package com.socialplatform.cache

import com.socialplatform.config.AppConfig
import redis.clients.jedis.JedisPooled
import kotlin.time.Duration

open class RedisCache(config: AppConfig.Redis) {
    private val jedis = try {
        JedisPooled(config.url)
    } catch (e: Exception) {
        println("Failed to connect to Redis: ${e.message}")
        null
    }
    open fun get(key: String): String? = jedis?.get(key)
    open fun set(key: String, value: String, ttl: Duration? = null) { if (jedis == null) return; if (ttl == null) jedis.set(key, value) else jedis.setex(key, ttl.inWholeSeconds, value) }
    open fun del(key: String) { jedis?.del(key) }
    open fun incr(key: String): Long = jedis?.incr(key) ?: 0L
    open fun publish(channel: String, payload: String) { jedis?.publish(channel, payload) }
    open fun sadd(key: String, member: String) { jedis?.sadd(key, member) }
    open fun srem(key: String, member: String) { jedis?.srem(key, member) }
    open fun smembers(key: String): Set<String> = jedis?.smembers(key) ?: emptySet()
}
