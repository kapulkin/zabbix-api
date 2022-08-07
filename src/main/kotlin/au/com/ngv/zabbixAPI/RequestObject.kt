package au.com.ngv.zabbixAPI

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class RequestObject() : Request
{
    val params = mutableMapOf<String, JsonElement>()

    override fun paramCount() = params.size

    fun paramEntry(key: String, value: JsonElement): RequestObject {
        params[key] = value
        return this
    }

    override fun serialise(): JsonObject = JsonObject(params)
}