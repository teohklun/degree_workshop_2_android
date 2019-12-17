package com.example.myfirstapplicaion;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SoundVisual extends AppCompatActivity {
    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;
    private Equalizer mEqualizer;

    private LinearLayout mLinearLayout;
    private SoundVisual.PlayerVisualizerView mVisualizerView;
    private TextView mStatusTextView;
    private TextView mInfoView;
    private static final float VISUALIZER_HEIGHT_DIP = 160f;
    //    private static final float VISUALIZER_HEIGHT_DIP = 500f;
    private int soundID;
    private String nextAction;
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mStatusTextView = new TextView(this);
//        mStatusTextView.setGravity();
        mStatusTextView.setGravity(Gravity.CENTER);
        mStatusTextView.setTextSize(20);
        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
//        mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//        layoutParams.setMargins(0, 200, 0, 0);
        layoutParams.setMargins(0, 450, 0, 0);
//        layoutParams.gravity = Gravity.CENTER;
        mLinearLayout.addView(mStatusTextView);
        Intent intent = getIntent();
        soundID = intent.getIntExtra("soundID", -1);
        if (intent.hasExtra("nextAction")) {
            nextAction = intent.getStringExtra("nextAction");
        }

        setContentView(mLinearLayout, layoutParams);
        mMediaPlayer = MediaPlayer.create(this, soundID);

        setupVisualizerFxAndUI();
//        setupEqualizerFxAndUI();

        mVisualizer.setEnabled(true);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            public void onCompletion(MediaPlayer mediaPlayer)
            {
                mVisualizer.setEnabled(false);
                //                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                //                setVolumeControlStream(AudioManager.STREAM_SYSTEM);
                mStatusTextView.setText("Music End");
                Intent resultIntent = new Intent();
                resultIntent.putExtra("Test", "newValue");
                if(!nextAction.equals("")){
                    resultIntent.putExtra("nextAction", nextAction);
                    Log.e("success", "got nextAction ");
                } else {
                    Log.e("error", "now nextAction fail");
                }

                setResult(99, resultIntent);
                finish();
//                startActivity(new Intent(SoundVisual.this,SurfaceCamera.class));
            }
        });

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mMediaPlayer.start();
        mStatusTextView.setText("Playing Music . . .");
    }

    private void setupVisualizerFxAndUI()
    {
        mVisualizerView = new SoundVisual.PlayerVisualizerView(this);
        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                (int) (VISUALIZER_HEIGHT_DIP * getResources()
                        .getDisplayMetrics().density)));
        mLinearLayout.addView(mVisualizerView);

        mInfoView = new TextView(this);
        String infoStr = "";

        int[] csr = Visualizer.getCaptureSizeRange();
        if(csr != null)
        {
            String csrStr = "CaptureSizeRange: ";
            for(int i = 0; i < csr.length; i ++)
            {
                csrStr += csr[i];
                csrStr +=" ";
            }
            infoStr += csrStr;
        }

        final int maxCR = Visualizer.getMaxCaptureRate();

        infoStr = infoStr + "\nMaxCaptureRate: " + maxCR;

        mInfoView.setText(infoStr);
//        mLinearLayout.addView(mInfoView);

//        mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
        mVisualizer = new Visualizer(0);
//        mVisualizer.setDataCaptureListener(this, Visualizer.getMaxCaptureRate(), true, false);
        mVisualizer.setCaptureSize(256);
        mVisualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener()
                {
                    public void onWaveFormDataCapture(Visualizer visualizer,
                                                      byte[] bytes, int samplingRate)
                    {
                        mVisualizerView.updateVisualizer(bytes);
                    }

                    public void onFftDataCapture(Visualizer visualizer,
                                                 byte[] fft, int samplingRate)
                    {
                        mVisualizerView.updateVisualizer(fft);
                    }
//                }, maxCR / 2, false, true);
                }, maxCR / 2, true, false);
    }
    public class PlayerVisualizerView extends View {

        /**
         * constant value for Height of the bar
         */
        public static final int VISUALIZER_HEIGHT = 28;

        /**
         * bytes array converted from file.
         */
        private byte[] bytes;

        /**
         * Percentage of audio sample scale
         * Should updated dynamically while audioPlayer is played
         */
        private float denseness;

        /**
         * Canvas painting for sample scale, filling played part of audio sample
         */
        private Paint playedStatePainting = new Paint();
        /**
         * Canvas painting for sample scale, filling not played part of audio sample
         */
        private Paint notPlayedStatePainting = new Paint();

        private int width;
        private int height;

        public PlayerVisualizerView(Context context) {
            super(context);
            init();
        }

        public PlayerVisualizerView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            bytes = null;

            playedStatePainting.setStrokeWidth(1f);
            playedStatePainting.setAntiAlias(true);
            playedStatePainting.setColor(Color.rgb(0, 128, 255));

//            playedStatePainting.setColor(ContextCompat.getColor(getContext(), R.color.gray));
//            playedStatePainting.setColor(255);
            notPlayedStatePainting.setStrokeWidth(1f);
            notPlayedStatePainting.setAntiAlias(true);
            playedStatePainting.setColor(Color.rgb(128, 128, 255));

//            notPlayedStatePainting.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        }

        /**
         * update and redraw Visualizer view
         */
        public void updateVisualizer(byte[] bytes) {
            this.bytes = bytes;
            invalidate();
        }

        /**
         * Update player percent. 0 - file not played, 1 - full played
         *
         * @param percent
         */
        public void updatePlayerPercent(float percent) {
            denseness = (int) Math.ceil(width * percent);
            if (denseness < 0) {
                denseness = 0;
            } else if (denseness > width) {
                denseness = width;
            }
            invalidate();
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            width = getMeasuredWidth();
            height = getMeasuredHeight();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (bytes == null || width == 0) {
                return;
            }
            float totalBarsCount = width / dp(3);
            if (totalBarsCount <= 0.1f) {
                return;
            }
            byte value;
            int samplesCount = (bytes.length * 8 / 5);
            float samplesPerBar = samplesCount / totalBarsCount;
            float barCounter = 0;
            int nextBarNum = 0;

            int y = (height - dp(VISUALIZER_HEIGHT)) / 2;
            int barNum = 0;
            int lastBarNum;
            int drawBarCount;

            for (int a = 0; a < samplesCount; a++) {
                if (a != nextBarNum) {
                    continue;
                }
                drawBarCount = 0;
                lastBarNum = nextBarNum;
                while (lastBarNum == nextBarNum) {
                    barCounter += samplesPerBar;
                    nextBarNum = (int) barCounter;
                    drawBarCount++;
                }

                int bitPointer = a * 5;
                int byteNum = bitPointer / Byte.SIZE;
                int byteBitOffset = bitPointer - byteNum * Byte.SIZE;
                int currentByteCount = Byte.SIZE - byteBitOffset;
                int nextByteRest = 5 - currentByteCount;
                value = (byte) ((bytes[byteNum] >> byteBitOffset) & ((2 << (Math.min(5, currentByteCount) - 1)) - 1));
                if (nextByteRest > 0) {
                    value <<= nextByteRest;
                    value |= bytes[byteNum + 1] & ((2 << (nextByteRest - 1)) - 1);
                }

                for (int b = 0; b < drawBarCount; b++) {
                    int x = barNum * dp(3);
                    float left = x;
                    float top = y + dp(VISUALIZER_HEIGHT - Math.max(1, VISUALIZER_HEIGHT * value / 31.0f));
                    float right = x + dp(2);
                    float bottom = y + dp(VISUALIZER_HEIGHT);
                    if (x < denseness && x + dp(2) < denseness) {
                        canvas.drawRect(left, top, right, bottom, notPlayedStatePainting);
                    } else {
                        canvas.drawRect(left, top, right, bottom, playedStatePainting);
                        if (x < denseness) {
                            canvas.drawRect(left, top, right, bottom, notPlayedStatePainting);
                        }
                    }
                    barNum++;
                }
            }
        }

        public int dp(float value) {
            if (value == 0) {
                return 0;
            }
            return (int) Math.ceil(getContext().getResources().getDisplayMetrics().density * value);
        }
    }
}
