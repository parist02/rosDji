package com.ucy.rosdji;

import android.os.Bundle;

import org.ros.android.MessageCallable;
import org.ros.android.RosActivity;
import org.ros.android.view.RosTextView;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import std_msgs.String;

public class MainActivityROS extends RosActivity {

    private RosTextView<std_msgs.String> rosTextView;
    private TalkerHeight talkerHeight;
    private Listener listener;

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
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());

        //talker = new Talker();
        //nodeMainExecutor.execute(talker,nodeConfiguration);

        //listener = new Listener();
        //nodeMainExecutor.execute(listener,nodeConfiguration);

        talkerHeight = new TalkerHeight();
        nodeMainExecutor.execute(talkerHeight,nodeConfiguration);

        nodeMainExecutor.execute(rosTextView, nodeConfiguration);

    }
}