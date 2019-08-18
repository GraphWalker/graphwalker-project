
export default [
  {
    selector: 'node',
    style: {
      content: 'data(name)',
      'text-wrap': 'wrap',
      'text-valign': 'center',
      'text-halign': 'center',
      shape: 'roundrectangle',
      width: 'label',
      height: 'label',
      color: 'black',
      'background-color': 'data(color)',
      'line-color': 'data(color)',
      'padding-left': '10',
      'padding-right': '10',
      'padding-top': '10',
      'padding-bottom': '10'
    },
  }, {
    selector: 'edge',
    style: {
      'content': 'data(name)',
      'text-wrap': 'wrap',
      'curve-style': 'bezier',
      'text-rotation': 'autorotate',
      'target-arrow-shape': 'triangle',
      'width': '4',
      'line-color': 'data(color)',
      'target-arrow-color': 'data(color)',
      'background-color': 'data(color)'
    },
  }, {
    selector: ':selected',
    style: {
      'border-width': 4,
      'border-color': 'black',
      'line-color': 'black',
      'target-arrow-color': 'black'
    }
  }
];
