package ardel.musonloader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

public class TrackAdapter extends BaseAdapter implements Constants {

    private final ArrayList<ListItem> data;
    Context context;
    private onAdapterListener listener;
    private RadioButton radioPlay;
    private ImageButton buttonLoad;

    private void log(String s) {
        Logger.getInstance().log("TrackAdapter", s);
    }

    public TrackAdapter(Context context, ArrayList<ListItem> data) {
        this.data = data;
        this.context = context;
    }

    public interface onAdapterListener {
        void adapterEvent(int event, int id);
    }

    public void setOnAdapterListener(onAdapterListener l) {
        this.listener = l;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (v == null) v = inflater.inflate(R.layout.list_item, parent, false);
        TextView artistInfo = (TextView) v.findViewById(R.id.artist);
        TextView trackInfo = (TextView) v.findViewById(R.id.track);
        TextView songDuration = (TextView) v.findViewById(R.id.duration);
        artistInfo.setText(data.get(position).getArtist());
        trackInfo.setText(data.get(position).getTrack());
        songDuration.setText(data.get(position).getDuration());
        radioPlay = (RadioButton) v.findViewById(R.id.radioPlay);
        radioPlay.setChecked(data.get(position).getChecked());
        buttonLoad = (ImageButton) v.findViewById(R.id.imageButtonLoad);

        radioPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.adapterEvent(PLAY, position);
            }
        });

        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.adapterEvent(LOAD, position);
            }
        });
        return v;
    }
}
