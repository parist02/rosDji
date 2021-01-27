package com.ucy.rosdji.dji;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ucy.rosdji.R;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.gimbal.XPortState.GimbalBalanceDetectionState;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

public class VirtualFlightController {

    private FlightController flightController;
    private Gimbal gimbal;
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
        internalDisableVirtualCommands();
        gimbal= DJISDKManager.getInstance().getProduct().getGimbal();
    }

    public void setPitchAngle(float pitchAngle,Context context){
        try {
            Rotation.Builder rotationBuilder = new Rotation.Builder().mode(RotationMode.ABSOLUTE_ANGLE).time(2);
            rotationBuilder.pitch(pitchAngle);
            gimbal.rotate(rotationBuilder.build(), new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    DialogUtils.showDialogBasedOnError(context, djiError);
                }
            });
        }catch (NullPointerException exception){
            DialogUtils.showDialog(context, exception.getLocalizedMessage());
        }
    }


    private void internalDisableVirtualCommands(){
        //internal command to initialize virtual stick mode to false
        flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
            }
        });
    }

    public void disableVirtualCommands(Context context){
        flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                DialogUtils.showDialogBasedOnError(context, djiError);
            }
        });
    }

    public void enableVirtualCommands(Context context){
        flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                DialogUtils.showDialogBasedOnError(context,djiError);
            }
        });
    }

    public void sendVirtualCommands(int [] commands){
        float roll,pitch,yaw,throttle;
        switch (commands[0]){
            case 1:
                pitch = (float)0.3;
                break;
            case 2:
                pitch =(float) -0.3;
                break;
            default:
                pitch =0;
        }
        switch (commands[1]){
            case 2:
                roll = (float)0.3;
                break;
            case 1:
                roll = (float)-0.3;
                break;
            default:
                roll =0;
        }
        switch (commands[2]){
            case 1:
                throttle = 1;
                break;
            case 2:
                throttle = -1;
                break;
            default:
                throttle =0;
        }
        switch (commands[3]){
            case 2:
                yaw = 20;
                break;
            case 1:
                yaw = -20;
                break;
            default:
                yaw =0;
        }
        flightController.sendVirtualStickFlightControlData(new FlightControlData(roll, pitch, yaw, throttle), djiError -> {});
    }

    public void sendVirtualCommandTakeOff(){
        flightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
    }

    public void sendVirtualCommandLanding(int startLanding){
        if (startLanding == 1) {
            new Thread() {
                @Override
                public void run() {
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
            }.start();
        }
    }
}
