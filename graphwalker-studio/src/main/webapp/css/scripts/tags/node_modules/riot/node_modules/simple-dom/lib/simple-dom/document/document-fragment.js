import Node from './node';

function DocumentFragment() {
  this.nodeConstructor(11, '#document-fragment', null);
}

DocumentFragment.prototype._cloneNode = function() {
  return new DocumentFragment();
};

DocumentFragment.prototype = Object.create(Node.prototype);
DocumentFragment.prototype.constructor = DocumentFragment;
DocumentFragment.prototype.nodeConstructor = Node;

export default DocumentFragment;
