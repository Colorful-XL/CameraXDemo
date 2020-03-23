package com.example.cameraxdemo

import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.lang.Exception

class QRAnalyzer : ImageAnalysis.Analyzer {
    private var listener:UIListener

    private val reader: MultiFormatReader = MultiFormatReader().apply {
        val map = mapOf<DecodeHintType , Collection<BarcodeFormat>>(
            Pair(DecodeHintType.POSSIBLE_FORMATS, arrayListOf(BarcodeFormat.QR_CODE))
        )
        setHints(map)
    }

    constructor(listener : UIListener):super(){
        this.listener = listener
    }

    override fun analyze(image: ImageProxy?, rotationDegrees: Int) {
        if (ImageFormat.YUV_420_888 != image?.format
            && ImageFormat.YUV_422_888 != image?.format
            && ImageFormat.YUV_444_888 != image?.format ){
            return
        }
        val buffer = image.planes[0].buffer
        val data = ByteArray(buffer.remaining())
        val height = image.height
        val width = image.width
        buffer.get(data)
        //调整crop的矩形区域，目前是全屏
        val source = PlanarYUVLuminanceSource(data,width,height,0,0,width,height,false)

        val bitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = reader.decode(bitmap)
            listener.onSuccess("$result")
            Log.e("Analyzer","success! = $result")
            return
        }catch (e:Exception){
            listener.onFailue()
            Log.d("Analyzer" , "Error decoding barcode")
        }
    }
}