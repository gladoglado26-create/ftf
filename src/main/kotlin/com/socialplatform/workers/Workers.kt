package com.socialplatform.workers

import com.socialplatform.bots.BotEngine
import com.socialplatform.posts.PostService
import io.ktor.server.application.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.hours

fun Application.startWorkers() {
    val posts by inject<PostService>()
    val bots by inject<BotEngine>()
    bots.start(this)
    launch {
        while (true) {
            delay(1.hours)
            posts.expireStories()
        }
    }
}
