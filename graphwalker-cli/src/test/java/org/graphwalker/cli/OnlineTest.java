/*
 * #%L
 * GraphWalker Command Line Interface
 * %%
 * Copyright (C) 2005 - 2014 GraphWalker
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

package org.graphwalker.cli;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;


public class OnlineTest extends CLITestRoot {

  final class RunOnlineWebsocketService extends Thread {

    String args[] = {"-d", "all", "online"};
    Result result;

    public Result getResult() {
      return result;
    }

    @Override
    public void run() {
      result = runCommand(args);
    }
  }

  @Test(timeout = 60000)
  public void websocket() throws IOException, ExecutionException, InterruptedException {

    RunOnlineWebsocketService runOnlineService = new RunOnlineWebsocketService();
    final ExecutorService executor = Executors.newSingleThreadExecutor();

    String actualOutput = "";
    do {
      Thread.sleep(100);
      if (runOnlineService != null && runOnlineService.getResult() != null) {
        actualOutput = runOnlineService.getResult().getOutput();
      }
    } while (actualOutput.contains("GraphWalkerServer started on port:"));

    executor.shutdown();

    if (!executor.isTerminated()) {
      executor.shutdownNow();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }
  }


  final class RunOnlineRestfulService extends Thread {

    String args[] = {"-d", "all", "online", "-s", "RESTFUL", "-p", "9999"};
    Result result;

    public Result getResult() {
      return result;
    }

    @Override
    public void run() {
      result = runCommand(args);
    }
  }

  @Test(timeout = 60000)
  public void restful() throws IOException, ExecutionException, InterruptedException {

    RunOnlineRestfulService runOnlineService = new RunOnlineRestfulService();
    final ExecutorService executor = Executors.newSingleThreadExecutor();

    String actualOutput = "";
    do {
      Thread.sleep(100);
      if (runOnlineService != null && runOnlineService.getResult() != null) {
        actualOutput = runOnlineService.getResult().getOutput();
      }
    }
    while (actualOutput.contains("Try http://localhost:9999/graphwalker/hasNext or http://localhost:9999/graphwalker/getNext" +
                                 System.lineSeparator() +
                                 "Press Control+C to end..."));

    executor.shutdown();

    if (!executor.isTerminated()) {
      executor.shutdownNow();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
