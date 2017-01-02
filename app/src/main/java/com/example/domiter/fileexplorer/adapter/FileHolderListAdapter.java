package com.example.domiter.fileexplorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.domiter.fileexplorer.R;
import com.example.domiter.fileexplorer.misc.FileHolder;
import com.example.domiter.fileexplorer.misc.ThumbnailHelper;
import com.example.domiter.fileexplorer.view.ViewHolder;

import java.util.List;

import static com.nostra13.universalimageloader.core.ImageLoader.getInstance;

public class FileHolderListAdapter extends BaseAdapter {
    private List<FileHolder> mItems;
	private int mItemLayoutId = R.layout.item_filelist;

	// Thumbnail specific
    private boolean scrolling = false;
    private OnItemToggleListener mOnItemToggleListener;

    public FileHolderListAdapter(List<FileHolder> files){
		mItems = files;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Set the layout to be used for item drawing.
	 * @param resId The item layout id. 0 to reset.
	 */
	public void setItemLayout(int resId){
		if(resId > 0)
			mItemLayoutId = resId;
		else
			mItemLayoutId = R.layout.item_filelist;
	}

	/**
	 * Creates a new list item view, along with it's ViewHolder set as a tag.
	 * @return The new view.
	 */
    View newView(Context context){
		View view = LayoutInflater.from(context).inflate(mItemLayoutId, null);

		ViewHolder holder = new ViewHolder();
		holder.icon = (ImageView) view.findViewById(R.id.icon);
		holder.primaryInfo = (TextView) view.findViewById(R.id.primary_info);
		holder.secondaryInfo = (TextView) view.findViewById(R.id.secondary_info);
		holder.tertiaryInfo = (TextView) view.findViewById(R.id.tertiary_info);

		view.setTag(holder);
		return view;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FileHolder item = mItems.get(position);
		if(convertView == null)
			convertView = newView(parent.getContext());
		ViewHolder holder = (ViewHolder) convertView.getTag();

        getInstance().cancelDisplayTask(holder.icon);
		holder.icon.setImageDrawable(item.getBestIcon());
		holder.primaryInfo.setText(item.getName());
		holder.secondaryInfo.setText(item.getFormattedModificationDate(convertView.getContext()));
		// Hide directories' size as it's irrelevant if we can't recursively find it.
		holder.tertiaryInfo.setText(item.getFile().isDirectory()? "" : item.getFormattedSize(
                convertView.getContext(), false));

        ThumbnailHelper.requestIcon(item, holder.icon);

		return convertView;
	}

    public OnItemToggleListener getOnItemToggleListener() {
        return mOnItemToggleListener;
    }

    public void setOnItemToggleListener(OnItemToggleListener mOnItemToggleListener) {
        this.mOnItemToggleListener = mOnItemToggleListener;
    }

    private boolean shouldLoadIcon(FileHolder item){
		return !scrolling && item.getFile().isFile() && !item.getMimeType().equals("video/mpeg");
	}

    public interface OnItemToggleListener {
        public void onItemToggle(int position);
    }
}