package com.example.woocom.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import kotlin.time.Instant

// Serializers for the two shapes that do not map one-to-one between Firestore and
// Postgres. Both are pure functions, so they are unit tested without a backend.

/**
 * Postgres `timestamptz` arrives from PostgREST as an ISO-8601 instant
 * (`2026-10-07T09:15:00.123456+00:00`), while the app stores and compares epoch
 * milliseconds. This bridges the two without changing the [OrderModel] field type.
 *
 * `kotlin.time.Instant` is used rather than `java.time` so the code runs unguarded on
 * minSdk 24 (java.time needs API 26 or core library desugaring).
 */
object IsoDateTimeAsEpochMillis : KSerializer<Long> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.example.woocom.model.IsoDateTimeAsEpochMillis", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Long {
        val raw = decoder.decodeString()
        return runCatching { Instant.parse(raw).toEpochMilliseconds() }.getOrElse { 0L }
    }

    override fun serialize(
        encoder: Encoder,
        value: Long,
    ) {
        encoder.encodeString(Instant.fromEpochMilliseconds(value).toString())
    }
}

/**
 * `ProductModel.otherDetails` is a Firestore map of loosely-typed values, which
 * kotlinx-serialization cannot reflect on. Decoding through JSON and widening the
 * primitives here keeps the `Map<String, Any>` the UI already renders.
 */
object AnyMapAsJsonObject : KSerializer<Map<String, Any>> {
    private val delegate = MapSerializer(String.serializer(), JsonElement.serializer())

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun deserialize(decoder: Decoder): Map<String, Any> =
        decoder.decodeSerializableValue(delegate).mapValues { (_, value) -> value.widen() }

    override fun serialize(
        encoder: Encoder,
        value: Map<String, Any>,
    ) {
        encoder.encodeSerializableValue(delegate, value.mapValues { (_, element) -> element.narrow() })
    }

    private fun JsonElement.widen(): Any =
        when (this) {
            is JsonObject -> entries.associate { (key, child) -> key to child.widen() }
            is JsonArray -> map { it.widen() }
            is JsonPrimitive ->
                when {
                    isString -> content
                    else -> booleanOrNull ?: longOrNull ?: doubleOrNull ?: content
                }
        }

    private fun Any.narrow(): JsonElement =
        when (this) {
            is JsonElement -> this
            is Boolean -> JsonPrimitive(this)
            is Number -> JsonPrimitive(this)
            is String -> JsonPrimitive(this)
            is Map<*, *> -> JsonObject(entries.associate { (key, child) -> key.toString() to child.narrowOrJsonNull() })
            is Iterable<*> -> JsonArray(map { it.narrowOrJsonNull() })
            else -> JsonPrimitive(toString())
        }

    private fun Any?.narrowOrJsonNull(): JsonElement = this?.narrow() ?: kotlinx.serialization.json.JsonNull
}
