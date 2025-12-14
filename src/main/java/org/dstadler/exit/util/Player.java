package org.dstadler.exit.util;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT_DURATION;

/**
 * Simple audio-player which executes the external application 'oxmplayer.bin'
 */
public class Player {
    private final static Logger log = LoggerFactory.make();

    private ExecuteWatchdog watchdog;
    private Thread player = null;
    private volatile boolean stopping = false;

    public boolean isPlaying() {
        return
                // check if watchdog exists
                (watchdog != null &&
                // check if playing thread exists and did not yet finish
                (player != null && player.isAlive()));
    }

    public synchronized void play(File file) throws IOException {
        // stop if there is already something player
        stop();

        if(!file.exists()) {
            throw new IOException("Cannot find file " + file.getAbsolutePath());
        }

        log.info("Starting to play file " + file.getAbsolutePath());
        CommandLine cmdLine = new CommandLine("/usr/bin/omxplayer.bin");
        //cmdLine.addArgument("--loop");
        cmdLine.addArgument("--no-osd");
        cmdLine.addArgument("--no-keys");
        cmdLine.addArgument(file.getAbsolutePath(), false);

        DefaultExecutor executor = getDefaultExecutor(new File("."), 0);

        player = new Thread(() -> {
            try {
                try (ByteArrayOutputStream outStr = new ByteArrayOutputStream();
                     ByteArrayOutputStream errStr = new ByteArrayOutputStream()) {
                    executor.setStreamHandler(new PumpStreamHandler(outStr, errStr));
                    try {
                        execute(cmdLine, new File("."), executor);

                        if (errStr.toByteArray().length > 0) {
                            log.info("Had stderr: " + errStr.toString(StandardCharsets.UTF_8));
                        }

                        if (outStr.toByteArray().length > 0) {
                            log.info("Had stdout: " + outStr.toString(StandardCharsets.UTF_8));
                        }
                    } catch (IOException e) {
                        log.warning("Had output before error: " + outStr);
                        throw new IOException(e);
                    }
                }
            } catch (IOException e) {
                if(stopping) {
                    log.info("Had exception while stopping: " + e);
                } else {
                    log.log(Level.WARNING, "Failed to start command", e);
                }
            }
        });

        player.setDaemon(true);
        player.start();
    }

    public synchronized void stop() throws IOException {
        stopping = true;
        if(watchdog != null) {
            log.info("Stopping via watchdog");
            if(watchdog.isWatching()) {
                log.info("Destroy Process");
                watchdog.destroyProcess();
            }
            watchdog.stop();
            try {
                watchdog.checkException();
            } catch (Exception e) {
                throw new IOException(e);
            }
            watchdog = null;
        }
        if(player != null) {
            log.info("Stopping playing thread");
            try {
                player.join(5000, 0);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
            player = null;
        }
        stopping = false;
    }

    private static void execute(CommandLine cmdLine, File dir, DefaultExecutor executor) throws IOException {
        log.info("-Executing(" + dir + "): " + cmdLine);
        try {
            int exitValue = executor.execute(cmdLine, (Map<String,String>)null);
            if (exitValue != 0) {
                log.info("Had exit code " + exitValue + " when calling " + cmdLine);
            }
        } catch (ExecuteException e) {
            throw new ExecuteException("While executing (" + dir + "); " + cmdLine, e.getExitValue(), e);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private DefaultExecutor getDefaultExecutor(File dir, int expectedExit) {
        DefaultExecutor executor = DefaultExecutor.builder().
                setWorkingDirectory(dir).
                get();
        if(expectedExit != -1) {
            executor.setExitValue(expectedExit);
        } else {
            executor.setExitValues(null);
        }

        watchdog = ExecuteWatchdog.builder().
                setTimeout(INFINITE_TIMEOUT_DURATION).
                get();
        executor.setWatchdog(watchdog);

        return executor;
    }
}
