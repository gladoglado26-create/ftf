package com.socialplatform.cache

import kotlin.time.Duration

class NoOpRedisCache : RedisCache(url = "") {
    override fun get(key: String): String? = null
    override fun set(key: String, value: String, ttl: Duration?) {}
    override fun del(key: String) {}
    override fun incr(key: String): Long = 0L
    override fun publish(channel: String, payload: String) {}
    override fun sadd(key: String, member: String) {}
    override fun srem(key: String, member: String) {}
    override fun smembers(key: String): Set<String> = emptySet()
}
