import React, { Component } from 'react';
import IconButton from 'material-ui/IconButton';
import FullscreenButton from 'material-ui-icons/Fullscreen';
import FullscreenExitButton from 'material-ui-icons/FullscreenExit';
import screenfull from 'screenfull';

export class Fullscreen extends Component {

  state = {
    isFullscreen: screenfull.isFullscreen,
  };

  componentDidMount() {
    screenfull.on('change', this.updateState)
}

  componentWillUnmount() {
    screenfull.on('change', this.updateState)
  }

  updateState = () => {
    this.setState({
      isFullscreen: screenfull.isFullscreen,
    });
  };

  toggleFullscreen = () => {
    if (this.state.isFullscreen) {
      screenfull.exit();
    } else {
      screenfull.request();
    }
  };

  render() {
    if (!screenfull.enabled) {
      return null;
    }
    if (this.state.isFullscreen) {
      return (
        <IconButton color="contrast" onClick={this.toggleFullscreen}>
          <FullscreenExitButton/>
        </IconButton>
      );
    }
    return (
      <IconButton color="contrast" onClick={this.toggleFullscreen}>
        <FullscreenButton/>
      </IconButton>
    );
  }
}
