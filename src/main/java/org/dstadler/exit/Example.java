package org.dstadler.exit;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class Example {
    public static void main(String[] args) {
        // create gpio controller instance
        final GpioController gpio = GpioFactory.getInstance();

        /*

        // provision gpio pins #04 as an output pin and make sure is is set to LOW at startup
        GpioPinDigitalOutput myLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04,   // PIN NUMBER
               "My LED",           // PIN FRIENDLY NAME (optional)
               PinState.LOW);      // PIN STARTUP STATE (optional)
        // explicitly set a state on the pin object
        myLed.setState(PinState.HIGH);


        // use convenience wrapper method to set state on the pin object
        myLed.low();
        myLed.high();

        // use toggle method to apply inverse state on the pin object
        myLed.toggle();

        // use pulse method to set the pin to the HIGH state for
        // an explicit length of time in milliseconds
        myLed.pulse(1000);


        // configure the pin shutdown behavior; these settings will be
        // automatically applied to the pin when the application is terminated
        // ensure that the LED is turned OFF when the application is shutdown
        myLed.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);


        // get explicit state enumeration for the GPIO pin associated with the button
        PinState myButtonState = myButton.getState();

        // use convenience wrapper method to interrogate the button state
        boolean buttonPressed = myButton.isHigh();
        */

        for (Pin pin : RaspiPin.allPins(PinMode.DIGITAL_INPUT)) {
            GpioPinDigitalInput button = gpio.provisionDigitalInputPin(pin,
                    "Pin" + pin.getName(),
                    PinPullResistance.PULL_DOWN);

            // create and register gpio pin listener
            button.addListener(new GpioUsageExampleListener());
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
