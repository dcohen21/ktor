package org.jetbrains.ktor.request

import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.cio.*
import org.jetbrains.ktor.content.*
import org.jetbrains.ktor.pipeline.*
import org.jetbrains.ktor.util.*
import java.io.*
import kotlin.reflect.*

class ApplicationReceiveRequest(val type: KClass<*>, val value: Any)
open class ApplicationReceivePipeline : Pipeline<ApplicationReceiveRequest, ApplicationCall>(Before, Transform, After) {
    companion object Phases {
        val Before = PipelinePhase("Before")

        val Transform = PipelinePhase("Transform")

        val After = PipelinePhase("After")
    }
}

@Deprecated("Use receiveOrNull instead", ReplaceWith("receiveOrNull()"))
inline suspend fun <reified T : Any> ApplicationCall.tryReceive(): T? = receiveOrNull()

inline suspend fun <reified T : Any> ApplicationCall.receiveOrNull(): T? = receiveOrNull(T::class)
inline suspend fun <reified T : Any> ApplicationCall.receive(): T {
    val type = T::class
    return receiveOrNull(type) ?: throw ContentTransformationException("Cannot transform this request's content to $type")
}

/**
 * Receive content for this request
 */
suspend fun <T : Any> ApplicationCall.receiveOrNull(type: KClass<T>): T? {
    val incomingContent = request.receiveContent()
    val receiveRequest = ApplicationReceiveRequest(type, incomingContent)
    val transformed = request.pipeline.execute(this, receiveRequest).value
    if (transformed is IncomingContent)
        return null

    @Suppress("UNCHECKED_CAST")
    return transformed as? T
}

@Suppress("NOTHING_TO_INLINE")
inline suspend fun ApplicationCall.receiveText(): String = receive()

@Suppress("NOTHING_TO_INLINE")
inline suspend fun ApplicationCall.receiveChannel(): ReadChannel = receive()

@Suppress("NOTHING_TO_INLINE")
inline suspend fun ApplicationCall.receiveStream(): InputStream = receive()

@Suppress("NOTHING_TO_INLINE")
inline suspend fun ApplicationCall.receiveMultipart(): MultiPartData = receive()

@Suppress("NOTHING_TO_INLINE")
inline suspend fun ApplicationCall.receiveParameters(): ValuesMap = receive()

class ContentTransformationException(message: String) : Exception(message)