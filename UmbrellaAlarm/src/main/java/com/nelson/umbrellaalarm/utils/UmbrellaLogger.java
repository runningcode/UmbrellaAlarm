package com.nelson.umbrellaalarm.utils;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class UmbrellaLogger {

    private static final String LOGGER_KEY = "umbrella_logger_key";
    private static final int MAX_LOG_SIZE = 512 * 1024;
    private static final Logger logger = Logger.getLogger(LOGGER_KEY);

    public static Logger getLogger() {
        // just return if we already have a logger so we don't create a new file
        if (logger.getHandlers().length > 0){
            return logger;
        }
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File loggerFile = new File(externalStorageDirectory, ".UmbrellaAlarmLog.log");
        try {
            if (externalStorageDirectory.canWrite()) {
                FileHandler fileHandler = new FileHandler(loggerFile.getAbsolutePath(), MAX_LOG_SIZE, 1, true);
                fileHandler.setFormatter(new SimpleFormatter());
                logger.addHandler(fileHandler);
                logger.info("logging started");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logger;
    }
}
