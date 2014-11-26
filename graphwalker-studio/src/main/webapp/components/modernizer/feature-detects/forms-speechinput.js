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
// speech input for inputs
// by @alrra


// `webkitSpeech` in elem 
// doesn`t work correctly in all versions of Chromium based browsers.
//   It can return false even if they have support for speech i.imgur.com/2Y40n.png
//  Testing with 'onwebkitspeechchange' seems to fix this problem

// this detect only checks the webkit version because
// the speech attribute is likely to be deprecated in favor of a JavaScript API.
// http://lists.w3.org/Archives/Public/public-webapps/2011OctDec/att-1696/speechapi.html

// FIXME: add support for detecting the new spec'd behavior

Modernizr.addTest('speechinput', function(){
    var elem = document.createElement('input'); 
    return 'speech' in elem || 'onwebkitspeechchange' in elem; 
});