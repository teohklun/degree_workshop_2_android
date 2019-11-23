package com.example.myfirstapplicaion;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity implements SurfaceHolder.Callback {

    public MainActivity(){

    }
    //    private static final String urlServer = "http://192.168.43.115:5000/"; //lun cellular data
    private static final String urlServer = "http://192.168.43.172:5000/"; //lun cellular data
//    private static final String urlServer = "http://192.168.0.128:5000/"; //ICA2 ipv4
//    private static final String urlServer = "http://10.200.51.29:5000/"; //kasturi ipv4


    private static final String downloadPath = Environment.getExternalStorageDirectory() + "/download";
    private static final String soundPath = downloadPath + "/sound"; // please add this two path
    private static final String videoPath = downloadPath + "/video";
    private static final String recordVideoFileName = "recordVideoFile.mp4";
    private static final String recordAudioFileName = "recordAudioFile.mp4";
    private static final String recordAudioPath = soundPath + '/' + recordAudioFileName;
    private static final String recordVideoPath = videoPath+ '/' + recordVideoFileName;

    private final HashMap happySoundMap = getHappyHashMap();
    private final HashMap unHappySoundMap = getUnhappyHashMap();
    private final HashMap actionHahsMap = getActionsHashMap();

    private static int VIDEO_REQUEST = 101;
    private Uri videoUri = null;
    //    @Override
//    public void onClick(View view) {
//    }
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private View recordButton;
    private TextView infoText;
    private boolean mInitSuccesful;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // we shall take the video in landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        initPermisison();
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        recordButton = findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new OnClickListener() {
            @Override
            // toggle video recording
            public void onClick(View v) {
//                if (recordButton.isChecked()) {
                recordButton.setVisibility(View.GONE);
//                try {
//                    Thread.sleep(1 * 5); // This will recode for 10 seconds, if you don't want then just remove it.
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                mMediaRecorder.start();
            }
        });
    }

    /* Init the MediaRecorder, the order the methods are called is vital to
     * its correct functioning */
    private void initRecorder(Surface surface) throws IOException {
        // It is very important to unlock the camera before doing setCamera
        // or it will results in a black preview
        if(mCamera == null) {
            boolean found = false;
            int i;
            for (i=0; i< Camera.getNumberOfCameras(); i++) {
                Camera.CameraInfo newInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(i, newInfo);
                if (newInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    found = true;
                    break;
                }
            }
            mCamera = Camera.open(i);
            mCamera.unlock();
        }

        if(mMediaRecorder == null)  mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setPreviewDisplay(surface);
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        //       mMediaRecorder.setOutputFormat(8);
        mMediaRecorder.setMaxDuration(5000);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(640, 480);
        mMediaRecorder.setOutputFile(recordVideoPath);
        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    mr.stop();
//                    Log.e("Maximumd", "MaximsetDataSourceum Duration Reached");
                    mr.release();
                    setContentView(R.layout.main);
                    infoText = findViewById(R.id.textView);
                    infoText.setText("Sending video file to server ");
                    new sendFilesToServerAsync("face",
                            recordVideoPath).execute(urlServer + "api_emo_face");
//                    finish();
                }
            }
        });
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            // This is thrown if the previous calls are not called with the
            // proper order
            e.printStackTrace();
        }

        mInitSuccesful = true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if(!mInitSuccesful)
                initRecorder(mHolder.getSurface());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        shutdown();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    private void shutdown() {
        // Release MediaRecorder and especially the Camera as it's a shared
        // object that can be used by other applications
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mCamera.release();

        // once the objects have been released they can't be reused
        mMediaRecorder = null;
        mCamera = null;
    }

    public void captureVideo(View view) {
        File file = new File(recordVideoPath);
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
        if(videoIntent.resolveActivity(getPackageManager())!=null);
        {
            startActivityForResult(videoIntent, VIDEO_REQUEST);
        }
    }

    public void playVideo(View view) {
        Intent playIntent = new Intent(this, VideoPlayActivity.class);
        playIntent.putExtra("VideoUri", videoUri.toString());
        startActivity(playIntent);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_REQUEST && resultCode == RESULT_OK) {
//            videoUri = data.getData();
            Log.w("finish", "here");
            String url = urlServer + "api_speech";
//            String path = recordAudioPath;
            String path = recordVideoPath;
//            new sendFilesToServerAsync("happy", path).execute(url);
            new sendFilesToServerAsync("face",
                    recordVideoPath).execute(urlServer + "api_emo_face");
        }
    }

    enum Emotion {
        happy,
        unhappy,
        more,
        repeat,
        yesMore
    }

    public HttpURLConnection initConnection(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // Allow Inputs
        conn.setDoInput(true);
        // Allow Outputs
        conn.setDoOutput(true);
        // Don't use a cached copy.
        conn.setUseCaches(false);
        // Use a post method.
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "audio/wav");
        return conn;
    }

    public StringBuilder readServerResponse(HttpURLConnection conn) throws IOException {
        DataInputStream dis = new DataInputStream(conn.getInputStream());
        StringBuilder response = new StringBuilder();
        String line = "";
        while ((line = dis.readLine()) != null) {
            response.append(line);
//                response.append('\r');
        }
        dis.close();
        return response;
    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void writeFilesParamToDataOutputStream(HttpURLConnection conn, File file, String action) throws IOException {
        String lineEnd = "\r\n", twoHyphens = "--",
                boundary = "AaB03x87yxdkjnxvi7";
        //to be use for separate multiple files

        byte[] buffer;
        int maxBufferSize = 50000 * 1024;
        FileInputStream fileInputStream = new FileInputStream(file);
        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

        String urlParameters  = "action=" + action;
        String delimiter = "--aaWEdFXvDF--" + "\r\n";
        dos.write(urlParameters.getBytes());
        dos.write(delimiter.getBytes());

//                dos.writeBytes(twoHyphens + boundary + lineEnd);
//                dos.writeBytes("Content-Disposition: form-data; filename=\"" + "test" + "\"" + lineEnd); // after form-data; --> name="" + fileParameterName + "";
//                dos.writeBytes("Content-Type: audio/wav" + lineEnd);
//                dos.writeBytes(lineEnd);
//                Log.d("filetostring", file.toString());

        // create a buffer of maximum size
        buffer = new byte[Math.min((int) file.length(), maxBufferSize)];
        buffer = new byte[1024 * 1024];

        int length = 0;
////         read file and write it into form...
//        while ((length = fileInputStream.read(buffer)) != -1) {
        while ( ( length = fileInputStream.read( buffer ) ) > 0 ) {
            dos.write(buffer, 0, length);
        }


        //for (String name : parameters.keySet()) {
        //    dos.writeBytes(lineEnd);
        //    dos.writeBytes(twoHyphens + boundary + lineEnd);
        //    dos.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + lineEnd);
        //    dos.writeBytes(lineEnd);
        //    dos.writeBytes(parameters.get(name));
        //}

        // send multipart form data necessary after file data...
//                dos.writeBytes(lineEnd);
//                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
        dos.flush();
        fileInputStream.close();
        dos.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void sendFilesToServer(String action, String filePath, String... strings) {
        URL url = null;
        StringBuilder response = null;
        try {
            url = new URL(strings[0]);
            File file = new File(filePath);
            HttpURLConnection conn = initConnection(url);

            writeFilesParamToDataOutputStream(conn, file, action);
            response = readServerResponse(conn);
//            buttonChoose = findViewById(R.id.button);
//            buttonChoose.setOnClickListener(this);

            try {
                JSONObject responseJson = new JSONObject(response.toString());
//                String responseString = (String) responseJson.get("result").toString();

                Log.d("response", (String) responseJson.get("result").toString() + "HERE!!!");
                processResponse(responseJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
//            buttonChoose.setText("Fail message response!!!");
            Log.d("response fail", "Fail message response!!!");
        }

    }

    public void processResponse(JSONObject responseJson) throws JSONException {
        Log.d("response place", "response method");
        String action = responseJson.get("processed").toString();
        String result = responseJson.get("result").toString();

        if(action.equals("api_emo_face")) {
            infoText.setText("Recognized as : " + result + " face");
            if(result.equals(Emotion.happy.toString())) {
                partHappy();
            } else if(result.equals("sad")) {
                partUnhappy();
            } else {
                Log.d("api_emo_face", "undefined response to emo face");
            }
        } else if(action.equals("pick")) {
            String actionType = responseJson.get("actionType").toString();
            infoText.setText("Picking for " + actionType + "item");
            Log.e("action_pick", responseJson.toString());
            if(actionType.equals(Emotion.happy.toString())){
                MediaPlayer mpMore = getMediaPlayerWantMore(Emotion.happy.toString());
                mpMore.start();
            }else{ //unhappy
                recordAudio(Emotion.happy.toString());
            }
        } else if(action.equals("api_speech")){
            String actionType = responseJson.get("actionType").toString();

            if(result.equals("1")) {
                infoText.setText("Detected speech as YES");
                //yes
                processActionWantMore(actionType);

            }else if(result.equals("2")){
                infoText.setText("Detected speech as NO");

                processActionSufficient(actionType);
                //no
                // logic next step or end
            }
            else {
                infoText.setText("Could not recognize speech as YES or NO");
                //could not detect properly
                processActionRepeatVoice(actionType);
            }
        } else {
            Log.e("process_response", "unhandled result");
        }

    }

    public void processActionWantMore(final String action) {

        MediaPlayer mpWanMore = createMediaPlayer(Emotion.yesMore.toString());
        mpWanMore.start();

        mpWanMore.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //write the retake chocolate or flower command
                if(action.equals(Emotion.unhappy.toString())) {
                    //function to move robot arm etc
                    new asyncRequestPick(action).execute(); // should be do in function to move robot arm

                }else if (action.equals(Emotion.happy.toString())) {
                    //function to move robot arm etc

                    //do the retrigger the mediarecorder
                    new asyncRequestPick(action).execute(); // should be do in function to move robot arm

                } else {
                    Log.e("processtMoreFail", "1234");
                }
            }
        });


    }

    public void processActionSufficient(String action){

    }

    public void processActionRepeatVoice(final String action){
        MediaPlayer mpRepeat = createMediaPlayer(Emotion.repeat.toString());
        mpRepeat.start();
        mpRepeat.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                recordAudio(action);
            }
        });

        //MediaPlayer mpMore = createMediaPlayer(Emotion.more.toString());
    }

    private class sendFilesToServerAsync extends AsyncTask<String, Void, Void> {

        String action;
        String filePath;

        private sendFilesToServerAsync(String action, String filePath) {
            this.action  = action;
            this.filePath = filePath;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected Void doInBackground(String... strings) {
            sendFilesToServer(this.action, filePath, strings);
            return null;
        }
    }

    public void initPermisison(){
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
            //check all permissions
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //trigger the android request this permission
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }
    }

    //to be use by part happy and unhappy
    public void requestServerGet(String urlString, String action) {
        //call api server
        String result = "";
        Log.e("pick", action);
        // HTTP Get
        try {
            URL url = new URL(urlString+"?action=" + action);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            InputStream inputStream = conn.getInputStream();
            result = readServerResponse(conn).toString();
//            result = getBytesByInputStream(inputStream);
//            if (null != inputStream)
//                result = readServerResponse(conn).toString();
            JSONObject responseJson = new JSONObject(result);
            Log.e("request_get", action + "is doing ...");
            processResponse(responseJson);

        } catch (Exception e) {
//            System.out.println(e.getMessage());
            Log.e("Error", e.getMessage());
        }

        //accept the response
        boolean done = true;

    }

    private class asyncRequestPick extends AsyncTask<String, String, Void> {
        String url = urlServer + "pick";
        String action;

        private asyncRequestPick(String action) {
            this.action  = action;
        }

        private asyncRequestPick(){}

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected Void doInBackground(String... strings) {
            requestServerGet(url, action);
            return null;
        }
        protected void onPostExecute(String result) {
        }
    }


    //produce sound, request pick
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void partHappy(){

        //show generate random sound
//        MediaPlayer mpHappy = MediaPlayer.create(MainActivity.this, getRandomValueFromHashMap(unHappySoundMap));


        MediaPlayer mpHappy = createMediaPlayer(Emotion.happy.toString());

        mpHappy.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                new asyncRequestPick(Emotion.happy.toString()).execute();
            }
        });
        mpHappy.start();
    }


    // unhappy remember got spray water
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void partUnhappy(){
        MediaPlayer mpUnHappy = createMediaPlayer(Emotion.unhappy.toString());
        MediaPlayer mpMore = getMediaPlayerWantMore(Emotion.unhappy.toString());
        mpUnHappy.setNextMediaPlayer(mpMore);
        mpUnHappy.start();
    }

    public MediaPlayer getMediaPlayerWantMore(final String action){
        Log.e("getMediaPlayerWantMore", "reached");

        MediaPlayer mpMore = createMediaPlayer(Emotion.more.toString());//maybe change to want more flower

        mpMore.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                recordAudio(action);
//                sendAudioFile(action); // debug use only
            }
        });
        return mpMore;
    }

    public HashMap getActionsHashMap(){
        HashMap<String, Integer> actions = new HashMap<String, Integer>();
        actions.put(Emotion.happy.toString(), R.raw.happy);
        actions.put(Emotion.unhappy.toString(), R.raw.unhappy);
        actions.put(Emotion.repeat.toString(), R.raw.please_repeat);
        actions.put(Emotion.more.toString(), R.raw.want_more);
        actions.put(Emotion.yesMore.toString(), R.raw.yes_more);
        return actions;
    }

    public HashMap getUnhappyHashMap(){
        HashMap<String, Integer> unhappySoundMap = new HashMap<String, Integer>();
        unhappySoundMap.put("d", R.raw.d);
        unhappySoundMap.put("e", R.raw.e);
        unhappySoundMap.put("f", R.raw.f);

        return unhappySoundMap;
    }

    public HashMap getHappyHashMap(){
        HashMap<String, Integer> happySoundMap = new HashMap<String, Integer>();
        happySoundMap.put("a", R.raw.a);
        happySoundMap.put("b", R.raw.b);
        happySoundMap.put("c", R.raw.c);
        return happySoundMap;
    }

    public int getRandomValueFromHashMap(HashMap soundMap){
        List<Integer> valuesList = new ArrayList<Integer>(soundMap.values());
        int randomIndex = new Random().nextInt(valuesList.size());
        Integer randomValue = valuesList.get(randomIndex);
        return randomValue;
    }

    public MediaPlayer createMediaPlayer(final String action){
        MediaPlayer mp = MediaPlayer.create(MainActivity.this, (int) actionHahsMap.get(action));
        return mp;
    }

    public void recordAudio(final String action){
        MediaRecorder recorder = initMediaPlayer();
        recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    mr.stop();
                    Log.e("Maximumd", "MaximsetDataSourceum Duration Reached");
                    mr.release();
                    sendAudioFile(action);
                }
            }
        });
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("try record prepare", "prepare() failed");
        }
        recorder.start();
    }

    public MediaRecorder initMediaPlayer(){
        MediaRecorder recorder = new MediaRecorder();
        String path = recordAudioPath;
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(path);
        recorder.setMaxDuration(5000);
        recorder.setAudioSamplingRate(44100);
        recorder.setAudioEncodingBitRate(1024 * 1024);
        return recorder;
    }

    public MediaRecorder setMediaPlayerListener(MediaRecorder recorder, final String action){
        recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    mr.stop();
                    Log.e("Maximumd", "MaximsetDataSourceum Duration Reached");
                    mr.release();
                    sendAudioFile(action);
                }
            }
        });
        return recorder;
    }

    public void sendAudioFile(String action){
        String url = urlServer + "api_speech";
        String path = recordAudioPath;
        new sendFilesToServerAsync(action, path).execute(url);
    }

    public boolean onCreateOptionsMenu (Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}

