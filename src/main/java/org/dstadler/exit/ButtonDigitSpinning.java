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
import com.pi4j.io.gpio.trigger.GpioInverseSyncStateTrigger;
import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.dstadler.exit.util.Player;
import org.dstadler.exit.util.TM1638;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"BusyWait"})
public class ButtonDigitSpinning {
    private final static Logger log = LoggerFactory.make();

    private static final String EXPECTED_CODE = "83737762";
    private static final String SOLUTION_TEXT = "CODE 617";
    private static final String HAHA_TEXT = "hAhA.hAhA";

    private static final File MUSIC_DEFAULT = new File("audio/Isis & Mozes - Aerial (Yør Kultura Mix)-IJBbxDZnC-s.mp3");
    private static final File MUSIC_FANFARE = new File("audio/Royal Entrance Fanfare - Randy Dunn, heralding trumpet-NkD0MxNY_Bw_trimmed.mp3");
    private static final File MUSIC_LAUGH = new File("audio/Evil Laugh.wav-219110.mp3");
    private static final File MUSIC_SHOUT = new File("audio/Female-shout.wav-218417.mp3");

    private static final File[] MUSIC_DONE = new File[] {
            MUSIC_SHOUT,
            MUSIC_LAUGH,
            new File("audio/MORIARTY-MISS ME  _ SHERLOCK HOLMES _ 4 SEASON _ THE FINAL PROBLEM-3_Yht_v1BoM.mp3"),
            new File("audio/SHERLOCK _ Moriarty - Did you miss me-2uaYcnFQF-g.mp3"),
    };

    private static final Player player = new Player();

    public static void main(String[] args) throws Exception {
        LoggerFactory.initLogging();

        log.info("Setting up GPIO input events and TM1638 device");

        // create gpio controller instance
        final GpioController gpio = GpioFactory.getInstance();

        GpioPinDigitalOutput led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "LED");

        GpioPinDigitalInput onOffButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_23,
                "Button 1|0",PinPullResistance.PULL_DOWN);
        GpioPinDigitalInput switchButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_25,
                "Switch Button",PinPullResistance.PULL_DOWN);
        GpioPinDigitalInput voltButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_24,
                "230V Button",PinPullResistance.PULL_DOWN);

        led.setState(!onOffButton.getState().isHigh());

        // create a gpio synchronization trigger on the input pin
        // when the input state changes, also set LED controlling gpio pin to same state
        //onOffButton.addTrigger(new GpioSyncStateTrigger(led));

        onOffButton.addListener(new GpioUsageListener());
        switchButton.addListener(new GpioUsageListener());
        voltButton.addListener(new GpioUsageListener());

        setupBuzzerOnSwitch(gpio, switchButton);

        onOffButton.addTrigger(new GpioInverseSyncStateTrigger(led));

        TM1638 tm1638 = new TM1638(gpio, RaspiPin.GPIO_00, RaspiPin.GPIO_02, RaspiPin.GPIO_03);
        tm1638.enable();

        char[] text = "00000000".toCharArray();
        //tm1638.set_text("00000000");

        int buttons_prev = -1;
        log.info("Setup finished, waiting for input-events or CTRL-C");

        // wait for CTRL-C
        String textStr = null;
        while (true) {
            if(!player.isPlaying() && !switchButton.isHigh()) {
                player.play(MUSIC_DEFAULT);
            }

            int buttons = tm1638.get_buttons64();
            if(buttons != 0) {
                log.info("Buttons: " + buttons);
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

                    textStr = new String(text);
                    log.info("Having " + segment + " and " + textStr);
                    tm1638.set_text(textStr);
                }
            }

            // check if we have the code and the led
            if(checkForSuccess(textStr, led, tm1638, switchButton)) {
                log.info("Resetting...");
                text = "00000000".toCharArray();
                textStr = new String(text);
                tm1638.set_text(textStr);
                buttons_prev = -1;
                led.setState(!onOffButton.getState().isHigh());
            }

            // shut down on top 4 buttons pressed at the same time
            if(buttons == (4  + 64 + 1024 + 16384)) {
                log.info("Shutting down...");

                player.stop();

                led.setState(PinState.HIGH);
                tm1638.set_text("        ");

                gpio.shutdown();
                break;
            }

            Thread.sleep(100);
        }
    }

    private static boolean checkForSuccess(String text, GpioPinDigitalOutput led, TM1638 tm1638, GpioPinDigitalInput switchButton) throws IOException, InterruptedException {
        if(
                // code does not match
                //!"00000001".equals(text) ||
                !EXPECTED_CODE.equals(text) ||

                // led is not enabled
                led.isHigh()) {
            return false;
        }

        // play some Fanfare first
        if (!switchButton.isHigh()) {
            player.play(MUSIC_FANFARE);
        }

        // simply iterate the scream, laugh and Sherlock talk endlessly
        int music = 0;

        // blink led and correct code
        while(true) {
            // off
            tm1638.set_text(HAHA_TEXT);
            led.setState(PinState.LOW);

            Thread.sleep(1000);

            // on
            tm1638.set_text(SOLUTION_TEXT);
            led.setState(PinState.HIGH);

            Thread.sleep(1000);

            // play the next item in a loop
            if(!player.isPlaying() && !switchButton.isHigh()) {
                player.play(MUSIC_DONE[music]);
                music = (music + 1) % MUSIC_DONE.length;
            }

            // check if the two top-left buttons are pressed for a reset
            int buttons = tm1638.get_buttons64();
            if(buttons == (4 + 64)) {
                player.stop();
                return true;
            }
        }
    }

    private static void setupBuzzerOnSwitch(GpioController gpio, GpioPinDigitalInput switchButton) {
        GpioPinDigitalOutput buzzer  = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_26, "Buzzer");

        // combine buzzer with switch
        Thread buzzerThread = new Thread(() -> {
            try {
                while (true) {
                    if (switchButton.getState().isHigh()) {
                        // stop playing when we should hear the ticking
                        if (player.isPlaying()) {
                            player.stop();
                        }

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

    public static class GpioUsageListener implements GpioPinListenerDigital {
        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            // display pin state on console
            log.info(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = "
                    + event.getState());
        }
    }

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
}
