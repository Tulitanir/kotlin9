package com.example.project.util

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.FloatBuffer

class ImageTagger(filesDir: File) {
    private val imageSize = 448;
    private val model = File(filesDir, "model.onnx")
    private val tagsFile = File(filesDir, "selected_tags.csv")
    private var tags: List<Tag>? = null

    suspend fun inference(bitmap: Bitmap): String {
        with(Dispatchers.Default) {
            if (tags == null) {
                tags = readTagsFromCSV()
            }

            val env = OrtEnvironment.getEnvironment()
            val session = env.createSession(model.absolutePath)

            val input = preProcess(bitmap, env)
            val result = session.run((mapOf("input" to input)))

            val outputTensor = result[0].value as Array<FloatArray>
            val threshold = 0.35
            val probabilities = outputTensor[0].toList()

            val filteredTags = tags!!.zip(probabilities)
                .filter { it.second > threshold }
                .sortedByDescending { it.second }
                .map { it.first.name.replace("_", " ") }

            Log.i("Tag: ", filteredTags.size.toString())

            val resultString = filteredTags.joinToString(", ")
            Log.i("Tag: ", resultString)

            input.close()
            result.close()
            session.close()
            env.close()

            return resultString
        }
    }

    private fun preProcess(bitmap: Bitmap, env: OrtEnvironment): OnnxTensor {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false)

        val floatBuffer = FloatBuffer.allocate(imageSize * imageSize * 3)

        val stride = imageSize * imageSize
        val intValues = IntArray(stride)
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

        val mean = floatArrayOf(0.5f, 0.5f, 0.5f)
        val std = floatArrayOf(0.5f, 0.5f, 0.5f)
        floatBuffer.rewind()
        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val idx = imageSize * i + j
                val pixelValue = intValues[idx]
                floatBuffer.put(((pixelValue and 0xFF)).toFloat())
                floatBuffer.put(((pixelValue shr 8 and 0xFF).toFloat()))
                floatBuffer.put(((pixelValue shr 16 and 0xFF).toFloat()))
            }
        }
        floatBuffer.rewind()
        return OnnxTensor.createTensor(env, floatBuffer, longArrayOf(1, imageSize.toLong(), imageSize.toLong(), 3))
    }

    private fun readTagsFromCSV(): List<Tag> {
        val tags = mutableListOf<Tag>()
        val inputStream = FileInputStream(tagsFile)
        val reader = CSVReader(InputStreamReader(inputStream))
        reader.skip(1)

        var line: Array<String>?

        while (reader.readNext().also { line = it } != null) {
            val tag = Tag(
                tagId = line!![0].toLong(),
                name = line!![1],
                category = line!![2].toInt(),
                count = line!![3].toLong()
            )
            tags.add(tag)
        }
        reader.close()
        return tags
    }
}