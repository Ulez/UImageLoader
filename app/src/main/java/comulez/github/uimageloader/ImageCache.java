package comulez.github.uimageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static comulez.github.uimageloader.Utils.keyFormUrl;


/**
 * Created by Ulez on 2017/3/31.
 * Email：lcy1532110757@gmail.com
 */

public class ImageCache implements IImageCacahe {
    private LruCache<String, Bitmap> lruCache;
    private DiskLruCache diskLruCache;
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 20;
    private static final int DISK_CACHE_INDEX = 0;
    private String TAG = "ImageCache";

    public ImageCache(Context mContext) {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        lruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
        File diskCacheDir = getDiskCacheDir(mContext.getApplicationContext(), "bitmap");
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }
        try {
            diskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getDiskCacheDir(Context context, String uniqueName) {
        boolean externalStorageAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if (externalStorageAvailable) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        Log.i(TAG, cachePath + File.separator + uniqueName);
        return new File(cachePath + File.separator + uniqueName);
    }

    private void addToDiskCache(Bitmap bitmap, String key) throws IOException {
        DiskLruCache.Editor editor = diskLruCache.edit(key);
        if (editor != null) {
            OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
            if (addToDisk(bitmap, outputStream)) {
                editor.commit();
            } else {
                editor.abort();
            }
            diskLruCache.flush();
            outputStream.close();
        }
    }


    public boolean addToDisk(Bitmap bitmap, OutputStream outputStream) {
        return bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
    }


    private void addToLruCache(Bitmap bitmap, String key) {
        if (getLruCache(key) == null)
            lruCache.put(key, bitmap);
    }

    private Bitmap getLruCache(String key) {
        return lruCache.get(key);
    }

    @Override
    public Bitmap getFromCache(String url, int width, int height) {
        Log.e("LCY", "getFromCache,width=" + width + ",height=" + height);
        String key = keyFormUrl(url);
        Bitmap bitmap = lruCache.get(key);
        if (bitmap == null) {
            try {
                DiskLruCache.Snapshot snapShot = diskLruCache.get(key);
                Log.i(TAG, "getFromDiskCache,key=" + key + ",value=" + snapShot);
                if (snapShot != null) {
                    InputStream is = snapShot.getInputStream(DISK_CACHE_INDEX);
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                    return bitmap;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "getDiskLruCache error");
            }
        }
        return bitmap;
    }

    @Override
    public void addToCache(Bitmap bitmap, String url) throws IOException {
        String key = Utils.keyFormUrl(url);
        addToLruCache(bitmap, key);
        addToDiskCache(bitmap, key);
    }
}
