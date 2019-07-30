package com.gezi.opengl_es_android;


import android.opengl.GLES20;

public class MineRender implements EglSurfaceView.Render {

    public MineRender() {
    }

    @Override
    public void onSurfaceCreated() {
        GLES20.glClearColor(1.0f,0.0f,0.0f,1.0f);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);

    }

    @Override
    public void onDrawFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

    }
}
