package com.socialplatform.database

import com.socialplatform.config.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: AppConfig.Database) {
        val rawUrl = System.getenv("DATABASE_URL") ?: System.getenv("DB_URL") ?: config.jdbcUrl
        
        var finalJdbcUrl = rawUrl
        var finalUser = System.getenv("DATABASE_USER") ?: System.getenv("DB_USER") ?: config.username
        var finalPass = System.getenv("DATABASE_PASSWORD") ?: System.getenv("DB_PASSWORD") ?: config.password

        // Manual parsing for postgresql://user:pass@host:port/db
        if (rawUrl.startsWith("postgresql://") || rawUrl.startsWith("postgres://")) {
            try {
                val cleanUrl = rawUrl.substringAfter("://")
                if (cleanUrl.contains("@")) {
                    val credentials = cleanUrl.substringBefore("@")
                    val hostPart = cleanUrl.substringAfter("@")
                    
                    finalUser = credentials.substringBefore(":")
                    finalPass = credentials.substringAfter(":")
                    finalJdbcUrl = "jdbc:postgresql://$hostPart"
                    
                    println("Manually parsed credentials from DATABASE_URL.")
                } else {
                    finalJdbcUrl = "jdbc:postgresql://$cleanUrl"
                }
            } catch (e: Exception) {
                println("Failed to manually parse DATABASE_URL, falling back to simple replacement: ${e.message}")
                finalJdbcUrl = rawUrl.replace("postgresql://", "jdbc:postgresql://").replace("postgres://", "jdbc:postgresql://")
            }
        }

        println("Initializing database connection...")
        println("Final JDBC URL (masked): ${finalJdbcUrl.take(30)}...") 

        val hikari = HikariConfig().apply {
            this.jdbcUrl = finalJdbcUrl
            this.username = finalUser
            this.password = finalPass
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
