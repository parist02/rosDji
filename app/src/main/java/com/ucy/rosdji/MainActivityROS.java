package com.ucy.rosdji;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.ros.android.MessageCallable;
import org.ros.android.RosActivity;
import org.ros.android.view.RosTextView;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import dji.common.error.DJIError;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.util.CommonCallbacks;
import dji.internal.camera.P;
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
    private LiveStreamManager liveStreamManager;

    public MainActivityROS() {
        super("ROS DJI", "ROS");
    }

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
        liveStreamManager = DJISDKManager.getInstance().getLiveStreamManager();
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
        EditText editTextUrl = findViewById(R.id.editTextStreamUrl);
        liveStreamManager.setLiveUrl(editTextUrl.getText().toString());
        liveStreamManager.setAudioMuted(false);
        liveStreamManager.startStream();
        if  (liveStreamManager.isStreaming()){
            TextView textViewFps = findViewById(R.id.textViewLivesteramFps);
            TextView textViewBitRate = findViewById(R.id.textViewLiveStreamBitRate);
            java.lang.String stringFps = "FPS:    "+liveStreamManager.getLiveVideoFps();
            java.lang.String stringBitRate = "BitRate:    "+liveStreamManager.getLiveVideoBitRate();
            textViewFps.setText(stringFps);
            textViewBitRate.setText(stringBitRate);
            Toast.makeText(getApplicationContext(), "LiveStream has started successfully", Toast.LENGTH_SHORT).show();
            Button button = findViewById(R.id.buttonStartLiveStream);
            Button button2= findViewById(R.id.buttonStopLiveStream);
            button.setEnabled(false);
            button2.setEnabled(true);

        }else {
            Toast.makeText(getApplicationContext(), "LiveStream failed to start", Toast.LENGTH_SHORT).show();
        }
    }
    
    public void stopLiveStream(View view){
        liveStreamManager.stopStream();
        if (!liveStreamManager.isStreaming()) {
            Button button = findViewById(R.id.buttonStartLiveStream);
            Button button2 = findViewById(R.id.buttonStopLiveStream);
            TextView textViewFps = findViewById(R.id.textViewLivesteramFps);
            TextView textViewBitRate = findViewById(R.id.textViewLiveStreamBitRate);
            java.lang.String stringFps = "FPS:    LiveStream has stopped";
            java.lang.String stringBitRate = "BitRate:    LiveStream has stopped";
            textViewFps.setText(stringFps);
            textViewBitRate.setText(stringBitRate);
            button.setEnabled(true);
            button2.setEnabled(false);
            Toast.makeText(getApplicationContext(), "LiveStream has stopped successfully", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "LiveStream failed to stop", Toast.LENGTH_SHORT).show();
        }
    }



}