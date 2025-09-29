package com.example.lab_week_05

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.lab_week_05.api.CatApiService
import com.example.lab_week_05.model.ImageData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit by lazy{
        Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/v1/")
            .addConverterFactory(MoshiConverterFactory.create(moshi)) // Restored original converter
            .build()
    }

    private val catApiService by lazy {
        retrofit.create(CatApiService::class.java)
    }

    private val apiResponseView: TextView by lazy {
        findViewById(R.id.api_response)
    }

    private val imageResultView: ImageView by lazy {
        findViewById(R.id.image_result)
    }
    private val imageLoader: ImageLoader by lazy {
        GlideLoader(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getCatImageResponse()
    }

    private fun getCatImageResponse() {

        val call = catApiService.searchImages(1, "full")
        

        call.enqueue(object : Callback<List<ImageData>> {
            override fun onFailure(call: Call<List<ImageData>>, t: Throwable) {
                Log.e(MAIN_ACTIVITY, "Failed to get response", t)
            }

            override fun onResponse(
                call: Call<List<ImageData>>,
                response: Response<List<ImageData>>
            ) {

                if (response.isSuccessful) {
                    val images = response.body()
                    Log.d(MAIN_ACTIVITY, "Parsed Response: $images")

                    val firstImage = images?.firstOrNull()?.imageUrl.orEmpty()
                    val catBreedInfo = images?.firstOrNull()?.breeds.orEmpty().firstOrNull()

                    val breedName = catBreedInfo?.name ?: "Unknown"

                    if (firstImage.isNotBlank()) {
                        imageLoader.loadImage(firstImage, imageResultView)
                    } else {
                        Log.d(MAIN_ACTIVITY, "Missing image URL")
                    }

                    val displayText = "Cat Breed: $breedName"
                    apiResponseView.text = displayText
                    
                } else {
                    Log.e(MAIN_ACTIVITY, "Error: ${response.errorBody()?.string()}")
                }
            }
        })
    }


    companion object{
        const val MAIN_ACTIVITY = "MAIN_ACTIVITY"
    }
}
