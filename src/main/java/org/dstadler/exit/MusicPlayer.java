package org.dstadler.exit;

import org.dstadler.commons.logging.jdk.LoggerFactory;
import org.dstadler.exit.util.Player;

import java.io.File;
import java.util.logging.Logger;

public class MusicPlayer {
    private final static Logger log = LoggerFactory.make();

    public static void main(String[] args) throws Exception {
        LoggerFactory.initLogging();

        if(args.length == 0) {
            System.err.println("Usage: MusicPlayer <file> ...");
            System.exit(1);
        }

        Player player = new Player();

        log.info("Playing before: " + player.isPlaying());

        for (String file : args) {
            player.play(new File(file));

            log.info("Playing: " + player.isPlaying());

            Thread.sleep(5000);
        }

        player.stop();

        log.info("Playing after: " + player.isPlaying());
    }
}
