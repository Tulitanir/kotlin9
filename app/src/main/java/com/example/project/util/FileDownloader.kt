package com.example.project.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileDownloader(private val filesDir: File, private val context: Context?) {
    private val client = OkHttpClient()
    private val modelUrl = "https://huggingface.co/SmilingWolf/wd-swinv2-tagger-v3/resolve/main/model.onnx"
    private val tagsUrl = "https://huggingface.co/SmilingWolf/wd-swinv2-tagger-v3/resolve/main/selected_tags.csv"
    fun downloadFiles() {
        try {
            downloadFile(tagsUrl, "selected_tags.csv")
            downloadFile(modelUrl, "model.onnx")
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteFiles() {
        deleteFile("selected_tags.csv")
        deleteFile("model.onnx")

        Toast.makeText(context, "Файлы удалены", Toast.LENGTH_SHORT).show()
    }

    private fun downloadFile(url: String, fileName: String) {
        if (isFileExists(fileName)) {
            Log.d("File download: ", "File exists")
            return
        }

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("File download: ", e.message.toString())
                throw Exception("Не удалось загрузить файл $fileName")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { body ->
                    saveFile(body.byteStream(), fileName)
                }
            }
        })
    }

    private fun saveFile(inputStream: java.io.InputStream, fileName: String) {
        try {
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("File download: ", "success")
        } catch (e: IOException) {
            Log.d("File download: ", e.message.toString())
            throw Exception("Не удалось сохранить файл")
        }
    }

    private fun isFileExists(fileName: String): Boolean {
        val file = File(filesDir, fileName)
        return file.exists()
    }

    private fun deleteFile(fileName: String) {
        val file = File(filesDir, fileName)
        if (file.exists()) {
            file.delete()
            Log.d("File download: ","File was deleted")
        } else {
            Log.d("File download: ","File was not found")
        }
    }
}