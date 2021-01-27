package com.ucy.rosdji.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ucy.rosdji.R;
import com.ucy.rosdji.dji.VideoFeedView;
import com.ucy.rosdji.dji.VirtualFlightController;
import com.ucy.rosdji.ros.ListenerFlightController;
import com.ucy.rosdji.ros.TalkerHeight;
import com.ucy.rosdji.ros.TalkerVelocity;

import org.ros.android.MessageCallable;
import org.ros.android.RosActivity;
import org.ros.android.view.RosTextView;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import dji.sdk.camera.VideoFeeder;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LiveStreamManager;
import std_msgs.String;

public class MainActivityROS extends RosActivity {

    private RosTextView<std_msgs.String> rosTextView;
    private TalkerHeight talkerHeight;
    private TalkerVelocity talkerVelocity;
    private ListenerFlightController listenerFlightController;
    private Gimbal gimbal;
    private java.lang.String serialNumber;
    private ToggleButton toggleButtonVirtualControl;


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

        gimbal= DJISDKManager.getInstance().getProduct().getGimbal();

        VideoFeedView primaryVideoFeedView = (VideoFeedView) findViewById(R.id.video_view_primary_video_feed);
        primaryVideoFeedView.registerLiveVideo(VideoFeeder.getInstance().getPrimaryVideoFeed(), true);
        listener = new LiveStreamManager.OnLiveChangeListener() {
            @Override
            public void onStatusChanged(int i) {
                Toast.makeText(getApplicationContext(), "status changed: " + i, Toast.LENGTH_SHORT).show();
            }
        };

        toggleButtonVirtualControl= findViewById(R.id.toggleButtonToggleVirtualControl);
        toggleButtonVirtualControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    listenerFlightController.enableVirtualCommands(MainActivityROS.this);
                }else{
                    listenerFlightController.disableVirtualCommands(MainActivityROS.this);
                }
            }
        });
    }

    public void startVirtualLanding(View view){
        listenerFlightController.sendVirtualCommandLanding();
    }

    public void startVirtualTakeoff(View view){
        listenerFlightController.sendVirtualCommandTakeoff();
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());

        listenerFlightController =new ListenerFlightController();
        nodeMainExecutor.execute(listenerFlightController, nodeConfiguration);

        talkerHeight = new TalkerHeight();
        nodeMainExecutor.execute(talkerHeight,nodeConfiguration);

        talkerVelocity = new TalkerVelocity();
        nodeMainExecutor.execute(talkerVelocity,nodeConfiguration);

        nodeMainExecutor.execute(rosTextView, nodeConfiguration);

    }


    public void setPitchAngleFromEditText(View view){
        EditText editTextPitchAngle =  findViewById(R.id.editTextPitchAngle);
        java.lang.String pitchAngleString = editTextPitchAngle.getText().toString();
        if (!pitchAngleString.isEmpty()) {
            float pitchAngle = Float.parseFloat(pitchAngleString);
            listenerFlightController.setPitchAngle(pitchAngle,MainActivityROS.this);
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
        DJISDKManager.getInstance().getLiveStreamManager().setAudioMuted(true);
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

        }else {
            Toast.makeText(getApplicationContext(), "LiveStream failed to start", Toast.LENGTH_SHORT).show();
        }
    }
    
    public void stopLiveStream(View view) {
        DJISDKManager.getInstance().getLiveStreamManager().stopStream();
        Button button = findViewById(R.id.buttonStartLiveStream);
        Button button2 = findViewById(R.id.buttonStopLiveStream);
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
}