package com.mariana.agent.core.logging.core;

import com.mariana.agent.common.util.DefaultNamedThreadFactory;
import com.mariana.agent.common.util.RunnableWithExceptionProtection;
import com.mariana.agent.core.config.Config;
import com.mariana.agent.core.config.Constants;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class FileWriter implements IWriter{

    private FileOutputStream fileOutputStream;
    private final ArrayBlockingQueue<String> logBuffer;
    private volatile int fileSize;
    private final Pattern filenamePattern = Pattern.compile(Config.Logging.FILE_NAME + "\\.\\d{4}_\\d{2}_\\d{2}_\\d{2}_\\d{2}_\\d{2}");

    private FileWriter() {
        logBuffer = new ArrayBlockingQueue<>(1024);
        final ArrayList<String> outputLogs = new ArrayList<>(200);
        Executors.newSingleThreadScheduledExecutor(new DefaultNamedThreadFactory("LogFileWriter"))
                .scheduleAtFixedRate(new RunnableWithExceptionProtection(() -> {
                    try {
                        logBuffer.drainTo(outputLogs);
                        for (String log : outputLogs) {
                            writeToFile(log + Constants.LINE_SEPARATOR);
                        }
                        try {
                            if (fileOutputStream != null) {
                                fileOutputStream.flush();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } finally {
                        outputLogs.clear();
                    }
                }, t -> {

                }), 0, 1, TimeUnit.SECONDS);

    }

    public static FileWriter get() {
        return FileWriterHolder.INSTANCE;
    }

    @Override
    public void write(String message) {
        logBuffer.offer(message);
    }

    private void writeToFile(String message) {
        if (prepareWriteStream()) {
            try {
                fileOutputStream.write(message.getBytes());
                fileSize += message.length();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                switchFile();
            }
        }
    }

    private boolean prepareWriteStream() {
        if (fileOutputStream != null) {
            return true;
        }
        File logFilePath = new File(Config.Logging.DIR);
        if (!logFilePath.exists()) {
            logFilePath.mkdirs();
        } else if (!logFilePath.isDirectory()) {
            System.err.println("Log dir(" + Config.Logging.DIR + ") is not a directory.");
        }
        try {
            fileOutputStream = new FileOutputStream(new File(logFilePath, Config.Logging.FILE_NAME), true);
            fileSize = Long.valueOf(new File(logFilePath, Config.Logging.FILE_NAME).length()).intValue();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return fileOutputStream != null;
    }

    private void switchFile() {
        if (fileSize > Config.Logging.MAX_FILE_SIZE) {
            forceExecute(() -> {
                fileOutputStream.flush();
                return null;
            });
            forceExecute(() -> {
                fileOutputStream.close();
                return null;
            });
            forceExecute(() -> {
                new File(Config.Logging.DIR, Config.Logging.FILE_NAME).renameTo(new File(Config.Logging.DIR, Config.Logging.FILE_NAME + new SimpleDateFormat(".yyyy_MM_dd_HH_mm_ss")
                        .format(new Date())));
                return null;
            });
            forceExecute(() -> {
                fileOutputStream = null;
                return null;
            });

            if (Config.Logging.MAX_HISTORY_FILES > 0) {
                deleteExpiredFiles();
            }
        }
    }

    private void deleteExpiredFiles() {
        String[] historyFileArr = getHistoryFilePath();
        if (historyFileArr != null && historyFileArr.length > Config.Logging.MAX_HISTORY_FILES) {

            Arrays.sort(historyFileArr, Comparator.reverseOrder());

            for (int i = Config.Logging.MAX_HISTORY_FILES; i < historyFileArr.length; i++) {
                File expiredFile = new File(Config.Logging.DIR, historyFileArr[i]);
                expiredFile.delete();
            }
        }
    }

    private String[] getHistoryFilePath() {
        File path = new File(Config.Logging.DIR);

        return path.list((dir, name) -> filenamePattern.matcher(name).matches());
    }

    private void forceExecute(Callable<?> callable) {
        try {
            callable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final class FileWriterHolder {
        private static final FileWriter INSTANCE = new FileWriter();
    }

}
