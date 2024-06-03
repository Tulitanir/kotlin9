package com.example.project.image

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.project.R
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class ImageGenerationFragment : Fragment() {

    private lateinit var retrofitBuilder: Retrofit
    private lateinit var apiInterface: HuggingFaceApiInterface
    private lateinit var promptEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var generateButton: Button
    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar
    private var generatedImageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val token = getTokenFromPreferences()

        retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://api-inference.huggingface.co/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build())
            .build()

        apiInterface = retrofitBuilder.create(HuggingFaceApiInterface::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image_generation, container, false)
        promptEditText = view.findViewById(R.id.promptEditTextHF)
        generateButton = view.findViewById(R.id.generateButtonHF)
        saveButton = view.findViewById(R.id.saveButtonHF)
        imageView = view.findViewById(R.id.imageViewHF)
        progressBar = view.findViewById(R.id.progressBar)

        generateButton.setOnClickListener {
            val prompt = promptEditText.text.toString().trim()
            if (prompt.isEmpty()) {
                Toast.makeText(context, "Пожалуйста, введите подсказку", Toast.LENGTH_SHORT).show()
            }

            else {
                fetchImage(prompt)
            }
        }

        saveButton.setOnClickListener {
            generatedImageFile?.let { saveImageToDevice(it) }
        }

        return view
    }

    private fun saveImageToDevice(file: File) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "generated_image_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val contentResolver = requireContext().contentResolver
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            contentResolver.openOutputStream(uri).use { outputStream ->
                file.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream!!)
                }
            }

            Toast.makeText(context, "Изображение сохранено на устройство", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Не удалось сохранить изображение на устройство", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchImage(prompt: String) {
        val seed = Random.nextInt()
        imageView.visibility = View.GONE
        saveButton.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        val payload = mapOf("inputs" to prompt, "seed" to seed.toString())
        val call = apiInterface.query(payload)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    progressBar.visibility = View.GONE
                    response.body()?.let { responseBody ->
                        val inputStream = responseBody.byteStream()
                        val imageBytes = inputStream.readBytes()
                        val tempFile = File.createTempFile("generated_image", ".png", context?.cacheDir)
                        tempFile.writeBytes(imageBytes)
                        generatedImageFile = tempFile
                        imageView.visibility = View.VISIBLE
                        saveButton.visibility = View.VISIBLE
                        Picasso.get().load(tempFile).into(imageView)
                    }
                } else {
                    progressBar.visibility = View.GONE
                    if (response.code() == 400) {
                        Toast.makeText(context, "Ошибка при получении изображения: ${response.message()}. Возможно API токен недействителен", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(context, "Ошибка при получении изображения: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getTokenFromPreferences(): String {
        val sharedPreferences = requireContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("api_token", "") ?: ""
    }

    companion object {
        @JvmStatic
        fun newInstance() = ImageGenerationFragment()
    }
}
