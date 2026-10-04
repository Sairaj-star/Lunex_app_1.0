package com.example.lunex.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorderHelper(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    var isRecording = false

    fun startRecording(): File? {
        try {
            val file = File(context.cacheDir, "lunex_voice_${System.currentTimeMillis()}.m4a")
            currentOutputFile = file

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            return file
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Failed to start recording", e)
            stopRecording()
            return null
        }
    }

    fun stopRecording(): File? {
        if (!isRecording && recorder == null) return null
        try {
            recorder?.stop()
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Failed to stop recorder", e)
        } finally {
            recorder?.release()
            recorder = null
            isRecording = false
        }
        return currentOutputFile
    }
}

class AudioPlayerHelper {
    private var mediaPlayer: MediaPlayer? = null
    var isPlaying: Boolean = false
    var currentPlayingUri: String? = null

    fun play(uri: String, onCompletion: () -> Unit) {
        stop()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(uri)
                prepare()
                setOnCompletionListener {
                    this@AudioPlayerHelper.isPlaying = false
                    this@AudioPlayerHelper.currentPlayingUri = null
                    onCompletion()
                }
                start()
            }
            isPlaying = true
            currentPlayingUri = uri
        } catch (e: IOException) {
            Log.e("AudioPlayerHelper", "Failed to play audio", e)
            isPlaying = false
            currentPlayingUri = null
            onCompletion()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            isPlaying = false
            currentPlayingUri = null
        }
    }
}
