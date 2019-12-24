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
import android.widget.ImageView;
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
//private static final String urlServer = "http://192.168.43.9:5000/"; //lun cellular data
//    private static final String urlServer = "http://192.168.43.31:5000/"; //lun cellular data
    private static final String urlServer = "http://192.168.0.137:5000/"; //ica2


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
    private final HashMap actioeNutralSoundMap = getNeutralHashMap();

    private MediaPlayer mpBgFlower;

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

    private final String HappyPick = "pick for flower";
    private final String SadPick = "pick for chocolate and release EO";

    private final String confirmHappy = "confirm as happy";
    private final String confirmSad = " confirm as sad";
    private final String confirmNeutral = " confirm as neutral";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPermisison();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        startIntentSound(R.raw.hello_robot, "videoCapture");
    }

    public void onResume() {
        super.onResume();
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    // make out emotion image based on emotion
    // set loading book gone to make sure the position of book replace by the emotion
    public void processEmotionImage(final String emotion){
        new Handler(Looper.getMainLooper()).post(new Runnable(){

            @Override
            public void run() {
                ImageView img= (ImageView) findViewById(R.id.image);
                BookLoading bookLoading = findViewById(R.id.bookloading);

                if(emotion.equals("happy")) {
                    img.setImageResource(R.drawable.happy);
                } else if(emotion.equals("sad")) {
                    img.setImageResource(R.drawable.sad);
                } else if(emotion.equals("neutral")) {
                    img.setImageResource(R.drawable.neutral);
                } else {
                    Log.e("undefined", "error image");
                }
                bookLoading.setVisibility(View.GONE);
                img.setVisibility(View.VISIBLE);
            }
        });
    }

    public void showEmotionImage(){
        new Handler(Looper.getMainLooper()).post(new Runnable(){

            @Override
            public void run() {
                ImageView img= (ImageView) findViewById(R.id.image);
                img.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hideEmotionImage(){
        new Handler(Looper.getMainLooper()).post(new Runnable(){

            @Override
            public void run() {
                ImageView img= (ImageView) findViewById(R.id.image);
                img.setVisibility(View.GONE);

            }
        });
    }


    public void processLoading(Boolean on, String text, boolean playSound){
        setInfoText(text);
        if(on) startLoading(playSound);
        else stopLoading();
    }

    public static void stopXMSecForDisplay(int MSec){
        try {
            Thread.sleep(MSec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //sound is the process of emotion ...
    public void startLoading(boolean playSound) {
        if(playSound) {
            final MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.please_wait_while_your_emotion);
            Log.e("loading", "start sound");
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
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable(){
                @Override
                public void run() {
                    BookLoading bookLoading = findViewById(R.id.bookloading);
                    bookLoading.start();
                    bookLoading.setVisibility(View.VISIBLE);
                }
            });
        }

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

    // custom stop time the ui to display the text longer
    public void setInfoText(final String text, final int time) {
        new Handler(Looper.getMainLooper()).post(new Runnable(){
            @Override
            public void run() {
                TextView infoText = findViewById(R.id.textView);
                infoText.setText(text);
                stopXMSecForDisplay(time);
            }
        });
    }

    public void updateProgressBar(final int size){
        new Handler(Looper.getMainLooper()).post(new Runnable(){
            @Override
            public void run() {
                ProgressBar progressBar = findViewById(R.id.progressBar);
                if(progressBar.getVisibility() != View.VISIBLE){
                    progressBar.setVisibility(View.VISIBLE);
                }
                progressBar.setProgress(size);
            }
        });
    }

    public void hideProgressBar(){
        new Handler(Looper.getMainLooper()).post(new Runnable(){
            @Override
            public void run() {
                ProgressBar progressBar = findViewById(R.id.progressBar);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

    }

    // start sound in another activity to show bar style wave then
    // tell where the flow should go with nextAction for onActivityResult
    public void startIntentSound(int soundID, String nextAction) {
//        Intent intent = new Intent(this, SoundVisual.class);
        Intent intent = new Intent(this, SoundVisual.class);
        intent.putExtra("soundID",soundID);
        intent.putExtra("nextAction",nextAction);
        startActivityForResult(intent, 99);
//        overridePendingTransition(0, 0);
        overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
    }

    public void recordVideo(){
        Log.e("99", "request save video activities");
        Intent intent2 = new Intent(this, SurfaceCamera.class);
        startActivityForResult(intent2, file_server_response_code);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case (sound_response_code):
                String action = "";
                if (data.hasExtra("nextAction")) {
                    action = data.getStringExtra("nextAction");
                    Log.e("success", "data got extra");
                } else {
                    Log.e("error", "data no extra");
                }
//                if (action.equals("please_record")) {
//                    introCaptureVideo();
//                }
                if (action.equals("videoCapture")) {
                    recordVideo();
//                    Log.e("99", "request save video activities");
//                    Intent intent2 = new Intent(this, SurfaceCamera.class);
//                    startActivityForResult(intent2, file_server_response_code);
//                } else if (action.equals(NeutralAction.neutralsound_1.toString())) {
//                    //end here
////                    partSoundEnd();
//                    startIntentSound(getRandomValueFromHashMap(getNeutralHashMap()), "semi_end");
                } else if (action.equals(NeutralAction.neutralsound_1.toString())) {
                    //end here
//                    partSoundEnd();
                    tryProcessPick(action);
//                    startIntentSound(getRandomValueFromHashMap(getNeutralHashMap()), "semi_end");
                } else if (action.equals(HappyPick)) {
                    tryProcessPick(action);
                } else if (action.equals(SadPick)) {
                    tryProcessPick(action);
                } else if (action.equals("repeat_1_happy")) { // repeat pick process
                    recordAudio(HappyPick);
                } else if (action.equals("repeat_1_sad")) { // repeat pick process
                    recordAudio(SadPick);
                } else if (action.equals("want_more_happy")){
                    playSoundWantMore(HappyPick);
                } else if (action.equals("want_more_sad")) {
                    playSoundWantMore(SadPick);
                } else if (action.equals("confirm_happy")){ //confirm as what emotion

                    recordAudio(confirmHappy);
                } else if (action.equals("confirm_sad")){//confirm as what emotion

                    recordAudio(confirmSad);
                } else if (action.equals("confirm_neutral")) {//confirm as what emotion

                    recordAudio(confirmNeutral);
                } else if (action.equals("repeat_confirm_happy")){
                    playSoundEmotion("happy");
                } else if (action.equals("repeat_confirm_sad")){
                    playSoundEmotion("sad");
                } else if (action.equals("emotion_confirm_YN_happy")){
                    soundYesNo("confirm_happy");
                } else if (action.equals("emotion_confirm_YN_sad")){

                    soundYesNo("confirm_sad");
                } else if (action.equals("emotion_confirm_YN_neutral")){

                    soundYesNo("confirm_neutral");
                } else if(action.equals("end")) {
                    setInfoText("This is the end of the robot.");
                } else if (action.equals("semi_end")){
                    partSoundEnd();
                }
                else if (action.equals("repeat_1")) {
                }else{

                    Log.e("Error", "undefined" + action);
                }
                break;
            case (100):
                Log.e("100", "sending to server");
                setInfoText("Video recorded");
//                new sendFilesToServerAsync("recognition of face ",
//                        recordVideoPath).execute(urlServer + "api_emo_face");
                new sendVideoToServerAsync("recognition of face ",
                        recordVideoPath).execute(urlServer + "api_emo_face");
                break;
        }
    }

    public void introCaptureVideo(){
        recordVideo();
//        startIntentSound(R.raw.no, "videoCapture");
    }

    // if confirmed the emotion, then will proceed
    public void tryProcessPick(String actionString){

//        if(actionString.equals(HappyPick)) {
//            processTriggerPick(HappyPick);
//        } else if (actionString.equals(SadPick)){
//            processTriggerPick(SadPick);
//        }
        Log.e("tryProcessPick", actionString);
        if(canProceedConfirm()) {
            if(actionString.equals(HappyPick)) {
                Log.e("happypick", actionString);
                processTriggerPick(HappyPick);
            } else if (actionString.equals(SadPick)){
                Log.e("sadpick", actionString);
                processTriggerPick(SadPick);
            } else if (actionString.equals(NeutralAction.neutralsound_1.toString())) {
                Log.e("neutral part", "here");
                startIntentSound(getRandomValueFromHashMap(getNeutralHashMap()), "semi_end");
            } else {
                Log.e("error Try", "undefined 1");
            }
        } else {
            loopOfEmotionFace=1;
            if(actionString.equals(HappyPick)){
                startIntentSound(R.raw.feel_happy, "emotion_confirm_YN_happy");
            } else if (actionString.equals(SadPick)){
                startIntentSound(R.raw.feel_sad, "emotion_confirm_YN_sad");
            } else if (actionString.equals(NeutralAction.neutralsound_1.toString())) {
                startIntentSound(R.raw.feel_neutral, "emotion_confirm_YN_neutral");
            } else {
                Log.e("error Try 2 ", "undefined 2");
            }
        }
    }

    public void soundYesNo(String action){
        startIntentSound(R.raw.pls_respond_yes_no, action);
    }

    public boolean canProceedConfirm(){
        Log.e("canProceed", Integer.toString(loopOfEmotionFace));
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

    // after the return of server process, logic and decide which path to go
    public void processResponse(JSONObject responseJson) throws JSONException {
        Log.d("response place", "response method2");
        updateProgressBar(99);
        hideEmotionImage();
        stopXMSecForDisplay(1000);
        Log.d("response place", "response method");
        String action = responseJson.get("processed").toString();
        String result = responseJson.get("result").toString();

        if(action.equals("api_emo_face")) {
            processEmotionImage(result);
            processLoading(false, "Recognized as : " + result + " face", false);
//            processEmotionImage(result);
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
            processLoading(false, "Finish for " + actionType, false);
            Log.e("testlog", actionType );
            if(actionType.equals(HappyPick)){
                new Handler(Looper.getMainLooper()).post(new Runnable(){
                    @Override
                    public void run() {
                        mpBgFlower.stop();
                        mpBgFlower.release();
                    }
                });
                startIntentSound(R.raw.take_flower,"semi_end");
            } else {
                Log.e("testlog2", "sad pick" );
                playSoundWantMore(actionType);
            }

        } else if(action.equals("api_speech")){
            stopLoading();
            String actionType = responseJson.get("actionType").toString();
            if(result.equals("1")) {
                processLoading(false, "Detected speech as YES", false);
//                setInfoText("Detected speech as YES");
                //yes

                //dirty method
                Log.e("test actionType", actionType);
//                if(isActionConfirm(actionType))processConfirmAs("yes", actionType);
//                else processActionWantMore(actionType);

                if(actionType.equals(HappyPick)) {
                    processActionWantMore(actionType);
                } else if(actionType.equals(SadPick)) {
                    processActionWantMore(actionType);
                } else {
                    processConfirmAs("yes", actionType);
                    // do the check confirm
                }

            }else if(result.equals("2")){
                processLoading(false, "Detected speech as NO", false);

                if(actionType.equals(HappyPick)) {
                    startIntentSound(R.raw.take_flower,"semi_end");
                } else if(actionType.equals(SadPick)) {
                    startIntentSound(R.raw.take_cho,"semi_end");
                } else {
                    processConfirmAs("no", actionType);
                    // do the check confirm
                }
            }
            else {
                processLoading(false, "Could not recognize speech as YES or NO", false);
                if(actionType.equals(HappyPick)) {
                    final String finalActionType = actionType;
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {

                            // run AsyncTask here.
                            processActionRepeatVoice(finalActionType);
                        }
                    }, 5000);
                } else if(actionType.equals(SadPick)) {
                    final String finalActionType = actionType;
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {

                            // run AsyncTask here.
                            processActionRepeatVoice(finalActionType);
                        }
                    }, 5000);                }
                else {
                    processConfirmAs("fail", actionType);
                    // do the check confirm
                }
            }
        } else {
            Log.e("process_response", "unhandled result");
        }
    }

    public void processConfirmAs(String detectAnswer, String actionType){
        Log.e("processConfirmAs", actionType);

        if(detectAnswer.equals("yes")) {
            if(actionType.equals(confirmHappy)) startIntentSound(R.raw.your_emotion_is_happy, HappyPick);
//                processTriggerPick(HappyPick);
            else if (actionType.equals(confirmSad)) startIntentSound(R.raw.your_emotion_is_sad, SadPick);
//                processTriggerPick(SadPick);
            else if (actionType.equals(confirmNeutral)) partNeutral();
        } else if (detectAnswer.equals("no")){
//            loopOfEmotionFace=1;
            recordVideo();

//            introCaptureVideo();
        } else if (detectAnswer.equals("fail")) {
//            loopOfEmotionFace = 0;
            if(actionType.equals(confirmHappy))
//                HappyPick

                soundYesNo(HappyPick);
//                soundYesNo("confirm_happy");
//            soundYesNo("confirm_happy");
            if(actionType.equals(confirmSad))
//                soundYesNo("confirm_sad");
                soundYesNo(SadPick);
//                soundYesNo("confirm_sad");
            if(actionType.equals(confirmNeutral))
//                soundYesNo("confirm_neutral");
                soundYesNo(NeutralAction.neutralsound_1.toString());

        }
    }

    // trigger robot arm pick by calling server
    public void
    processTriggerPick(String action){
//        if(action.equals(Emotion.unhappy.toString())) {
        Log.e("processTriggerPick", action);

        if(action.equals(SadPick)) {
            new asyncRequestPick(action).execute(); // should be do in function to move robot arm

        }else if (action.equals(HappyPick)) {
            mpBgFlower = MediaPlayer.create(MainActivity.this, R.raw.bg_music);
            mpBgFlower.start();
            new asyncRequestPick(action).execute(); // should be do in function to move robot arm

        } else {
            Log.e("processTriggerPick", "1234");
        }
    }

    public void processActionWantMore(final String action) {
        Log.e("processActionW", action);
        tryProcessPick(action);
//        startIntentSound(R.raw.yes_more, action.equals(Emotion.happy.toString()) ? HappyAction.happysound_1.toString() : UnHappyAction.unhappysound_1.toString());
    }

    public void partSoundEnd(){
        showEmotionImage();
        hideProgressBar();
        startIntentSound(R.raw.thats_all_thank_u, "end");
    }

    public void processActionSufficient(){
        setInfoText("This is the end of the robot.");
    }

    public void playSoundWantMore(String actionType){
        String temp_nextAction;
        if(actionType.equals(HappyPick)) {
            temp_nextAction = "repeat_1_happy";
        } else if(SadPick.equals(actionType)) {
            temp_nextAction = "repeat_1_sad";
        }
        else {
            temp_nextAction = "asdass";
            Log.e("asdasd", "bug");

        }
//        else {
//            temp_nextAction = "repeat_1_sad";
//        }
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
        Log.e("actionRepeat", action);
        if(action.equals(HappyPick)) startIntentSound(R.raw.we_cannot_detect, "want_more_happy");
        else startIntentSound(R.raw.we_cannot_detect, "want_more_sad");
    }

    //send file to the server and the paramater as action if got
    // the file send will be the filepath
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
            hideEmotionImage();
            if(TextUtils.isEmpty(actionText)) {
                processLoading(true, "Processing for " + action + " please be patient . . .",false);
            } else {
                processLoading(true, "Processing for " + actionText + " please be patient . . .", false);
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

    // diff from file because this want sound
    public class sendVideoToServerAsync extends AsyncTask<String, Integer, Void> {

        String action;
        String filePath;
        String actionText;
        protected void onProgressUpdate(Integer... values){
            updateProgressBar(values[0]);
        }
        private sendVideoToServerAsync(String action, String filePath) {
            this.action  = action;
            this.filePath = filePath;
        }

        private sendVideoToServerAsync(String action, String filePath, String actionPass) {
            this.action  = action;
            this.actionText  = actionPass;
            this.filePath = filePath;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            if(TextUtils.isEmpty(actionText)) {
                processLoading(true, "Processing for " + action + " please be patient . . .",true);
            } else {
                processLoading(true, "Processing for " + actionText + " please be patient . . .", true);
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

    // permission needed for this project apk
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
            hideEmotionImage();
            processLoading(true, "Processing for " + action + " please be patient . . .",false);
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
        playSoundEmotion("happy");
//        startIntentSound(R.raw.your_emotion_is_happy, HappyPick));
    }

    // play one more sound after emotion neutral
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void partNeutral(){
        setInfoText("doing part neutral");
//        if(canProceedConfirm()) startIntentSound(getRandomValueFromHashMap(actioneutralSoundMap), NeutralAction.neutralsound_1.toString());
//        else confirmEmotion(confirmNeutral);

        playSoundEmotion("neutral");

//        startIntentSound(R.raw.neutral, NeutralAction.neutralsound_1.toString());
//        startIntentSound(getRandomValueFromHashMap(actioneutralSoundMap), NeutralAction.neutralsound_1.toString());
    }


    // unhappy remember got spray water
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void partUnhappy(){
        setInfoText("doing part unhappy");
        playSoundEmotion("sad");
    }

    public void playSoundEmotion(String action) {
        if(action.equals("sad")) {
            startIntentSound(R.raw.your_emotion_is_sad, SadPick);
        } else if(action.equals("happy")) {
            startIntentSound(R.raw.your_emotion_is_happy, HappyPick);
        } else if(action.equals("neutral")){
            startIntentSound(R.raw.your_emotion_is_neutral, NeutralAction.neutralsound_1.toString());
        }
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
        actions.put(Emotion.happy.toString(), R.raw.your_emotion_is_happy);
        actions.put(Emotion.unhappy.toString(), R.raw.your_emotion_is_sad);
        actions.put(Emotion.repeat.toString(), R.raw.we_cannot_detect);
        actions.put(Emotion.more.toString(), R.raw.want_more);
        actions.put(Emotion.yesMore.toString(), R.raw.yes_more);
        actions.put(Emotion.neutral.toString(), R.raw.your_emotion_is_neutral);
        return actions;
    }

    public HashMap getUnhappyHashMap(){
        HashMap<String, Integer> unhappySoundMap = new HashMap<String, Integer>();
//        unhappySoundMap.put("d", R.raw.d);
//        unhappySoundMap.put("e", R.raw.e);
//        unhappySoundMap.put("f", R.raw.f);

        return unhappySoundMap;
    }

    public HashMap getHappyHashMap(){
        HashMap<String, Integer> happySoundMap = new HashMap<String, Integer>();
//        happySoundMap.put("a", R.raw.a);
//        happySoundMap.put("b", R.raw.b);
//        happySoundMap.put("c", R.raw.c);
        return happySoundMap;
    }

    // sound files prepare to be random called
    public HashMap getNeutralHashMap(){
        HashMap<String, Integer> neutralSoundMap = new HashMap<String, Integer>();
        neutralSoundMap.put("q_!", R.raw.q_1);
        neutralSoundMap.put("q_2", R.raw.q_2);
        neutralSoundMap.put("q_3", R.raw.q_3);
        neutralSoundMap.put("q_4", R.raw.q_4);
        neutralSoundMap.put("q_5", R.raw.q_5);
        neutralSoundMap.put("q_6", R.raw.q_6);
        neutralSoundMap.put("q_7", R.raw.q_7);
        neutralSoundMap.put("q_8", R.raw.q_8);
        neutralSoundMap.put("q_9", R.raw.q_9);
        neutralSoundMap.put("q_10", R.raw.q_10);
        return neutralSoundMap;
    }

    // in android, file is actually the integer of the R.raw.(int)
    // so this is android
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
        setInfoText("Recording sound . . .",0);
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
//        recorder.setMaxDuration(2000);
        recorder.setMaxDuration(2000);
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

