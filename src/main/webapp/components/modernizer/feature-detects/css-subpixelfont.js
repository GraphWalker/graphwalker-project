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
/*
 * Test for SubPixel Font Rendering
 * (to infer if GDI or DirectWrite is used on Windows)
 * Authors: @derSchepp, @gerritvanaaken, @rodneyrehm, @yatil, @ryanseddon
 * Web: https://github.com/gerritvanaaken/subpixeldetect
 */
Modernizr.addTest('subpixelfont', function() {
    var bool,
        styles = "#modernizr{position: absolute; top: -10em; visibility:hidden; font: normal 10px arial;}#subpixel{float: left; font-size: 33.3333%;}";
    
    // see https://github.com/Modernizr/Modernizr/blob/master/modernizr.js#L97
    Modernizr.testStyles(styles, function(elem) {
        var subpixel = elem.firstChild;

        subpixel.innerHTML = 'This is a text written in Arial';

        bool = window.getComputedStyle ?
            window.getComputedStyle(subpixel, null).getPropertyValue("width") !== '44px'
            : false;
    }, 1, ['subpixel']);

    return bool;
});
