package org.dstadler.exit;

import com.google.common.collect.ImmutableMap;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import org.dstadler.exit.util.TM1638;

import java.util.Map;

public class ButtonSpinning {
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

                    System.out.println("Having " + segment + " and " + segments[segment]);
                    tm1638.send_char(segments[segment], 128 >> segment - 1);
                }
            }

            Thread.sleep(100);
        }
    }
}
