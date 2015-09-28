define(['riot', 'jsplumb', 'jquery'],
function(riot, jsp, $) {

  var testAll = function(n,m) {
    console.profile('testAll');
    console.time('testAll');
    testAddVertex.call(this, n);
    testAddEdge.call(this, m);
    console.timeEnd('testAll');
    console.profileEnd();
  }

  var testAddVertex = function(n) {
    // Get canvas width & height
    var padding      = 40;
    var vertexHeight = 80;
    var vertexWidth  = 120;
    var canvasHeight = this.root.offsetHeight;
    var canvasWidth  = this.root.offsetWidth;
    var canvasTop    = this.root.offsetTop + vertexHeight;
    var canvasLeft   = this.root.offsetLeft + vertexWidth;

    var rows = Math.floor(canvasHeight / (vertexHeight + padding));
    var cols = Math.floor(canvasWidth / (vertexWidth + padding));

    outer:
    for (var i = 0, counter = 0; i < rows; i++) {
      for (var j = 0; j < cols; j++) {
        if (n && counter++ >= n) break outer;
        var mockMouseEvent = new MouseEvent('doubleclick', {
          clientX: canvasLeft + j*(vertexWidth + padding),
          clientY: canvasTop + i*(vertexHeight + padding)
        });

        this.addVertex(mockMouseEvent);
      }
    }
    if (n > counter) console.log('Mounted', (n - counter), 'less vertices than the request', n, 'due to canvas size');
  }

  var testAddEdge = function(m) {
    var vertices = $('vertex');
    for (var i = 0; i < m; i++) {
      var rand1 = Math.floor(Math.random() * vertices.length);
      var rand2 = Math.floor(Math.random() * vertices.length);
      this.addEdge(vertices[rand1].id, vertices[rand2].id);
    }
  }

  return function(canvas) {
    return {
      testAll: testAll.bind(canvas),
      testAddVertex: testAddVertex.bind(canvas),
      testAddEdge: testAddEdge.bind(canvas),
    }
  };
});
