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
function dumpModernizr(){
  var str = '';
  dumpModernizr.old = dumpModernizr.old || {};

    for (var prop in Modernizr) {

      // skip previously done ones.
      if (dumpModernizr.old[prop]) continue;
      else dumpModernizr.old[prop] = true;

      if (typeof Modernizr[prop] === 'function') continue;
      // skip unit test items
      if (/^test/.test(prop)) continue;

      if (~TEST.inputs.indexOf(prop)) {
        str += '<li><b>'+prop+'{}</b><ul>';
        for (var field in Modernizr[prop]) {
          str += '<li class="' + (Modernizr[prop][field] ? 'yes' : '') + '">' + field + ': ' + Modernizr[prop][field] + '</li>';
        }
        str += '</ul></li>';
      } else {
        str += '<li  id="'+prop+'" class="' + (Modernizr[prop] ? 'yes' : '') + '">' + prop + ': ' + Modernizr[prop] + '</li>';
      }
  }
  return str;
}


function grabFeatDetects(){
  // thx github.js
  $.getScript('https://api.github.com/repos/Modernizr/Modernizr/git/trees/master?recursive=1&callback=processTree');
}


function processTree(data){
  var filenames = [];

  for (var i = 0; i < data.data.tree.length; i++){
    var file = data.data.tree[i];
    var match = file.path.match(/^feature-detects\/(.*)/);
    if (!match) continue;

    var relpath = location.host == "modernizr.github.com" ?
                    '../modernizr-git/' : '../';

    filenames.push(relpath + match[0]);
  }

  var jqxhrs = filenames.map(function(filename){
    return jQuery.getScript(filename);
  });

  jQuery.when.apply(jQuery, jqxhrs).done(resultsToDOM);

}

function resultsToDOM(){

  var modOutput = document.createElement('div'),
      ref = document.getElementById('qunit-testresult') || document.getElementById('qunit-tests');

  modOutput.className = 'output';
  modOutput.innerHTML = dumpModernizr();

  ref.parentNode.insertBefore(modOutput, ref);

  // Modernizr object as text
  document.getElementsByTagName('textarea')[0].innerHTML = JSON.stringify(Modernizr);

}

/* uno    */ resultsToDOM();
/* dos    */ grabFeatDetects();
/* tres   */ setTimeout(resultsToDOM,  5e3);
/* quatro */ setTimeout(resultsToDOM, 15e3);
