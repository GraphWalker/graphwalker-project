define(['jquery'], function($) {
  'use strict';

  // Rubberband helper functions
  var _getElementOffset = function(element) {
    var elementOffset = {};
    elementOffset.left = element.offsetLeft;
    elementOffset.top =  element.offsetTop;

    // Distance to the right is: left + width
    elementOffset.right = elementOffset.left + element.offsetWidth;

    // Distance to the bottom is: top + height
    elementOffset.bottom = elementOffset.top + element.offsetHeight;

    return elementOffset;
  };
  var _getSelectedElements = function(selector, rubberband) {
    var selectedElements = [];
    var rubberbandOffset = _getElementOffset(rubberband);
    $(selector).each(function() {
      var itemOffset = _getElementOffset(this);
      // Check if element falls inside the rubberband
      if(itemOffset.top > rubberbandOffset.top &&
        itemOffset.left > rubberbandOffset.left &&
        itemOffset.right < rubberbandOffset.right &&
        itemOffset.bottom < rubberbandOffset.bottom) {
          // If it does, add it to selection
          selectedElements.push(this);
        }
      });
      return selectedElements;
    };

    var attachHandlerTo = function(canvas, selector, fn) {
      var boundGetSelectedElements = _getSelectedElements.bind(null, selector);
      $(canvas).on('rubberband', function(evt, mouseEvt) {
        // Trigger only when clicked directly on canvas to prevent
        // rubberband appearing when e.g. resizing vertices.
        if (mouseEvt.target !== this) return;

        // Record the starting point
        var startpos = {
          Y: mouseEvt.offsetY,
          X: mouseEvt.offsetX
        };

        // Create the rubberband div and append it to container
        var rb = $('<div/>').attr('id', 'rubberband').css({
          top: startpos.Y,
          left: startpos.X
        }).hide().appendTo(this);

        var _mouseMoved = false;

        // Append temporary handlers
        var eupHandler, emvHandler;
        $(this)
          .one('mousemove', function() {
            _mouseMoved = true;

            // Don't display the rubberband until user moves the cursor
            rb.show();
          })
          .on('mousemove', function emvHandler(emv) {
            if (emv.target !== this) return;
            // Update dimensions
            rb.css({
              'top':    Math.min(startpos.Y, emv.offsetY),
              'left':   Math.min(startpos.X, emv.offsetX),
              'width':  Math.abs(startpos.X - emv.offsetX),
              'height': Math.abs(startpos.Y - emv.offsetY)
            });

            // Add hover class to elements currently in selection
            var oldSelection = $('.rubberband-hover');
            var newSelection = $(boundGetSelectedElements(rb[0]));
            if (!oldSelection.isSameAs(newSelection)) {
              oldSelection.removeClass('rubberband-hover');
              newSelection.addClass('rubberband-hover');
            }
          })
          .on('mouseup', function eupHandler(eup) {
            if (_mouseMoved) {
              // Reset
              _mouseMoved = false;

              // Add to existing selection if meta key is down
              var append = eup.metaKey;

              // Select elements that (fully) fall inside the rubberband
              var selectedElements = boundGetSelectedElements(rb[0]);
              fn(selectedElements, append);

              // Clear hover class. Add time out to allow the new
              // selection class to be set on the elements
              setTimeout(function() {
                $('.rubberband-hover').removeClass('rubberband-hover');
              }, 200);
            }

            // Remove rubberband
            rb.remove();

            // Remove handlers
            $(this).off('mouseup', eupHandler);
            $(this).off('mousemove', emvHandler);
          });
      });
    };

    return attachHandlerTo;
  });
