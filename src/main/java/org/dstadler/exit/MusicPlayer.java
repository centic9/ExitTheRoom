package org.dstadler.exit;

import org.dstadler.exit.util.Player;

import java.io.File;

public class MusicPlayer {
    public static void main(String[] args) throws Exception {
        if(args.length != 2) {
            System.err.println("Usage: MusicPlayer <file1> <file2>");
            System.exit(1);
        }

        Player player = new Player();

        player.play(new File(args[0]));

        Thread.sleep(5000);

        player.play(new File(args[1]));

        Thread.sleep(5000);
    }
}
