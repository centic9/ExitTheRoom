package org.dstadler.exit.util;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.trigger.GpioInverseSyncStateTrigger;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("BusyWait")
public class Hardware {
    private final static Logger log = LoggerFactory.make();

    public interface RunnableWithCheckedException {
        void run() throws IOException;
    }

    private final RunnableWithCheckedException stopPlaying;

    // create gpio controller instance
    private final GpioController gpio = GpioFactory.getInstance();

    private final GpioPinDigitalOutput led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "LED");

    private final GpioPinDigitalInput onOffButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_23,
            "Button 1|0", PinPullResistance.PULL_DOWN);
    private final GpioPinDigitalInput switchButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_25,
            "Switch Button",PinPullResistance.PULL_DOWN);

    public Hardware(RunnableWithCheckedException stopPlaying) {
        this.stopPlaying = stopPlaying;

        // set LED depending on the state of the 0|1-Button
        initLED();

        GpioPinDigitalInput voltButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_24,
                "230V Button", PinPullResistance.PULL_DOWN);

        // 0|1-Button: link LED to it (LED requires "low" for "on")
        onOffButton.addListener(new LogStateChangeListener());
        onOffButton.addTrigger(new GpioInverseSyncStateTrigger(led));

        // Switch-button turns on/off buzzer
        switchButton.addListener(new LogStateChangeListener());
        setupBuzzerOnSwitch(gpio, switchButton);

        // 230V-Button just stops audio-playing so a new random track starts
        voltButton.addListener(new LogStateChangeListener());
        voltButton.addListener((GpioPinListenerDigital) event -> {
            // just stop playing, it will automatically start with the next appropriate song
            try {
                stopPlaying.run();
            } catch (IOException e) {
                log.log(Level.WARNING, "Failed to stop playing", e);
            }
        });
    }

    public void initLED() {
        led.setState(!onOffButton.getState().isHigh());
    }

    public TM1638 createTM1638() {
        return new TM1638(gpio, RaspiPin.GPIO_00, RaspiPin.GPIO_02, RaspiPin.GPIO_03);
    }

    public boolean isSwitchButton() {
        return switchButton.isHigh();
    }

    public boolean isLed() {
        // LED is "on" when "low"!
        return !led.isHigh();
    }

    public void ledOn() {
        // LED is "on" when "low"!
        led.setState(PinState.LOW);
    }

    public void ledOff() {
        // LED is "on" when "low"!
        led.setState(PinState.HIGH);
    }

    public void shutdown() {
        ledOff();
        gpio.shutdown();
    }

    private void setupBuzzerOnSwitch(GpioController gpio, GpioPinDigitalInput switchButton) {
        GpioPinDigitalOutput buzzer  = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_26, "Buzzer");

        // combine buzzer with switch
        Thread buzzerThread = new Thread(() -> {
            try {
                while (true) {
                    if (switchButton.getState().isHigh()) {
                        // stop playing when we should hear the ticking
                        stopPlaying.run();

                        buzzer.setState(PinState.HIGH);

                        Thread.sleep(500);

                        buzzer.setState(PinState.LOW);
                    }

                    Thread.sleep(500);
                }
            } catch (InterruptedException | IOException e) {
                log.log(Level.WARNING, "Caught exception", e);
            }
        });
        buzzerThread.setDaemon(true);
        buzzerThread.start();
    }

    public static class LogStateChangeListener implements GpioPinListenerDigital {
        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            // display pin state on console
            log.info(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = "
                    + event.getState());
        }
    }
}
