package org.dstadler.exit;

import com.google.common.collect.ImmutableMap;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.Map;

public class Example {
    // See https://www.dstadler.org/dswiki/index.php?title=PiRadio
    private static final Map<Integer, Integer> BUTTON_MAP = ImmutableMap.<Integer,Integer>builder().
            put(0, 0).
            put(4, 1).
            put(64, 2).
            put(1024, 3).
            put(16384, 4).
            put(262144, 5).
            put(4194304, 6).
            put(67108864, 7).
            put(1073741824, 8).
            put(2, 9).
            put(32, 10).
            put(512, 11).
            put(8192, 12).
            put(131072, 13).
            put(2097152, 14).
            put(33554432, 15).
            put(536870912, 16).
            build();

    public static void main(String[] args) throws Exception {
        System.out.println("Setting up GPIO input events and TM1638 device");

        // create gpio controller instance
        final GpioController gpio = GpioFactory.getInstance();

        for (Pin pin : RaspiPin.allPins(PinMode.DIGITAL_INPUT)) {
            if(pin.supportsPinPullResistance() && pin.getSupportedPinPullResistance().contains(PinPullResistance.PULL_DOWN) &&

			// the two pins are labelled "CE0" and "CE1" in the pin-layout chart from "gpio readall"
            // they do not work as input
			pin.getAddress() != 10 && pin.getAddress() != 11 &&

            // buzzer
            pin.getAddress() != 26 &&

			// don't block ports used for the TM1638 device below
			pin.getAddress() != 0 && pin.getAddress() != 2 && pin.getAddress() != 3) {
                GpioPinDigitalInput button = gpio.provisionDigitalInputPin(pin,
                        "Pin" + pin.getName(),
                        PinPullResistance.PULL_DOWN);

                // create and register gpio pin listener
                button.addListener(new GpioUsageExampleListener());
            }
        }

        GpioPinDigitalOutput buzzer = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_26, "Buzzer");
        GpioPinDigitalOutput led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_21, "LED");

        TM1638 tm1638 = new TM1638(gpio, RaspiPin.GPIO_00, RaspiPin.GPIO_02, RaspiPin.GPIO_03);
        tm1638.enable();

        int buttons_prev = -1;
        System.out.println("Setup finished, waiting for input-events or CTRL-C");
        // wait for CTRL-C
        int i = 0;
        while (true) {
            int buttons = tm1638.get_buttons64();
            if(buttons != 0) {
                System.out.println("Buttons: " + buttons);
            }

            // set display whenever buttons change
            if(buttons != buttons_prev) {
                tm1638.set_text(Integer.toHexString(buttons));
                buttons_prev = buttons;
            }

            if (i % 10 == 0) {
                buzzer.setState(i % 20 == 0);
                led.setState(i % 20 == 0);
            }
            i++;

            Thread.sleep(100);
        }
    }

    public static class GpioUsageExampleListener implements GpioPinListenerDigital {
        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            // display pin state on console
            System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = "
                    + event.getState());
        }
    }
}
