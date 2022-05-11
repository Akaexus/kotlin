package com.example.audioapp

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioFilePath: String
    private var isRecording = false


    lateinit var playButton: Button
    lateinit var recordButton: Button
    lateinit var stopButton: Button

    private val RECORD_REQUEST_CODE = 101
    private val STORAGE_REQUEST_CODE = 102

    private fun hasMicrophone(): Boolean {
        val pmanager = this.packageManager
        return pmanager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
    }

    private fun requestPermission(permissionType: String, requestCode: Int) {
        val permission = ContextCompat.checkSelfPermission(this, permissionType)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permissionType), requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    recordButton.isEnabled = false
                    Toast.makeText(this, "Wymagana zgoda na nagrywanie", Toast.LENGTH_LONG).show()
                } else {
                    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_REQUEST_CODE)
                }
                return
            }
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    recordButton.isEnabled = false
                    Toast.makeText(this, "Wymagana zgoda na zapis w pamięci zewnętrznej", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    private fun audioSetup() {
        if (!hasMicrophone()) {
            stopButton.isEnabled = false
            playButton.isEnabled = false
            recordButton.isEnabled = false
        } else {
            playButton.isEnabled = false
            stopButton.isEnabled = false
        }

        audioFilePath = this.getExternalFilesDir(null)?.absolutePath + "/myaudio.3gp"
        requestPermission(Manifest.permission.RECORD_AUDIO, RECORD_REQUEST_CODE)
    }

    fun recordAudio(v: View) {
        isRecording = true
        stopButton.isEnabled = true
        playButton.isEnabled = false
        recordButton.isEnabled = false
        try {
            mediaRecorder = MediaRecorder()
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder.setOutputFile(audioFilePath)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder.start()
    }

    fun stopAudio(v: View) {
        stopButton.isEnabled = false
        playButton.isEnabled = true

        if (isRecording) {
            recordButton.isEnabled = false
            mediaRecorder.stop()
            mediaRecorder.release()
            isRecording = false
        } else {
            mediaPlayer.release()
            recordButton.isEnabled = true
        }
    }

    fun playAudio(v: View) {
        playButton.isEnabled = false
        recordButton.isEnabled = false
        stopButton.isEnabled = true
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener {
            stopButton.isEnabled = false
            playButton.isEnabled = true
            it.release()
        }
        mediaPlayer.setDataSource(audioFilePath)
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.playButton = findViewById(R.id.button)
        this.recordButton = findViewById(R.id.button2)
        this.stopButton = findViewById(R.id.button3)

        playButton.setOnClickListener {
            this.playAudio(it)
        }

        recordButton.setOnClickListener {
            this.recordAudio(it)
        }

        stopButton.setOnClickListener {
            this.stopAudio(it)
        }
        audioSetup()
    }
}