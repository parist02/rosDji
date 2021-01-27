package com.ucy.rosdji.ros;

import android.util.Log;

import com.ucy.rosdji.dji.VirtualFlightController;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

import java.io.StringReader;
import java.text.DecimalFormat;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.util.CommonCallbacks;
import dji.internal.camera.P;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import std_msgs.Float32;
import std_msgs.String;

public class TalkerHeight extends AbstractNodeMain {
    private DecimalFormat decimalFormat;

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("/talkerHeightApp");
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
        decimalFormat = new DecimalFormat("0.00");
        decimalFormat.setMaximumFractionDigits(2);
        final Publisher<std_msgs.String> publisher = connectedNode.newPublisher("drone_height", String._TYPE);
        // This CancellableLoop will be canceled automatically when the node shuts down.
        connectedNode.executeCancellableLoop(new CancellableLoop() {
            private float droneHeight;
            private java.lang.String msg;

            @Override
            protected void setup() {
                droneHeight = (float) 0.0;
                msg = "Drone Height: = "+droneHeight;
            }

            @Override
            protected void loop() throws InterruptedException {
                droneHeight = ((Aircraft)DJISDKManager.getInstance().getProduct()).getFlightController().getState().getUltrasonicHeightInMeters();
                msg = "Drone Height: = "+decimalFormat.format(droneHeight);
                std_msgs.String droneHeightMessage = publisher.newMessage();
                droneHeightMessage.setData(msg);
                publisher.publish(droneHeightMessage);
                Thread.sleep(1000); //change according to the timing of the calculations
            }
        });
    }
}