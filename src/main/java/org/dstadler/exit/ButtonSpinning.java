package org.dstadler.exit;

import com.google.common.collect.ImmutableMap;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

import java.util.Map;

public class ButtonSpinning {
    // See https://www.dstadler.org/dswiki/index.php?title=PiRadio
    private static final Map<Integer, Integer> BUTTON_MAP = ImmutableMap.<Integer,Integer>builder().
//            put(0, 0).
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

        TM1638 tm1638 = new TM1638(gpio, RaspiPin.GPIO_00, RaspiPin.GPIO_02, RaspiPin.GPIO_03);
        tm1638.enable();

        int[] segments = new int[8];
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
                buttons_prev = buttons;

                // this avoids multi-button states and "-1" which we sometimes see
                Integer button = BUTTON_MAP.get(buttons);
                if (button != null) {
                    // 2 buttons to increase/decrease
                    int segment = button/2;

                    // one button increases, the other decreases
                    if (segment * 2 == button) {
                        if (segments[segment] == 0) {
                            segments[segment] = 1;
                        }
                        if (segments[segment] != 64) {
                            // we have 7 steps
                            segments[segment] = segments[segment] << 1;
                        }
                    } else {
                        // when reaching zero we don't reduce any further
                        segments[segment] = segments[segment] >> 1;
                    }

                    tm1638.send_char(segment, segments[segment]);
                }
            }

            Thread.sleep(100);
        }
    }
}
