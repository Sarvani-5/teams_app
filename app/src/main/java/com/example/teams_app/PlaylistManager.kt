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
                if (fromUser && currentPlayingIndex != -1 && currentPlayingIndex < mediaPlayers.size) {
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

        // Clear existing media players
        releaseAllPlayers()
        mediaPlayers.clear()

        // Load new media players from favorites
        for (favorite in favorites) {
            favorite.audioPath?.let { path ->
                try {
                    val mediaPlayer = MediaPlayer()
                    mediaPlayer.setDataSource(path)
                    mediaPlayer.prepare() // Pre-prepare all media players
                    mediaPlayers.add(mediaPlayer)
                } catch (e: IOException) {
                    Toast.makeText(context, "Error loading song: ${favorite.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set completion listener for continuous playback
        mediaPlayers.forEachIndexed { index, player ->
            player.setOnCompletionListener {
                playNextSong()
            }
        }

        // Update UI to reflect current state
        updateControlsState()
    }

    private fun updateControlsState() {
        if (mediaPlayers.isEmpty()) {
            playlistStatusText.text = "No songs available"
            playlistControlButton.text = "Play"
            playlistControlButton.isEnabled = false
        } else {
            playlistControlButton.isEnabled = true
            if (currentPlayingIndex == -1) {
                playlistStatusText.text = "Playlist Ready"
                playlistControlButton.text = "Play"
            } else {
                val isPlaying = mediaPlayers[currentPlayingIndex].isPlaying
                playlistStatusText.text = if (isPlaying)
                    "Now Playing: ${getSongName(currentPlayingIndex)}"
                else
                    "Paused: ${getSongName(currentPlayingIndex)}"
                playlistControlButton.text = if (isPlaying) "Pause" else "Resume"
            }
        }
    }

    private fun togglePlayPause() {
        if (currentPlayingIndex == -1) {
            // Start playing from the first song
            currentPlayingIndex = 0
            playCurrentSong()
        } else {
            // Toggle play/pause for current song
            val currentPlayer = mediaPlayers[currentPlayingIndex]
            if (currentPlayer.isPlaying) {
                currentPlayer.pause()
                playlistStatusText.text = "Paused: ${getSongName(currentPlayingIndex)}"
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
            // Reset all other players to make sure only one is playing
            mediaPlayers.forEachIndexed { index, player ->
                if (index != currentPlayingIndex && player.isPlaying) {
                    player.pause()
                    player.seekTo(0)
                }
            }

            val currentPlayer = mediaPlayers[currentPlayingIndex]
            if (!currentPlayer.isPlaying) {
                currentPlayer.start()
            }

            playlistStatusText.text = "Now Playing: ${getSongName(currentPlayingIndex)}"
            playlistControlButton.text = "Pause"

            playlistSeekBar.max = currentPlayer.duration
            playlistSeekBar.progress = currentPlayer.currentPosition
            startProgressUpdate()
        }
    }

    private fun playNextSong() {
        // Stop progress updates while changing songs
        stopProgressUpdate()

        // Stop current song if playing
        if (currentPlayingIndex in mediaPlayers.indices) {
            val currentPlayer = mediaPlayers[currentPlayingIndex]
            if (currentPlayer.isPlaying) {
                currentPlayer.pause()
            }
            currentPlayer.seekTo(0) // Reset position
        }

        // Move to next song (or loop back to first)
        if (mediaPlayers.isNotEmpty()) {
            currentPlayingIndex = (currentPlayingIndex + 1) % mediaPlayers.size
            playCurrentSong()
        } else {
            currentPlayingIndex = -1
            updateControlsState()
        }
    }

    private fun startProgressUpdate() {
        // Cancel any existing runnables
        stopProgressUpdate()

        updateProgressRunnable = object : Runnable {
            override fun run() {
                if (currentPlayingIndex != -1 && currentPlayingIndex < mediaPlayers.size) {
                    val currentPlayer = mediaPlayers[currentPlayingIndex]
                    val currentPosition = currentPlayer.currentPosition
                    playlistSeekBar.progress = currentPosition

                    val currentTimeText = formatTime(currentPosition)
                    val totalTimeText = formatTime(currentPlayer.duration)
                    playlistProgressText.text = "$currentTimeText / $totalTimeText"

                    handler.postDelayed(this, 500) // Update every half second for smoother progress
                }
            }
        }
        updateProgressRunnable?.let { handler.post(it) }
    }

    private fun stopProgressUpdate() {
        updateProgressRunnable?.let { handler.removeCallbacks(it) }
        updateProgressRunnable = null
    }

    private fun formatTime(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun getSongName(index: Int): String {
        val dbHelper = FavoritesDbHelper(context)
        val favorites = dbHelper.getFavorites(memberName, "songs")
        return if (index in favorites.indices) favorites[index].name else "Unknown Song"
    }

    // Add ability to skip to next song manually
    fun playNext() {
        playNextSong()
    }

    // Add ability to skip to previous song
    fun playPrevious() {
        if (mediaPlayers.isEmpty()) return

        stopProgressUpdate()

        if (currentPlayingIndex in mediaPlayers.indices) {
            val currentPlayer = mediaPlayers[currentPlayingIndex]
            if (currentPlayer.isPlaying) {
                currentPlayer.pause()
            }
            currentPlayer.seekTo(0)
        }

        // Go to previous song or wrap around to the last song
        currentPlayingIndex = if (currentPlayingIndex <= 0)
            mediaPlayers.size - 1
        else
            currentPlayingIndex - 1

        playCurrentSong()
    }

    private fun releaseAllPlayers() {
        stopProgressUpdate()
        mediaPlayers.forEach {
            try {
                if (it.isPlaying) it.stop()
                it.release()
            } catch (e: Exception) {
                // Handle any exceptions during release
            }
        }
    }

    fun release() {
        stopProgressUpdate()
        releaseAllPlayers()
        mediaPlayers.clear()
        currentPlayingIndex = -1
    }
}