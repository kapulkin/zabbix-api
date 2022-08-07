package au.com.ngv.zabbixAPI

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

class RequestArray(): Request
{
    private val params: MutableList<JsonElement> = mutableListOf()

    override fun paramCount() = params.size

    fun paramEntry(value: JsonElement): RequestArray {
        params.add(value)
        return this
    }

    override fun serialise(): JsonElement = JsonArray(params)
}