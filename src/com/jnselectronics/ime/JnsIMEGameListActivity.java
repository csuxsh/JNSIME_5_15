package com.jnselectronics.ime;


import com.jnselectronics.ime.uiadapter.JnsIMEGameListAdapter;
import com.jnselectronics.ime.uiadapter.JnsIMEPopAddAdapter;
import com.jnselectronics.ime.util.AppHelper;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

public class JnsIMEGameListActivity extends Activity{ 

	private JnsIMEPopAddAdapter popAdapter; 
	public static JnsIMEGameListAdapter gameAdapter;
	private ListView gameList;
	Dialog adddialog;
	private  ImageView defautCb[] = new ImageView[4];
    private  Button defaultD[] = new Button[4];
    private  Button defaultMap[] = new Button[4];
	
	@Override
	  public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_game);
        gameList = (ListView) this.findViewById(R.id.gamelist);
        Button add = (Button) this.findViewById(R.id.add_game);
        defautCb[0] = (ImageView) this.findViewById(R.id.dian_c1);
        defautCb[1] = (ImageView) this.findViewById(R.id.dian_c2);
        defautCb[2] = (ImageView) this.findViewById(R.id.dian_c3);
        defautCb[3] = (ImageView) this.findViewById(R.id.dian_c4);
        defaultD[0] = (Button) this.findViewById(R.id.dian1);
        defaultD[1] = (Button) this.findViewById(R.id.dian2);
        defaultD[2] = (Button) this.findViewById(R.id.dian3);
        defaultD[3] = (Button) this.findViewById(R.id.dian4);
        defaultMap[0] = (Button) this.findViewById(R.id.default_keymapping1);
        defaultMap[1] = (Button) this.findViewById(R.id.default_keymapping2);
        defaultMap[2] = (Button) this.findViewById(R.id.default_keymapping3);
        defaultMap[3] = (Button) this.findViewById(R.id.default_keymapping4);

        showGameList(gameList);
        chageDefaultCheckBox(JnsIMECoreService.currentDeaultIndex);
        add.setOnClickListener(ocl);
        defaultD[0].setOnClickListener(ocl);
        defaultD[1].setOnClickListener(ocl);
        defaultD[2].setOnClickListener(ocl);
        defaultD[3].setOnClickListener(ocl);
        defaultMap[0].setOnClickListener(ocl);
        defaultMap[1].setOnClickListener(ocl);
        defaultMap[2].setOnClickListener(ocl);
        defaultMap[3].setOnClickListener(ocl);
    }
	
	private void chageDefaultCheckBox(int index)
	{
		defautCb[0].setVisibility(View.GONE);
		defautCb[1].setVisibility(View.GONE);
		defautCb[2].setVisibility(View.GONE);
		defautCb[3].setVisibility(View.GONE);
		defautCb[index].setVisibility(View.VISIBLE);
	}
	private void showGameList(ListView lv)
	{
		if(JnsIMECoreService.aph== null)
			JnsIMECoreService.aph = new AppHelper(this);
		AppHelper aph = JnsIMECoreService.aph;
		Cursor cursor = aph.Qurey(null);
	    gameAdapter = new JnsIMEGameListAdapter(cursor, this);
		lv.setAdapter(gameAdapter);
	}
	
	OnClickListener ocl = new OnClickListener()
	{
		private void startKeyMapping(String name)
		{
			String lable = name;
			Intent mapin = new Intent();
			mapin.setClass(JnsIMEGameListActivity.this, JnsIMEKeyMappingActivity.class);
			mapin.putExtra("filename", name+".keymap");
			mapin.putExtra("lable", lable);
			JnsIMEGameListActivity.this.startActivity(mapin);
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId())
			{
			case R.id.add_confirm:
				AppHelper aph = JnsIMECoreService.aph;
				for(int i = 0; i < JnsIMEPopAddAdapter.isSelected.size(); i++)
				{
					if(JnsIMEPopAddAdapter.isSelected.get(i))
					{
						ResolveInfo rinfo = (ResolveInfo)((popAdapter.apps.get(i)).get("resolveInfo"));
						aph.Insert(rinfo.activityInfo.packageName, "true");
					}
				}
				Cursor cursor = aph.Qurey(null);
				gameAdapter.setCursor(cursor);
				gameAdapter.notifyDataSetChanged();
				adddialog.dismiss();
				break;
			case R.id.add_cancal:
				adddialog.dismiss();
				break;
				case R.id.default_keymapping1:
					startKeyMapping("default1");
					break;
				case R.id.default_keymapping2:
					startKeyMapping("default2");
					break;
				case R.id.default_keymapping3:
					startKeyMapping("default3");
					break;
				case R.id.default_keymapping4:
					startKeyMapping("default4");
					break;
				case R.id.dian1:
					 JnsIMECoreService.currentDeaultIndex = 0;
					 chageDefaultCheckBox(JnsIMECoreService.currentDeaultIndex);
					  break;
				case R.id.dian2:
					 JnsIMECoreService.currentDeaultIndex = 1;
					 chageDefaultCheckBox(JnsIMECoreService.currentDeaultIndex);
					  break;
				case R.id.dian3:
					 JnsIMECoreService.currentDeaultIndex = 2;
					 chageDefaultCheckBox(JnsIMECoreService.currentDeaultIndex);
					  break;
				case R.id.dian4:
					 JnsIMECoreService.currentDeaultIndex = 3;
					 chageDefaultCheckBox(JnsIMECoreService.currentDeaultIndex);
					  break;
				case R.id.add_game:
					View view = JnsIMEGameListActivity.this.getLayoutInflater().inflate(R.layout.add_game, null);
					ListView lv = (ListView) view.findViewById(R.id.lv);
					Button comfirm = (Button) view.findViewById(R.id.add_confirm);
					Button cancel = (Button) view.findViewById(R.id.add_cancal);
					comfirm.setOnClickListener(ocl);
					cancel.setOnClickListener(ocl);
					popAdapter = new JnsIMEPopAddAdapter(JnsIMEGameListActivity.this);
					lv.setAdapter(popAdapter);
					lv.setOnItemClickListener(new OnItemClickListener()
					{

						@Override
						public void onItemClick(AdapterView<?> arg0, View view,
								int position, long arg3) {
							// TODO Auto-generated method stub
							// ViewHolder vHollder = (ViewHolder) view.getTag();    
							//在每次获取点击的item时将对于的checkbox状态改变，同时修改map的值。    
							CheckBox cbox = (CheckBox) view.findViewById(R.id.cb); 
							Log.v("check box","select at" + position);
							cbox.toggle();    
							JnsIMEPopAddAdapter.isSelected.put(position, cbox.isChecked());
						}

					});
				//	adddialog = new AlertDialog.Builder(JnsIMEGameListActivity.this,R.style.mydialog).setView(view).create();
					adddialog = new Dialog(JnsIMEGameListActivity.this, R.style.mydialog);
					adddialog.setContentView(view);
					adddialog.setCancelable(true);
					adddialog.show();
					break;
			}
		}
		
	};
	@Override
	public void onResume()
	{
		super.onResume();
		showGameList(gameList);
	}
	
}
