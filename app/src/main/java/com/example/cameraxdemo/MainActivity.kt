package com.example.cameraxdemo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.concurrent.Executors

//权限
private const val REQUEST_CODE_PERMISSIONS = 10
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class MainActivity : AppCompatActivity(), LifecycleOwner {
    private val executor = Executors.newSingleThreadExecutor()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (checkAllPermissionsGranted()) {
            viewFinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }

    private fun startCamera() {
        // 为取景器用例创建配置对象
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(viewFinder.width, viewFinder.height))
        }.build()

        // 构建取景器用例
        val preview = Preview(previewConfig)

        // 每次取景器更新时，重新计算布局
        preview.setOnPreviewOutputUpdateListener {
            // 要更新SurfaceTexture，我们必须将其删除并重新添加
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)
            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }
        // 为图像捕获用例创建配置对象
        val imageCaptureConfig = ImageCaptureConfig.Builder().apply {
            // 我们没有为图像捕获设置分辨率；相反，我们
            //选择一个可以推断出合适的捕获模式
            // 基于宽高比和请求模式的分辨率
            setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
        }.build()

        // 构建图像捕获用例并附加按钮单击侦听器
        val imageCapture = ImageCapture(imageCaptureConfig)
        buttonTakePhoto.setOnClickListener {
            val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
            imageCapture.takePicture(file, executor, object : ImageCapture.OnImageSavedListener {
                override fun onImageSaved(file: File) {
                    val msg = "成功: ${file.absolutePath}"
                    Log.d("CameraXApp", msg)
                    viewFinder.post {
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(
                    imageCaptureError: ImageCapture.ImageCaptureError,
                    message: String,
                    cause: Throwable?
                ) {
                    val msg = "失败: $message"
                    Log.e("CameraXApp", msg, cause)
                    viewFinder.post {
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    }
                }

            })
        }


        //将用例绑定到生命周期
//         CameraX.bindToLifecycle(this, preview)//只是预览
        CameraX.bindToLifecycle(this, preview,imageCapture)//可以预览可以拍照
    }

    private fun updateTransform() {
        val matrix = Matrix()

        //计算取景器的中心
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        // 纠正预览输出以适应显示旋转
        val rotationDegrees = when (viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // 最后，将转换应用于TextureView
        viewFinder.setTransform(matrix)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (checkAllPermissionsGranted()) {
                viewFinder.post { startCamera() }
            } else {
                Toast.makeText(applicationContext, "您没有同意权限", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 检查所有权限
     */
    private fun checkAllPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


}