package au.com.ngv.zabbixAPI

import kotlinx.serialization.json.JsonElement

interface Request
{
    fun paramCount(): Int

    fun serialise(): JsonElement
}