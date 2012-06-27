package com.ndn.example;

import com.ndn.menurandom.ImageDownloader;
import com.ndn.menurandom.R;
import com.ndn.menurandom.R.drawable;
import com.ndn.menurandom.R.id;
import com.ndn.menurandom.R.layout;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LazyAdapter extends BaseAdapter {

  private Activity activity;
  private Context context;

  private String[] eventTitle;
  private String[] eventDate;
  private String[] eventImage;
  private LayoutInflater inflater=null;
  
  private ImageDownloader downloader;
  //public static ImageLoader imageLoader;
  private static String tag = "LazyAdapter"; 

  public LazyAdapter(Activity a, String[] eventTitle,String[] eventDate,String[] eventImage) {
    activity = a;
    this.eventTitle = eventTitle;
    this.eventDate = eventDate;
    this.eventImage = eventImage;
    inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    //imageLoader=new ImageLoader(activity.getApplicationContext());
    
    downloader = new ImageDownloader(a, "/cache/lazyList", R.drawable.ic_launcher, false);
  }
 

  public LazyAdapter(Context a, String[] eventTitle,String[] eventDate,String[] eventImage) {
    context = a;
    this.eventTitle = eventTitle;
    this.eventDate = eventDate;
    this.eventImage = eventImage;
    
    inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    //inflater = LayoutInflater.from(context);

    //imageLoader=new ImageLoader(context.getApplicationContext());
    downloader = new ImageDownloader(a, "/cache/lazyList", R.drawable.ic_launcher, false);
    
  }


//  public LazyAdapter(OnClickListener onClickListener,String[] eventTitle, String[] eventDate,String[] eventImage) {
//    this.context =(Context) onClickListener;
//    this.eventTitle = eventTitle;
//    this.eventDate = eventDate;
//    this.eventImage = eventImage;
//    inflater = LayoutInflater.from((Context) onClickListener);
//    imageLoader=new ImageLoader(((Context) onClickListener).getApplicationContext());
//
//  }

  public int getCount() {
    return eventTitle.length;
  }

  public Object getItem(int position) {
    return position;
  }

  public long getItemId(int position) {
    return position;
  }

  public static class ViewHolder{
    public TextView firstLine;
    public TextView secondLine;
    public ImageView image;
  }

  public View getView(int position, View convertView, ViewGroup parent) {
    View vi=convertView;
    ViewHolder holder;
    System.out.println("in getView");
    if(convertView==null){
      System.out.println("getView is null");
      vi = inflater.inflate(R.layout.mylist, null);
      holder=new ViewHolder();
      holder.firstLine=(TextView)vi.findViewById(R.id.text);
      holder.secondLine=(TextView)vi.findViewById(R.id.id);
      holder.image=(ImageView)vi.findViewById(R.id.img);
      vi.setTag(holder);
    }
    else
      holder=(ViewHolder)vi.getTag();
    System.out.println("getView tagging now");
    Log.i(tag , "event "+eventTitle[position]+"date "+eventDate[position]+"link "+eventImage[position]);
    holder.firstLine.setText(eventTitle[position]);
    holder.secondLine.setText(eventDate[position]);
    holder.image.setTag(eventImage[position]);

    downloader.download(eventImage[position], holder.image);
    
    //imageLoader(eventImage[position], context, holder.image);

    return vi;
  }
}