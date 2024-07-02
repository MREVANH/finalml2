package com.example.afinal

import android.annotation.SuppressLint
import android.content.res.AssetManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SimodelActivity : AppCompatActivity() {

    private lateinit var interpreter: Interpreter
    private val mModelPath = "obesity.tflite"

    private lateinit var resultText: TextView
    private lateinit var Age: EditText
    private lateinit var Height: EditText
    private lateinit var Weight: EditText
    private lateinit var FCVC: EditText
    private lateinit var NCP: EditText
    private lateinit var CH2O: EditText
    private lateinit var FAF: EditText
    private lateinit var checkButton : Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simodel)

        resultText = findViewById(R.id.txtResult)
        Age = findViewById(R.id.Age)
        Height = findViewById(R.id.Height)
        Weight = findViewById(R.id.Weight)
        FCVC = findViewById(R.id.FCVC)
        NCP = findViewById(R.id.NCP)
        CH2O = findViewById(R.id.CH2O)
        FAF = findViewById(R.id.FAF)
        checkButton = findViewById(R.id.btnCheck)

        checkButton.setOnClickListener {
            try {
                val result = doInference(
                    Age.text.toString(),
                    Height.text.toString(),
                    Weight.text.toString(),
                    FCVC.text.toString(),
                    NCP.text.toString(),
                    CH2O.text.toString(),
                    FAF.text.toString())
                runOnUiThread {
                    resultText.text = if (result == 0) {
                        "Terkena Obesitas"
                    } else {
                        "Tidak Terkena Obesitas"
                    }
                }
            } catch (e: Exception) {
                Log.e("SimodelActivity", "Inference error: ${e.message}")
            }
        }
        initInterpreter()
    }

    private fun initInterpreter() {
        try {
            val options = Interpreter.Options().apply {
                setNumThreads(7)
                setUseNNAPI(true)
            }
            interpreter = Interpreter(loadModelFile(assets, mModelPath), options)
        } catch (e: Exception) {
            Log.e("SimodelActivity", "Interpreter initialization error: ${e.message}")
        }
    }

    private fun doInference(input1: String, input2: String, input3: String, input4: String, input5: String, input6: String, input7: String): Int {
        return try {
            val inputVal = FloatArray(7).apply {
                this[0] = input1.toFloat()
                this[1] = input2.toFloat()
                this[2] = input3.toFloat()
                this[3] = input4.toFloat()
                this[4] = input5.toFloat()
                this[5] = input6.toFloat()
                this[6] = input7.toFloat()
            }
            val output = Array(1) { FloatArray(4) }
            interpreter.run(inputVal, output)

            Log.e("result", (output[0].toList()+" ").toString())

            output[0].indexOfFirst { it == output[0].maxOrNull() }
        } catch (e: Exception) {
            Log.e("SimodelActivity", "Inference computation error: ${e.message}")
            -1  // Return an invalid index in case of error
        }
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        return try {
            val fileDescriptor = assetManager.openFd(modelPath)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        } catch (e: Exception) {
            Log.e("SimodelActivity", "Model file loading error: ${e.message}")
            throw e
        }
    }
}
