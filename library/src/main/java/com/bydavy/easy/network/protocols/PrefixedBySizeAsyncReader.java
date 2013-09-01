package com.bydavy.easy.network.protocols;

import com.bydavy.easy.network.EasyInternalSettings;
import com.bydavy.easy.network.EasyMessage;
import com.bydavy.easy.network.EasyMessageBaseAsyncReader;
import com.bydavy.easy.network.utils.LogHelper;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.io.IOException;
import java.nio.ByteBuffer;

public class PrefixedBySizeAsyncReader extends EasyMessageBaseAsyncReader {
    private static final String TAG = "EasyMessageAsyncReader";
    private static final boolean DEBUG = EasyInternalSettings.DEBUG ? false : EasyInternalSettings.DEBUG_DEFAULT_FALSE;
    private static final boolean DEBUG_READ = EasyInternalSettings.DEBUG ? false : EasyInternalSettings.DEBUG_DEFAULT_FALSE;

    @GuardedBy("mLock")
    boolean mReadContentLength;
    @Nullable
    @GuardedBy("mLock")
    private ByteBuffer mContentBuffer;
    @Nullable
    @GuardedBy("mLock")
    private ByteBuffer mContentLengthBuffer;
    @Nullable
    @GuardedBy("mLock")
    private PrefixedBySizeReader mMessageReader;


    public PrefixedBySizeAsyncReader(PrefixedBySizeReader messageReader) {
        super();
        mReadContentLength = true;

        mMessageReader = messageReader;
        mContentLengthBuffer = ByteBuffer.allocate(messageReader.getMessageContentLengthSizeInBytes());
        mContentBuffer = ByteBuffer.allocate(messageReader.getMessageContentSizeMaxInBytes());
    }

    @Override
    public boolean read() throws IOException {
        synchronized (mLock) {
            try {
                if (!isStarted()) {
                    return false;
                }

                if (mReadContentLength) {
                    int read = getChannel().read(mContentLengthBuffer);
                    if (read == -1) {
                        mListener.onStreamClosed(this);
                        return false;
                    }
                    if (DEBUG_READ) {
                        LogHelper.d(TAG, "Read " + read + " bytes from " + getChannel());
                    }
                    if (!mContentLengthBuffer.hasRemaining()) {
                        mContentLengthBuffer.flip();
                        int contentSize = mMessageReader.readMessageContentLength(mContentLengthBuffer);
                        mContentLengthBuffer.clear();

                        mContentBuffer.clear();
                        mContentBuffer.limit(contentSize);
                        mReadContentLength = false;
                    }
                }

                if (!mReadContentLength) {
                    int read = getChannel().read(mContentBuffer);
                    if (read == -1) {
                        mListener.onStreamClosed(this);
                        return false;
                    }
                    if (DEBUG_READ) {
                        LogHelper.d(TAG, "Read " + read + " bytes from " + getChannel());
                    }
                    if (!mContentBuffer.hasRemaining()) {
                        mContentBuffer.flip();
                        EasyMessage easyMessage = mMessageReader.readMessageContent(mContentBuffer);
                        if (DEBUG) {
                            LogHelper.d(TAG, "Received " + easyMessage);
                        }
                        mListener.onMessageRead(this, easyMessage);
                        mContentBuffer.clear();
                        mReadContentLength = true;
                    }
                }

            } catch (IOException e) {
                mListener.onError(this);
                throw e;
            }

            return true;
        }
    }
}
