package it.niedermann.owncloud.notes.persistence

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody

class ResponseBodyTypeAdapter : TypeAdapter<ResponseBody>() {
    override fun write(out: JsonWriter, value: ResponseBody?) {
        throw UnsupportedOperationException("Not needed")
    }

    override fun read(jsonReader: JsonReader): ResponseBody {
        return ResponseBody.create("application/json".toMediaTypeOrNull(), jsonReader.nextString())
    }
}
