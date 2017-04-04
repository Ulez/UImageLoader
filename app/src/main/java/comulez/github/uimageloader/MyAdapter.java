package comulez.github.uimageloader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Ulez on 2017/3/31.
 * Email：lcy1532110757@gmail.com
 */


public class MyAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> urls;
    private LayoutInflater mInflater;

    public MyAdapter(Context context, ArrayList<String> urls) {
        this.context = context;
        this.urls = urls;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public Object getItem(int position) {
        return urls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.image_item, null);
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ImageLoader.getInstance(context).defaultImg(R.drawable.image_default).error(R.drawable.load_error).url(urls.get(position), holder.imageView);
        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
    }
}
