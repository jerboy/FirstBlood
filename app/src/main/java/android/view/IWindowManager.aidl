package android.view;

import android.graphics.Point;

interface IWindowManager {
  int getRotation();
   void getInitialDisplaySize(int displayId, out Point size);
   void getBaseDisplaySize(int displayId, out Point size);
}