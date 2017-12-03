import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { enterFullscreen, exitFullscreen } from '../../redux/actions/layout';
import IconButton from 'material-ui/IconButton';
import FullscreenButton from 'material-ui-icons/Fullscreen';
import FullscreenExitButton from 'material-ui-icons/FullscreenExit';
import screenfull from 'screenfull';

class Fullscreen extends Component {

  static propTypes = {
    enterFullscreen: PropTypes.func,
    exitFullscreen: PropTypes.func,
    isFullscreen: PropTypes.bool,
  };

  componentDidMount() {
    screenfull.on('change', this.updateState)
}

  componentWillUnmount() {
    screenfull.off('change', this.updateState)
  }

  updateState = () => {
    if (screenfull.isFullscreen) {
      this.props.enterFullscreen();
    } else {
      this.props.exitFullscreen();
    }
  };

  toggleFullscreen = () => {
    if (this.props.isFullscreen) {
      screenfull.exit();
    } else {
      screenfull.request();
    }
  };

  render() {
    if (!screenfull.enabled) {
      return null;
    }
    if (this.props.isFullscreen) {
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

export default connect(
  state => ({
    isFullscreen: state.layout.isFullscreen,
  }),
  dispatch => ({
    enterFullscreen: () => dispatch(enterFullscreen()),
    exitFullscreen: () => dispatch(exitFullscreen()),
  })
)(Fullscreen);
