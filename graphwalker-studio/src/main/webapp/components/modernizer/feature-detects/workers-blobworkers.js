/*
 * #%L
 * GraphWalker Dashboard
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
// by jussi-kalliokoski


// This test is asynchronous. Watch out.

// The test will potentially add garbage to console.

(function(){
  try {
    // we're avoiding using Modernizr._domPrefixes as the prefix capitalization on
    // these guys are notoriously peculiar.
    var BlobBuilder = window.MozBlobBuilder || window.WebKitBlobBuilder || window.MSBlobBuilder || window.OBlobBuilder || window.BlobBuilder;
    var URL         = window.MozURL || window.webkitURL || window.MSURL || window.OURL || window.URL;
    var data    = 'Modernizr',
        blob,
        bb,
        worker,
        url,
        timeout,
        scriptText = 'this.onmessage=function(e){postMessage(e.data)}';

    try {
      blob = new Blob([scriptText], {type:'text/javascript'});
    } catch(e) {
      // we'll fall back to the deprecated BlobBuilder
    }
    if (!blob) {
      bb = new BlobBuilder();
      bb.append(scriptText);
      blob = bb.getBlob();
    }

    url = URL.createObjectURL(blob);
    worker = new Worker(url);

    worker.onmessage = function(e) {
      addTest('blobworkers', data === e.data);
      cleanup();
    };

    // Just in case...
    worker.onerror = fail;
    timeout = setTimeout(fail, 200);

    worker.postMessage(data);
  } catch (e) {
    fail();
  }

  function fail() {
    addTest('blobworkers', false);
    cleanup();
  }

  function cleanup() {
    if (url) {
      URL.revokeObjectURL(url);
    }
    if (worker) {
      worker.terminate();
    }
    if (timeout) {
      clearTimeout(timeout);
    }
  }
}());
