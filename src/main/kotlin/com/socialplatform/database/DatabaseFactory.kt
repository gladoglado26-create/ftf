package com.socialplatform.database

import com.socialplatform.config.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: AppConfig.Database) {
        val hikari = HikariConfig().apply {
            jdbcUrl = config.jdbcUrl
            username = config.username
            password = config.password
            maximumPoolSize = config.poolSize
            driverClassName = "org.postgresql.Driver"
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
