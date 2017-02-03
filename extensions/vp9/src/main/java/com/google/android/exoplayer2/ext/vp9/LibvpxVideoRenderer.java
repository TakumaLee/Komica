/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer2.ext.vp9;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Surface;
import com.google.android.exoplayer2.BaseRenderer;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.FormatHolder;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.decoder.DecoderInputBuffer;
import com.google.android.exoplayer2.drm.DrmSession;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.ExoMediaCrypto;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.TraceUtil;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.google.android.exoplayer2.video.VideoRendererEventListener.EventDispatcher;

/**
 * Decodes and renders video using the native VP9 decoder.
 */
public final class LibvpxVideoRenderer extends BaseRenderer {

  /**
   * The type of a message that can be passed to an instance of this class via
   * {@link ExoPlayer#sendMessages} or {@link ExoPlayer#blockingSendMessages}. The message object
   * should be the target {@link VpxOutputBufferRenderer}, or null.
   */
  public static final int MSG_SET_OUTPUT_BUFFER_RENDERER = C.MSG_CUSTOM_BASE;

  /**
   * The number of input buffers and the number of output buffers. The renderer may limit the
   * minimum possible value due to requiring multiple output buffers to be dequeued at a time for it
   * to make progress.
   */
  private static final int NUM_BUFFERS = 16;
  private static final int INITIAL_INPUT_BUFFER_SIZE = 768 * 1024; // Value based on cs/SoftVpx.cpp.

  private final boolean scaleToFit;
  private final long allowedJoiningTimeMs;
  private final int maxDroppedFramesToNotify;
  private final boolean playClearSamplesWithoutKeys;
  private final EventDispatcher eventDispatcher;
  private final FormatHolder formatHolder;
  private final DrmSessionManager<ExoMediaCrypto> drmSessionManager;

  private DecoderCounters decoderCounters;
  private Format format;
  private VpxDecoder decoder;
  private DecoderInputBuffer inputBuffer;
  private VpxOutputBuffer outputBuffer;
  private VpxOutputBuffer nextOutputBuffer;
  private DrmSession<ExoMediaCrypto> drmSession;
  private DrmSession<ExoMediaCrypto> pendingDrmSession;

  private Bitmap bitmap;
  private boolean renderedFirstFrame;
  private long joiningDeadlineMs;
  private Surface surface;
  private VpxOutputBufferRenderer outputBufferRenderer;
  private int outputMode;
  private boolean waitingForKeys;

  private boolean inputStreamEnded;
  private boolean outputStreamEnded;
  private int lastReportedWidth;
  private int lastReportedHeight;

  private long droppedFrameAccumulationStartTimeMs;
  private int droppedFrames;
  private int consecutiveDroppedFrameCount;

  /**
   * @param scaleToFit Whether video frames should be scaled to fit when rendering.
   * @param allowedJoiningTimeMs The maximum duration in milliseconds for which this video renderer
   *     can attempt to seamlessly join an ongoing playback.
   */
  public LibvpxVideoRenderer(boolean scaleToFit, long allowedJoiningTimeMs) {
    this(scaleToFit, allowedJoiningTimeMs, null, null, 0);
  }

  /**
   * @param scaleToFit Whether video frames should be scaled to fit when rendering.
   * @param allowedJoiningTimeMs The maximum duration in milliseconds for which this video renderer
   *     can attempt to seamlessly join an ongoing playback.
   * @param eventHandler A handler to use when delivering events to {@code eventListener}. May be
   *     null if delivery of events is not required.
   * @param eventListener A listener of events. May be null if delivery of events is not required.
   * @param maxDroppedFramesToNotify The maximum number of frames that can be dropped between
   *     invocations of {@link VideoRendererEventListener#onDroppedFrames(int, long)}.
   */
  public LibvpxVideoRenderer(boolean scaleToFit, long allowedJoiningTimeMs,
      Handler eventHandler, VideoRendererEventListener eventListener,
      int maxDroppedFramesToNotify) {
    this(scaleToFit, allowedJoiningTimeMs, eventHandler, eventListener, maxDroppedFramesToNotify,
        null, false);
  }

  /**
   * @param scaleToFit Whether video frames should be scaled to fit when rendering.
   * @param allowedJoiningTimeMs The maximum duration in milliseconds for which this video renderer
   *     can attempt to seamlessly join an ongoing playback.
   * @param eventHandler A handler to use when delivering events to {@code eventListener}. May be
   *     null if delivery of events is not required.
   * @param eventListener A listener of events. May be null if delivery of events is not required.
   * @param maxDroppedFramesToNotify The maximum number of frames that can be dropped between
   *     invocations of {@link VideoRendererEventListener#onDroppedFrames(int, long)}.
   * @param drmSessionManager For use with encrypted media. May be null if support for encrypted
   *     media is not required.
   * @param playClearSamplesWithoutKeys Encrypted media may contain clear (un-encrypted) regions.
   *     For example a media file may start with a short clear region so as to allow playback to
   *     begin in parallel with key acquisition. This parameter specifies whether the renderer is
   *     permitted to play clear regions of encrypted media files before {@code drmSessionManager}
   *     has obtained the keys necessary to decrypt encrypted regions of the media.
   */
  public LibvpxVideoRenderer(boolean scaleToFit, long allowedJoiningTimeMs,
      Handler eventHandler, VideoRendererEventListener eventListener,
      int maxDroppedFramesToNotify, DrmSessionManager<ExoMediaCrypto> drmSessionManager,
      boolean playClearSamplesWithoutKeys) {
    super(C.TRACK_TYPE_VIDEO);
    this.scaleToFit = scaleToFit;
    this.allowedJoiningTimeMs = allowedJoiningTimeMs;
    this.maxDroppedFramesToNotify = maxDroppedFramesToNotify;
    this.drmSessionManager = drmSessionManager;
    this.playClearSamplesWithoutKeys = playClearSamplesWithoutKeys;
    joiningDeadlineMs = -1;
    clearLastReportedVideoSize();
    formatHolder = new FormatHolder();
    eventDispatcher = new EventDispatcher(eventHandler, eventListener);
    outputMode = VpxDecoder.OUTPUT_MODE_NONE;
  }

  @Override
  public int supportsFormat(Format format) {
    return VpxLibrary.isAvailable() && MimeTypes.VIDEO_VP9.equalsIgnoreCase(format.sampleMimeType)
        ? (FORMAT_HANDLED | ADAPTIVE_SEAMLESS) : FORMAT_UNSUPPORTED_TYPE;
  }

  @Override
  public void render(long positionUs, long elapsedRealtimeUs) throws ExoPlaybackException {
    if (outputStreamEnded) {
      return;
    }

    // Try and read a format if we don't have one already.
    if (format == null && !readFormat()) {
      // We can't make progress without one.
      return;
    }

    if (isRendererAvailable()) {
      drmSession = pendingDrmSession;
      ExoMediaCrypto mediaCrypto = null;
      if (drmSession != null) {
        int drmSessionState = drmSession.getState();
        if (drmSessionState == DrmSession.STATE_ERROR) {
          throw ExoPlaybackException.createForRenderer(drmSession.getError(), getIndex());
        } else if (drmSessionState == DrmSession.STATE_OPENED
            || drmSessionState == DrmSession.STATE_OPENED_WITH_KEYS) {
          mediaCrypto = drmSession.getMediaCrypto();
        } else {
          // The drm session isn't open yet.
          return;
        }
      }
      try {
        if (decoder == null) {
          // If we don't have a decoder yet, we need to instantiate one.
          long codecInitializingTimestamp = SystemClock.elapsedRealtime();
          TraceUtil.beginSection("createVpxDecoder");
          decoder = new VpxDecoder(NUM_BUFFERS, NUM_BUFFERS, INITIAL_INPUT_BUFFER_SIZE,
              mediaCrypto);
          decoder.setOutputMode(outputMode);
          TraceUtil.endSection();
          long codecInitializedTimestamp = SystemClock.elapsedRealtime();
          eventDispatcher.decoderInitialized(decoder.getName(), codecInitializedTimestamp,
              codecInitializedTimestamp - codecInitializingTimestamp);
          decoderCounters.decoderInitCount++;
        }
        TraceUtil.beginSection("drainAndFeed");
        while (drainOutputBuffer(positionUs)) {}
        while (feedInputBuffer()) {}
        TraceUtil.endSection();
      } catch (VpxDecoderException e) {
        throw ExoPlaybackException.createForRenderer(e, getIndex());
      }
    } else {
      skipToKeyframeBefore(positionUs);
    }
    decoderCounters.ensureUpdated();
  }

  private boolean drainOutputBuffer(long positionUs) throws VpxDecoderException {
    if (outputStreamEnded) {
      return false;
    }

    // Acquire outputBuffer either from nextOutputBuffer or from the decoder.
    if (outputBuffer == null) {
      if (nextOutputBuffer != null) {
        outputBuffer = nextOutputBuffer;
        nextOutputBuffer = null;
      } else {
        outputBuffer = decoder.dequeueOutputBuffer();
      }
      if (outputBuffer == null) {
        return false;
      }
      decoderCounters.skippedOutputBufferCount += outputBuffer.skippedOutputBufferCount;
    }

    if (nextOutputBuffer == null) {
      nextOutputBuffer = decoder.dequeueOutputBuffer();
    }

    if (outputBuffer.isEndOfStream()) {
      outputStreamEnded = true;
      outputBuffer.release();
      outputBuffer = null;
      return false;
    }

    // Drop the frame if we're joining and are more than 30ms late, or if we have the next frame
    // and that's also late. Else we'll render what we have.
    if ((joiningDeadlineMs != -1 && outputBuffer.timeUs < positionUs - 30000)
        || (nextOutputBuffer != null && !nextOutputBuffer.isEndOfStream()
        && nextOutputBuffer.timeUs < positionUs)) {
      decoderCounters.droppedOutputBufferCount++;
      droppedFrames++;
      consecutiveDroppedFrameCount++;
      decoderCounters.maxConsecutiveDroppedOutputBufferCount = Math.max(
          consecutiveDroppedFrameCount,
          decoderCounters.maxConsecutiveDroppedOutputBufferCount);
      if (droppedFrames == maxDroppedFramesToNotify) {
        maybeNotifyDroppedFrames();
      }
      outputBuffer.release();
      outputBuffer = null;
      return true;
    }

    // If we have not rendered any frame so far (either initially or immediately following a seek),
    // render one frame irrespective of the state or current position.
    if (!renderedFirstFrame
        || (getState() == STATE_STARTED && outputBuffer.timeUs <= positionUs + 30000)) {
      renderBuffer();
    }
    return false;
  }

  private void renderBuffer() {
    decoderCounters.renderedOutputBufferCount++;
    consecutiveDroppedFrameCount = 0;
    maybeNotifyVideoSizeChanged(outputBuffer.width, outputBuffer.height);
    if (outputBuffer.mode == VpxDecoder.OUTPUT_MODE_RGB && surface != null) {
      renderRgbFrame(outputBuffer, scaleToFit);
      if (!renderedFirstFrame) {
        renderedFirstFrame = true;
        eventDispatcher.renderedFirstFrame(surface);
      }
      outputBuffer.release();
    } else if (outputBuffer.mode == VpxDecoder.OUTPUT_MODE_YUV && outputBufferRenderer != null) {
      // The renderer will release the buffer.
      outputBufferRenderer.setOutputBuffer(outputBuffer);
      if (!renderedFirstFrame) {
        renderedFirstFrame = true;
        eventDispatcher.renderedFirstFrame(null);
      }
    } else {
      outputBuffer.release();
    }
    outputBuffer = null;
  }

  private void renderRgbFrame(VpxOutputBuffer outputBuffer, boolean scale) {
    if (bitmap == null || bitmap.getWidth() != outputBuffer.width
        || bitmap.getHeight() != outputBuffer.height) {
      bitmap = Bitmap.createBitmap(outputBuffer.width, outputBuffer.height, Bitmap.Config.RGB_565);
    }
    bitmap.copyPixelsFromBuffer(outputBuffer.data);
    Canvas canvas = surface.lockCanvas(null);
    if (scale) {
      canvas.scale(((float) canvas.getWidth()) / outputBuffer.width,
          ((float) canvas.getHeight()) / outputBuffer.height);
    }
    canvas.drawBitmap(bitmap, 0, 0, null);
    surface.unlockCanvasAndPost(canvas);
  }

  private boolean feedInputBuffer() throws VpxDecoderException, ExoPlaybackException {
    if (inputStreamEnded) {
      return false;
    }

    if (inputBuffer == null) {
      inputBuffer = decoder.dequeueInputBuffer();
      if (inputBuffer == null) {
        return false;
      }
    }

    int result;
    if (waitingForKeys) {
      // We've already read an encrypted sample into buffer, and are waiting for keys.
      result = C.RESULT_BUFFER_READ;
    } else {
      result = readSource(formatHolder, inputBuffer);
    }

    if (result == C.RESULT_NOTHING_READ) {
      return false;
    }
    if (result == C.RESULT_FORMAT_READ) {
      onInputFormatChanged(formatHolder.format);
      return true;
    }
    if (inputBuffer.isEndOfStream()) {
      inputStreamEnded = true;
      decoder.queueInputBuffer(inputBuffer);
      inputBuffer = null;
      return false;
    }
    boolean bufferEncrypted = inputBuffer.isEncrypted();
    waitingForKeys = shouldWaitForKeys(bufferEncrypted);
    if (waitingForKeys) {
      return false;
    }
    inputBuffer.flip();
    decoder.queueInputBuffer(inputBuffer);
    decoderCounters.inputBufferCount++;
    inputBuffer = null;
    return true;
  }

  private boolean shouldWaitForKeys(boolean bufferEncrypted) throws ExoPlaybackException {
    if (drmSession == null) {
      return false;
    }
    int drmSessionState = drmSession.getState();
    if (drmSessionState == DrmSession.STATE_ERROR) {
      throw ExoPlaybackException.createForRenderer(drmSession.getError(), getIndex());
    }
    return drmSessionState != DrmSession.STATE_OPENED_WITH_KEYS
        && (bufferEncrypted || !playClearSamplesWithoutKeys);
  }

  private void flushDecoder() {
    inputBuffer = null;
    waitingForKeys = false;
    if (outputBuffer != null) {
      outputBuffer.release();
      outputBuffer = null;
    }
    if (nextOutputBuffer != null) {
      nextOutputBuffer.release();
      nextOutputBuffer = null;
    }
    decoder.flush();
  }

  @Override
  public boolean isEnded() {
    return outputStreamEnded;
  }

  @Override
  public boolean isReady() {
    if (waitingForKeys) {
      return false;
    }
    if (format != null && (isSourceReady() || outputBuffer != null)
        && (renderedFirstFrame || !isRendererAvailable())) {
      // Ready. If we were joining then we've now joined, so clear the joining deadline.
      joiningDeadlineMs = -1;
      return true;
    } else if (joiningDeadlineMs == -1) {
      // Not joining.
      return false;
    } else if (SystemClock.elapsedRealtime() < joiningDeadlineMs) {
      // Joining and still within the joining deadline.
      return true;
    } else {
      // The joining deadline has been exceeded. Give up and clear the deadline.
      joiningDeadlineMs = -1;
      return false;
    }
  }

  @Override
  protected void onEnabled(boolean joining) throws ExoPlaybackException {
    decoderCounters = new DecoderCounters();
    eventDispatcher.enabled(decoderCounters);
  }

  @Override
  protected void onPositionReset(long positionUs, boolean joining) {
    inputStreamEnded = false;
    outputStreamEnded = false;
    renderedFirstFrame = false;
    consecutiveDroppedFrameCount = 0;
    if (decoder != null) {
      flushDecoder();
    }
    joiningDeadlineMs = joining && allowedJoiningTimeMs > 0
        ? (SystemClock.elapsedRealtime() + allowedJoiningTimeMs) : -1;
  }

  @Override
  protected void onStarted() {
    droppedFrames = 0;
    droppedFrameAccumulationStartTimeMs = SystemClock.elapsedRealtime();
  }

  @Override
  protected void onStopped() {
    joiningDeadlineMs = -1;
    maybeNotifyDroppedFrames();
  }

  @Override
  protected void onDisabled() {
    inputBuffer = null;
    outputBuffer = null;
    format = null;
    waitingForKeys = false;
    clearLastReportedVideoSize();
    try {
      releaseDecoder();
    } finally {
      try {
        if (drmSession != null) {
          drmSessionManager.releaseSession(drmSession);
        }
      } finally {
        try {
          if (pendingDrmSession != null && pendingDrmSession != drmSession) {
            drmSessionManager.releaseSession(pendingDrmSession);
          }
        } finally {
          drmSession = null;
          pendingDrmSession = null;
          decoderCounters.ensureUpdated();
          eventDispatcher.disabled(decoderCounters);
        }
      }
    }
  }

  private void releaseDecoder() {
    if (decoder != null) {
      decoder.release();
      decoder = null;
      decoderCounters.decoderReleaseCount++;
      waitingForKeys = false;
      if (drmSession != null && pendingDrmSession != drmSession) {
        try {
          drmSessionManager.releaseSession(drmSession);
        } finally {
          drmSession = null;
        }
      }
    }
  }

  private boolean readFormat() throws ExoPlaybackException {
    int result = readSource(formatHolder, null);
    if (result == C.RESULT_FORMAT_READ) {
      onInputFormatChanged(formatHolder.format);
      return true;
    }
    return false;
  }

  private void onInputFormatChanged(Format newFormat) throws ExoPlaybackException {
    Format oldFormat = format;
    format = newFormat;

    boolean drmInitDataChanged = !Util.areEqual(format.drmInitData, oldFormat == null ? null
        : oldFormat.drmInitData);
    if (drmInitDataChanged) {
      if (format.drmInitData != null) {
        if (drmSessionManager == null) {
          throw ExoPlaybackException.createForRenderer(
              new IllegalStateException("Media requires a DrmSessionManager"), getIndex());
        }
        pendingDrmSession = drmSessionManager.acquireSession(Looper.myLooper(), format.drmInitData);
        if (pendingDrmSession == drmSession) {
          drmSessionManager.releaseSession(pendingDrmSession);
        }
      } else {
        pendingDrmSession = null;
      }
    }

    eventDispatcher.inputFormatChanged(format);
  }

  @Override
  public void handleMessage(int messageType, Object message) throws ExoPlaybackException {
    if (messageType == C.MSG_SET_SURFACE) {
      setOutput((Surface) message, null);
    } else if (messageType == MSG_SET_OUTPUT_BUFFER_RENDERER) {
      setOutput(null, (VpxOutputBufferRenderer) message);
    } else {
      super.handleMessage(messageType, message);
    }
  }

  private void setOutput(Surface surface, VpxOutputBufferRenderer outputBufferRenderer) {
    // At most one output may be non-null. Both may be null if the output is being cleared.
    Assertions.checkState(surface == null || outputBufferRenderer == null);
    // Clear state so that we always call the event listener with the video size and when a frame
    // is rendered, even if the output hasn't changed.
    renderedFirstFrame = false;
    clearLastReportedVideoSize();
    // We only need to update the decoder if the output has changed.
    if (this.surface != surface || this.outputBufferRenderer != outputBufferRenderer) {
      this.surface = surface;
      this.outputBufferRenderer = outputBufferRenderer;
      outputMode = outputBufferRenderer != null ? VpxDecoder.OUTPUT_MODE_YUV
          : surface != null ? VpxDecoder.OUTPUT_MODE_RGB : VpxDecoder.OUTPUT_MODE_NONE;
      updateDecoder();
    }
  }

  private void updateDecoder() {
    if (decoder != null) {
      if (outputMode == VpxDecoder.OUTPUT_MODE_NONE) {
        releaseDecoder();
      } else {
        decoder.setOutputMode(outputMode);
      }
    }
  }

  private boolean isRendererAvailable() {
    return surface != null || outputBufferRenderer != null;
  }

  private void clearLastReportedVideoSize() {
    lastReportedWidth = Format.NO_VALUE;
    lastReportedHeight = Format.NO_VALUE;
  }

  private void maybeNotifyVideoSizeChanged(int width, int height) {
    if (lastReportedWidth != width || lastReportedHeight != height) {
      lastReportedWidth = width;
      lastReportedHeight = height;
      eventDispatcher.videoSizeChanged(width, height, 0, 1);
    }
  }

  private void maybeNotifyDroppedFrames() {
    if (droppedFrames > 0) {
      long now = SystemClock.elapsedRealtime();
      long elapsedMs = now - droppedFrameAccumulationStartTimeMs;
      eventDispatcher.droppedFrames(droppedFrames, elapsedMs);
      droppedFrames = 0;
      droppedFrameAccumulationStartTimeMs = now;
    }
  }

}
