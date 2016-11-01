package net.jbong.libffmpeg;

import net.jbong.libffmpeg.ResponseHandler;

public interface FFmpegLoadBinaryResponseHandler extends ResponseHandler {

    /**
     * on Fail
     */
    public void onFailure();

    /**
     * on Success
     */
    public void onSuccess();

}
