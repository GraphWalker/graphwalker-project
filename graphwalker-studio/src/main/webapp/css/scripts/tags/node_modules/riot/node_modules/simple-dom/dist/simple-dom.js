(function() {
    "use strict";
    function simple$dom$document$node$$Node(nodeType, nodeName, nodeValue) {
      this.nodeType = nodeType;
      this.nodeName = nodeName;
      this.nodeValue = nodeValue;

      this.childNodes = new simple$dom$document$node$$ChildNodes(this);

      this.parentNode = null;
      this.previousSibling = null;
      this.nextSibling = null;
      this.firstChild = null;
      this.lastChild = null;
    }

    simple$dom$document$node$$Node.prototype._cloneNode = function() {
      return new simple$dom$document$node$$Node(this.nodeType, this.nodeName, this.nodeValue);
    };

    simple$dom$document$node$$Node.prototype.cloneNode = function(deep) {
      var node = this._cloneNode();

      if (deep) {
        var child = this.firstChild, nextChild = child;

        while (nextChild) {
          nextChild = child.nextSibling;
          node.appendChild(child.cloneNode(true));
          child = nextChild;
        }
      }

      return node;
    };

    simple$dom$document$node$$Node.prototype.appendChild = function(node) {
      if (node.nodeType === simple$dom$document$node$$Node.DOCUMENT_FRAGMENT_NODE) {
        simple$dom$document$node$$insertFragment(node, this, this.lastChild, null);
        return;
      }

      if (node.parentNode) { node.parentNode.removeChild(node); }

      node.parentNode = this;
      var refNode = this.lastChild;
      if (refNode === null) {
        this.firstChild = node;
        this.lastChild = node;
      } else {
        node.previousSibling = refNode;
        refNode.nextSibling = node;
        this.lastChild = node;
      }
    };

    function simple$dom$document$node$$insertFragment(fragment, newParent, before, after) {
      if (!fragment.firstChild) { return; }

      var firstChild = fragment.firstChild;
      var lastChild = firstChild;
      var node = firstChild;

      firstChild.previousSibling = before;
      if (before) {
        before.nextSibling = firstChild;
      } else {
        newParent.firstChild = firstChild;
      }

      while (node) {
        node.parentNode = newParent;
        lastChild = node;
        node = node.nextSibling;
      }

      lastChild.nextSibling = after;
      if (after) {
        after.previousSibling = lastChild;
      } else {
        newParent.lastChild = lastChild;
      }
    }

    simple$dom$document$node$$Node.prototype.insertBefore = function(node, refNode) {
      if (refNode == null) {
        return this.appendChild(node);
      }

      if (node.nodeType === simple$dom$document$node$$Node.DOCUMENT_FRAGMENT_NODE) {
        simple$dom$document$node$$insertFragment(node, this, refNode ? refNode.previousSibling : null, refNode);
        return;
      }

      node.parentNode = this;

      var previousSibling = refNode.previousSibling;
      if (previousSibling) {
        previousSibling.nextSibling = node;
        node.previousSibling = previousSibling;
      }else{
        node.previousSibling = null
      }

      refNode.previousSibling = node;
      node.nextSibling = refNode;

      if (this.firstChild === refNode) {
        this.firstChild = node;
      }
    };

    simple$dom$document$node$$Node.prototype.removeChild = function(refNode) {
      if (this.firstChild === refNode) {
        this.firstChild = refNode.nextSibling;
      }
      if (this.lastChild === refNode) {
        this.lastChild = refNode.previousSibling;
      }
      if (refNode.previousSibling) {
        refNode.previousSibling.nextSibling = refNode.nextSibling;
      }
      if (refNode.nextSibling) {
        refNode.nextSibling.previousSibling = refNode.previousSibling;
      }
      refNode.parentNode = null;
      refNode.nextSibling = null;
      refNode.previousSibling = null;
    };

    simple$dom$document$node$$Node.ELEMENT_NODE = 1;
    simple$dom$document$node$$Node.ATTRIBUTE_NODE = 2;
    simple$dom$document$node$$Node.TEXT_NODE = 3;
    simple$dom$document$node$$Node.CDATA_SECTION_NODE = 4;
    simple$dom$document$node$$Node.ENTITY_REFERENCE_NODE = 5;
    simple$dom$document$node$$Node.ENTITY_NODE = 6;
    simple$dom$document$node$$Node.PROCESSING_INSTRUCTION_NODE = 7;
    simple$dom$document$node$$Node.COMMENT_NODE = 8;
    simple$dom$document$node$$Node.DOCUMENT_NODE = 9;
    simple$dom$document$node$$Node.DOCUMENT_TYPE_NODE = 10;
    simple$dom$document$node$$Node.DOCUMENT_FRAGMENT_NODE = 11;
    simple$dom$document$node$$Node.NOTATION_NODE = 12;

    function simple$dom$document$node$$ChildNodes(node) {
      this.node = node;
    }

    simple$dom$document$node$$ChildNodes.prototype.item = function(index) {
      var child = this.node.firstChild;

      for (var i = 0; child && index !== i; i++) {
        child = child.nextSibling;
      }

      return child;
    };

    var simple$dom$document$node$$default = simple$dom$document$node$$Node;

    function simple$dom$document$element$$Element(tagName) {
      tagName = tagName.toUpperCase();

      this.nodeConstructor(1, tagName, null);
      this.attributes = [];
      this.tagName = tagName;
    }

    simple$dom$document$element$$Element.prototype = Object.create(simple$dom$document$node$$default.prototype);
    simple$dom$document$element$$Element.prototype.constructor = simple$dom$document$element$$Element;
    simple$dom$document$element$$Element.prototype.nodeConstructor = simple$dom$document$node$$default;

    simple$dom$document$element$$Element.prototype._cloneNode = function() {
      var node = new simple$dom$document$element$$Element(this.tagName);

      node.attributes = this.attributes.map(function(attr) {
        return { name: attr.name, value: attr.value, specified: attr.specified };
      });

      return node;
    };

    simple$dom$document$element$$Element.prototype.getAttribute = function(_name) {
      var attributes = this.attributes;
      var name = _name.toLowerCase();
      var attr;
      for (var i=0, l=attributes.length; i<l; i++) {
        attr = attributes[i];
        if (attr.name === name) {
          return attr.value;
        }
      }
      return '';
    };

    simple$dom$document$element$$Element.prototype.setAttribute = function(_name, _value) {
      var attributes = this.attributes;
      var name = _name.toLowerCase();
      var value;
      if (typeof _value === 'string') {
        value = _value;
      } else {
        value = '' + _value;
      }
      var attr;
      for (var i=0, l=attributes.length; i<l; i++) {
        attr = attributes[i];
        if (attr.name === name) {
          attr.value = value;
          return;
        }
      }
      attributes.push({
        name: name,
        value: value,
        specified: true // serializer compat with old IE
      });
    };

    simple$dom$document$element$$Element.prototype.removeAttribute = function(name) {
      var attributes = this.attributes;
      for (var i=0, l=attributes.length; i<l; i++) {
        var attr = attributes[i];
        if (attr.name === name) {
          attributes.splice(i, 1);
          return;
        }
      }
    };

    var simple$dom$document$element$$default = simple$dom$document$element$$Element;

    function simple$dom$document$document$fragment$$DocumentFragment() {
      this.nodeConstructor(11, '#document-fragment', null);
    }

    simple$dom$document$document$fragment$$DocumentFragment.prototype._cloneNode = function() {
      return new simple$dom$document$document$fragment$$DocumentFragment();
    };

    simple$dom$document$document$fragment$$DocumentFragment.prototype = Object.create(simple$dom$document$node$$default.prototype);
    simple$dom$document$document$fragment$$DocumentFragment.prototype.constructor = simple$dom$document$document$fragment$$DocumentFragment;
    simple$dom$document$document$fragment$$DocumentFragment.prototype.nodeConstructor = simple$dom$document$node$$default;

    var simple$dom$document$document$fragment$$default = simple$dom$document$document$fragment$$DocumentFragment;

    function $$document$text$$Text(text) {
      this.nodeConstructor(3, '#text', text);
    }

    $$document$text$$Text.prototype._cloneNode = function() {
      return new $$document$text$$Text(this.nodeValue);
    };

    $$document$text$$Text.prototype = Object.create(simple$dom$document$node$$default.prototype);
    $$document$text$$Text.prototype.constructor = $$document$text$$Text;
    $$document$text$$Text.prototype.nodeConstructor = simple$dom$document$node$$default;

    var $$document$text$$default = $$document$text$$Text;

    function $$document$comment$$Comment(text) {
      this.nodeConstructor(8, '#comment', text);
    }

    $$document$comment$$Comment.prototype._cloneNode = function() {
      return new $$document$comment$$Comment(this.nodeValue);
    };

    $$document$comment$$Comment.prototype = Object.create(simple$dom$document$node$$default.prototype);
    $$document$comment$$Comment.prototype.constructor = $$document$comment$$Comment;
    $$document$comment$$Comment.prototype.nodeConstructor = simple$dom$document$node$$default;

    var $$document$comment$$default = $$document$comment$$Comment;

    function $$document$raw$html$section$$RawHTMLSection(text) {
      this.nodeConstructor(-1, "#raw-html-section", text);
    }

    $$document$raw$html$section$$RawHTMLSection.prototype = Object.create(simple$dom$document$node$$default.prototype);
    $$document$raw$html$section$$RawHTMLSection.prototype.constructor = $$document$raw$html$section$$RawHTMLSection;
    $$document$raw$html$section$$RawHTMLSection.prototype.nodeConstructor = simple$dom$document$node$$default;

    var $$document$raw$html$section$$default = $$document$raw$html$section$$RawHTMLSection;

    function simple$dom$document$$Document() {
      this.nodeConstructor(9, '#document', null);
      this.documentElement = new simple$dom$document$element$$default('html');
      this.head = new simple$dom$document$element$$default('head');
      this.body = new simple$dom$document$element$$default('body');
      this.documentElement.appendChild(this.head);
      this.documentElement.appendChild(this.body);
      this.appendChild(this.documentElement);
    }

    simple$dom$document$$Document.prototype = Object.create(simple$dom$document$node$$default.prototype);
    simple$dom$document$$Document.prototype.constructor = simple$dom$document$$Document;
    simple$dom$document$$Document.prototype.nodeConstructor = simple$dom$document$node$$default;

    simple$dom$document$$Document.prototype.createElement = function(tagName) {
      return new simple$dom$document$element$$default(tagName);
    };

    simple$dom$document$$Document.prototype.createTextNode = function(text) {
      return new $$document$text$$default(text);
    };

    simple$dom$document$$Document.prototype.createComment = function(text) {
      return new $$document$comment$$default(text);
    };

    simple$dom$document$$Document.prototype.createRawHTMLSection = function(text) {
      return new $$document$raw$html$section$$default(text);
    };

    simple$dom$document$$Document.prototype.createDocumentFragment = function() {
      return new simple$dom$document$document$fragment$$default();
    };

    var simple$dom$document$$default = simple$dom$document$$Document;
    function simple$dom$html$parser$$HTMLParser(tokenize, document, voidMap) {
      this.tokenize = tokenize;
      this.document = document;
      this.voidMap = voidMap;
      this.parentStack = [];
    }

    simple$dom$html$parser$$HTMLParser.prototype.isVoid = function(element) {
      return this.voidMap[element.nodeName] === true;
    };

    simple$dom$html$parser$$HTMLParser.prototype.pushElement = function(token) {
      var el = this.document.createElement(token.tagName);

      for (var i=0;i<token.attributes.length;i++) {
        var attr = token.attributes[i];
        el.setAttribute(attr[0], attr[1]);
      }

      if (this.isVoid(el)) {
        return this.appendChild(el);
      }

      this.parentStack.push(el);
    };

    simple$dom$html$parser$$HTMLParser.prototype.popElement = function(token) {
      var el = this.parentStack.pop();

      if (el.nodeName !== token.tagName.toUpperCase()) {
        throw new Error('unbalanced tag');
      }

      this.appendChild(el);
    };

    simple$dom$html$parser$$HTMLParser.prototype.appendText = function(token) {
      var text = this.document.createTextNode(token.chars);
      this.appendChild(text);
    };

    simple$dom$html$parser$$HTMLParser.prototype.appendComment = function(token) {
      var comment = this.document.createComment(token.chars);
      this.appendChild(comment);
    };

    simple$dom$html$parser$$HTMLParser.prototype.appendChild = function(node) {
      var parentNode = this.parentStack[this.parentStack.length-1];
      parentNode.appendChild(node);
    };

    simple$dom$html$parser$$HTMLParser.prototype.parse = function(html/*, context*/) {
      // TODO use context for namespaceURI issues
      var fragment = this.document.createDocumentFragment();
      this.parentStack.push(fragment);

      var tokens = this.tokenize(html);
      for (var i=0, l=tokens.length; i<l; i++) {
        var token = tokens[i];
        switch (token.type) {
          case 'StartTag':
            this.pushElement(token);
            break;
          case 'EndTag':
            this.popElement(token);
            break;
          case 'Chars':
            this.appendText(token);
            break;
          case 'Comment':
            this.appendComment(token);
            break;
        }
      }

      return this.parentStack.pop();
    };

    var simple$dom$html$parser$$default = simple$dom$html$parser$$HTMLParser;
    function simple$dom$html$serializer$$HTMLSerializer(voidMap) {
      this.voidMap = voidMap;
    }

    simple$dom$html$serializer$$HTMLSerializer.prototype.openTag = function(element) {
      return '<' + element.nodeName.toLowerCase() + this.attributes(element.attributes) + '>';
    };

    simple$dom$html$serializer$$HTMLSerializer.prototype.closeTag = function(element) {
      return '</' + element.nodeName.toLowerCase() + '>';
    };

    simple$dom$html$serializer$$HTMLSerializer.prototype.isVoid = function(element) {
      return this.voidMap[element.nodeName] === true;
    };

    simple$dom$html$serializer$$HTMLSerializer.prototype.attributes = function(namedNodeMap) {
      var buffer = '';
      for (var i=0, l=namedNodeMap.length; i<l; i++) {
        buffer += this.attr(namedNodeMap[i]);
      }
      return buffer;
    };

    simple$dom$html$serializer$$HTMLSerializer.prototype.escapeAttrValue = function(attrValue) {
      return attrValue.replace(/[&"]/g, function(match) {
        switch(match) {
          case '&':
            return '&amp;';
          case '\"':
            return '&quot;';
        }
      });
    };

    simple$dom$html$serializer$$HTMLSerializer.prototype.attr = function(attr) {
      if (!attr.specified) {
        return '';
      }
      if (attr.value) {
        return ' ' + attr.name + '="' + this.escapeAttrValue(attr.value) + '"';
      }
      return ' ' + attr.name;
    };

    simple$dom$html$serializer$$HTMLSerializer.prototype.escapeText = function(textNodeValue) {
      return textNodeValue.replace(/[&<>]/g, function(match) {
        switch(match) {
          case '&':
            return '&amp;';
          case '<':
            return '&lt;';
          case '>':
            return '&gt;';
        }
      });
    };

    simple$dom$html$serializer$$HTMLSerializer.prototype.text = function(text) {
      return this.escapeText(text.nodeValue);
    };

    simple$dom$html$serializer$$HTMLSerializer.prototype.rawHTMLSection = function(text) {
      return text.nodeValue;
    };

    simple$dom$html$serializer$$HTMLSerializer.prototype.comment = function(comment) {
      return '<!--'+comment.nodeValue+'-->';
    };

    simple$dom$html$serializer$$HTMLSerializer.prototype.serialize = function(node) {
      var buffer = '';
      var next;

      // open
      switch (node.nodeType) {
        case 1:
          buffer += this.openTag(node);
          break;
        case 3:
          buffer += this.text(node);
          break;
        case -1:
          buffer += this.rawHTMLSection(node);
          break;
        case 8:
          buffer += this.comment(node);
          break;
        default:
          break;
      }

      next = node.firstChild;
      if (next) {
        buffer += this.serialize(next);

        while(next = next.nextSibling) {
          buffer += this.serialize(next);
        }
      }

      if (node.nodeType === 1 && !this.isVoid(node)) {
        buffer += this.closeTag(node);
      }

      return buffer;
    };

    var simple$dom$html$serializer$$default = simple$dom$html$serializer$$HTMLSerializer;

    var simple$dom$void$map$$default = {
      AREA: true,
      BASE: true,
      BR: true,
      COL: true,
      COMMAND: true,
      EMBED: true,
      HR: true,
      IMG: true,
      INPUT: true,
      KEYGEN: true,
      LINK: true,
      META: true,
      PARAM: true,
      SOURCE: true,
      TRACK: true,
      WBR: true
    };

    (function (root, factory) {
      if (typeof define === 'function' && define.amd) {
        define([], factory);
      } else if (typeof exports === 'object') {
        module.exports = factory();
      } else {
        root.SimpleDOM = factory();
      }
    }(this, function () {
      return {
        Node: simple$dom$document$node$$default,
        Element: simple$dom$document$element$$default,
        DocumentFragment: simple$dom$document$document$fragment$$default,
        Document: simple$dom$document$$default,
        voidMap: simple$dom$void$map$$default,
        HTMLSerializer: simple$dom$html$serializer$$default,
        HTMLParser: simple$dom$html$parser$$default
      };
    }));
}).call(this);

//# sourceMappingURL=simple-dom.js.map