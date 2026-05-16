package com.socialplatform.database

import com.socialplatform.config.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: AppConfig.Database) {
        // Railway/Supabase often provide a full DATABASE_URL: postgresql://user:pass@host:port/db
        // Hikari/JDBC needs: jdbc:postgresql://host:port/db
        val rawUrl = System.getenv("DATABASE_URL") ?: System.getenv("DB_URL") ?: config.jdbcUrl
        
        // If the URL contains credentials (user:pass@), Hikari might struggle if we also provide user/pass separately.
        // Also, we must convert postgresql:// to jdbc:postgresql://
        val jdbcUrl = when {
            rawUrl.startsWith("postgresql://") -> rawUrl.replace("postgresql://", "jdbc:postgresql://")
            rawUrl.startsWith("postgres://") -> rawUrl.replace("postgres://", "jdbc:postgresql://")
            else -> rawUrl
        }

        // If the URL already contains the username/password, we should NOT set them again in HikariConfig
        // as it can cause "UnknownHostException" if the driver tries to parse the credentials as part of the host.
        val hasCredentialsInUrl = jdbcUrl.contains("@") && jdbcUrl.startsWith("jdbc:postgresql://")

        println("Initializing database connection...")
        println("JDBC URL detected (masked): ${jdbcUrl.take(20)}...") 

        val hikari = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            
            if (!hasCredentialsInUrl) {
                this.username = System.getenv("DATABASE_USER") ?: System.getenv("DB_USER") ?: config.username
                this.password = System.getenv("DATABASE_PASSWORD") ?: System.getenv("DB_PASSWORD") ?: config.password
            } else {
                println("Credentials detected in URL, skipping separate user/pass configuration.")
            }

            this.maximumPoolSize = config.poolSize
            this.driverClassName = "org.postgresql.Driver"
            
            // Railway/Supabase often require SSL
            addDataSourceProperty("sslmode", "require")
            
            // Connection timeout and validation
            connectionTimeout = 30000
            idleTimeout = 600000
            maxLifetime = 1800000
            
            validate()
        }
        
        try {
            Database.connect(HikariDataSource(hikari))
            println("Database connection established successfully.")
        } catch (e: Exception) {
            println("Failed to connect to database: ${e.message}")
            throw e
        }
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Users, Follows, Blocks, Posts, Stories, Likes, Saves, Comments,
                Conversations, ConversationMembers, Messages, MessageReactions,
                Calls, Notifications, DeviceTokens, BotRules, BotLogs, Uploads
            )
        }
    }
}
