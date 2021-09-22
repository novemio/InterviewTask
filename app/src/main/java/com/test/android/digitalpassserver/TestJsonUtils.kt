package com.test.android.digitalpassserver

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Type
import java.nio.charset.Charset



object TestJsonUtils {

    private val TEST_MOSHI = initializeGson()
/*	private val adapterMap: HashMap<KClass<*>, JsonAdapter<*>> = initAdapterMap()

	private fun initAdapterMap() =
		hashMapOf<KClass<*>, JsonAdapter<*>>().apply {
		}*/

/*	@Suppress("UNCHECKED_CAST")
	fun <T : Any> loadJson(path: String, clazz: KClass<T>): T {
		val json = getFileString(path)
		return if (adapterMap.containsKey(clazz)) {
			adapterMap[clazz]!!.fromJson(json) as T
		} else {
			TEST_MOSHI.adapter(clazz.java).fromJson(json)!!
		}
	}

	@Suppress("UNCHECKED_CAST")
	fun <T> loadJson(path: String, type: Type): T {
		val json = getFileString(path)

		return TEST_MOSHI.adapter<Any>(type).fromJson(json) as T
	}*/

    @Suppress("UNCHECKED_CAST")
    fun <T> loadAssetJson(context: Context, path: String, type: Type): T? {
        val json = loadJSONFromAsset(context, path)
        return json?.let {
            TEST_MOSHI.fromJson(it, type) as T
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> loadAssetJson(context: Context, path: String, clazz: Class<T>): T? {
        val json = loadJSONFromAsset(context, path)
        return json?.let {
            TEST_MOSHI.fromJson(it, clazz) as T
        }
    }

    fun loadJSONFromAsset(contex: Context, path: String): String? {
        val json: String? = try {
            val inputStream: InputStream = contex.assets.open(path)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

/*	private fun getFileString(path: String): String {
		val uri = TestJsonUtils::class.java.classLoader!!.getResource(path).toURI()
		return String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"))
	}*/

    private fun initializeGson(): Gson {
        val builder = GsonBuilder()
        return builder.create()
    }

}





