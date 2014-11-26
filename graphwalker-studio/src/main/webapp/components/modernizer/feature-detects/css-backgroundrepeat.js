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
// developer.mozilla.org/en/CSS/background-repeat

// test page: jsbin.com/uzesun/
// http://jsfiddle.net/ryanseddon/yMLTQ/6/    

(function(){


function getBgRepeatValue(elem){
    return (window.getComputedStyle ?
             getComputedStyle(elem, null).getPropertyValue('background') :
             elem.currentStyle['background']);
}
  

Modernizr.testStyles(' #modernizr { background-repeat: round; } ', function(elem, rule){ 

  Modernizr.addTest('bgrepeatround', getBgRepeatValue(elem) == 'round');

});



Modernizr.testStyles(' #modernizr { background-repeat: space; } ', function(elem, rule){ 

  Modernizr.addTest('bgrepeatspace', getBgRepeatValue(elem) == 'space');

});


})();
