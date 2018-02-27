package com.jerboy.firstblood;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.IPowerManager.Stub;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.IWindowManager;
import android.widget.Toast;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by caojianbo on 2018/2/26.
 */

public class Main {

  public static void main(String []args){
//    export CLASSPATH=/data/app/com.jerboy.firstblood-1/base.apk  exec app_process /system/bin com.jerboy.firstblood.Main '$@'
    System.out.println("Hello 22222 " + Process.myUid() + "  pid " + Process.myPid());
    try {
      System.out.println("Hello 222222222222222 ");
      Class<?> serviceManager = Class.forName("android.os.ServiceManager");
      final Method getService = serviceManager.getDeclaredMethod("getService", String.class);
      IBinder power = (IBinder) getService.invoke(null, "power");
      IPowerManager iPowerManager = Stub.asInterface(power);
      final Point point = new Point();
      IBinder windowManager = (IBinder) getService.invoke(null, "window");
      final IWindowManager iWindowManager = IWindowManager.Stub.asInterface(windowManager);
      iWindowManager.getBaseDisplaySize(0, point);

      new Thread(new Runnable() {
        @Override
        public void run() {

          AsyncHttpServer server = new AsyncHttpServer();
          server.get("/", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, final AsyncHttpServerResponse response) {
              System.out.println("Hello / " + Thread.currentThread().getName() + point.x + "  " + point.y  );
              Bitmap screenshot = null;
              try {
                screenshot = screenshot(point.x, point.y);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                screenshot.compress(Bitmap.CompressFormat.PNG, 100, stream);
                final byte[] byteArray = stream.toByteArray();
                response.send("image/png" ,byteArray);
              } catch (Exception e) {
                response.send(e.getMessage());
              }

            }
          });
          System.out.println("Hello watch server " + Thread.currentThread().getName() + getIPAddress(true));
          server.listen(5000);
        }
      }).start();

      Looper.prepare();
      Looper.loop();
    } catch (Exception e) {
      e.printStackTrace();
      System.out.print(e.getMessage());
    }

  }


  public static Bitmap screenshot(int width, int height){
    try {
      Method screenshot = Class.forName("android.view.SurfaceControl")
          .getDeclaredMethod("screenshot", int.class, int.class);
      return (Bitmap) screenshot.invoke(null, width, height);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Get IP address from first non-localhost interface
   * @return  address or empty string
   */
  public static String getIPAddress(boolean useIPv4) {
    try {
      List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface intf : interfaces) {
        List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
        for (InetAddress addr : addrs) {
          if (!addr.isLoopbackAddress()) {
            String sAddr = addr.getHostAddress();
            //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
            boolean isIPv4 = sAddr.indexOf(':')<0;

            if (useIPv4) {
              if (isIPv4)
                return sAddr;
            } else {
              if (!isIPv4) {
                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
              }
            }
          }
        }
      }
    } catch (Exception ex) { } // for now eat exceptions
    return "";
  }
}

