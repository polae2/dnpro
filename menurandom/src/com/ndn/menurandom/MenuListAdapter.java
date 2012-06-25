package com.ndn.menurandom;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.ndn.menurandom.LazyAdapter.ViewHolder;
import com.ndn.menurandom.data.MenuInfo;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuListAdapter extends BaseAdapter {

  private Activity activity;
  private Context context;

  private List<MenuInfo> mMenuInfos= null;
  private LayoutInflater inflater=null;
  
  private ImageDownloader downloader;
  

  public MenuListAdapter(Activity activity, List<MenuInfo> menuInfos) {
    this.activity = activity;
    this.mMenuInfos = menuInfos;
    
    inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    downloader = new ImageDownloader(activity, "/cache/lazyList", R.drawable.ic_launcher, false);
  }
  

  public int getCount() {
    return mMenuInfos.size();
  }

  public Object getItem(int position) {
    return mMenuInfos.get(position);
  }

  public long getItemId(int position) {
    return position;
  }

  public View getView(int position, View convertView, ViewGroup parent) {
    View vi=convertView;
    ViewHolder holder;
    if(convertView==null){
	      vi = inflater.inflate(R.layout.mylist, null);
	      holder=new ViewHolder();
	      holder.name=(TextView)vi.findViewById(R.id.text);
	      holder.image=(ImageView)vi.findViewById(R.id.img);
	      vi.setTag(holder);
    }
    else
    {
    	holder=(ViewHolder)vi.getTag();
    }
    
    MenuInfo menuInfo = mMenuInfos.get(position);
    holder.name.setText(menuInfo.getName());
    
    String url = "http://211.190.5.182/jpgdown/" + menuInfo.getPictureName() + ".jpg";
    downloader.download(url, holder.image);
    
    return vi;
  }
  
  public class ViewHolder{
	    public TextView name;
	    public ImageView image;
  }
  
}

