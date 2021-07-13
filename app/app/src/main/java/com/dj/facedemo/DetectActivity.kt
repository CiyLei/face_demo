package com.dj.facedemo

import android.graphics.*
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors


/**
 * Create by ChenLei on 2021/7/13
 * Describe:
 */
class DetectActivity : AppCompatActivity() {
    var client = OkHttpClient()
    var result: Result<DetectResult>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detect)
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

        findViewById<Button>(R.id.btnRemember).setOnClickListener {
            if (result?.data?.encodes?.size == 1) {
                MainActivity.encode = result?.data?.encodes?.first()!!
                Toast.makeText(this, "记住成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "只能有一个人才行", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun upload(width: Int, height: Int, content: ByteArray) {
        val formData =
            MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(
                "img",
                "img",
                content.toRequestBody("multipart/form-data".toMediaType())
            ).build()
        val request =
            Request.Builder().url("http://192.168.2.116:8080/detect").post(formData).build()
        val response = client.newCall(request).execute()
        val type = object : TypeToken<Result<DetectResult>>() {}.type
        result = Gson().fromJson<Result<DetectResult>>(response.body?.string() ?: "{}", type)
        findViewById<FaceBoundsOverlay>(R.id.fbo).also { v ->
            v.post {
                val locations = result?.data?.locations ?: return@post
                v.faceBound = locations.map {
                    // 自拍 所以要左右翻转一下
                    val l = v.width - it.left / width * v.width
                    val r = v.width - it.right / width * v.width
                    val t = it.top / height * v.height
                    val b = it.bottom / height * v.height
                    RectF(l, t, r, b)
                }.toList()
            }
        }
    }

}