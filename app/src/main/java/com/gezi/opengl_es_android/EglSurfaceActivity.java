package com.gezi.opengl_es_android;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class EglSurfaceActivity extends AppCompatActivity {
    private MineGlSurfaceView mineGlSurfaceView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_egl_surface);

        GLSurfaceView
        mineGlSurfaceView=findViewById(R.id.mine_glsurface);
    }
}
