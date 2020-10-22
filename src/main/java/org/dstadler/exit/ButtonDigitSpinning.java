package org.dstadler.exit;

import com.google.common.collect.ImmutableMap;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.trigger.GpioSyncStateTrigger;

import java.util.Map;

public class ButtonDigitSpinning {
    // See https://www.dstadler.org/dswiki/index.php?title=PiRadio
    private static final Map<Integer, Integer> BUTTON_MAP = ImmutableMap.<Integer,Integer>builder().
            put(4, 0).
            put(64, 1).
            put(1024, 2).
            put(16384, 3).
            put(262144, 4).
            put(4194304, 5).
            put(67108864, 6).
            put(1073741824, 7).
            put(2, 8).
            put(32, 9).
            put(512, 10).
            put(8192, 11).
            put(131072, 12).
            put(2097152, 13).
            put(33554432, 14).
            put(536870912, 15).
            build();

    public static void main(String[] args) throws Exception {
        System.out.println("Setting up GPIO input events and TM1638 device");

        // create gpio controller instance
        final GpioController gpio = GpioFactory.getInstance();

        GpioPinDigitalOutput led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_21, "LED");

        GpioPinDigitalInput onOffButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_23,
                "Button 1|0",PinPullResistance.PULL_DOWN);

        // create a gpio synchronization trigger on the input pin
        // when the input state changes, also set LED controlling gpio pin to same state
        onOffButton.addTrigger(new GpioSyncStateTrigger(led));

        onOffButton.addListener(new GpioUsageListener());

        TM1638 tm1638 = new TM1638(gpio, RaspiPin.GPIO_00, RaspiPin.GPIO_02, RaspiPin.GPIO_03);
        tm1638.enable();

        char[] text = "00000000".toCharArray();
        //tm1638.set_text("00000000");

        int buttons_prev = -1;
        System.out.println("Setup finished, waiting for input-events or CTRL-C");
        PinState onOffButtonState = PinState.LOW;

        // wait for CTRL-C
        while (true) {
            int buttons = tm1638.get_buttons64();
            if(buttons != 0) {
                System.out.println("Buttons: " + buttons);
            }

            // set display whenever buttons change
            if(buttons != buttons_prev) {
                buttons_prev = buttons;

                // this avoids multi-button states and "-1" which we sometimes see
                Integer button = BUTTON_MAP.get(buttons);
                if (button != null) {
                    // 2 buttons to increase/decrease
                    int segment = button/2;

                    // one button increases, the other decreases
                    if (segment * 2 != button) {
                        if (text[segment] != '9') {
                            int digit = text[segment] - '0';
                            text[segment] = Character.forDigit(digit+1, 10);
                        }
                    } else {
                        if (text[segment] != '0') {
                            int digit = text[segment] - '0';
                            text[segment] = Character.forDigit(digit-1, 10);
                        }
                    }

                    String textStr = new String(text);
                    System.out.println("Having " + segment + " and " + textStr);
                    tm1638.set_text(textStr);
                }
            }

            if(onOffButtonState != onOffButton.getState()) {
                System.out.println("Button state changed for 1|0: " + onOffButton.getState());
                onOffButtonState = onOffButton.getState();
            }

            Thread.sleep(100);
        }
    }

    public static class GpioUsageListener implements GpioPinListenerDigital {
        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            // display pin state on console
            System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = "
                    + event.getState());
        }
    }
}
