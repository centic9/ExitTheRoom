package org.dstadler.exit;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import java.io.IOException;
import java.util.logging.Logger;

public class ExitTheRoom {
    private static final Logger log = LoggerFactory.make();

    //boolean buttonState[]

    public static void main(String[] args) throws IOException {
        LoggerFactory.initLogging();

        log.info("Setting up GPIO input events and TM1638 device");

        // create gpio controller instance
        final GpioController gpio = GpioFactory.getInstance();

        GpioPinDigitalInput button10 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_23,
                "1|0", PinPullResistance.PULL_DOWN);
        GpioPinDigitalInput buttonSwitch = gpio.provisionDigitalInputPin(RaspiPin.GPIO_25,
                "1|0", PinPullResistance.PULL_DOWN);
        GpioPinDigitalInput button24V = gpio.provisionDigitalInputPin(RaspiPin.GPIO_24,
                "1|0", PinPullResistance.PULL_DOWN);

        /*GpioPinDigitalOutput buzzer = gpio.provisionPwmOutputPin()DigitalInputPin(RaspiPin.GPIO_26,
                "1|0", PinPullResistance.PULL_DOWN);*/


        // start up GPIO

        // define objects for buttons, TM1638 and buzzer

        // add listeners where possible to avoid polling on everything

        // main loop

        //
    }
}
