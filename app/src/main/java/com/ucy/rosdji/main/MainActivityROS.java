package com.ucy.rosdji.main;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;

import com.ucy.rosdji.R;
import com.ucy.rosdji.dji.VideoFeedView;
import com.ucy.rosdji.ros.ListenerFlightController;
import com.ucy.rosdji.ros.TalkerHeight;

import org.ros.android.MessageCallable;
import org.ros.android.RosActivity;
import org.ros.android.view.RosTextView;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LiveStreamManager;
import std_msgs.String;

public class MainActivityROS extends RosActivity {

    private RosTextView<std_msgs.String> rosTextView;
    private TalkerHeight talkerHeight;
    private ListenerFlightController listenerFlightController;
    private Aircraft aircraft;
    private FlightController flightController;
    private Gimbal gimbal;
    private java.lang.String serialNumber;


    public MainActivityROS() {
        super("ROS DJI", "ROS");
    }

    private LiveStreamManager.OnLiveChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ros);
        rosTextView = findViewById(R.id.textRos);
        rosTextView.setTopicName("drone_height");
        rosTextView.setMessageType(std_msgs.String._TYPE);
        rosTextView.setMessageToStringCallable(new MessageCallable<java.lang.String, String>() {
            @Override
            public java.lang.String call(String string) {
                return string.getData();
            }
        });
        aircraft = (Aircraft) DJISDKManager.getInstance().getProduct();
        gimbal= aircraft.getGimbal();

        VideoFeedView primaryVideoFeedView = (VideoFeedView) findViewById(R.id.video_view_primary_video_feed);
        primaryVideoFeedView.registerLiveVideo(VideoFeeder.getInstance().getPrimaryVideoFeed(), true);
        listener = new LiveStreamManager.OnLiveChangeListener() {
            @Override
            public void onStatusChanged(int i) {
                Toast.makeText(getApplicationContext(), "status changed: " + i, Toast.LENGTH_SHORT).show();
            }
        };
        flightController = ((Aircraft) DJISDKManager.getInstance().getProduct()).getFlightController();
        flightController.setVirtualStickModeEnabled(false, djiError -> Toast.makeText(getApplicationContext(), "Virtual Commands are disabled", Toast.LENGTH_SHORT).show());
        //liveStreamManager = DJISDKManager.getInstance().getLiveStreamManager();
        Switch switchVirtualControl = findViewById(R.id.switchToggleVirtualControl);
        switchVirtualControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    listenerFlightController.enableVirtualCommands();
                }else{
                    listenerFlightController.disableVirtualCommands();
                }
            }
        });
    }

    public void startVirtualLanding(View view){
        listenerFlightController.sendVirtualCommandLanding();
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());

        //talker = new Talker();
        //nodeMainExecutor.execute(talker,nodeConfiguration);

        //listener = new Listener();
        //nodeMainExecutor.execute(listener,nodeConfiguration);

        listenerFlightController =new ListenerFlightController();
        nodeMainExecutor.execute(listenerFlightController, nodeConfiguration);

        talkerHeight = new TalkerHeight();
        nodeMainExecutor.execute(talkerHeight,nodeConfiguration);

        nodeMainExecutor.execute(rosTextView, nodeConfiguration);

    }

    public void setPitchAngle(View view){
        EditText editTextPitchAngle =  findViewById(R.id.editTextPitchAngle);
        java.lang.String pitchAngleString = editTextPitchAngle.getText().toString();
        if (!pitchAngleString.isEmpty()) {
            try {
                float pitchAngle = Float.parseFloat(pitchAngleString);
                Rotation.Builder rotationBuilder = new Rotation.Builder().mode(RotationMode.ABSOLUTE_ANGLE).time(2);
                rotationBuilder.pitch(pitchAngle);
                gimbal.rotate(rotationBuilder.build(), djiError -> {
                });
            }catch (Exception exception){
                Toast.makeText(getApplicationContext(),"Unable to set angle",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "Please enter pitch angle in degrees", Toast.LENGTH_SHORT).show();
        }
    }
    
    public void startLiveStream(View view){
        if (!isLiveStreamManagerOn()){
            return;
        }
        Toast.makeText(getApplicationContext(), "Start LiveStream", Toast.LENGTH_SHORT).show();
        EditText editTextUrl = findViewById(R.id.editTextStreamUrl);
        java.lang.String stringUrl = editTextUrl.getText().toString();
        DJISDKManager.getInstance().getLiveStreamManager().registerListener(listener);
        DJISDKManager.getInstance().getLiveStreamManager().setLiveUrl(stringUrl);
        int result = DJISDKManager.getInstance().getLiveStreamManager().startStream();
        DJISDKManager.getInstance().getLiveStreamManager().setStartTime();
        Toast.makeText(getApplicationContext(), "startLive:" + result +
                "\n isVideoStreamSpeedConfigurable:" + DJISDKManager.getInstance().getLiveStreamManager().isVideoStreamSpeedConfigurable() +
                "\n isLiveAudioEnabled:" + DJISDKManager.getInstance().getLiveStreamManager().isLiveAudioEnabled(), Toast.LENGTH_SHORT).show();
        if  (DJISDKManager.getInstance().getLiveStreamManager().isStreaming()){
            Toast.makeText(getApplicationContext(), "LiveStream has started successfully", Toast.LENGTH_SHORT).show();
            Button button = findViewById(R.id.buttonStartLiveStream);
            Button button2= findViewById(R.id.buttonStopLiveStream);
            button.setEnabled(false);
            button2.setEnabled(true);
            //getLiveStreamInfo();

        }else {
            Toast.makeText(getApplicationContext(), "LiveStream failed to start", Toast.LENGTH_SHORT).show();
        }






//        EditText editTextUrl = findViewById(R.id.editTextStreamUrl);
//        java.lang.String stringUrl = editTextUrl.getText().toString();
//        Toast.makeText(getApplicationContext(), "URL: " + stringUrl, Toast.LENGTH_SHORT).show();
//        liveStreamManager.registerListener(i -> Toast.makeText(getApplicationContext(), "Callback: " + i, Toast.LENGTH_SHORT).show());
//        liveStreamManager.setVideoSource(LiveStreamManager.LiveStreamVideoSource.Primary);
//        liveStreamManager.setVideoEncodingEnabled(true);
//        liveStreamManager.setLiveUrl(editTextUrl.getText().toString());
//        liveStreamManager.startStream();
//        if  (liveStreamManager.isStreaming()){
//            TextView textViewFps = findViewById(R.id.textViewLivesteramFps);
//            TextView textViewBitRate = findViewById(R.id.textViewLiveStreamBitRate);
//            java.lang.String stringFps = "FPS:    "+liveStreamManager.getLiveVideoFps();
//            java.lang.String stringBitRate = "BitRate:    "+liveStreamManager.getLiveVideoBitRate();
//            textViewFps.setText(stringFps);
//            textViewBitRate.setText(stringBitRate);
//            Toast.makeText(getApplicationContext(), "LiveStream has started successfully", Toast.LENGTH_SHORT).show();
//            Button button = findViewById(R.id.buttonStartLiveStream);
//            Button button2= findViewById(R.id.buttonStopLiveStream);
//            button.setEnabled(false);
//            button2.setEnabled(true);
//
//        }else {
//            //Toast.makeText(getApplicationContext(), "LiveStream failed to start", Toast.LENGTH_SHORT).show();
//        }
    }
    
    public void stopLiveStream(View view) {
        DJISDKManager.getInstance().getLiveStreamManager().stopStream();
        Button button = findViewById(R.id.buttonStartLiveStream);
        Button button2 = findViewById(R.id.buttonStopLiveStream);
        //TextView textViewFps = findViewById(R.id.textViewLivesteramFps);
        //TextView textViewBitRate = findViewById(R.id.textViewLiveStreamBitRate);
        java.lang.String stringFps = "FPS:    LiveStream has stopped";
        java.lang.String stringBitRate = "BitRate:    LiveStream has stopped";
        //textViewFps.setText(stringFps);
        //textViewBitRate.setText(stringBitRate);
        button.setEnabled(true);
        button2.setEnabled(false);
        Toast.makeText(getApplicationContext(), "LiveStream has stopped", Toast.LENGTH_SHORT).show();
    }

    private boolean isLiveStreamManagerOn() {
        if (DJISDKManager.getInstance().getLiveStreamManager() == null) {
            Toast.makeText(getApplicationContext(), "No liveStream manager!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

//    public void getLiveStreamInfo(){
//        TextView textViewFps = findViewById(R.id.textViewLivesteramFps);
//        TextView textViewBitRate = findViewById(R.id.textViewLiveStreamBitRate);
//        java.lang.String stringFps = "FPS:    "+DJISDKManager.getInstance().getLiveStreamManager().getLiveVideoFps();
//        java.lang.String stringBitRate = "BitRate:    "+DJISDKManager.getInstance().getLiveStreamManager().getLiveVideoBitRate();
//        textViewFps.setText(stringFps);
//        textViewBitRate.setText(stringBitRate);
//    }


//    public static void sendDroneCommands(int [] commands){
//        float roll,pitch,yaw,throttle;
//        switch (commands[0]){
//            case 1:
//                pitch = 10;
//                break;
//            case 2:
//                pitch = -10;
//                break;
//            default:
//                pitch =0;
//        }
//        switch (commands[1]){
//            case 2:
//                roll = 10;
//                break;
//            case 1:
//                roll = -10;
//                break;
//            default:
//                roll =0;
//        }
//        switch (commands[2]){
//            case 1:
//                throttle = 2;
//                break;
//            case 2:
//                throttle = -2;
//                break;
//            default:
//                throttle =0;
//        }
//        switch (commands[3]){
//            case 2:
//                yaw = 15;
//                break;
//            case 1:
//                yaw = -15;
//                break;
//            default:
//                yaw =0;
//        }
//        flightController.sendVirtualStickFlightControlData(new FlightControlData(roll, pitch, yaw, throttle), djiError -> {});
//        if (commands[4] == 1) {
//            droneLand();
//        }
//    }
//
//
//    private void droneLand(){
//        flightController.startLanding(djiError -> {});
//        while(!flightController.getState().isLandingConfirmationNeeded()){
//            try {
//                Thread.sleep(200);
//            }catch (InterruptedException ignored){}
//        }
//        flightController.confirmLanding(djiError -> {});
//    }

}