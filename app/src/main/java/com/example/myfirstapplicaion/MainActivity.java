package com.example.myfirstapplicaion;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.victor.loading.book.BookLoading;

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
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    public MainActivity() {

    }

    //    private static final String urlServer = "http://192.168.43.115:5000/"; //lun cellular data
//    private static final String urlServer = "http://192.168.43.172:5000/"; //lun cellular data

//    private static final String urlServer = "http://192.168.0.128:5000/"; //ICA2 ipv4
//    private static final String urlServer = "http://10.200.51.29:5000/"; //kasturi ipv4

//    private static final String urlServer = "http://192.168.0.137:5000/"; //lun cellular data
    private static final String urlServer = "http://192.168.43.9:5000/"; //lun cellular data

    private static final int sound_response_code = 99;
    private static final int file_server_response_code = 100;

    private static final String downloadPath = Environment.getExternalStorageDirectory() + "/download";
    private static final String soundPath = downloadPath + "/sound"; // please add this two path

    private static final String recordAudioFileName = "recordAudioFile.mp4";
    private static final String recordAudioPath = soundPath + '/' + recordAudioFileName;

    private static final String videoPath = downloadPath + "/video";
    private static final String recordVideoFileName = "recordVideoFile.mp4";
    private static final String recordVideoPath = videoPath + '/' + recordVideoFileName;

    private final HashMap happySoundMap = getHappyHashMap();
    private final HashMap unHappySoundMap = getUnhappyHashMap();
    private final HashMap actionHahsMap = getActionsHashMap();
    private final HashMap actioneutralSoundMap = getNeutralHashMap();

    private int loopOfEmotionFace = 0;

    private static int VIDEO_REQUEST = 101;
    private Uri videoUri = null;

//    private String currentEmotion = "";

    enum Emotion {
        happy,
        unhappy,
        more,
        repeat,
        yesMore,
        neutral,
        neutralEnd
    }

    enum HappyAction {
        happysound_1,
    }
    enum NeutralAction {
        neutralsound_1,
        neutralsound_2
    }
    enum UnHappyAction {
        unhappysound_1,
    }

    private final String HappyPick = " pick for flower";
    private final String SadPick = " pick for chocolate";
    private final String confirmHappy = "confirm as happy";
    private final String confirmSad = " confirm as sad";
    private final String confirmNeutral = " confirm as neutral";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        initPermisison();
//        Intent intent = new Intent(this, MediaRecorderActivity.class);
//        startActivity(intent);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//            getSupportActionBar().setHomeButtonEnabled(false);
//        }
        setContentView(R.layout.main);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
//        getActionBar().hide(); //hide the title bar
//        Display display = ((WindowManager)this.getSystemService(this.WINDOW_SERVICE)).getDefaultDisplay();
//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        }
//        else {
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        }
        startIntentSound(R.raw.hello, "please_record");

    }

    public void onResume() {
        super.onResume();
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    public void processLoading(Boolean on, String text){
        setInfoText(text);
        if(on) startLoading();
        else stopLoading();
    }

    public static void stopXMSecForDisplay(int MSec){
        try {
            Thread.sleep(MSec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startLoading() {
        final MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.a_little_progress);
        new Handler(Looper.getMainLooper()).post(new Runnable(){
            @Override
            public void run() {
                BookLoading bookLoading = findViewById(R.id.bookloading);
                mp.start();
                bookLoading.start();
                bookLoading.setVisibility(View.VISIBLE);
//                bookLoading.setVisibility(View.GONE);
//                stopXMSecForDisplay(3000);
            }
        });
    }

    public void stopLoading(){
        new Handler(Looper.getMainLooper()).post(new Runnable(){
            @Override
            public void run() {
                BookLoading bookLoading = findViewById(R.id.bookloading);
                bookLoading.stop();
                bookLoading.setVisibility(View.INVISIBLE);
                hideProgressBar();
//                bookLoading.setVisibility(View.INVISIBLE);
//                stopXMSecForDisplay(3000);
            }
        });
    }

    public void setInfoText(final String text) {
        new Handler(Looper.getMainLooper()).post(new Runnable(){
            @Override
            public void run() {
                TextView infoText = findViewById(R.id.textView);
                infoText.setText(text);
                stopXMSecForDisplay(4000);
            }
        });
    }

    public void updateProgressBar(int size){
        ProgressBar progressBar = findViewById(R.id.progressBar);
//        progressBar.setIndeterminate(true);
        if(progressBar.getVisibility() != View.VISIBLE){
            progressBar.setVisibility(View.VISIBLE);
        }
        progressBar.setProgress(size);
    }

    public void hideProgressBar(){
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void setInfoText(final String text, final Boolean stop) {
        new Handler(Looper.getMainLooper()).post(new Runnable(){
            @Override
            public void run() {
                TextView infoText = findViewById(R.id.textView);
                infoText.setText(text);
                if(stop){
                    stopXMSecForDisplay(3000);
                }
            }
        });
    }

    public void startIntentSound(int soundID, String nextAction) {
//        Intent intent = new Intent(this, SoundVisual.class);
        Intent intent = new Intent(this, SoundVisual.class);
        intent.putExtra("soundID",soundID);
        intent.putExtra("nextAction",nextAction);
        startActivityForResult(intent, 99);
//        overridePendingTransition(0, 0);
        overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
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
        switch (resultCode) {
            case (sound_response_code):
                String action = "";
                if (data.hasExtra("nextAction")) {
                    action = data.getStringExtra("nextAction");
                    Log.e("success", "data got extra");
                } else {
                    Log.e("error", "data no extra");
                }
                if (action.equals("please_record")) {
                    introCaptureVideo();
                } else if (action.equals("videoCapture")) {
                    Log.e("99", "request save video activities");
                    Intent intent2 = new Intent(this, SurfaceCamera.class);
                    startActivityForResult(intent2, file_server_response_code);
                } else if (action.equals(NeutralAction.neutralsound_1.toString())) {
                    //end here
                    startIntentSound(R.raw.end, "end");
//                    startIntentSound(R.raw.neutral_end, NeutralAction.neutralsound_2.toString());
                } else if (action.equals(NeutralAction.neutralsound_2.toString())) {
//                    processLoading(false, "Part neutral completed");
                    Log.e(NeutralAction.neutralsound_2.toString(), "now end at neutral part");
                } else if (action.equals(HappyAction.happysound_1.toString())) {
                    if(canProceedConfirm()) processTriggerPick(HappyPick);
                    else confirmEmotion(confirmHappy);
                } else if (action.equals(UnHappyAction.unhappysound_1.toString())) {
                    if(canProceedConfirm()) processTriggerPick(SadPick);
                    else confirmEmotion(confirmSad);
                } else if (action.equals("repeat_1_happy")) {
                    recordAudio(HappyPick);
                } else if (action.equals("repeat_1_sad")) {
                    recordAudio(SadPick);
                } else if (action.equals("want_more_happy")){
                    playSoundWantMore(HappyPick);
                } else if (action.equals("want_more_sad")) {
                    playSoundWantMore(SadPick);
                } else if (action.equals("confirm_happy")){
                    recordAudio(confirmHappy);
                } else if (action.equals("confirm_sad")){
                    recordAudio(confirmSad);
                } else if (action.equals("confirm_neutral")){
                    recordAudio(confirmNeutral);
                } else if (action.equals("repeat_confirm_happy")){
                    confirmEmotion(confirmHappy);
                } else if (action.equals("repeat_confirm_sad")){
                    confirmEmotion(confirmSad);
                } else if (action.equals("repeat_confirm_neutral")){
                    confirmEmotion(confirmNeutral);
                }
                else if (action.equals("repeat_1")) {
                }else{
                    Log.e("Error", "undefined");
                }
                break;
            case (100):
                Log.e("100", "sending to server");
                setInfoText("Video recorded");
                new sendFilesToServerAsync("recognition of face ",
                        recordVideoPath).execute(urlServer + "api_emo_face");
                break;
        }
    }

    public void introCaptureVideo(){
        startIntentSound(R.raw.please_record, "videoCapture");
    }

    public void confirmEmotion(String emotion){
        loopOfEmotionFace++;
        if(emotion.equals(confirmHappy)) startIntentSound(R.raw.confirm_happy, "confirm_happy");
        else if(emotion.equals(confirmSad)) startIntentSound(R.raw.confirm_sad, "confirm_sad");
        else if(emotion.equals(confirmNeutral))startIntentSound(R.raw.confirm_neutral, "confirm_neutral");
        else Log.e("error_confirm", "emotion undefined.");
    }

    public boolean canProceedConfirm(){
        if(loopOfEmotionFace > 0 ) return true;
//        if (!currentEmotion.isEmpty()) return true;
        else return false;
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

    public void processResponse(JSONObject responseJson) throws JSONException {
        updateProgressBar(99);
        stopXMSecForDisplay(1000);
        Log.d("response place", "response method");
        String action = responseJson.get("processed").toString();
        String result = responseJson.get("result").toString();

        if(action.equals("api_emo_face")) {
            processLoading(false, "Recognized as : " + result + " face");

            if(result.equals(Emotion.happy.toString())) {
                partHappy();
            } else if(result.equals("sad")) {
                partUnhappy();
            } else if(result.equals("neutral")){
                partNeutral();
            }
            else {
                Log.d("api_emo_face", "undefined response to emo face");
            }
        } else if(action.equals("pick")) { // picking

            String actionType = responseJson.get("actionType").toString();
            processLoading(false, "Finish for " + actionType);
            playSoundWantMore(actionType);
        } else if(action.equals("api_speech")){
            stopLoading();
            String actionType = responseJson.get("actionType").toString();
            if(result.equals("1")) {
                processLoading(false, "Detected speech as YES");
//                setInfoText("Detected speech as YES");
                //yes

                //dirty method
                if(isActionConfirm(actionType))processConfirmAs("yes", actionType);
                else processActionWantMore(actionType);
            }else if(result.equals("2")){
                processLoading(false, "Detected speech as NO");
//                setInfoText("Detected speech as NO");
                if(isActionConfirm(actionType))processConfirmAs("no", actionType);
                else processActionSufficient(actionType);
                //no
                // logic next step or end
            }
            else {
                processLoading(false, "Could not recognize speech as YES or NO");
                if(isActionConfirm(actionType))processConfirmAs("fail", actionType);
//                setInfoText("Could not recognize speech as YES or NO");
                //could not detect properly
                else {
                        final String finalActionType = actionType;
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {

                            // run AsyncTask here.
                            processActionRepeatVoice(finalActionType);
                        }
                    }, 5000);
                }
            }
        } else {
            Log.e("process_response", "unhandled result");
        }
    }

    public boolean isActionConfirm(String actionType){
        if(actionType.equals(confirmHappy)) return true;
        else if (actionType.equals(confirmSad)) return true;
        else if (actionType.equals(confirmNeutral)) return true;
        return false;
    }

    public void processConfirmAs(String detectAnswer, String actionType){
        if(detectAnswer.equals("yes")) {
            if(actionType.equals(confirmHappy)) startIntentSound(R.raw.happy, HappyAction.happysound_1.toString());
//                processTriggerPick(HappyPick);
            else if (actionType.equals(confirmSad)) startIntentSound(R.raw.confirm_sad, UnHappyAction.unhappysound_1.toString());
//                processTriggerPick(SadPick);
            else if (actionType.equals(confirmNeutral)) partNeutral();
        } else if (detectAnswer.equals("no")){
            introCaptureVideo();
        } else if (detectAnswer.equals("fail")) {
            loopOfEmotionFace--;
            if (actionType.equals(confirmHappy)) startIntentSound(R.raw.please_repeat, "repeat_confirm_happy");
            if (actionType.equals(confirmSad)) startIntentSound(R.raw.please_repeat, "repeat_confirm_sad");
            if (actionType.equals(confirmNeutral)) startIntentSound(R.raw.please_repeat, "repeat_confirm_neutral");
        }
    }

    public void
    processTriggerPick(String action){
//        if(action.equals(Emotion.unhappy.toString())) {

        if(action.equals(SadPick)) {
            //function to move robot arm etc
            new asyncRequestPick(action).execute(); // should be do in function to move robot arm

//        }else if (action.equals(Emotion.happy.toString())) {
        }else if (action.equals(HappyPick)) {
            //function to move robot arm etc
            //do the retrigger the mediarecorder
            new asyncRequestPick(action).execute(); // should be do in function to move robot arm

        } else {
            Log.e("processtMoreFail", "1234");
        }
    }

    public void processActionWantMore(final String action) {
        startIntentSound(R.raw.yes_more, action.equals(Emotion.happy.toString()) ? HappyAction.happysound_1.toString() : UnHappyAction.unhappysound_1.toString());
    }

    public void processActionSufficient(String action){

    }

    public void playSoundWantMore(String actionType){
        String temp_nextAction;
        if(actionType.equals(HappyPick)) {
            temp_nextAction = "repeat_1_happy";
        } else {
            temp_nextAction = "repeat_1_sad";
        }
        final String nextAction = temp_nextAction;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                // run AsyncTask here.
                startIntentSound(R.raw.want_more, nextAction);
            }
        }, 5000);

    }


    public void processActionRepeatVoice(final String action){
        if(action.equals(HappyPick)) startIntentSound(R.raw.please_repeat, "want_more_happy");
        else startIntentSound(R.raw.please_repeat, "want_more_sad");
    }

    public class sendFilesToServerAsync extends AsyncTask<String, Integer, Void> {

        String action;
        String filePath;
        String actionText;
        protected void onProgressUpdate(Integer... values){
            updateProgressBar(values[0]);
        }
        private sendFilesToServerAsync(String action, String filePath) {
            this.action  = action;
            this.filePath = filePath;
        }

        private sendFilesToServerAsync(String action, String filePath, String actionPass) {
            this.action  = action;
            this.actionText  = actionPass;
            this.filePath = filePath;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            if(TextUtils.isEmpty(actionText)) {
                processLoading(true, "Processing for " + action + " please be patient . . .");
            } else {
                processLoading(true, "Processing for " + actionText + " please be patient . . .");
            }
        }
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected Void doInBackground(final String... strings) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
//                        sendFilesToServer(action, filePath, strings);
                        publishProgress(25);

                        URL url = null;
                        StringBuilder response = null;
                        try {
                            url = new URL(strings[0]);
                            File file = new File(filePath);
                            HttpURLConnection conn = initConnection(url);

                            writeFilesParamToDataOutputStream(conn, file, action);
                            response = readServerResponse(conn);
                            publishProgress(50);

                            try {
                                JSONObject responseJson = new JSONObject(response.toString());

                                Log.d("response", (String) responseJson.get("result").toString() + "HERE!!!");
                                publishProgress(75);
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
                }, 4000);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
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

    private class asyncRequestPick extends AsyncTask<String, Integer, Void> {
        String action;
        String URL = urlServer + "pick";

        private asyncRequestPick(String action) {
            this.action  = action;
        }

        private asyncRequestPick(){}

        protected void onPreExecute(){
//            setInfoText("doing for " + action);
//            startLoading();
            processLoading(true, "Processing for " + action + " please be patient . . .");
        }

        protected void onProgressUpdate(Integer... values){
            updateProgressBar(values[0]);
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected Void doInBackground(String... strings) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    new Timer().schedule(new TimerTask() {
                            @Override
                        public void run() {
//                            requestServerGet(url, action);
                                //call api server
                                String result = "";
                                Log.e("pick", action);
                                // HTTP Get
                                try {
                                    publishProgress(25);
                                    URL url = new URL(URL+"?action=" + action);
                                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                    InputStream inputStream = conn.getInputStream();
                                    publishProgress(50);
                                    result = readServerResponse(conn).toString();
                                    JSONObject responseJson = new JSONObject(result);
                                    Log.e("request_get", action + "is doing ...");
                                    publishProgress(75);
                                    processResponse(responseJson);

                                } catch (Exception e) {
                                    Log.e("Error", e.getMessage());
                                }

                                //accept the response
                                boolean done = true;
                        }
                    }, 4000);
                }
                return null;
                //this.action , no final string
        }
        protected void onPostExecute(String result) {
        }
    }


    //produce sound, request pick
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void partHappy(){

        //show generate random sound
//        MediaPlayer mpHappy = MediaPlayer.create(MainActivity.this, getRandomValueFromHashMap(unHappySoundMap));
        setInfoText("doing part happy");
        startIntentSound(R.raw.happy, HappyAction.happysound_1.toString());
    }

    // play one more sound after emotion neutral
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void partNeutral(){
        setInfoText("doing part neutral");
        if(canProceedConfirm()) startIntentSound(getRandomValueFromHashMap(actioneutralSoundMap), NeutralAction.neutralsound_1.toString());
        else confirmEmotion(confirmNeutral);
//        startIntentSound(R.raw.neutral, NeutralAction.neutralsound_1.toString());
//        startIntentSound(getRandomValueFromHashMap(actioneutralSoundMap), NeutralAction.neutralsound_1.toString());
    }


    // unhappy remember got spray water
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void partUnhappy(){
        setInfoText("doing part unhappy");
        startIntentSound(R.raw.unhappy, UnHappyAction.unhappysound_1.toString());
    }

    public MediaPlayer getMediaPlayerWantMore(final String action){
        Log.e("getMediaPlayerWantMore", "reached");

        MediaPlayer mpMore = createMediaPlayer(Emotion.more.toString());//maybe change to want more flower

        mpMore.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                recordAudio(action);
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
        actions.put(Emotion.neutral.toString(), R.raw.neutral);
        actions.put(Emotion.neutralEnd.toString(), R.raw.neutral_end);
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

    public HashMap getNeutralHashMap(){
        HashMap<String, Integer> happySoundMap = new HashMap<String, Integer>();
        happySoundMap.put("a", R.raw.randoma);
        happySoundMap.put("b", R.raw.randomb);
        happySoundMap.put("c", R.raw.randomc);
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
        setInfoText("Recording sound . . .");
        MediaRecorder recorder = initMediaPlayer();
        recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    setInfoText("Sound recorded . . .");
                    mr.stop();
                    Log.e("Maximumd", "MaximsetDataSourceum Duration Reached");
                    mr.release();
//                    processLoading(true, "Sending recorded voice . . .");
                    setInfoText("Sending recorded voice . . .");
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
        String actionText = "speech recognition" ;
        new sendFilesToServerAsync(action, path, actionText).execute(url);
    }

    public void sendAudioFile(String action, String actionText){
        String url = urlServer + "api_speech";
        String path = recordAudioPath;
        new sendFilesToServerAsync(action, path, actionText).execute(url);
    }

    public boolean onCreateOptionsMenu (Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}

