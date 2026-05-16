package com.socialplatform.database

import com.socialplatform.config.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: AppConfig.Database) {
        val dbUrl = System.getenv("DB_URL")?.trim() ?: config.jdbcUrl
        val dbUser = System.getenv("DB_USER")?.trim() ?: config.username
        val dbPass = System.getenv("DB_PASSWORD")?.trim() ?: config.password

        val hikari = HikariConfig().apply {
            jdbcUrl = dbUrl
            username = dbUser
            password = dbPass
            maximumPoolSize = config.poolSize
            driverClassName = "org.postgresql.Driver"
            addDataSourceProperty("sslmode", "require")
            validate()
        }
        Database.connect(HikariDataSource(hikari))
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Users, Follows, Blocks, Posts, Stories, Likes, Saves, Comments,
                Conversations, ConversationMembers, Messages, MessageReactions,
                Calls, Notifications, DeviceTokens, BotRules, BotLogs, Uploads
            )
        }
    }
}
