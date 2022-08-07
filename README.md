# zabbix-api
Zabbix API wrapper for Kotlin.

https://www.zabbix.com/wiki/doc/api

https://www.zabbix.com/documentation/5.0/manual/api/reference/user/login

Thanks @enngeevee for original repo with suppoert both for Kotlin and Java

## Info
This package is based on hengyunabc's but:
- Rewritten in Kotlin.
- Removed superfluous layers of builders, factories and incremental construction that for some reason always go with Java.
- Crucially, API requests that taken a `"param"` *array* value are supported.

- Bug reports are welcome.
- PRs for extra functionality that doesn't detract from the functional value-based style will be seriously considered.
- Requests/PRs for rewrites to accommodate builders, incremental construction etc. are not welcome.

## Example
You can set your own ```HttpClient``` but you don't have to;  a default will be supplied.

```kotlin
    val zabbixAPI=DefaultZabbixApi(URI(zabbixHost),
        zabbixUser,
        zabbixPassword,
        HttpClients
            .custom()
            .setConnectionManager(PoolingHttpClientConnectionManager())
            .setDefaultRequestConfig(
                RequestConfig.custom()
                .setConnectTimeout(5 * 1000)
                .setConnectionRequestTimeout(5 * 1000)
                .setSocketTimeout(5 * 1000)
                .build())
            .build())
    if (zabbixAPI.connected()) {
	    val hostJSON=zabbixAPI.call("host.get",
            RequestObject().paramEntry("filter",HostSpec(zabbixMonitorHost))).getJSONArray("result")
        logger.info("HOSTS: $hostJSON")
    }
    zabbixAPI.destroy()
```

## Licence
Apache Licence V2
