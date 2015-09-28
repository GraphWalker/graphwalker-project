import Node from './document/node';
import Element from './document/element';
import Text from './document/text';
import Comment from './document/comment';
import RawHTMLSection from './document/raw-html-section';
import DocumentFragment from './document/document-fragment';

function Document() {
  this.nodeConstructor(9, '#document', null);
  this.documentElement = new Element('html');
  this.head = new Element('head');
  this.body = new Element('body');
  this.documentElement.appendChild(this.head);
  this.documentElement.appendChild(this.body);
  this.appendChild(this.documentElement);
}

Document.prototype = Object.create(Node.prototype);
Document.prototype.constructor = Document;
Document.prototype.nodeConstructor = Node;

Document.prototype.createElement = function(tagName) {
  return new Element(tagName);
};

Document.prototype.createTextNode = function(text) {
  return new Text(text);
};

Document.prototype.createComment = function(text) {
  return new Comment(text);
};

Document.prototype.createRawHTMLSection = function(text) {
  return new RawHTMLSection(text);
};

Document.prototype.createDocumentFragment = function() {
  return new DocumentFragment();
};

export default Document;
