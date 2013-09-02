package com.bydavy.easy.network.protocols;

import com.bydavy.easy.network.EasyInternalSettings;
import com.bydavy.easy.network.EasyMessage;
import com.bydavy.easy.network.EasyMessageBaseAsyncWriter;
import com.bydavy.easy.network.utils.LogHelper;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.io.IOException;
import java.nio.ByteBuffer;

public class PrefixedBySizeAsyncWriter extends EasyMessageBaseAsyncWriter {
    private static final String TAG = "EasyMessageAsyncWriter";
    private static final boolean DEBUG = EasyInternalSettings.DEBUG ? false : EasyInternalSettings.DEBUG_DEFAULT_FALSE;

    @GuardedBy("mLock")
    private boolean mIsWriting;
    @Nullable
    @GuardedBy("mLock")
    private final ByteBuffer mBuffer;
    @Nullable
    @GuardedBy("mLock")
    private final PrefixedBySizeWriter mMessageWriter;
    @Nullable
    @GuardedBy("mLock")
    private EasyMessage mEasyMessage;

    public PrefixedBySizeAsyncWriter(PrefixedBySizeWriter messageWriter) {
        super();
        mMessageWriter = messageWriter;
        mBuffer = ByteBuffer.allocate(messageWriter.getEasyMessageSizeMaxInBytes());
    }

    public boolean isWriting() {
        return mIsStarted && mEasyMessage != null;
    }

    public boolean write() throws IOException {
        synchronized (mLock) {
            if (!mIsStarted) {
                return false;
            }

            if (!mIsWriting) {
                mEasyMessage = mIncomingQueue.poll();
                if (isStopMessage(mEasyMessage)) {
                    mIsStarted = false;
                    mEasyMessage = null;
                    mListener.onStopped(this);
                    return false;
                }

                if (mEasyMessage != null) {
                    mBuffer.clear();
                    mMessageWriter.writeMessage(mEasyMessage, mBuffer);
                    mBuffer.flip();
                    mIsWriting = true;
                    /*if (DEBUG) {
                        LogHelper.d(TAG, "Start writing " + mEasyMessage);
                    }*/
                } else {
                    return false;
                }
            }

            if (mIsWriting) {
                int write = getChannel().write(mBuffer);
                if (write == -1) {
                    mListener.onStreamClosed(this);
                    return false;
                }

                if (!mBuffer.hasRemaining()) {
                    mIsWriting = false;
                    mListener.onMessageWritten(this, mEasyMessage);
                    if (DEBUG) {
                        LogHelper.d(TAG, "Sent " + mEasyMessage);
                    }
                    // Give a GC opportunity for mEasyMessage
                    mEasyMessage = null;
                }
            }
            return true;
        }
    }
}
