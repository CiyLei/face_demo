package com.dj.facedemo

import android.graphics.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

/**
 * Create by ChenLei on 2021/7/13
 * Describe:
 */
class DistanceActivity : AppCompatActivity() {
    var client = OkHttpClient()
    var result: Result<DistanceResult>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_distance)
        val preview = Preview.Builder().build()
        val viewFinder: PreviewView = findViewById(R.id.pv)

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()

        // PreviewView creates a surface provider and is the recommended provider
        preview.setSurfaceProvider(viewFinder.surfaceProvider)

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), { imageProxy ->
//            Thread.sleep(1000)
            val image = imageProxy.image ?: return@setAnalyzer
            val yBuffer = image.planes?.get(0)?.buffer ?: return@setAnalyzer // Y
            val uBuffer = image.planes?.get(1)?.buffer ?: return@setAnalyzer // U
            val vBuffer = image.planes?.get(2)?.buffer ?: return@setAnalyzer // V

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            //U and V are swapped
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
            val imageBytes = out.toByteArray()

            var bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            val m = Matrix()
            m.setRotate(
                imageProxy.imageInfo.rotationDegrees.toFloat(),
                bmp.width.toFloat() / 2,
                bmp.height.toFloat() / 2
            )
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true);

            val bitMapOut = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bitMapOut)
            upload(bmp.width, bmp.height, bitMapOut.toByteArray())

            imageProxy.close()
        })

        // The use case is bound to an Android Lifecycle with the following code
        val camera = ProcessCameraProvider.getInstance(this).get()
            .bindToLifecycle(this, cameraSelector, imageAnalysis, preview)
    }

    fun upload(width: Int, height: Int, content: ByteArray) {
        val formData =
            MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(
                "img",
                "img",
                content.toRequestBody("multipart/form-data".toMediaType())
            ).addFormDataPart("encode", Gson().toJson(MainActivity.encode))
                .addFormDataPart("location", "1").build()
        val request =
            Request.Builder().url("http://192.168.2.116:8080/distance").post(formData).build()
        val response = client.newCall(request).execute()
        val type = object : TypeToken<Result<DistanceResult>>() {}.type
        result = Gson().fromJson<Result<DistanceResult>>(response.body?.string() ?: "{}", type)
        findViewById<FaceBoundsOverlay>(R.id.fbo).also { v ->
            v.post {
                val locations = result?.data?.locations ?: return@post
                println(result?.data?.distances)
                v.faceBound = locations.map {
                    // 自拍 所以要左右翻转一下
                    val l = v.width - it.left / width * v.width
                    val r = v.width - it.right / width * v.width
                    val t = it.top / height * v.height
                    val b = it.bottom / height * v.height
                    RectF(l, t, r, b)
                }.toList()
                v.descriptions =
                    result?.data?.distances?.map { "差异度:${String.format("%.2f", it * 100)}%" }
                        ?.toList() ?: ArrayList()
            }
        }
    }

}