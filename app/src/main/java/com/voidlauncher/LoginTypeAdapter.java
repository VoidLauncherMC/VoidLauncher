package com.voidlauncher;

import android.content.Context;
import android.view.*;
import android.widget.*;
import java.util.List;

public class LoginTypeAdapter extends BaseAdapter {

    private Context context;
    private List<LoginType> items;

    public LoginTypeAdapter(Context context, List<LoginType> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_login_type, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.icon);
        TextView text = convertView.findViewById(R.id.text);

        LoginType item = items.get(position);
        icon.setImageResource(item.iconRes);
        text.setText(item.name);

        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent);
    }
}
