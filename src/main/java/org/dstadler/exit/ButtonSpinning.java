package org.dstadler.exit;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import org.dstadler.exit.util.TM1638;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"InfiniteLoopStatement", "BusyWait"})
public class ButtonSpinning {
    // See https://www.dstadler.org/dswiki/index.php?title=PiRadio
    private static final Map<Integer, Integer> BUTTON_MAP = new HashMap<>();
    static {
        BUTTON_MAP.put(4, 0);
        BUTTON_MAP.put(64, 1);
        BUTTON_MAP.put(1024, 2);
        BUTTON_MAP.put(16384, 3);
        BUTTON_MAP.put(262144, 4);
        BUTTON_MAP.put(4194304, 5);
        BUTTON_MAP.put(67108864, 6);
        BUTTON_MAP.put(1073741824, 7);
        BUTTON_MAP.put(2, 8);
        BUTTON_MAP.put(32, 9);
        BUTTON_MAP.put(512, 10);
        BUTTON_MAP.put(8192, 11);
        BUTTON_MAP.put(131072, 12);
        BUTTON_MAP.put(2097152, 13);
        BUTTON_MAP.put(33554432, 14);
        BUTTON_MAP.put(536870912, 15);
    }

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
