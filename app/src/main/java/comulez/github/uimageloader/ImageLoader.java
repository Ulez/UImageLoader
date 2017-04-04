package comulez.github.uimageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Ulez on 2017/3/30.
 * Email：lcy1532110757@gmail.com
 */


public class ImageLoader {
    private static ImageLoader singleton;
    private IImageCacahe cache;
    private final ExecutorService cachedThreadPool;
    private static final String TAG = "ImageLoader";
    private static final int TAG_KEY_URI = R.id.imageloader_uri;
    private static final int IO_BUFFER_SIZE = 8 * 1024;

    private static final int SUCCESS_COMPLETE = 1;
    static final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS_COMPLETE: {
                    LoaderResult result = (LoaderResult) msg.obj;
                    ImageView imageView = result.imageView;
                    String uri = (String) imageView.getTag(TAG_KEY_URI);
                    if (uri.equals(result.url)) {
                        imageView.setImageBitmap(result.bitmap);
                    } else {
                        Log.e(TAG, "set image bitmap,but url has changed, ignored!");
                    }
                    break;
                }
                default:
                    throw new AssertionError("Unknown handler message received: " + msg.what);
            }
        }
    };

    private ImageLoader(Context mContext) {
        cache = new ImageCache(mContext);
        cachedThreadPool = Executors.newCachedThreadPool();
    }

    public ImageLoader setCache(IImageCacahe cache) {
        this.cache = cache;
        return this;
    }


    public static ImageLoader getInstance(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        if (singleton == null) {
            synchronized (ImageLoader.class) {
                if (singleton == null) {
                    singleton = new ImageLoader(context.getApplicationContext());
                }
            }
        }
        return singleton;
    }

    /**
     * 这里下载和保存了原图，不应该直接用此bitmap设置到ImageView中去。
     *
     * @param urlString
     * @param width
     * @param height
     * @return
     */
    private Bitmap downloadBitmapFromUrl(String urlString, int width, int height) {
        Bitmap bitmap = null;
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(in);
            if (cache instanceof ImageCache)
                ((ImageCache) cache).addToDiskCache(bitmap, Utils.keyFormUrl(urlString));
            bitmap.recycle();
            bitmap = cache.getFromCache(urlString, width, height);// TODO: 2017/4/4  这里因为第一次下载的原图需要重新采样获取需求大小的bitmap，而BitmapFactory.decodeStream(in),不能decode两次。第二次会为null。
        } catch (final IOException e) {
            Log.e(TAG, "Error in downloadBitmap: " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            Utils.close(in);
        }
        return bitmap;
    }

    /**
     * @param bitmap
     * @param width
     * @param height
     * @return
     */
    private int getSampleSize(Bitmap bitmap, int width, int height) {
//        bitmap.recycle();
        int simpleSize = 1;
        return simpleSize;
    }

    public ImageLoader url(final String url, final ImageView target) {
        target.setTag(TAG_KEY_URI, url);
        target.post(new Runnable() {
            @Override
            public void run() {
                taskDownImage(url, target, target.getMeasuredWidth(), target.getMeasuredHeight());
            }
        });
        return this;
    }

    private void taskDownImage(final String url, final ImageView target, final int width, final int height) {
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = cache.getFromCache(url, width, height);
                if (bitmap != null) {
                    handler.obtainMessage(SUCCESS_COMPLETE, new LoaderResult(target, url, bitmap)).sendToTarget();
                    return;
                } else {
                    bitmap = downloadBitmapFromUrl(url, width, height);
//                    Log.e("TAG", "Height=" + bitmap.getHeight() + ",Width=" + bitmap.getWidth());
                    if (bitmap != null) {
                        handler.obtainMessage(SUCCESS_COMPLETE, new LoaderResult(target, url, bitmap)).sendToTarget();
                        try {
                            cache.addToCache(bitmap, url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    static class LoaderResult {
        ImageView imageView;
        String url;
        Bitmap bitmap;

        public LoaderResult(ImageView imageView, String url, Bitmap bitmap) {
            this.imageView = imageView;
            this.url = url;
            this.bitmap = bitmap;
        }
    }

}
