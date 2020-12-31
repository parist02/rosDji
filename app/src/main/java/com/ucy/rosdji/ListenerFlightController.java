package com.ucy.rosdji;

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

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import std_msgs.Float32MultiArray;
import std_msgs.Int32MultiArray;
import std_msgs.String;

public class ListenerFlightController extends AbstractNodeMain {
    private Aircraft aircraft;
    private FlightController flightController;
    private java.lang.String serialNumber;

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("/listenerFlightController");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        aircraft = (Aircraft) DJISDKManager.getInstance().getProduct();
        flightController = aircraft.getFlightController();
        flightController.getSerialNumber(new CommonCallbacks.CompletionCallbackWith<java.lang.String>() {
            @Override
            public void onSuccess(java.lang.String s) {
                serialNumber = s;
            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });
        flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
        flightController.setVirtualStickModeEnabled(true, djiError -> {});
        Subscriber<std_msgs.Int32MultiArray> subscriber = connectedNode.newSubscriber("drone_movement", Int32MultiArray._TYPE);
        subscriber.addMessageListener(new MessageListener<Int32MultiArray>() {
            @Override
            public void onNewMessage(Int32MultiArray int32MultiArray) {
                sendDroneCommands(int32MultiArray.getData());
            }
        });



//        Subscriber<std_msgs.Float32MultiArray> subscriber = connectedNode.newSubscriber("drone_movement", Float32MultiArray._TYPE);
//        subscriber.addMessageListener(new MessageListener<Float32MultiArray>() {
//            @Override
//            public void onNewMessage(Float32MultiArray float32MultiArray) {
//                sendDroneCommands(float32MultiArray.getData());
//            }
//        });



        //final Log log = connectedNode.getLog();
//        Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber("chatter", String._TYPE);
//        subscriber.addMessageListener(new MessageListener<std_msgs.String>() {
//            @Override
//            public void onNewMessage(std_msgs.String message) {
//                //log.info("I heard: \"" + message.getData() + "\"");
//                Log.d("ROSlistener", message.getData());
//
//            }
//        });
    }

    private void sendDroneCommands(int [] commands){
        float roll,pitch,yaw,throttle;
        switch (commands[0]){
            case 1:
                pitch = 10;
                break;
            case -1:
                pitch = -10;
                break;
            default:
                pitch =0;
        }
        switch (commands[1]){
            case 1:
                roll = 10;
                break;
            case -1:
                roll = -10;
                break;
            default:
                roll =0;
        }
        switch (commands[2]){
            case 1:
                throttle = 2;
                break;
            case -1:
                throttle = -2;
                break;
            default:
                throttle =0;
        }
        switch (commands[3]){
            case 1:
                yaw = 15;
                break;
            case -1:
                yaw = -15;
                break;
            default:
                yaw =0;
        }
        flightController.sendVirtualStickFlightControlData(new FlightControlData(roll, pitch, yaw, throttle), djiError -> {});
        if (commands[4] == 1) {
            droneLand();
        }
    }

    private void droneLand(){
        flightController.startLanding(djiError -> {});
        while(!flightController.getState().isLandingConfirmationNeeded()){
            try {
                Thread.sleep(200);
            }catch (InterruptedException ignored){}
        }
        flightController.confirmLanding(djiError -> {});
    }

}