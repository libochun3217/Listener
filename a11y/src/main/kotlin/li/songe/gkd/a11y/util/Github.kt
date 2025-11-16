package li.songe.gkd.a11y.util

import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMessageBuilder
import io.ktor.http.HttpStatusCode


private var apiUserKey = ""
private val apiDevKey = "LJgKomuYvNVcfgMPRZrRXRPwShXGs3mk"
private fun HttpMessageBuilder.setCommonHeaders(cookie: String) {
    header("Cookie", cookie)
    header("Referer", "https://github.com/gkd-kit/inspect/issues/46")
    header("Origin", "https://github.com")
    header(
        "User-Agent",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 Edg/125.0.0.0"
    )
}


data class GithubCookieException(override val message: String) : Exception(message)

suspend fun upload(data: String) {
    val response = client.post("https://pastebin.com/api/api_post.php") {
        setBody(MultiPartFormDataContent(formData {
            append("api_dev_key", apiDevKey)
            append("api_paste_code", data)
            append("api_paste_name", "${System.currentTimeMillis().format("yyyy-MM-dd HH:mm")}")
            append("api_paste_expire_date", "1Y")
            append("api_user_key", apiUserKey)
            append("api_option", "paste")
        }))
    }
    if (response.status != HttpStatusCode.OK) {
        val res = response.bodyAsText()
        if (res.contains("expired api_user_key")) {
            apiUserKey = ""
            login()
        }
    }
}

suspend fun login() {
    if (apiUserKey.isNotEmpty()) return
    val res = client.post("https://pastebin.com/api/api_login.php") {
        setBody(MultiPartFormDataContent(formData {
            append("api_dev_key", apiDevKey)
            append("api_user_name", "charlee3218")
            append("api_user_password", "8921603217abc")
        }))
    }

    if (res.status == HttpStatusCode.OK) {
        apiUserKey = res.bodyAsText()
    }
}


