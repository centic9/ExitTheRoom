package org.dstadler.exit;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.RandomUtils;
import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.dstadler.exit.util.Hardware;
import org.dstadler.exit.util.Player;
import org.dstadler.exit.util.TM1638;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

@SuppressWarnings({"BusyWait"})
public class ButtonDigitSpinning {
    private final static Logger log = LoggerFactory.make();

    private static final String EXPECTED_CODE = "83737762";
    private static final String SOLUTION_TEXT = "CODE 617";
    private static final String HAHA_TEXT = "hAhA.hAhA";

    private static final File[] MUSIC_RANDOM = new File[] {
            new File("audio/Isis & Mozes - Aerial (Yor Kultura Mix)-IJBbxDZnC-s.mp3"),
            new File("audio/MEUTE - Hey Hey (Dennis Ferrer Rework)-NYtjttnp1Rs.mp3"),
            new File("audio/Ben Böhmer live above Cappadocia in Turkey for Cercle-RvRhUHTV_8k.mp3"),
            new File("audio/MORIARTY-MISS ME  _ SHERLOCK HOLMES _ 4 SEASON _ THE FINAL PROBLEM-3_Yht_v1BoM.mp3"),
            new File("audio/SHERLOCK _ Moriarty - Did you miss me-2uaYcnFQF-g.mp3"),
    };

    private static final File MUSIC_FANFARE = new File("audio/Royal Entrance Fanfare - Randy Dunn, heralding trumpet-NkD0MxNY_Bw_trimmed.mp3");

    private static final File[] MUSIC_DONE = new File[] {
            new File("audio/Ultimate-Victory-WST010901.mp3"),
            new File("audio/Female-shout.wav-218417.mp3"),
            new File("audio/Evil Laugh.wav-219110.mp3"),
            new File("audio/MORIARTY-MISS ME  _ SHERLOCK HOLMES _ 4 SEASON _ THE FINAL PROBLEM-3_Yht_v1BoM.mp3"),
            new File("audio/SHERLOCK _ Moriarty - Did you miss me-2uaYcnFQF-g.mp3"),
    };

    private static final Player player = new Player();
    private static final Hardware hardware = new Hardware(() -> {
        if (player.isPlaying()) {
            player.stop();
        }
    });

    public static void main(String[] args) throws Exception {
        LoggerFactory.initLogging();

        log.info("Setting up GPIO input events and TM1638 device");

        TM1638 tm1638 = hardware.createTM1638();
        tm1638.enable();

        char[] text = "00000000".toCharArray();
        //tm1638.set_text("00000000");

        int buttons_prev = -1;
        log.info("Setup finished, waiting for input-events or CTRL-C");

        // wait for CTRL-C
        String textStr = null;
        while (true) {
            if(canPlay()) {
                player.play(MUSIC_RANDOM[RandomUtils.nextInt(0, MUSIC_RANDOM.length)]);
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
            if(checkForSuccess(textStr, tm1638)) {
                // return true means it was successful and thus we should reset everything
                log.info("Resetting...");
                text = "00000000".toCharArray();
                textStr = new String(text);
                tm1638.set_text(textStr);
                buttons_prev = -1;
                hardware.initLED();
            }

            // shut down on top 4 buttons pressed at the same time
            if(buttons == (4  + 64 + 1024 + 16384)) {
                log.info("Shutting down...");

                player.stop();
                tm1638.set_text("        ");
                hardware.shutdown();

                break;
            }

            Thread.sleep(100);
        }
    }

    private static boolean canPlay() {
        return !player.isPlaying() && !hardware.isSwitchButton();
    }

    private static boolean checkForSuccess(String text, TM1638 tm1638) throws IOException, InterruptedException {
        if(
                // code does not match
                //!"00000001".equals(text) ||
                !EXPECTED_CODE.equals(text) ||

                // led is not enabled
                hardware.isLed()) {
            return false;
        }

        // play some Fanfare first
        if (!hardware.isSwitchButton()) {
            player.play(MUSIC_FANFARE);
        }

        // simply iterate the scream, laugh and Sherlock talk endlessly
        int music = 0;

        // blink led and correct code
        while(true) {
            // off
            tm1638.set_text(HAHA_TEXT);
            hardware.ledOff();

            Thread.sleep(1000);

            // on
            tm1638.set_text(SOLUTION_TEXT);
            hardware.ledOn();

            Thread.sleep(1000);

            // play the next item in a loop
            if(canPlay()) {
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
