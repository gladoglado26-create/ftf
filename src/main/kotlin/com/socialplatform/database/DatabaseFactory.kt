package com.socialplatform.database

import com.socialplatform.config.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: AppConfig.Database) {
        // Railway provides DATABASE_URL in the format: postgresql://user:pass@host:port/db
        // Hikari/JDBC needs: jdbc:postgresql://host:port/db
        val rawUrl = System.getenv("DATABASE_URL") ?: System.getenv("DB_URL") ?: config.jdbcUrl
        
        val jdbcUrl = if (rawUrl.startsWith("postgresql://")) {
            rawUrl.replace("postgresql://", "jdbc:postgresql://")
        } else {
            rawUrl
        }

        val dbUser = System.getenv("DATABASE_USER") ?: System.getenv("DB_USER") ?: config.username
        val dbPass = System.getenv("DATABASE_PASSWORD") ?: System.getenv("DB_PASSWORD") ?: config.password

        println("Initializing database connection...")
        println("JDBC URL: ${jdbcUrl.take(25)}...") // Log partial URL for safety

        val hikari = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = dbUser
            this.password = dbPass
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
