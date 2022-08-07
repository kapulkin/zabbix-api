package au.com.ngv.zabbixAPI

import kotlinx.serialization.json.JsonElement

public interface ZabbixApi
{
    fun destroy()
    fun apiVersion(): String
    fun call(name: String, request: Request): JsonElement
}