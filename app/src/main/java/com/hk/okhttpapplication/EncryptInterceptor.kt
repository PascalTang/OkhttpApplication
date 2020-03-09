package com.hk.okhttpapplication

import okhttp3.*
import java.io.IOException


class EncryptInterceptor() : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        when (chain.request().method) {
            "GET" -> {
                return encryptGet(chain)
            }
            "POST", "PUT", "DELETE" -> {
                return encryptMethod(chain, chain.request().method)
            }
        }

        return chain.proceed(chain.request())
    }

    private fun encryptGet(chain: Interceptor.Chain): Response {
        val params: HashMap<String, String> = HashMap()
        val url: HttpUrl = chain.request().url
        val urlBuilder = url.newBuilder()

        for (i in 0 until url.querySize) {
            val key = url.queryParameterName(i)
            val value = url.queryParameterValue(i)
            if (!value.isNullOrBlank()) params[key] = value

            //加密的url要移除get params
            urlBuilder.removeAllQueryParameters(key)
        }

        //將舊的path改成params
        var pathParams = ""
        for (position in 0 until url.pathSegments.size) {
            if (position < url.pathSize) pathParams += "/"
            pathParams += url.pathSegments[position]
        }

        //更新path
        for (position in 0 until url.pathSegments.size) {
            urlBuilder.removePathSegment(0)
        }
        urlBuilder.addPathSegment("api")

        params["path"] = pathParams
        params["method"] = "GET"
        params["timestamp"] = (System.currentTimeMillis() / 1000).toString()

        val formBody = FormBody.Builder()
        //將encryptParams加入post
        for ((key, value) in (params)) {
            formBody.add(key, value)
        }

        //form 務必要加上這個header
        val builder = chain
            .request()
            .newBuilder()
            .post(formBody.build())
            .url(urlBuilder.build())
            .addHeader("Content-Type", "application/x-www-form-urlencoded")

        val request = builder.build()

        return chain.proceed(request)
    }

    private fun encryptMethod(chain: Interceptor.Chain, method: String): Response {

        var params: HashMap<String, String> = HashMap()

        if (chain.request().body is FormBody) {
            params = getHttpUrlParams(chain.request().body as FormBody)
        }

        val url: HttpUrl = chain.request().url
        val urlBuilder = url.newBuilder()

        val formBody = FormBody.Builder()
//        將encryptParams加入post
        for ((key, value) in (params)) {
            formBody.addEncoded(key, value)
        }
        //fixme If add this line will 400
        formBody.addEncoded("aaa","123")

        val builder = chain
            .request()
            .newBuilder()
            .post(formBody.build())
            .url(urlBuilder.build())

        val request = builder.build()

        return chain.proceed(request)
    }

    private fun getHttpUrlParams(formBody: FormBody?): HashMap<String, String> {
        val paramMap: HashMap<String, String> = HashMap()
        if (formBody != null) {
            for (i in 0 until formBody.size) {
                paramMap[formBody.name(i)] = formBody.value(i)
            }
        }
        return paramMap
    }
}