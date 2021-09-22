package com.test.android.digitalpassserver.model

import android.content.Context
import com.google.gson.reflect.TypeToken
import com.test.android.digitalpassserver.TestJsonUtils
import java.lang.reflect.Type
import java.util.*
import kotlin.random.Random


object CreateUserResponse {


    fun create(context: Context) = mutableMapOf<String, Any>().apply {
        put("user", getUser(context))
        getAllPasses(context).apply {
            println("passes ${this.size}")
        }.forEach {
            println("pass $it")
            val nextInt = UUID.randomUUID()
            println("PassName id $nextInt")
            put("pass${nextInt}", it)
        }

    }


    private fun getAllPasses(context: Context): List<Pass> {
        val id: Long = System.currentTimeMillis()% 4
        val name = "mock/passes/passes$id.json"
        val type: Type = object : TypeToken<List<Pass>>() {}.type
        println("getAllPasses path:$name")//todo Zasto println
        return TestJsonUtils.loadAssetJson<List<Pass>>(context, name, type)
            ?: listOf(Pass("ErrorPass", "ParseError", "base64Image"))

    }

    private fun getUser(context: Context): UserAccount {
        return TestJsonUtils.loadAssetJson(context, "mock/cache/user.json", UserAccount::class.java)
            ?: UserAccount("BenError", "ColmonError", "base64Image")

    }

}