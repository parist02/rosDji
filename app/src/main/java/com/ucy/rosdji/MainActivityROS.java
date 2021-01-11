package com.ucy.rosdji;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
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
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
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
        Rotation.Builder rotationBuilder=new Rotation.Builder().mode(RotationMode.ABSOLUTE_ANGLE).time(2);
        rotationBuilder.pitch(45);
        gimbal.rotate(rotationBuilder.build(), djiError -> {

        });
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

    }



}