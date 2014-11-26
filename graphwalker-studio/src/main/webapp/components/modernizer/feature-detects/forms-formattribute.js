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
// Detects whether input form="form_id" is available on the platform
// E.g. IE 10 (and below), don't support this
Modernizr.addTest("formattribute", function() {
	var form = document.createElement("form"),
		input = document.createElement("input"),
		div = document.createElement("div"),
		id = "formtest"+(new Date().getTime()),
		attr,
		bool = false;

		form.id = id;

	//IE6/7 confuses the form idl attribute and the form content attribute
	if(document.createAttribute){
		attr = document.createAttribute("form");
		attr.nodeValue = id;
		input.setAttributeNode(attr);
		div.appendChild(form);
		div.appendChild(input);

		document.documentElement.appendChild(div);

		bool = form.elements.length === 1 && input.form == form;

		div.parentNode.removeChild(div);
	}

	return bool;
});