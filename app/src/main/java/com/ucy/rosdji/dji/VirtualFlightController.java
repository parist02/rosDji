package com.ucy.rosdji.dji;

import android.widget.Toast;

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

public class VirtualFlightController {

    private FlightController flightController;
    private Thread threadForLanding;
    private String serialNumber;

    public VirtualFlightController() {
        flightController = ((Aircraft) DJISDKManager.getInstance().getProduct()).getFlightController();
        flightController.getSerialNumber(new CommonCallbacks.CompletionCallbackWith<java.lang.String>() {
            @Override
            public void onSuccess(String s) {
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
        threadForLanding = new Thread() {
            @Override
            public void run() {
//                super.run();
                flightController.startLanding(djiError -> {
                });
                while (!flightController.getState().isLandingConfirmationNeeded()) {
                    try {
                        sleep(200);
                    } catch (InterruptedException ignored) {
                    }
                }
                flightController.confirmLanding(djiError -> {
                });
            }
        };
    }



    public void disableVirtualCommands(){
        flightController.setVirtualStickModeEnabled(false, djiError -> {
        });
    }

    public void enableVirtualCommands(){
        flightController.setVirtualStickModeEnabled(true, djiError -> {
        });
    }

    public void sendVirtualCommands(int [] commands){
        float roll,pitch,yaw,throttle;
        switch (commands[0]){
            case 1:
                pitch = 5;
                break;
            case 2:
                pitch = -5;
                break;
            default:
                pitch =0;
        }
        switch (commands[1]){
            case 2:
                roll = 5;
                break;
            case 1:
                roll = -5;
                break;
            default:
                roll =0;
        }
        switch (commands[2]){
            case 1:
                throttle = 2;
                break;
            case 2:
                throttle = -2;
                break;
            default:
                throttle =0;
        }
        switch (commands[3]){
            case 2:
                yaw = 25;
                break;
            case 1:
                yaw = -25;
                break;
            default:
                yaw =0;
        }
        flightController.sendVirtualStickFlightControlData(new FlightControlData(roll, pitch, yaw, throttle), djiError -> {});
    }

    public void sendVirtualCommandLanding(int startLanding){
        if (startLanding == 1) {
            threadForLanding.start();
        }
    }
}
