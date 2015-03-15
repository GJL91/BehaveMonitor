package com.example.BehaveMonitor.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.BehaveMonitor.FileHandler;
import com.example.BehaveMonitor.R;

import java.io.File;
import java.util.List;

public class SessionHistoryListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<File> sessions;

    public SessionHistoryListAdapter(Context context, List<File> sessions) {
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.sessions = sessions;
    }

    private class ViewHolder {
        TextView sessionName;
        ImageButton deleteButton;
        Button continueButton;
    }

    @Override
    public int getCount() {
        return sessions.size();
    }

    @Override
    public Object getItem(int position) {
        return sessions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();

            convertView = inflater.inflate(R.layout.session_history_list_item, parent, false);

            viewHolder.sessionName = (TextView) convertView.findViewById(R.id.list_session_name);
            viewHolder.continueButton = (Button) convertView.findViewById(R.id.list_sessions_continue_btn);
            viewHolder.deleteButton = (ImageButton) convertView.findViewById(R.id.list_sessions_delete_btn);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        File session = sessions.get(position);
        viewHolder.sessionName.setText(getName(session));
        viewHolder.continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continueSession(position);
            }
        });

        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSession(position);
            }
        });

        return convertView;
    }

    private String getName(File file) {
        return file.getName().split("\\.(?=[^\\.]+$)")[0];
    }

    private void deleteSession(final int position) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        final File session = sessions.get(position);
        alert.setTitle("Delete " + getName(session) + "?");
        alert.setMessage("Are you sure you want to delete this session?");

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                FileHandler.deleteSession(session);
                sessions.remove(position);

                notifyDataSetChanged();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    private void continueSession(int position) {
        Log.e("Behave", "Continuing session");
    }
}