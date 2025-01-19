package com.SKO.Taskmanager.classes

import android.content.Context
import com.SKO.Taskmanager.interfaces.ApiInterface
import com.SKO.Taskmanager.interfaces.RefreshTokenApiInterface
import com.SKO.Taskmanager.responses.TokenResponse
import com.SKO.Taskmanager.utils.SharedPreferencesUtil
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response as RetrofitResponse


class AuthInterceptor (private val context: Context) : Interceptor {

    //val BASE_URL = "http://10.0.2.2:5165/"
   //val BASE_URL = "https://6a60811f-18f9-4912-90f6-e32bc85aa494.mock.pstmn.io/"
     val BASE_URL = "https://demo.skosystems.co/taskmanage/"

    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    val api = retrofit.create(RefreshTokenApiInterface::class.java)


    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = SharedPreferencesUtil.getAccessToken(context)

        // If no access token is found, proceed with the request without authorization
        if (accessToken == null) {
            return chain.proceed(chain.request())
        }

        // Build the request with the current access token
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        // Proceed with the original request
        var response = chain.proceed(request)

        // If the response code is 401 (Unauthorized), the token might have expired
        if (response.code == 401) {
            // Get the refresh token from SharedPreferences
            val refreshToken = SharedPreferencesUtil.getRefreshToken(context)

            if (refreshToken != null) {
                // Refresh the token asynchronously
                refreshAccessTokenAsync(refreshToken) { newAccessToken, newRefreshToken ->
                    // If new tokens are available, retry the original request with the new access token
                    if (newAccessToken != null) {
                        // Save the new tokens
                        SharedPreferencesUtil.saveTokens(
                            context,
                            newAccessToken,
                            newRefreshToken ?: refreshToken
                        )

                        // Retry the original request with the new access token
                        val newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer $newAccessToken")
                            .build()

                        // Proceed with the retried request
                        response = chain.proceed(newRequest)
                    }
                }
            }
        }

        return response
    }


    private fun refreshAccessTokenAsync(
        refreshToken: String,
        callback: (String?, String?) -> Unit
    ) {
        val call = api.refreshToken("Bearer $refreshToken")

        // Make the call asynchronously
        call.enqueue(object : Callback<TokenResponse> {
            override fun onResponse(
                call: Call<TokenResponse>,
                response: RetrofitResponse<TokenResponse>
            ) {
                if (response.isSuccessful) {
                    // If the response is successful, get the new tokens
                    val newAccessToken = response.body()?.accessToken
                    val newRefreshToken = response.body()?.refreshToken
                    // Return the new access token and refresh token via callback
                    callback(newAccessToken, newRefreshToken)
                } else {
                    // Handle unsuccessful response (e.g., token refresh failed)
                    callback(null, null)
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                // Handle failure (e.g., network error)
                t.printStackTrace()
                callback(null, null)
            }
        })


    }

}