import Node from './node';

function Element(tagName) {
  tagName = tagName.toUpperCase();

  this.nodeConstructor(1, tagName, null);
  this.attributes = [];
  this.tagName = tagName;
}

Element.prototype = Object.create(Node.prototype);
Element.prototype.constructor = Element;
Element.prototype.nodeConstructor = Node;

Element.prototype._cloneNode = function() {
  var node = new Element(this.tagName);

  node.attributes = this.attributes.map(function(attr) {
    return { name: attr.name, value: attr.value, specified: attr.specified };
  });

  return node;
};

Element.prototype.getAttribute = function(_name) {
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

Element.prototype.setAttribute = function(_name, _value) {
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

Element.prototype.removeAttribute = function(name) {
  var attributes = this.attributes;
  for (var i=0, l=attributes.length; i<l; i++) {
    var attr = attributes[i];
    if (attr.name === name) {
      attributes.splice(i, 1);
      return;
    }
  }
};

export default Element;
