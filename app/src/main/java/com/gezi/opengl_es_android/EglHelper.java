package com.gezi.opengl_es_android;

import android.opengl.EGL14;
import android.util.Log;
import android.view.Surface;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * 创建EglHelper步骤
 * 1.得到Egl实例
 * 2.得到默认的显示设备(窗口)
 * 3.初始化默认显示设备
 * 4.设置显示设备的属性
 * 5.从系统中获取对应属性的配置
 * 6.创建EglContext
 * 7.创建渲染的Surface
 * 8.绑定EglContext和surface到显示设备中
 * 9.刷新数据,显示渲染场景
 */

public class EglHelper {

    EGL10 mEgl;
    EGLDisplay mEglDisplay;
    EGLContext mEglContext;
    EGLSurface mEglSurface;


    public void initEgl(Surface surface, EGLContext eglContext) {


        /*
         * Get an EGL instance
         */
        mEgl = (EGL10) EGLContext.getEGL();

        /*
         * Get to the default display.
         */
        mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed(egl获取显示设备失败)");
        }

        /*
         * We can now initialize EGL for that display
         * 初始化默认显示设备
         */
        int[] version = new int[2];
        if (!mEgl.eglInitialize(mEglDisplay, version)) {
            throw new RuntimeException("eglInitialize failed(初始化默认显示设备失败)");
        }

        /**
         * 设置属性
         */

        int[] attribute = new int[]{
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 8,//深度大小
                EGL10.EGL_STENCIL_SIZE, 8,//模板大小
                EGL10.EGL_RENDERABLE_TYPE, 4,//2.0版本 值为4
                EGL10.EGL_NONE  //以此结尾
        };
        //5.
        int[] num_config = new int[1];
        if (!mEgl.eglChooseConfig(mEglDisplay, attribute, null, 1, num_config)) {
            throw new IllegalArgumentException("eglChooseConfig failed(从系统中获取对应的属性失败)");
        }

        int numConfigs = num_config[0];

        if (numConfigs <= 0) {
            throw new IllegalArgumentException(
                    "No configs match configSpec(没有匹配到相应的属性)");
        }

        EGLConfig[] configs = new EGLConfig[numConfigs];
        if (!mEgl.eglChooseConfig(mEglDisplay, attribute, configs, numConfigs,
                num_config)) {
            throw new IllegalArgumentException("eglChooseConfig#2 failed");
        }


        //6,
        int[] attrib_list = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE};


        if (eglContext != null) {
            //如果不是null，
            mEglContext = mEgl.eglCreateContext(mEglDisplay,
                    configs[0], eglContext, attrib_list);
        } else {
            //如果是null，创建一个新的eglContext
            mEglContext = mEgl.eglCreateContext(mEglDisplay,
                    configs[0], EGL10.EGL_NO_CONTEXT, attrib_list);
        }

        //7.
        mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay,
                configs[0],
                surface,
                null
        );

        if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
            int error = mEgl.eglGetError();
            if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                Log.e("EglHelper", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.(创建surface失败)");
            }
        }

        //8


        if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw new IllegalArgumentException("eglMakeCurrent failed " + mEgl.eglGetError());
        }

    }


    public boolean swapBuffers() {
        if (null != mEgl) {
            return mEgl.eglSwapBuffers(mEglDisplay,
                    mEglSurface);
        } else {
            throw new IllegalArgumentException("mEgl is null ");
        }
    }

    public EGLContext getmEglContext() {
        return mEglContext;
    }


    public void destory() {
        if (null != mEgl) {

            if (mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {
                mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_CONTEXT);
                mEgl.eglDestroySurface(mEglDisplay, mEglSurface);
                mEglSurface = null;
            }

            if (mEglContext != null) {
                mEgl.eglDestroyContext(mEglDisplay, mEglContext);
                mEglContext = null;
            }

            if (mEglDisplay != null) {
                mEgl.eglTerminate(mEglDisplay);
                mEglDisplay = null;
            }
            mEgl = null;
        }
    }
}
