package org.dstadler.exit;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import org.dstadler.exit.util.TM1638;

public class Dots {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Setting up GPIO input events and TM1638 device");

        // create gpio controller instance
        final GpioController gpio = GpioFactory.getInstance();

        TM1638 tm1638 = new TM1638(gpio, RaspiPin.GPIO_00, RaspiPin.GPIO_02, RaspiPin.GPIO_03);
        tm1638.enable();

        for(int i = 0;i < 256;i++) {
            System.out.println("i: " + i);
            tm1638.send_char(7, i);

            Thread.sleep(1000);
        }
    }
}
