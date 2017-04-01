package comulez.github.uimageloader;

import android.graphics.Bitmap;

import java.io.IOException;

/**
 * Created by Ulez on 2017/3/31.
 * Email：lcy1532110757@gmail.com
 */

public interface IImageCacahe {
    Bitmap getFromCache(String url, int width, int height);
    void addToCache(Bitmap bitmap, String url) throws IOException;
}
