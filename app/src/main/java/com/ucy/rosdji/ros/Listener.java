package com.ucy.rosdji.ros;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

//import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

import std_msgs.String;

public class Listener extends AbstractNodeMain {

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("/listener");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        //final Log log = connectedNode.getLog();
        Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber("chatter", String._TYPE);
        subscriber.addMessageListener(new MessageListener<std_msgs.String>() {
            @Override
            public void onNewMessage(std_msgs.String message) {
                //log.info("I heard: \"" + message.getData() + "\"");
                Log.d("ROSlistener", message.getData());

            }
        });
    }
}