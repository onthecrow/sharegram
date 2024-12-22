package com.onthecrow.sharegram

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onthecrow.sharegram.service.InstagramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val instagramRepository: InstagramRepository,
) : ViewModel() {

    private val instagramPostUrl: String get() = savedStateHandle[Intent.EXTRA_TEXT] ?: ""
    private val reelRegex = Regex("/reel/(...+?)/\\?")
    private val postRegex = Regex("/p/(...+?)/\\?")
    private val _fileToShare = Channel<String>()
    val fileToShare: Channel<String> = _fileToShare

    init {
        viewModelScope.launch {
            val match = reelRegex.find(instagramPostUrl)?.let { it to true }
                ?: postRegex.find(instagramPostUrl)?.let { it to false }
            if (match != null) {
                val postResult = instagramRepository.getPost(match.first.groupValues[1])
                if (postResult.isSuccess && postResult.getOrNull() != null) {
                    val downloadUrl = if (match.second) {
                        postResult.getOrThrow().data.xdtShortcodeMedia.videoUrl
                    } else {
                        postResult.getOrThrow().data.xdtShortcodeMedia.displayUrl
                    }

                    val downloadResult =
                        instagramRepository.downloadPost(downloadUrl ?: return@launch, match.second)
                    if (downloadResult.isSuccess && downloadResult.getOrNull() != null) {
                        _fileToShare.send(downloadResult.getOrThrow())
                    }
                }
            }
        }
    }
}