package com.ucy.rosdji.ros;

import com.ucy.rosdji.dji.VirtualFlightController;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import java.text.DecimalFormat;

import dji.common.flightcontroller.FlightControllerState;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import std_msgs.Float32MultiArray;

public class TalkerVelocity extends AbstractNodeMain {

    private DecimalFormat decimalFormat;
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("/talkerVelocitytApp");
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
        decimalFormat = new DecimalFormat("0.00");
        decimalFormat.setMaximumFractionDigits(2);
        final Publisher<Float32MultiArray> publisher = connectedNode.newPublisher("drone_velocity", Float32MultiArray._TYPE);
        // This CancellableLoop will be canceled automatically when the node shuts down.
        connectedNode.executeCancellableLoop(new CancellableLoop() {
            private float [] velocity = new float[3];;

            @Override
            protected void setup() {
                velocity[0]=0;
                velocity[1]=0;
                velocity[2]=0;
            }

            @Override
            protected void loop() throws InterruptedException {
                FlightControllerState flightControllerState = ((Aircraft) DJISDKManager.getInstance().getProduct()).getFlightController().getState();
                velocity[0]=flightControllerState.getVelocityX();
                velocity[1]=flightControllerState.getVelocityY();;
                velocity[2]=flightControllerState.getVelocityZ();;
                Float32MultiArray float32MultiArrayVelocity = publisher.newMessage();
                float32MultiArrayVelocity.setData(velocity);
                publisher.publish(float32MultiArrayVelocity);
                Thread.sleep(1000); //change according to the timing of the calculations
            }
        });
    }
}
