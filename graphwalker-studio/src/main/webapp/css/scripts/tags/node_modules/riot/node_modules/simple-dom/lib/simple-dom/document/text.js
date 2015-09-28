import Node from './node';

function Text(text) {
  this.nodeConstructor(3, '#text', text);
}

Text.prototype._cloneNode = function() {
  return new Text(this.nodeValue);
};

Text.prototype = Object.create(Node.prototype);
Text.prototype.constructor = Text;
Text.prototype.nodeConstructor = Node;

export default Text;
