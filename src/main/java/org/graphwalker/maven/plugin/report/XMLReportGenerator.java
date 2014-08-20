package org.graphwalker.maven.plugin.report;

/*
 * #%L
 * GraphWalker Maven Plugin
 * %%
 * Copyright (C) 2011 - 2014 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.util.FileUtils;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.java.test.Executor;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Nils Olsson
 */
public class XMLReportGenerator {

    private static final String NEWLINE = "\n";
    private static final String INDENT = "    ";
    private static final String SPACE = " ";

    private final File reportDirectory;
    private final MavenSession session;

    public XMLReportGenerator(File reportDirectory, MavenSession session) {
        this.reportDirectory = reportDirectory;
        this.session = session;
    }

    public void writeReport(Executor executor, Machine machine) {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(NEWLINE);
        builder.append("<testsuites>").append(NEWLINE);
        Report report = new Report(machine, session.getStartTime());
        builder.append(INDENT).append("<testsuite")
                .append(SPACE).append("tests=\"").append(report.getTests()).append("\"")
                .append(SPACE).append("skipped=\"").append(report.getSkipped()).append("\"")
                .append(SPACE).append("failures=\"").append(report.getFailures()).append("\"")
                .append(SPACE).append("time=\"").append(report.getTime()).append("\"")
                .append(SPACE).append("timestamp=\"").append(report.getTimestamp()).append("\"")
                .append(">").append(NEWLINE);
        Properties properties = session.getSystemProperties();
        List<String> keys = new ArrayList<>(properties.stringPropertyNames());
        Collections.sort(keys);
        builder.append(INDENT).append(INDENT).append("<properties>").append(NEWLINE);
        for (String name: keys) {
            builder.append(INDENT).append(INDENT).append(INDENT).append("<property")
                    .append(SPACE).append("name=\"").append(name).append("\"")
                    .append(SPACE).append("value=\"").append(properties.getProperty(name).replaceAll("\\\\", "\\\\")).append("\"/>").append(NEWLINE);
        }
        builder.append(INDENT).append(INDENT).append("</properties>").append(NEWLINE);
        for (ExecutionContext context: machine.getExecutionContexts()) {
            builder.append(INDENT).append(INDENT).append("<testcase")
                    .append(SPACE).append("classname=\"").append(context.getClass().getName()).append("\"")
                    .append(SPACE).append("name=\"").append(context.getClass().getSimpleName()).append("\"")
                    .append(SPACE).append("time=\"").append(getSeconds(context.getProfiler().getProfile().getTotalExecutionTime(TimeUnit.MILLISECONDS))).append("\"")
                    .append(">").append(NEWLINE);
            // if failed
            if (executor.isFailure(context)) {
                Throwable throwable = executor.getFailure(context).getCause();
                builder.append(INDENT).append(INDENT).append(INDENT).append("<failure")
                        .append(SPACE).append("type=\"").append(throwable.getClass().getName()).append("\"")
                        .append(SPACE).append("message=\"").append(throwable.getMessage()).append("\"")
                        .append(">").append(NEWLINE);
                builder.append(getStackTrace(throwable));
                builder.append(INDENT).append(INDENT).append(INDENT).append("</failure>").append(NEWLINE);
            }
            builder.append(INDENT).append(INDENT).append("</testcase>").append(NEWLINE);
        }
        builder.append(INDENT).append("</testsuite>").append(NEWLINE);
        builder.append("</testsuites>").append(NEWLINE);
        write("TEST-TestSuites.xml", builder.toString());
    }

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter, true);
        throwable.printStackTrace(printWriter);
        StringBuffer buffer = new StringBuffer();
        for (String line: stringWriter.getBuffer().toString().split("\n")) {
            buffer.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(line).append(NEWLINE);
        }
        return buffer.toString();
    }

    private double getSeconds(long milliseconds) {
        return (double)milliseconds / 1000.0;
    }

    private void write(String name, String data) {
        OutputStream outputStream = getOutputStream(reportDirectory, name);
        try {
            outputStream.write(data.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new XMLReportException(e);
        }
    }

    private OutputStream getOutputStream(File directory, String reportName) {
        FileUtils.mkdir(directory.getAbsolutePath());
        try {
            return new FileOutputStream(new File(directory, reportName));
        } catch (FileNotFoundException e) {
            throw new XMLReportException(e);
        }
    }

    private class Report {

        private final List<ExecutionContext> failedExecutions = new ArrayList<>();
        private long skipped = 0;
        private long tests = 0;
        private long failures = 0;
        private long time = 0;
        private final String timestamp;

        Report(Machine machine, Date startTime) {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            timestamp = formatter.format(startTime);
            for (ExecutionContext context: machine.getExecutionContexts()) {
                tests++;
                switch (context.getExecutionStatus()) {
                    case FAILED: {
                        failures++;
                        failedExecutions.add(context);
                    }
                    break;
                    case NOT_EXECUTED: {
                        skipped++;
                    }
                    break;
                    case EXECUTING: {
                        failures++;
                    }
                }
                time += context.getProfiler().getProfile().getTotalExecutionTime(TimeUnit.MILLISECONDS);
            }
        }

        public boolean failed(ExecutionContext context) {
            return failedExecutions.contains(context);
        }

        public long getSkipped() {
            return skipped;
        }

        public long getTests() {
            return tests;
        }

        public long getFailures() {
            return failures;
        }

        public double getTime() {
            return (double)time / 1000.0;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }
}
