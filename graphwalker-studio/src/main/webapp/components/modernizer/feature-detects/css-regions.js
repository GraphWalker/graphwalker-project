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
// CSS Regions
// http://www.w3.org/TR/css3-regions/
// By: Mihai Balan

// We start with a CSS parser test then we check page geometry to see if it's affected by regions
// Later we might be able to retire the second part, as WebKit builds with the false positives die out

Modernizr.addTest('regions', function() {

	/* Get the 'flowFrom' property name available in the browser. Either default or vendor prefixed.
	If the property name can't be found we'll get Boolean 'false' and fail quickly */
	var flowFromProperty = Modernizr.prefixed("flowFrom"),
		flowIntoProperty = Modernizr.prefixed("flowInto");

	if (!flowFromProperty || !flowIntoProperty){
		return false;
	}

	/* If CSS parsing is there, try to determine if regions actually work. */
	var container		= document.createElement('div'),
		content			= document.createElement('div'),
		region			= document.createElement('div'),

	/* we create a random, unlikely to be generated flow number to make sure we don't
	clash with anything more vanilla, like 'flow', or 'article', or 'f1' */
	flowName = 'modernizr_flow_for_regions_check';

	/* First create a div with two adjacent divs inside it. The first will be the
	content, the second will be the region. To be able to distinguish between the two,
	we'll give the region a particular padding */
	content.innerText		= 'M';
	container.style.cssText	= 'top: 150px; left: 150px; padding: 0px;';
	region.style.cssText	= 'width: 50px; height: 50px; padding: 42px;';

	region.style[flowFromProperty] = flowName;
	container.appendChild(content);
	container.appendChild(region);
	document.documentElement.appendChild(container);

	/* Now compute the bounding client rect, before and after attempting to flow the
	content div in the region div. If regions are enabled, the after bounding rect
	should reflect the padding of the region div.*/
	var flowedRect, delta,
		plainRect = content.getBoundingClientRect();


	content.style[flowIntoProperty] = flowName;
	flowedRect = content.getBoundingClientRect();

	delta = flowedRect.left - plainRect.left;
	document.documentElement.removeChild(container);
	content = region = container = undefined;

	return (delta == 42);
});
