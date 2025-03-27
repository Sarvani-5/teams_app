package com.example.teams_app

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import java.io.IOException

class PlaylistManager(
    private val context: Context,
    private val memberName: String
) {
    private val mediaPlayers = mutableListOf<MediaPlayer>()
    private var currentPlayingIndex = -1
    private lateinit var playlistStatusText: TextView
    private lateinit var playlistControlButton: Button
    private lateinit var playlistSeekBar: SeekBar
    private lateinit var playlistProgressText: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var updateProgressRunnable: Runnable? = null

    fun setupPlaylistControls(
        statusTextView: TextView,
        controlButton: Button,
        seekBar: SeekBar,
        progressText: TextView
    ) {
        playlistStatusText = statusTextView
        playlistControlButton = controlButton
        playlistSeekBar = seekBar
        playlistProgressText = progressText

        playlistControlButton.setOnClickListener {
            if (mediaPlayers.isEmpty()) {
                loadPlaylistFromFavorites()
            }

            if (mediaPlayers.isNotEmpty()) {
                togglePlayPause()
            } else {
                Toast.makeText(context, "No songs in playlist", Toast.LENGTH_SHORT).show()
            }
        }

        playlistSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && currentPlayingIndex != -1) {
                    mediaPlayers[currentPlayingIndex].seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun loadPlaylistFromFavorites() {
        val dbHelper = FavoritesDbHelper(context)
        val favorites = dbHelper.getFavorites(memberName, "songs")

        mediaPlayers.clear()
        for (favorite in favorites) {
            favorite.audioPath?.let { path ->
                try {
                    val mediaPlayer = MediaPlayer()
                    mediaPlayer.setDataSource(path)
                    mediaPlayer.prepare()
                    mediaPlayers.add(mediaPlayer)
                } catch (e: IOException) {
                    Toast.makeText(context, "Error loading song: ${favorite.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        mediaPlayers.forEachIndexed { index, player ->
            player.setOnCompletionListener {
                playNextSong()
            }
        }
    }

    private fun togglePlayPause() {
        if (currentPlayingIndex == -1) {
            currentPlayingIndex = 0
            playCurrentSong()
        } else {
            val currentPlayer = mediaPlayers[currentPlayingIndex]
            if (currentPlayer.isPlaying) {
                currentPlayer.pause()
                playlistStatusText.text = "Playlist Paused"
                playlistControlButton.text = "Resume"
                stopProgressUpdate()
            } else {
                currentPlayer.start()
                playlistStatusText.text = "Now Playing: ${getSongName(currentPlayingIndex)}"
                playlistControlButton.text = "Pause"
                startProgressUpdate()
            }
        }
    }

    private fun playCurrentSong() {
        if (currentPlayingIndex in mediaPlayers.indices) {
            val currentPlayer = mediaPlayers[currentPlayingIndex]
            currentPlayer.start()
            playlistStatusText.text = "Now Playing: ${getSongName(currentPlayingIndex)}"
            playlistControlButton.text = "Pause"

            playlistSeekBar.max = currentPlayer.duration
            startProgressUpdate()
        }
    }

    private fun startProgressUpdate() {
        updateProgressRunnable = object : Runnable {
            override fun run() {
                if (currentPlayingIndex != -1) {
                    val currentPlayer = mediaPlayers[currentPlayingIndex]
                    val currentPosition = currentPlayer.currentPosition
                    playlistSeekBar.progress = currentPosition

                    val currentTimeText = formatTime(currentPosition)
                    val totalTimeText = formatTime(currentPlayer.duration)
                    playlistProgressText.text = "$currentTimeText / $totalTimeText"

                    handler.postDelayed(this, 1000)
                }
            }
        }
        updateProgressRunnable?.let { handler.post(it) }
    }

    private fun stopProgressUpdate() {
        updateProgressRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun formatTime(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun playNextSong() {
        if (currentPlayingIndex in mediaPlayers.indices) {
            mediaPlayers[currentPlayingIndex].stop()
        }

        currentPlayingIndex = (currentPlayingIndex + 1) % mediaPlayers.size

        mediaPlayers[currentPlayingIndex].prepare()
        playCurrentSong()
    }

    private fun getSongName(index: Int): String {
        val dbHelper = FavoritesDbHelper(context)
        val favorites = dbHelper.getFavorites(memberName, "songs")
        return if (index in favorites.indices) favorites[index].name else "Unknown Song"
    }

    fun release() {
        stopProgressUpdate()
        mediaPlayers.forEach { it.release() }
        mediaPlayers.clear()
    }
}