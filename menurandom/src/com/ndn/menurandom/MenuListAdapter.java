package com.ndn.menurandom;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ndn.menurandom.data.MenuInfo;
//import com.ndn.menurandom.MainTab1Activity;

public class MenuListAdapter extends BaseAdapter{

	private MainTab1Activity activity;
		
	private List<MenuInfo> mMenuInfos= null;
	private LayoutInflater inflater=null;
	  
	private ImageDownloader downloader;
  

  

	public MenuListAdapter(MainTab1Activity activity, List<MenuInfo> menuInfos) {
		this.activity = activity;
		this.mMenuInfos = menuInfos;
		
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		downloader = new ImageDownloader(activity, "/cache/menurandom", R.drawable.ic_launcher, false);
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

	public View getView(final int position, View convertView, ViewGroup parent) {
		View vi=convertView;
		ViewHolder holder;
    
    
		if(convertView==null){
			vi = inflater.inflate(R.layout.mylist, null);
			holder=new ViewHolder();
			holder.name=(TextView)vi.findViewById(R.id.text);
			holder.image=(ImageView)vi.findViewById(R.id.img);
			holder.btn =(ImageButton)vi.findViewById(R.id.btn);
	      
	      //this.getItemId(position)
			vi.setTag(holder);
		}
		else
		{
			holder=(ViewHolder)vi.getTag();
		}
    
		final MenuInfo menuInfo = mMenuInfos.get(position);
		final String name = menuInfo.getName();
		holder.name.setText(name);
		final String pictureName = menuInfo.getPictureName();
    
		String url = "http://211.190.5.182/jpgdown/" + menuInfo.getPictureName() + ".jpg";
		downloader.download(url, holder.image);

			holder.btn.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					
					//Toast toast = Toast.makeText(activity, menuInfo.getId(), Toast.LENGTH_SHORT);
					//toast.show();
					
					
					activity.moveShowPage(name, pictureName);
					
					//String str = mMenuInfos.get(position);
					//int str2 = arSrc.get(pos).Icon;

					//moveShowPage(str,"");//메뉴 소개 페이지로 이동!
					
					//activity.moveShowPage(txt, img)
				}
			});

		return vi;
	}
	  
	public class ViewHolder{
		public TextView name;
		public ImageView image;
		public ImageButton btn;
	}
}



