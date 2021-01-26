package com.ucy.rosdji.ros;


import com.ucy.rosdji.dji.VirtualFlightController;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;
import std_msgs.Int32MultiArray;

public class ListenerFlightController extends AbstractNodeMain {
    private java.lang.String serialNumber;
    private VirtualFlightController virtualFlightController;

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("/listenerFlightController");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        virtualFlightController = new VirtualFlightController();
        Subscriber<std_msgs.Int32MultiArray> subscriber = connectedNode.newSubscriber("drone_movement", Int32MultiArray._TYPE);
        subscriber.addMessageListener(new MessageListener<Int32MultiArray>() {
            @Override
            public void onNewMessage(Int32MultiArray int32MultiArray) {
                virtualFlightController.sendVirtualCommands(int32MultiArray.getData());
                virtualFlightController.sendVirtualCommandLanding(int32MultiArray.getData()[4]);
            }
        });
    }

    public void disableVirtualCommands(){
        virtualFlightController.disableVirtualCommands();
    }

    public void enableVirtualCommands(){
        virtualFlightController.enableVirtualCommands();
    }

    public void sendVirtualCommandLanding(){
        virtualFlightController.sendVirtualCommandLanding(1);
    }

    public String getSerialNumber() {
        return serialNumber;
    }

}