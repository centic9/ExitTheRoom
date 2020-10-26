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
import java.util.Map;
import java.util.logging.Logger;

import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;

public class Player {
    private final static Logger log = LoggerFactory.make();

    private ExecuteWatchdog watchdog;
    private Thread player = null;

    public void play(File file) throws Exception {
        // stop if there is already something player
        stop();

        if(!file.exists()) {
            throw new IOException("Cannot find file " + file.getAbsolutePath());
        }

        log.info("Starting to play file " + file.getAbsolutePath());
        player = new Thread(() -> {
            CommandLine cmdLine = new CommandLine("/usr/bin/omxplayer");
            cmdLine.addArgument("--loop");
            cmdLine.addArgument("--no-osd");
            cmdLine.addArgument("--no-keys");
            cmdLine.addArgument(file.getAbsolutePath());

            try {
                DefaultExecutor executor = getDefaultExecutor(new File("."), 0);

                try (ByteArrayOutputStream outStr = new ByteArrayOutputStream();
                     ByteArrayOutputStream errStr = new ByteArrayOutputStream()) {
                    executor.setStreamHandler(new PumpStreamHandler(outStr, errStr));
                    try {
                        execute(cmdLine, new File("."), executor);

                        if (errStr.toByteArray().length > 0) {
                            log.info("Had stderr: " + errStr.toString("UTF-8"));
                        }

                        if (outStr.toByteArray().length > 0) {
                            log.info("Had stdout: " + outStr.toString("UTF-8"));
                        }
                    } catch (IOException e) {
                        log.warning("Had output before error: " + new String(outStr.toByteArray()));
                        throw new IOException(e);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        player.setDaemon(true);
        player.start();
    }

    public synchronized void stop() throws Exception {
        if(watchdog != null) {
            log.info("Stopping via watchdog");
            if(watchdog.isWatching()) {
                watchdog.destroyProcess();
            }
            watchdog.stop();
            watchdog.checkException();
            watchdog = null;
        }
        if(player != null) {
            log.info("Stopping playing thread");
            player.join(5000, 0);
            player = null;
        }
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

    private DefaultExecutor getDefaultExecutor(File dir, int expectedExit) {
        DefaultExecutor executor = new DefaultExecutor();
        if(expectedExit != -1) {
            executor.setExitValue(expectedExit);
        } else {
            executor.setExitValues(null);
        }

        watchdog = new ExecuteWatchdog(INFINITE_TIMEOUT);
        executor.setWatchdog(watchdog);
        executor.setWorkingDirectory(dir);

        return executor;
    }
}
