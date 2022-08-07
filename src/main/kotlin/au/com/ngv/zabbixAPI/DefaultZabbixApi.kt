package au.com.ngv.zabbixAPI

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI

class DefaultZabbixApi(val uri: URI,
                       user: String,
                       password: String,
                       val httpClient: CloseableHttpClient = HttpClients.custom().build(),
                       val jsonrpc: String = "2.0",
                       val Id: Int = 1) : ZabbixApi
{
    override fun destroy() =
        try {
            if (connected())
                call("user.logout", RequestObject())
            httpClient.close()
        } catch (e:Exception) {
            LoggerFactory.getLogger(DefaultZabbixApi::class.java).error("Close HTTPClient", e)
        }

    private val auth: String? =
        callNoAuth(
            "user.login",
            RequestObject()
                .paramEntry("user", JsonPrimitive(user))
                .paramEntry("password", JsonPrimitive(password))
        )[resultText]?.let { Json.decodeFromJsonElement<String>(it) }

    private fun callNoAuth(name: String, request: Request): JsonObject {
        val body = mapOf(
            "jsonrpc" to JsonPrimitive(jsonrpc),
            "method" to JsonPrimitive(name),
            "params" to request.serialise(),
            "id" to JsonPrimitive(Id)
        )
        return call0(JsonObject(body))
    }

    fun connected() = auth != null

    override fun apiVersion() = Json.decodeFromJsonElement<String>(
        call(
            "apiinfo.version", RequestObject()
        )[resultText] ?: throw IllegalStateException("Response field $resultText is empty")
    )

    fun hostExists(name:String) =
        Json.decodeFromJsonElement<Boolean>(
            call(
                "host.exists",
                RequestObject().paramEntry("name", JsonPrimitive(name))
            )[resultText] ?: throw IllegalStateException("Response field $resultText is empty")
        )

    fun hostCreate(host: String, groupId: String): String{
        val group = JsonObject(
            mapOf("groupid" to JsonPrimitive(groupId))
        )
        val groups = JsonArray(listOf(group))
        val response = call(
            "host.create",
            RequestObject()
                .paramEntry("host", JsonPrimitive(host))
                .paramEntry("groups", groups)
        )
        return Json.decodeFromJsonElement(
            ((response[resultText] as JsonObject)["hostids"] as JsonArray)[0]
        )
    }

    fun hostgroupExists(name:String) = Json.decodeFromJsonElement<Boolean>(
        call(
            "hostgroup.exists", RequestObject().paramEntry("name", JsonPrimitive(name))
        )[resultText] ?: throw IllegalStateException("Response field $resultText is empty")
    )

    /**
     *
     * @param name
     * @return groupId
     */
    fun hostgroupCreate(name:String) = Json.decodeFromJsonElement<String>(
        ((call(
            "hostgroup.create", RequestObject().paramEntry("name", JsonPrimitive(name))
        )[resultText] as JsonObject)["groupids"] as JsonArray)[0]
    )

    override fun call(name: String, request: Request): JsonObject {
        val body = mapOf(
            "jsonrpc" to JsonPrimitive(jsonrpc),
            "method" to JsonPrimitive(name),
            "params" to request.serialise(),
            "id" to JsonPrimitive(Id),
            "auth" to JsonPrimitive(auth))
        return call0(JsonObject(body))
    }

    private fun call0(body: JsonObject): JsonObject {
        try {
            val httpRequest = org.apache.http.client.methods.RequestBuilder.post().setUri(uri)
                .addHeader("Content-Type", "application/json")
                .setEntity(StringEntity(Json.encodeToString(body), ContentType.APPLICATION_JSON)).build()
            val response = httpClient.execute(httpRequest)
            return Json.decodeFromString(String(EntityUtils.toByteArray(response.entity)))
        } catch (e: IOException) {
            throw RuntimeException("DefaultZabbixApi call exception!", e)
        }
    }
}


fun JsonObject.returnOK() = containsKey(resultText)

private const val resultText = "result"
