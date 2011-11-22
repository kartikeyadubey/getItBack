package ssui.project;

import java.util.ArrayList;

import session.SessionStore;

import com.facebook.android.Facebook;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BillListAdapter extends ArrayAdapter<IdDescription> {
	private ArrayList<IdDescription> idDescriptions;
	private Activity activity;
	public ImageManager imageManager;
	private Facebook mFacebook;
	private String APP_ID = "161076967321553";

	public BillListAdapter(Activity a, int textViewResourceId, ArrayList<IdDescription> objects) {
		super(a, textViewResourceId, objects);
		this.idDescriptions = objects;
		activity = a;
		mFacebook = new Facebook(APP_ID );
		SessionStore.restore(mFacebook, getContext());
		imageManager = 
			new ImageManager(activity.getApplicationContext());
	}

	public static class ViewHolder{
		public TextView description;
		public ImageView image;
		public ProgressBar progress; //ADDED
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder holder;
		if (v == null) {		
			LayoutInflater vi = 
				(LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row_layout, null);
			holder = new ViewHolder();
			holder.description = (TextView) v.findViewById(R.id.billName);
			holder.image = (ImageView) v.findViewById(R.id.billIcon);
			holder.progress = (ProgressBar) v.findViewById(R.id.progress_bar); //ADDED
			v.setTag(holder);
		}
		else
			holder=(ViewHolder)v.getTag();

		final IdDescription idDescription = idDescriptions.get(position);
		if (idDescription != null) {
			holder.description.setText(idDescription.description);
			String url = "https://graph.facebook.com/"+idDescriptions.get(position).userId+
					"/picture&access_token="+mFacebook.getAccessToken();
			holder.image.setTag(url);
			imageManager.displayImage(url, activity, holder.image, holder.progress); //CHANGED
		}
		return v;
	}
}
