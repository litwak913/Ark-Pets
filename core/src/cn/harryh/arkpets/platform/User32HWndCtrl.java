/** Copyright (c) 2022-2024, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.platform;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser;

import java.util.ArrayList;


public class User32HWndCtrl extends HWndCtrl {
    protected final HWND hWnd;
    protected final Pointer windowPointer;

    public static final int WS_EX_TOPMOST       = 0x00000008;
    public static final int WS_EX_TRANSPARENT   = 0x00000020;
    public static final int WS_EX_TOOLWINDOW    = 0x00000080;
    public static final int WS_EX_LAYERED       = 0x00080000;

    public static final int WM_MOUSEMOVE    = 0x0200;
    public static final int WM_LBUTTONDOWN  = 0x0201;
    public static final int WM_LBUTTONUP    = 0x0202;
    public static final int WM_RBUTTONDOWN  = 0x0204;
    public static final int WM_RBUTTONUP    = 0x0205;
    public static final int WM_MBUTTONDOWN  = 0x0207;
    public static final int WM_MBUTTONUP    = 0x0208;

    private static final int MK_LBUTTON  = 0x0001;
    private static final int MK_RBUTTON  = 0x0002;
    private static final int MK_MBUTTON  = 0x0010;


    /** HWnd Controller instance.
     * @param hWnd The handle of the window.
     */
    protected User32HWndCtrl(HWND hWnd) {
        super(getWindowText(hWnd), getWindowRect(hWnd));
        this.hWnd = hWnd;
        windowPointer = getWindowIdx(hWnd);
    }

    /** Finds a window.
     * @param className The class name of the window.
     * @param windowName The title of the window.
     */
    public static HWndCtrl find(String className, String windowName) {
        HWND hwnd = User32.INSTANCE.FindWindow(className, windowName);
        if (hwnd != null) {
            return new User32HWndCtrl(hwnd);
        }
        return null;
    }

    @Override
    public boolean isForeground() {
        return hWnd.equals(User32.INSTANCE.GetForegroundWindow());
    }

    @Override
    public boolean isVisible() {
        return visible(hWnd);
    }

    @Override
    public boolean close(int timeout) {
        return User32.INSTANCE.SendMessageTimeout(hWnd, 0x10, null, null, timeout, WinUser.SMTO_NORMAL, null).intValue() == 0;
    }

    @Override
    public HWndCtrl updated() {
        return new User32HWndCtrl(hWnd);
    }

    /** Gets the value of the window's extended styles.
     * @return EX_STYLE value.
     * @see WinUser
     */
    public int getWindowExStyle() {
        return User32.INSTANCE.GetWindowLong(hWnd, WinUser.GWL_EXSTYLE);
    }

    @Override
    public void setForeground() {
        User32.INSTANCE.SetForegroundWindow(hWnd);
    }

    @Override
    public void setWindowAlpha(float alpha) {
        alpha = Math.max(0, Math.min(1, alpha));
        byte byteAlpha = (byte)((int)(alpha * 255) & 0xFF);
        User32.INSTANCE.SetLayeredWindowAttributes(hWnd, 0, byteAlpha, User32.LWA_ALPHA);
    }

    /** Sets the window's extended styles.
     * @param newLong New EX_STYLE value.
     * @see WinUser
     */
    public void setWindowExStyle(int newLong) {
        User32.INSTANCE.SetWindowLong(hWnd, WinUser.GWL_EXSTYLE, newLong);
    }

    @Override
    public void setWindowPosition(HWndCtrl insertAfter, int x, int y, int w, int h) {
        User32.INSTANCE.SetWindowPos(hWnd, ((User32HWndCtrl)insertAfter).hWnd, x, y, w, h, WinUser.SWP_NOACTIVATE);
    }

    @Override
    public void setWindowTransparent(boolean enable) {
        if (enable)
            setWindowExStyle(getWindowExStyle() | User32HWndCtrl.WS_EX_TRANSPARENT);
        else
            setWindowExStyle(getWindowExStyle() & ~User32HWndCtrl.WS_EX_TRANSPARENT);
    }

    @Override
    public void setToolWindow(boolean enable) {
        if (enable)
            setWindowExStyle(getWindowExStyle() | User32HWndCtrl.WS_EX_TOOLWINDOW);
        else
            setWindowExStyle(getWindowExStyle() & ~User32HWndCtrl.WS_EX_TOOLWINDOW);
    }

    @Override
    public void setLayered(boolean enable) {
        if (enable)
            setWindowExStyle(getWindowExStyle() | User32HWndCtrl.WS_EX_LAYERED);
        else
            setWindowExStyle(getWindowExStyle() & ~User32HWndCtrl.WS_EX_LAYERED);
    }

    @Override
    public void setTopmost(boolean enable) {
        if (enable)
            setWindowExStyle(getWindowExStyle() | User32HWndCtrl.WS_EX_TOPMOST);
        else
            setWindowExStyle(getWindowExStyle() & ~User32HWndCtrl.WS_EX_TOPMOST);
    }

    @Override
    public void sendMouseEvent(MouseEvent msg, int x, int y) {
        int wmsg = switch (msg) {
            case MOUSEMOVE -> WM_MOUSEMOVE;
            case LBUTTONDOWN -> WM_LBUTTONDOWN;
            case LBUTTONUP -> WM_LBUTTONUP;
            case RBUTTONDOWN -> WM_RBUTTONDOWN;
            case RBUTTONUP -> WM_RBUTTONUP;
            case MBUTTONDOWN -> WM_MBUTTONDOWN;
            case MBUTTONUP -> WM_MBUTTONUP;
            default -> 0;
        };
        int wParam = switch (msg) {
            case LBUTTONDOWN -> MK_LBUTTON;
            case RBUTTONDOWN -> MK_RBUTTON;
            case MBUTTONDOWN -> MK_MBUTTON;
            default -> 0;
        };
        int lParam = (y << 16) | x;
        User32.INSTANCE.SendMessage(hWnd, wmsg, new WinDef.WPARAM(wParam), new WinDef.LPARAM(lParam));
    }

    /** Gets the current list of windows.
     * @param only_visible Whether exclude the invisible window.
     * @return An ArrayList consists of HWndCtrls.
     */
    public static ArrayList<User32HWndCtrl> getWindowList(boolean only_visible) {
        ArrayList<User32HWndCtrl> windowList = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hWnd, arg1) -> {
            if (User32.INSTANCE.IsWindow(hWnd) && (!only_visible || visible(hWnd)))
                windowList.add(new User32HWndCtrl(hWnd));
            return true;
        }, null);
        return windowList;
    }

    /** Gets the current list of windows. (Advanced)
     * @param only_visible Whether exclude the invisible window.
     * @param exclude_ws_ex Exclude the specific window-style-extra.
     * @return An ArrayList consists of HWndCtrls.
     */
    public static ArrayList<User32HWndCtrl> getWindowList(boolean only_visible, long exclude_ws_ex) {
        ArrayList<User32HWndCtrl> windowList = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hWnd, arg1) -> {
            if (User32.INSTANCE.IsWindow(hWnd) && (!only_visible || visible(hWnd))
                    && (User32.INSTANCE.GetWindowLong(hWnd, WinUser.GWL_EXSTYLE) & exclude_ws_ex) != exclude_ws_ex)
                windowList.add(new User32HWndCtrl(hWnd));
            return true;
        }, null);
        return windowList;
    }

    /** Gets the topmost window.
     * @return The topmost window's HWndCtrl.
     */
    public static User32HWndCtrl getTopmost() {
        return new User32HWndCtrl(new HWND(Pointer.createConstant(-1)));
    }

    private static boolean visible(HWND hWnd) {
        try {
            if (!User32.INSTANCE.IsWindowVisible(hWnd) || !User32.INSTANCE.IsWindowEnabled(hWnd))
                return false;
            WindowRect rect = getWindowRect(hWnd);
            if (rect.top() == rect.bottom() || rect.left() == rect.right())
                return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static Pointer getWindowIdx(HWND hWnd) {
        return hWnd.getPointer();
    }

    public static String getWindowText(HWND hWnd) {
        char[] text = new char[1024];
        User32.INSTANCE.GetWindowText(hWnd, text, 1024);
        return Native.toString(text);
    }

    public static WindowRect getWindowRect(HWND hWnd) {
        RECT rect = new RECT();
        User32.INSTANCE.GetWindowRect(hWnd, rect);
        return new WindowRect(rect.top, rect.bottom, rect.left, rect.right);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User32HWndCtrl hWndCtrl = (User32HWndCtrl)o;
        return hWnd.equals(hWndCtrl.hWnd);
    }

    @Override
    public int hashCode() {
        return hWnd.hashCode();
    }

    @Override
    public String toString() {
        return "‘" + windowText + "’ " + windowWidth + "*" + windowHeight + " style=" + getWindowExStyle();
    }
}
