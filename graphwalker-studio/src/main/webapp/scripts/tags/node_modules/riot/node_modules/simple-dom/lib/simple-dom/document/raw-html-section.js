import Node from './node';

function RawHTMLSection(text) {
  this.nodeConstructor(-1, "#raw-html-section", text);
}

RawHTMLSection.prototype = Object.create(Node.prototype);
RawHTMLSection.prototype.constructor = RawHTMLSection;
RawHTMLSection.prototype.nodeConstructor = Node;

export default RawHTMLSection;
