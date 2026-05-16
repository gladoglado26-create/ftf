package com.socialplatform.config

data class AppConfig(
    val app: App = App(),
    val database: Database,
    val redis: Redis,
    val rabbit: Rabbit,
    val supabase: Supabase,
    val security: Security,
    val fcm: Fcm,
    val jamendo: Jamendo
) {
    data class App(val env: String = env("APP_ENV", "dev"), val publicUrl: String = env("PUBLIC_URL", "http://localhost:8080"))
    data class Database(val jdbcUrl: String, val username: String, val password: String, val poolSize: Int)
    data class Redis(val url: String)
    data class Rabbit(val uri: String, val exchange: String)
    data class Supabase(val url: String, val anonKey: String, val serviceRoleKey: String, val jwtSecret: String, val storageBucket: String)
    data class Security(val jwtAudience: String, val jwtIssuer: String, val rateLimitPerMinute: Int)
    data class Fcm(val serverKey: String)
    data class Jamendo(val clientId: String)

    companion object {
        fun fromEnv() = AppConfig(
            database = Database(env("DATABASE_URL", "jdbc:postgresql://localhost:5432/social"), env("DATABASE_USER", "postgres"), env("DATABASE_PASSWORD", "postgres"), env("DATABASE_POOL_SIZE", "10").toInt()),
            redis = Redis(env("REDIS_URL", "redis://localhost:6379")),
            rabbit = Rabbit(env("RABBITMQ_URI", "amqp://guest:guest@localhost:5672/"), env("RABBITMQ_EXCHANGE", "social.events")),
            supabase = Supabase(env("SUPABASE_URL", "http://localhost:54321"), env("SUPABASE_ANON_KEY", "dev-anon"), env("SUPABASE_SERVICE_ROLE_KEY", "dev-service"), env("SUPABASE_JWT_SECRET", "dev-secret-change-me"), env("SUPABASE_STORAGE_BUCKET", "media")),
            security = Security(env("JWT_AUDIENCE", "authenticated"), env("JWT_ISSUER", "supabase"), env("RATE_LIMIT_PER_MINUTE", "120").toInt()),
            fcm = Fcm(env("FCM_SERVER_KEY", "")),
            jamendo = Jamendo(env("JAMENDO_CLIENT_ID", ""))
        )
    }
}

fun env(name: String, default: String? = null): String = System.getenv(name) ?: default ?: error("Missing required env var: $name")
