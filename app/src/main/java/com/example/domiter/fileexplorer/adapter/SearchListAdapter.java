package com.example.domiter.fileexplorer.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.example.domiter.fileexplorer.misc.FileHolder;
import com.example.domiter.fileexplorer.view.ViewHolder;

import java.util.List;

public class SearchListAdapter extends FileHolderListAdapter {

    public SearchListAdapter(List<FileHolder> files) {
        super(files);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        ((ViewHolder) view.getTag()).secondaryInfo.setText(
                ((FileHolder) getItem(position)).getFile().getAbsolutePath());

        return view;
    }
}