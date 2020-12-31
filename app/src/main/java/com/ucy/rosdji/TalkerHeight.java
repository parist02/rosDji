package com.ucy.rosdji;

import android.util.Log;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

import java.io.StringReader;

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

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("/talkerHeightApp");
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
        Aircraft aircraft = (Aircraft) DJISDKManager.getInstance().getProduct();
        FlightController flightController = aircraft.getFlightController();
        flightController.getSerialNumber(new CommonCallbacks.CompletionCallbackWith<java.lang.String>() {
            @Override
            public void onSuccess(java.lang.String s) {

            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });
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
                FlightControllerState flightControllerState = flightController.getState();
                //TODO: remove the if
                if (flightControllerState.isUltrasonicBeingUsed()){
                    droneHeight = flightControllerState.getUltrasonicHeightInMeters();
                    msg = "Drone Height1: = "+droneHeight;
                    Log.d("ROSdji: ", "UltrasonicHeightInMeters = " + droneHeight);
                }else{
                    //this is wrong
                    //TODO: get the height with the use of the barometer that is correct
                    //TODO: flightControllerState.getAircraftLocation().getAltitude()
                    droneHeight = flightControllerState.getTakeoffLocationAltitude();
                    msg = "Drone Height2: = "+droneHeight;
                    Log.d("ROSdji: ", "TakeoffLocationAltitude = " + droneHeight);
                }
                std_msgs.String droneHeightMessage = publisher.newMessage();
                droneHeightMessage.setData(msg);
                publisher.publish(droneHeightMessage);
                Thread.sleep(1000); //change according to the timing of the calculations
            }
        });
    }
}