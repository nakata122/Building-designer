package opengl.example.user.hideandseek;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * Created by User on 24.9.2016 г..
 */
public class Adapter extends BaseAdapter{
    Context ctx;
    String[] images;

    public void setCtx(Context context, String[] img) {
        this.ctx = context;
        this.images = img;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View v = inflater.inflate(R.layout.imgitem, null);

        ImageView imView;
        imView = (ImageView) v.findViewById(R.id.imageItem);
        imView.setImageResource(ctx.getResources().getIdentifier(images[position],"drawable",ctx.getPackageName()));
        return v;
    }
}
