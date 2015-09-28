<canvas-settings-subpane>
  <h5>Canvas settings</h5>
  <ul>
    <li>
      Scroll zoom sensitivity<br>
      <input name="sensitivity" type="range" onchange={ setSensitivity } />
      <span id="sensValue"></span>
    </li>
    <li>
      Show minimap
      <input name="minimap" type="checkbox" onchange={ setMinimap } />
    </li>
  </ul>

  var self = this;

  self.on('mount', function() {
    $.extend(self.sensitivity, {
      max: 1,
      min: 0.01,
      step: 0.01,
      value: opts.options.canvas && opts.options.canvas.scrollIncrement || 0.3
    });
    self.setSensitivity();

    self.minimap.checked = self.opts.options.canvas.minimap;
  });

  setSensitivity() {
    var sensValue = self.sensValue.innerHTML = self.sensitivity.value;
    $.extend(true, self.opts.options, {canvas: {scrollIncrement: sensValue}})
  }

  setMinimap() {
    $.extend(true, self.opts.options, {canvas: {minimap: self.minimap.checked}})
    riot.update();
  }

</canvas-settings-subpane>
