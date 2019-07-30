package com.gezi.opengl_es_android;

import android.content.Context;
import android.util.AttributeSet;

public class MineGlSurfaceView extends EglSurfaceView {
    public MineGlSurfaceView(Context context) {
        this(context,null);
    }

    public MineGlSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MineGlSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setRender(new MineRender());
    }
}
