package com.SKO.Taskmanager.classes
import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var retrofit: Retrofit? = null
   //const val BASE_URL = "http://10.0.2.2:5165/"
   // const val BASE_URL = "https://6a60811f-18f9-4912-90f6-e32bc85aa494.mock.pstmn.io/"
    const val BASE_URL = "https://demo.skosystems.co/taskmanage/"

    val gson = GsonBuilder().setLenient() // Enables lenient parsing to allow malformed JSON
                     .create()

    @JvmStatic
    fun getMainClient(context: Context): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .retryOnConnectionFailure(true)
            .connectTimeout(40, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .build()

        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit!!
    }
}
