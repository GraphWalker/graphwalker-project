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
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.java.test.Executor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
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

    private final File reportDirectory;
    private final MavenSession session;

    public XMLReportGenerator(File reportDirectory, MavenSession session) {
        this.reportDirectory = reportDirectory;
        this.session = session;
    }

    public void writeReport(Executor executor) {
        Testsuites testsuites = new Testsuites();
        List<Report> reports = new ArrayList<>();
        for (Machine machine: executor.getMachines()) {
            Report report = new Report(machine, session.getStartTime());
            Testsuite testsuite = new Testsuite();
            List<String> keys = new ArrayList<>(session.getSystemProperties().stringPropertyNames());
            Collections.sort(keys);
            Properties properties = new Properties();
            for (String name: keys) {
                Property property = new Property();
                property.setName(name);
                property.setValue(session.getSystemProperties().getProperty(name));
                properties.getProperty().add(property);
            }
            testsuite.setProperties(properties);
            testsuite.setTests(report.getTestsAsString());
            testsuite.setFailures(report.getFailuresAsString());
            testsuite.setErrors(report.getErrorsAsString());
            testsuite.setTime(report.getTimeAsString());
            testsuite.setTimestamp(report.getTimestamp());
            for (Context context: machine.getContexts()) {
                Testcase testcase = new Testcase();
                testcase.setName(context.getClass().getSimpleName());
                testcase.setClassname(context.getClass().getName());
                testcase.setTime(getSeconds(context.getProfiler().getProfile().getTotalExecutionTime(TimeUnit.MILLISECONDS)));
                if (executor.isFailure(context)) {
                    Throwable throwable = executor.getFailure(context).getCause();
                    Error error = new Error();
                    error.setType(throwable.getClass().getName());
                    error.setMessage(throwable.getMessage());
                    error.setContent(getStackTrace(throwable));
                    testcase.getError().add(error);
                } else if (report.isFailure(context)) {
                    Failure failure = new Failure();
                    failure.setType("Not fulfilled");
                    double fulfilment = context.getPathGenerator().getStopCondition().getFulfilment(context);
                    failure.setMessage(String.valueOf(Math.round(100*fulfilment)));
                    testcase.getFailure().add(failure);
                }
                testsuite.getTestcase().add(testcase);
            }
            testsuites.getTestsuite().add(testsuite);
            reports.add(report);
        }
        consolidate(testsuites, reports);
        try {
            JAXBContext context = JAXBContext.newInstance("org.graphwalker.maven.plugin.report");
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(testsuites, getOutputStream(reportDirectory, getName()));
        } catch (JAXBException e) {
            throw new XMLReportException(e);
        }
    }

    private void consolidate(Testsuites testsuites, List<Report> reports) {
        long tests = 0;
        long failures = 0;
        long errors = 0;
        double time = Double.MAX_VALUE;
        for (Report report: reports) {
            tests += report.getTests();
            failures += report.getFailures();
            errors += report.getErrors();
            if (time > report.getTime()) {
                time = report.getTime();
            }
        }
        testsuites.setTime(String.valueOf(time));
        testsuites.setFailures(String.valueOf(failures));
        testsuites.setErrors(String.valueOf(errors));
        testsuites.setTests(String.valueOf(tests));
    }

    private String getName() {
        return "TEST-TestSuites.xml";
    }

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter, true);
        throwable.printStackTrace(printWriter);
        StringBuffer buffer = new StringBuffer().append(NEWLINE);
        for (String line: stringWriter.getBuffer().toString().split(NEWLINE)) {
            buffer.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(line).append(NEWLINE);
        }
        return buffer.toString();
    }

    private String getSeconds(long milliseconds) {
        return String.valueOf((double)milliseconds / 1000.0);
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

        private final List<Context> failedExecutions = new ArrayList<>();
        private int errors = 0;
        private int tests = 0;
        private int failures = 0;
        private int time = 0;
        private final String timestamp;

        Report(Machine machine, Date startTime) {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            timestamp = formatter.format(startTime);
            for (Context context: machine.getContexts()) {
                tests++;
                switch (context.getExecutionStatus()) {
                    case FAILED: {
                        errors++;
                    }
                    break;
                    case NOT_EXECUTED:
                    case EXECUTING: {
                        failures++;
                        failedExecutions.add(context);
                    }
                }
                time += context.getProfiler().getProfile().getTotalExecutionTime(TimeUnit.MILLISECONDS);
            }
        }

        public boolean isFailure(Context context) {
            return failedExecutions.contains(context);
        }

        public int getErrors() {
            return errors;
        }

        public String getErrorsAsString() {
            return String.valueOf(errors);
        }

        public int getTests() {
            return tests;
        }

        public String getTestsAsString() {
            return String.valueOf(tests);
        }

        public int getFailures() {
            return failures;
        }

        public String getFailuresAsString() {
            return String.valueOf(failures);
        }

        public double getTime() {
            return (double)time / 1000.0;
        }

        public String getTimeAsString() {
            return String.valueOf((double) time / 1000.0);
        }

        public String getTimestamp() {
            return timestamp;
        }
    }
}
