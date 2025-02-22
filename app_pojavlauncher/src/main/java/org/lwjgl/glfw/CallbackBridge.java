package org.lwjgl.glfw;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.*;
import android.widget.*;
import net.kdt.pojavlaunch.*;
import android.content.*;

public class CallbackBridge {
    public static final int ANDROID_TYPE_GRAB_STATE = 0;
    
    public static final int CLIPBOARD_COPY = 2000;
    public static final int CLIPBOARD_PASTE = 2001;
    
    public static volatile int windowWidth, windowHeight;
    public static int mouseX, mouseY;
    public static boolean mouseLeft;
    public static StringBuilder DEBUG_STRING = new StringBuilder();
    
    // volatile private static boolean isGrabbing = false;

    public static void putMouseEventWithCoords(int button, int x, int y /* , int dz, long nanos */) {
        putMouseEventWithCoords(button, 1, x, y);
        putMouseEventWithCoords(button, 0, x, y);
    }
    
    public static void putMouseEventWithCoords(int button, int state, int x, int y /* , int dz, long nanos */) {
        sendCursorPos(x, y);
        sendMouseKeycode(button, CallbackBridge.getCurrentMods(), state == 1);
    }

    private static boolean threadAttached;
    public static void sendCursorPos(int x, int y) {
        if (!threadAttached) {
            threadAttached = CallbackBridge.nativeAttachThreadToOther(true, BaseMainActivity.isInputStackCall);
        }
        
        DEBUG_STRING.append("CursorPos=" + x + ", " + y + "\n");
        mouseX = x;
        mouseY = y;
        nativeSendCursorPos(x, y);
    }
    
    public static void sendPrepareGrabInitialPos() {
        DEBUG_STRING.append("Prepare set grab initial posititon");
        sendMouseKeycode(-1, CallbackBridge.getCurrentMods(), false);
    }

    public static void sendKeycode(int keycode, char keychar, int scancode, int modifiers, boolean isDown) {
        DEBUG_STRING.append("KeyCode=" + keycode + ", Char=" + keychar);
        // TODO CHECK: This may cause input issue, not receive input!
/*
        if (!nativeSendCharMods((int) keychar, modifiers) || !nativeSendChar(keychar)) {
            nativeSendKey(keycode, 0, isDown ? 1 : 0, modifiers);
        }
*/

        //nativeSendKeycode(keycode, keychar, scancode, isDown ? 1 : 0, modifiers);
        if(keycode != 0)  nativeSendKey(keycode,scancode,isDown ? 1 : 0, modifiers);
        else nativeSendKey(32,scancode,isDown ? 1 : 0, modifiers);
        if(isDown && keychar != '\u0000') {
            nativeSendCharMods(keychar,modifiers);
            nativeSendChar(keychar);
        }
        //throw new IllegalStateException("Tracing call");
        // sendData(JRE_TYPE_KEYCODE_CONTROL, keycode, Character.toString(keychar), Boolean.toString(isDown), modifiers);
    }

    public static void sendMouseKeycode(int button, int modifiers, boolean isDown) {
        DEBUG_STRING.append("MouseKey=" + button + ", down=" + isDown + "\n");
        // if (isGrabbing()) DEBUG_STRING.append("MouseGrabStrace: " + android.util.Log.getStackTraceString(new Throwable()) + "\n");
        nativeSendMouseButton(button, isDown ? 1 : 0, modifiers);
    }

    public static void sendMouseKeycode(int keycode) {
        sendMouseKeycode(keycode, CallbackBridge.getCurrentMods(), true);
        sendMouseKeycode(keycode, CallbackBridge.getCurrentMods(), false);
    }
    
    public static void sendScroll(double xoffset, double yoffset) {
        DEBUG_STRING.append("ScrollX=" + xoffset + ",ScrollY=" + yoffset);
        nativeSendScroll(xoffset, yoffset);
    }

    public static void sendUpdateWindowSize(int w, int h) {
        nativeSendScreenSize(w, h);
    }

    public static boolean isGrabbing() {
        // return isGrabbing;
        return nativeIsGrabbing();
    }

    // Called from JRE side
    public static String accessAndroidClipboard(int type, String copy) {
        switch (type) {
            case CLIPBOARD_COPY:
                BaseMainActivity.GLOBAL_CLIPBOARD.setPrimaryClip(ClipData.newPlainText("Copy", copy));
                return null;
                
            case CLIPBOARD_PASTE:
                if (BaseMainActivity.GLOBAL_CLIPBOARD.hasPrimaryClip() && BaseMainActivity.GLOBAL_CLIPBOARD.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    return BaseMainActivity.GLOBAL_CLIPBOARD.getPrimaryClip().getItemAt(0).getText().toString();
                } else {
                    return "";
                }
                
            default: return null;
        }
    }
    public static void receiveCallback(int type, String data) {
        switch (type) {
            case ANDROID_TYPE_GRAB_STATE:
                // isGrabbing = Boolean.parseBoolean(data);
                break;
        }
    }
/*
    private static String currData;
    public static void sendData(int type, Object... dataArr) {
        currData = "";
        for (int i = 0; i < dataArr.length; i++) {
            if (dataArr[i] instanceof Integer) {
                currData += Integer.toString((int) dataArr[i]);
            } else if (dataArr[i] instanceof String) {
                currData += (String) dataArr[i];
            } else {
                currData += dataArr[i].toString();
            }
            currData += (i + 1 < dataArr.length ? ":" : "");
        }
        nativeSendData(true, type, currData);
    }
    private static native void nativeSendData(boolean isAndroid, int type, String data);
*/

    public static boolean holdingAlt, holdingCapslock, holdingCtrl,
        holdingNumlock, holdingShift;
    public static int getCurrentMods() {
        int currMods = 0;
        if (holdingAlt) {
            currMods &= LWJGLGLFWKeycode.GLFW_MOD_ALT;
        } if (holdingCapslock) {
            currMods &= LWJGLGLFWKeycode.GLFW_MOD_CAPS_LOCK;
        } if (holdingCtrl) {
            currMods &= LWJGLGLFWKeycode.GLFW_MOD_CONTROL;
        } if (holdingNumlock) {
            currMods &= LWJGLGLFWKeycode.GLFW_MOD_NUM_LOCK;
        } if (holdingShift) {
            currMods &= LWJGLGLFWKeycode.GLFW_MOD_SHIFT;
        }
        return currMods;
    }

    public static native boolean nativeAttachThreadToOther(boolean isAndroid, boolean isUsePushPoll);

    private static native boolean nativeSendChar(char codepoint);
    // GLFW: GLFWCharModsCallback deprecated, but is Minecraft still use?
    private static native boolean nativeSendCharMods(char codepoint, int mods);
    private static native void nativeSendKey(int key, int scancode, int action, int mods);
    // private static native void nativeSendCursorEnter(int entered);
    private static native void nativeSendCursorPos(int x, int y);
    private static native void nativeSendMouseButton(int button, int action, int mods);
    private static native void nativeSendScroll(double xoffset, double yoffset);
    private static native void nativeSendScreenSize(int width, int height);
    
    public static native boolean nativeIsGrabbing();
    public static native void nativePutControllerAxes(FloatBuffer axBuf);
    public static native void nativePutControllerButtons(ByteBuffer axBuf);
    static {
        System.loadLibrary("pojavexec");
    }
}

