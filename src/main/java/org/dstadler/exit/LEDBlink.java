package org.dstadler.exit;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.RaspiPin;

import java.util.ArrayList;
import java.util.List;

public class LEDBlink {
    public static void main(String[] args) throws Exception {
        System.out.println("Setting up GPIO LED Blinking");

        // create gpio controller instance
        final GpioController gpio = GpioFactory.getInstance();

        List<GpioPinDigitalOutput> leds = new ArrayList<>();
        for (Pin pin : RaspiPin.allPins(PinMode.DIGITAL_OUTPUT)) {
            if (
                        // the two pins are labelled "CE0" and "CE1" in the pin-layout chart from "gpio readall"
                        // they do not work as input
			pin.getAddress() != 10 && pin.getAddress() != 11) {
                leds.add(gpio.provisionDigitalOutputPin(pin, "Pin-" + pin.getAddress()));
            }
        }

        System.out.println("Setup finished, waiting for CTRL-C");
        // wait for CTRL-C
        int i = 0;
        int led = 0;
        while (true) {
            if (i % 10 == 0) {
                System.out.println("LED-" + led + ": " + (led/2) + "(" + leds.get(led/2) + "): " + (i % 20 == 0));
                leds.get(led/2).setState(i % 20 == 0);
                led = (led + 1) % (leds.size()*2);
            }

            i++;

            Thread.sleep(100);
        }
    }
}
