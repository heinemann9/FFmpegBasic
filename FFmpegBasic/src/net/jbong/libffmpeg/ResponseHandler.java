package net.jbong.libffmpeg;

abstract interface ResponseHandler {

    /**
     * on Start
     */
    public void onStart();

    /**
     * on Finish
     */
    public void onFinish();

}
