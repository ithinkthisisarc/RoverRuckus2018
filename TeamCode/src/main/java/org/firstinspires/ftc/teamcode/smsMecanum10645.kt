package org.firstinspires.ftc.teamcode

/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import android.graphics.Color

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.NormalizedColorSensor
import com.qualcomm.robotcore.hardware.NormalizedRGBA
import com.qualcomm.robotcore.hardware.SwitchableLight
import com.qualcomm.robotcore.util.Range
import com.qualcomm.robotcore.hardware.DcMotor

@TeleOp(name = "10645", group = "Pushbot")
class smsMecanum10645 : LinearOpMode() {

    /* Declare OpMode members. */
    internal var robot = smsHardware()   // Use a Pushbot's hardware

    internal var hsvValues = FloatArray(3)
    internal val values = hsvValues
    internal var amPos: Int = 0
    internal var aePos: Int = 0
    internal var aeOffset: Int = 0
    internal var previousDPD = false
    internal var previousDPU = false
    internal var previousDPL = false
    internal var previousDPR = false
    internal var armNominalPower = 0.3f
    internal var driveNominalPower = 0.3f

    @Override
    fun runOpMode() {
        var powerReducer = driveNominalPower

        robot.init(hardwareMap, false)

        // Send telemetry message to signify robot waiting;
        telemetry.addData("Say", "Hello Driver")    //
        telemetry.update()

        // Wait for the game to start (driver presses PLAY)

        aeOffset = 0
        waitForStart()


        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            var gamepad1LeftY = -gamepad1.left_stick_y
            var gamepad1LeftX = gamepad1.left_stick_x
            var gamepad1RightY = -gamepad1.right_stick_y
            var gamepad1RightX = gamepad1.right_stick_x

            val gamepad2LeftY = gamepad2.left_stick_y
            val gamepad2RightY = -gamepad2.right_stick_y

            val gamepad2RightTrigger = gamepad2.right_trigger
            val gamepad2LeftTrigger = gamepad2.left_trigger


            if (Math.abs(gamepad1LeftY) < 0.2) {
                gamepad1LeftY = 0f
            }
            if (Math.abs(gamepad1LeftX) < 0.2) {
                gamepad1LeftX = 0f
            }
            if (Math.abs(gamepad1RightX) < 0.2) {
                gamepad1RightX = 0f
            }
            if (Math.abs(gamepad1RightY) < 0.2) {
                gamepad1RightY = 0f
            }

            // Allow driver to select Tank vs POV by pressing START
            var dpad_check = gamepad2.dpad_up
            if (dpad_check && dpad_check != previousDPU) {
                aeOffset += 25
            }
            previousDPU = dpad_check

            dpad_check = gamepad2.dpad_down
            if (dpad_check && dpad_check != previousDPD) {
                aeOffset -= 25
            }
            previousDPD = dpad_check

            dpad_check = gamepad2.dpad_left
            if (dpad_check && dpad_check != previousDPL) {
                armNominalPower -= 0.05f
            }
            previousDPL = dpad_check

            dpad_check = gamepad2.dpad_right
            if (dpad_check && dpad_check != previousDPR) {
                armNominalPower += 0.05f
            }
            previousDPR = dpad_check


            var FrontLeft = 0f
            var BackLeft = 0f
            var FrontRight = 0f
            var BackRight = 0f

            // y-axis motion
            if (Math.abs(gamepad1LeftY) > Math.abs(gamepad1RightX) && Math.abs(gamepad1LeftY) > Math.abs(gamepad1LeftX)) {//Activates if y is largest {
                FrontLeft = gamepad1LeftY
                FrontRight = gamepad1LeftY
                BackLeft = gamepad1LeftY
                BackRight = gamepad1LeftY
            } else if (Math.abs(gamepad1RightX) > Math.abs(gamepad1LeftY) && Math.abs(gamepad1RightX) > Math.abs(gamepad1LeftX)) {//Activates if x is largest {
                FrontLeft = gamepad1RightX * -1
                FrontRight = gamepad1RightX
                BackLeft = gamepad1RightX
                BackRight = gamepad1RightX * -1
            } else if (Math.abs(gamepad1LeftX) > Math.abs(gamepad1LeftY) && Math.abs(gamepad1LeftX) > Math.abs(gamepad1RightX)) {
                FrontLeft = gamepad1LeftX
                FrontRight = gamepad1LeftX * -1
                BackLeft = gamepad1LeftX
                BackRight = gamepad1LeftX * -1


            } else {
                FrontLeft = 0.toFloat()
                BackLeft = 0.toFloat()
                FrontRight = 0.toFloat()
                BackRight = 0.toFloat()
            }// Otherwise sticks are not pushed
            // gamepad1LeftX-axis motion
            // x-axis motion

            if (gamepad1.right_trigger > 0) {
                powerReducer = 1.0f
            } else {
                powerReducer = driveNominalPower
            }

            // clip the right/left values so that the values never exceed +/- 1
            FrontRight = Range.clip(FrontRight, -1, 1) * powerReducer
            FrontLeft = Range.clip(FrontLeft, -1, 1) * powerReducer
            BackLeft = Range.clip(BackLeft, -1, 1) * powerReducer
            BackRight = Range.clip(BackRight, -1, 1) * powerReducer


            // write the values to the motors
            if (robot.frontRightDrive != null) robot.frontRightDrive.setPower(FrontRight)
            if (robot.frontLeftDrive != null) robot.frontLeftDrive.setPower(FrontLeft)
            if (robot.rearRightDrive != null) robot.rearRightDrive.setPower(BackRight)
            if (robot.rearLeftDrive != null) robot.rearLeftDrive.setPower(BackLeft)

            //if (robot.armMove != null) robot.armMove.setPower(armMove);
            //robot.armMove.setPower(armUpDown);
            //robot.armExtend.setPower(armEx);

            var armMove = Range.clip(gamepad2LeftY, -1, 1) * armNominalPower  // cap the arm-move to 50% but without clipping
            val armEx = Range.clip(gamepad2RightY, -1, 1) * 0.2f   // cap the arm-extend to 20% but without clipping

            if (robot.collector != null) {
                if (gamepad2LeftTrigger > 0f) {
                    robot.collector.setPower(1)
                } else if (gamepad2RightTrigger > 0f) {
                    robot.collector.setPower(-1)
                } else {
                    robot.collector.setPower(0)
                }
            }


            // idea 1 - clip amPos when aligning the collector to avoid it moving back up
            // idea 2 - ALWAYS move using run_to_position - by doing add 100 for positive, -100 for negative.   setPower to math.abs(armMove)

            if (robot.armMove != null) {
                robot.armMove.setMode(DcMotor.RunMode.RUN_TO_POSITION)
                if (gamepad2.y) {
                    amPos = 2500
                    armMove = armNominalPower
                } else {
                    amPos = robot.armMove.getCurrentPosition() + armMove / Math.abs(armMove) * 100
                    amPos = Range.clip(amPos, 0, 7425)
                }

                if (gamepad2.a) {
                    amPos = 0
                    armMove = 1.0.toInt().toFloat()
                }

                robot.armMove.setTargetPosition(amPos)
                robot.armMove.setPower(Math.abs(armMove))

                //                    robot.armMove.setTargetPosition(robot.armMove.getCurrentPosition)

                //                if (gamepad2.y) {
                //robot.armMove.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                //                    robot.armMove.setTargetPosition(2500);
                //                    robot.armMove.setPower(armNominalPower);
                //                } else {
                //                    if (robot.armMove.isBusy()) {

                //                    } else {

                //                        robot.armMove.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                //                        robot.armMove.setPower(armMove);
                //                    }
                //                }
                amPos = robot.armMove.getCurrentPosition()
            }




            if (robot.armExtend != null) {
                robot.armExtend.setMode(DcMotor.RunMode.RUN_TO_POSITION)
                // Keep aligned with main arm Position (amPos)
                aePos = (aeOffset + Range.clip(amPos, 0, 5500) / 7.11) as Int
                // Dump
                if (gamepad2.b) {
                    aePos += 1000
                }
                robot.armExtend.setTargetPosition(aePos) // based on a double 15:40 tooth reduction setup
                robot.armExtend.setPower(0.2f)
                //                robot.armExtend.setPower(armEx);
                aePos = robot.armExtend.getCurrentPosition()

            }



            telemetry.addLine()
                    .addData("front right", FrontRight)
                    .addData("front left", FrontLeft)
                    .addData("back left", BackLeft)
                    .addData("back right", BackRight)
                    .addData("armExtend ", aePos)
                    .addData("armExtendPower", armEx)
                    .addData("armMove ", amPos)
                    .addData("armMovePower", armMove)

            telemetry.update()

        }
    }
}