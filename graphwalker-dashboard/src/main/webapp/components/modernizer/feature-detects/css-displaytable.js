/*
 * #%L
 * GraphWalker Dashboard
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
// display: table and table-cell test. (both are tested under one name "table-cell" )
// By @scottjehl

// all additional table display values are here: http://pastebin.com/Gk9PeVaQ though Scott has seen some IE false positives with that sort of weak detection.
// more testing neccessary perhaps.

Modernizr.addTest( "display-table",function(){
  
  var doc   = window.document,
      docElem = doc.documentElement,   
      parent  = doc.createElement( "div" ),
      child = doc.createElement( "div" ),
      childb  = doc.createElement( "div" ),
      ret;
  
  parent.style.cssText = "display: table";
  child.style.cssText = childb.style.cssText = "display: table-cell; padding: 10px";    
          
  parent.appendChild( child );
  parent.appendChild( childb );
  docElem.insertBefore( parent, docElem.firstChild );
  
  ret = child.offsetLeft < childb.offsetLeft;
  docElem.removeChild(parent);
  return ret; 
});

