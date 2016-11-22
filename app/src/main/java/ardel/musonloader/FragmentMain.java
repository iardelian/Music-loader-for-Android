package ardel.musonloader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class FragmentMain extends Fragment implements Constants, MainHandler.OnHandleListener, View.OnClickListener, TrackAdapter.onAdapterListener {

    ArrayList<ListItem> data = new ArrayList<>();
    private static MainHandler h;
    private EditText edit;
    private TrackAdapter trackAdapter;
    AsyncTask at = null;
    MediaPlayer mediaPlayer;
    private int playingSong = -1;
    ProgressBar progressBar;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        Context context = getActivity().getApplicationContext();

        h = new MainHandler();
        h.setOnHandleListener(this);
        trackAdapter = new TrackAdapter(context, data);
        ListView list = (ListView) v.findViewById(R.id.listView);
        list.setAdapter(trackAdapter);
        ImageButton btnSearch = (ImageButton) v.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(this);
        edit = (EditText) v.findViewById(R.id.search);
        trackAdapter.setOnAdapterListener(this);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);

        return v;
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }



    private void getFileList(String request) {


            if (at != null) {
                at.cancel(false);
            }

        if(isNetworkAvailable(getContext())){

            at = new AsyncTask<String, Integer, Void>() {

                @Override
                protected void onPreExecute() {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                }

                @Override
                protected Void doInBackground(String... params) {

                    try {
                        Elements nextPage;
                        Elements elements;
                        int count = 0;
                        String url = String.format(URL_STRING, URLEncoder.encode(params[0], "UTF-8"));

                        do {
                            Document doc = Jsoup.connect(url).userAgent(USERAGENT).timeout(25000).get();
                            elements = doc.select(".responses__wrapper");
                            nextPage = elements.select(".page-navig a:contains(Вперед)");

                            for (Element element : elements.select(".zaycev__block")) {
                                String[] name = element.select(".result__title").text().split(" - ");
                                String artName = name[0];
                                String trackName = name[1];
                                Elements resultSnp = element.select(".result__snp");
                                String trackDur = resultSnp.text().split(" ")[1];
                                final String trackLink = Jsoup.connect(resultSnp.select("p a")
                                        .attr("abs:href"))
                                        .userAgent(USERAGENT)
                                        .timeout(25000)
                                        .get()
                                        .select("#audiotrack-download-link")
                                        .attr("abs:href");

                                final String title = artName + " - " + trackName + " : " + trackDur;

                                if (isCancelled()) {
                                    return null;
                                }

                                Message msg = new Message();
                                Bundle bundle = new Bundle();
                                bundle.putInt(EXTRA_ID, DATA);
                                bundle.putString(EXTRA_TITLE, title);
                                bundle.putString(EXTRA_LINK, trackLink);
                                msg.setData(bundle);
                                h.sendMessage(msg);
                                count++;

                            }
                            url = nextPage.attr("abs:href");

                        } while (nextPage.hasAttr("href") && count < 100);
                    } catch (Exception e) {
                        log(" " + e);

                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void param) {
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                }
            }.execute(request);

        }else{
            Toast toast = Toast.makeText(getContext(),
                    "Internet connection error",
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }


    }



    @Override
    public void onHandleString(String title, String link) {
        data.add(new ListItem(title, link));
        trackAdapter.notifyDataSetChanged();
    }

    private void log(String s) {
        Logger.getInstance().log("FragmentMain", s);
    }

    @Override
    public void onClick(View view) {
        data.clear();
        getFileList(edit.getEditableText().toString());
    }

    @Override
    public void adapterEvent(int event, int id) {
        final String link = data.get(id).getTrackLink();
        final String name = data.get(id).getArtist() + " - " + data.get(id).getTrack();
        switch (event) {
            case PLAY:
                boolean checked = data.get(id).getChecked();
                if (checked) {
                    playingSong = id;
                    data.get(id).setChecked(false);
                } else {
                    for (int i = 0; i < data.size(); i++) {
                        if (i != id) data.get(i).setChecked(false);
                        else data.get(i).setChecked(true);
                    }
                }
                trackAdapter.notifyDataSetChanged();
                play(id, checked);
                break;

            case LOAD:

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setTitle("Downloading...");
                alertDialog.setMessage("Download " + name + " ?");
                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                    }
                });
                alertDialog.show();
                break;
        }

    }

    private void play(int id, boolean checked) { // checked = false if track is playing, and true if not

        final String link = data.get(id).getTrackLink();

        if (playingSong == id) {

            if (checked && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                return;

            } else {
                mediaPlayer.start();
                return;
            }

        } else {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }

        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(link);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
            }
        });
        mediaPlayer.prepareAsync();
    }


}
