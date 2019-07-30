package com.gezi.opengl_es_android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

/**
 * 自定义GlSurfaceView
 * <p>
 * 1.继承SurfaceView,并实现其CallBack回调
 * 2.自定义GlThread线程类，用于OpenGL的绘制操作
 * 3.添加设置Surface和EglContext的方法
 * 4.提供和系统GlSurfaceView相同的调用方法
 */
public abstract class EglSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;


    private Surface surface;
    private EGLContext eglContext;
    private GlThread glThread;
    private Render mRender;

    private int mRenderMode = RENDERMODE_CONTINUOUSLY;



    public EglSurfaceView(Context context) {
        this(context, null);
    }

    public EglSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EglSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }


    public void setRender(Render mRender) {
        this.mRender = mRender;
    }

    public void setRenderMode(int mRenderMode) {
        if (mRender == null) {
            throw new RuntimeException("must set render before");
        }
        this.mRenderMode = mRenderMode;
    }

    public void setSurfaceAndEglContext(Surface surface, EGLContext eglContext) {
        this.surface = surface;
        this.eglContext = eglContext;
    }

    public EGLContext getEglContext() {
        if (glThread != null) {
            return glThread.getEglContext();
        }
        return null;
    }

    public void requestRender() {
        if (glThread != null) {
            glThread.requestRender();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (null == surface) {
            surface = holder.getSurface();
        }

        glThread = new GlThread(new WeakReference<EglSurfaceView>(this));
        glThread.isCreate = true;
        glThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        glThread.width = width;
        glThread.height = height;
        glThread.isChange = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        glThread.onDestory();

        glThread = null;
        surface = null;
        eglContext = null;
    }

    /**
     *
     */
    public interface Render {
        void onSurfaceCreated();

        void onSurfaceChanged(int width, int height);

        void onDrawFrame();
    }

    /**
     * egl线程
     */
    static class GlThread extends Thread {
        private Object object = null;//线程等待方法需要创建一个object

        private WeakReference<EglSurfaceView> mineGlSurfaceViewWeakReference;
        private EglHelper eglHelper = null;

        private boolean isExit = false;//是否退出
        private boolean isCreate = false;//是否创建
        private boolean isChange = false;
        private boolean isStart = false;//是否开始

        private int width;
        private int height;


        public GlThread(WeakReference<EglSurfaceView> mineGlSurfaceViewWeakReference) {
            this.mineGlSurfaceViewWeakReference = mineGlSurfaceViewWeakReference;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            isStart = false;
            object = new Object();
            eglHelper = new EglHelper();
            eglHelper.initEgl(mineGlSurfaceViewWeakReference.get().surface,
                    mineGlSurfaceViewWeakReference.get().eglContext);

            while (true) {

                if (isExit) {
                    //释放资源
                    release();
                    break;
                }

                if (isStart) {
                    //如果是手动刷新
                    if (mineGlSurfaceViewWeakReference.get().mRenderMode == EglSurfaceView.RENDERMODE_WHEN_DIRTY) {
                        synchronized (object) {
                            try {
                                object.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    } else if (mineGlSurfaceViewWeakReference.get().mRenderMode ==
                            EglSurfaceView.RENDERMODE_CONTINUOUSLY) {
                        try {
                            Thread.sleep(1000 / 60);//保证每秒60帧的刷新速率
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw new RuntimeException("mrenderMode is wrong value");
                    }
                }


                onCreate();
                onChange(width, height);
                onDraw();

                isStart = true;
            }
        }

        private void onCreate() {
            if (isCreate && mineGlSurfaceViewWeakReference.get().mRender != null) {
                isCreate = false;
                mineGlSurfaceViewWeakReference.get().mRender.onSurfaceCreated();
            }
        }

        private void onChange(int width, int height) {
            if (isChange && mineGlSurfaceViewWeakReference.get().mRender != null) {
                isChange = false;
                mineGlSurfaceViewWeakReference.get().mRender.onSurfaceChanged(width, height);
            }
        }

        private void onDraw() {
            if (mineGlSurfaceViewWeakReference.get().mRender != null
                    && eglHelper != null
            ) {
                mineGlSurfaceViewWeakReference.get().mRender.onDrawFrame();
                if (!isStart) {
                    mineGlSurfaceViewWeakReference.get().mRender.onDrawFrame();
                }
                eglHelper.swapBuffers();
            }
        }

        private void requestRender() {
            if (object != null) {
                synchronized (object) {
                    object.notifyAll();
                }
            }
        }

        public void onDestory() {
            isExit = true;
            requestRender();
        }

        public void release() {
            if (eglHelper != null) {
                eglHelper.destory();
                eglHelper = null;
                object = null;
                mineGlSurfaceViewWeakReference = null;
            }
        }

        public EGLContext getEglContext() {
            if (eglHelper != null) {
                return eglHelper.getmEglContext();
            }
            return null;
        }

    }
}
