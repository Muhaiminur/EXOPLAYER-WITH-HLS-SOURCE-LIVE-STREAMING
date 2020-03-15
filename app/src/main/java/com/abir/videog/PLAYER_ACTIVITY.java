package com.abir.videog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.abir.videog.CONFIG.VideoPlayerConfig;
import com.abir.videog.databinding.ActivityPLAYERACTIVITYBinding;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class PLAYER_ACTIVITY extends AppCompatActivity implements PlaybackPreparer, PlayerControlView.VisibilityListener, Player.EventListener, AdapterView.OnItemSelectedListener {

    ActivityPLAYERACTIVITYBinding playeractivityBinding;


    String videoUrl = VideoPlayerConfig.DEFAULT_VIDEO_URL;
    SimpleExoPlayer player;
    Handler mHandler;
    Runnable mRunnable;
    DefaultTrackSelector trackSelector;

    Context context;

    int p1080 = 2118150;
    int p720 = 1458150;
    int p480 = 963150;
    int p360 = 743150;
    int p240 = 578150;
    int p144 = 358150;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_p_l_a_y_e_r__a_c_t_i_v_i_t_y);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        getSupportActionBar().hide();
        playeractivityBinding = DataBindingUtil.setContentView(this, R.layout.activity_p_l_a_y_e_r__a_c_t_i_v_i_t_y);

        //spinner work
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.quality_array, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        playeractivityBinding.videoQuality.setAdapter(adapter);
        playeractivityBinding.videoQuality.setOnItemSelectedListener(this);
        //playeractivityBinding.videoQuality.setSelection(2);


        context = PLAYER_ACTIVITY.this;
        setUp();
        updateButtonVisibilities();
        Log.d("visiblity", "check = ");
        playeractivityBinding.videoFullScreenPlayer.setControllerVisibilityListener(this);
        playeractivityBinding.videoFullScreenPlayer.requestFocus();
        playeractivityBinding.videoFullScreenPlayer.setShutterBackgroundColor(Color.TRANSPARENT);
    }

    private void setUp() {
        initializePlayer();
        if (videoUrl == null) {
            return;
        }
        buildMediaSource(Uri.parse(videoUrl));
    }

    private void initializePlayer() {
        if (player == null) {
            LoadControl loadControl = new DefaultLoadControl.Builder().setAllocator(new DefaultAllocator(true, 16)).setBufferDurationsMs(2 * VideoPlayerConfig.MIN_BUFFER_DURATION,
                    2 * VideoPlayerConfig.MAX_BUFFER_DURATION,
                    VideoPlayerConfig.MIN_PLAYBACK_START_BUFFER,
                    VideoPlayerConfig.MIN_PLAYBACK_RESUME_BUFFER)/*.setBackBuffer(-1, true)*/.createDefaultLoadControl();
            /*TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(bandwidthMeter);
            trackSelector =
                    new DefaultTrackSelector(videoTrackSelectionFactory);*/
            trackSelector = new DefaultTrackSelector(context);
            DefaultTrackSelector.Parameters defaultTrackParam = trackSelector.buildUponParameters().build();
            trackSelector.setParameters(defaultTrackParam);
            player = new SimpleExoPlayer.Builder(context).setTrackSelector(trackSelector).setLoadControl(loadControl).build();
            playeractivityBinding.videoFullScreenPlayer.setPlayer(player);
        }
    }

    private void buildMediaSource(Uri mUri) {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(context).build();
        // Create a data source factory.
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(PLAYER_ACTIVITY.this, "app-name"), bandwidthMeter);
        // Create a HLS media source pointing to a playlist uri.
        HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mUri);
        // Create a player instance.
        //SimpleExoPlayer player = new SimpleExoPlayer.Builder(PLAYER_ACTIVITY.this).build();
        // Prepare the player with the media source.
        player.prepare(hlsMediaSource);
        player.setPlayWhenReady(true);
        player.addListener(this);
    }


    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void pausePlayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    private void resumePlayer() {
        if (player != null) {
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlayer();
        if (mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        resumePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    private void updateButtonVisibilities() {
        if (player == null) {
            Log.d("OK", "ONE");
            return;
        }

        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo == null) {
            Log.d("OK", "TWO");
            return;
        }

        for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
            if (trackGroups.length != 0) {
                int label;
                switch (player.getRendererType(i)) {
                    case C.TRACK_TYPE_AUDIO:
                        label = R.string.exo_track_selection_title_audio;
                        Log.d("OK", label + "audio");
                        break;
                    case C.TRACK_TYPE_VIDEO:
                        label = R.string.exo_track_selection_title_video;
                        Log.d("OK", label + "video");
                        break;
                    case C.TRACK_TYPE_TEXT:
                        label = R.string.exo_track_selection_title_text;
                        Log.d("OK", label + "text");
                        break;
                    default:
                        continue;
                }
                Log.d("OK", label + "ONE");
                //playeractivityBinding.videoQuality.setText(label);
                //playeractivityBinding.videoQuality.setTag(i);
            }
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {

            case Player.STATE_BUFFERING:
                playeractivityBinding.playerSpinner.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_ENDED:
                // Activate the force enable
                break;
            case Player.STATE_IDLE:

                break;
            case Player.STATE_READY:
                playeractivityBinding.playerSpinner.setVisibility(View.GONE);

                break;
            default:
                // status = PlaybackStatus.IDLE;
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    //player activity
    @Override
    public void preparePlayback() {
        initializePlayer();
    }

    @Override
    public void onVisibilityChange(int visibility) {
        Log.d("visiblity", "check = " + visibility);
        playeractivityBinding.videoQuality.setVisibility(visibility);
    }


    //spinner work
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Log.d("Video Quality", pos + "");
        long time = player.getCurrentPosition();
        switch (pos) {
            case 0:
                Log.d("Video Quality", pos + "144");
                DefaultTrackSelector.Parameters parameters0 = trackSelector.buildUponParameters()
                        .setMaxVideoBitrate(p144)
                        .setForceHighestSupportedBitrate(true)
                        .build();
                trackSelector.setParameters(parameters0);
                break;
            case 1:
                Log.d("Video Quality", pos + "240");
                DefaultTrackSelector.Parameters parameters1 = trackSelector.buildUponParameters()
                        .setMaxVideoBitrate(p240)
                        .setForceHighestSupportedBitrate(true)
                        .build();
                trackSelector.setParameters(parameters1);
                break;
            case 2:
                Log.d("Video Quality", pos + "360");
                DefaultTrackSelector.Parameters parameters2 = trackSelector.buildUponParameters()
                        .setMaxVideoBitrate(p360)
                        .setForceHighestSupportedBitrate(true)
                        .build();
                trackSelector.setParameters(parameters2);
                break;
            case 3:
                /*buildMediaSource(Uri.parse(quality.getThree()));
                player.seekTo(0, time);*/
                Log.d("Video Quality", pos + "480");
                DefaultTrackSelector.Parameters parameters3 = trackSelector.buildUponParameters()
                        .setMaxVideoBitrate(p480)
                        .setForceHighestSupportedBitrate(true)
                        .build();
                trackSelector.setParameters(parameters3);
                break;
            case 4:
                /*buildMediaSource(Uri.parse(quality.getFour()));
                player.seekTo(0, time);*/
                Log.d("Video Quality", pos + "720");
                DefaultTrackSelector.Parameters parameters4 = trackSelector.buildUponParameters()
                        .setMaxVideoBitrate(p720)
                        .setForceHighestSupportedBitrate(true)
                        .build();
                trackSelector.setParameters(parameters4);
                break;
            case 5:
                /*buildMediaSource(Uri.parse(quality.getFive()));
                player.seekTo(0, time);*/
                Log.d("Video Quality", pos + "1080");
                DefaultTrackSelector.Parameters parameters5 = trackSelector.buildUponParameters()
                        .setMaxVideoBitrate(p1080)
                        .setForceHighestSupportedBitrate(true)
                        .build();
                trackSelector.setParameters(parameters5);
                break;
            default:
                System.out.println("Invalid grade");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
