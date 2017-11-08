import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { createMuiTheme, MuiThemeProvider } from 'material-ui/styles';
import Studio from './Studio';

function getTheme(theme) {
  return createMuiTheme({
    palette: {
      type: theme.paletteType,
    },
  });
}

class Wrapper extends Component {
  render() {
    return (
      <MuiThemeProvider theme={getTheme(this.props.uiTheme)}>
        <Studio/>
      </MuiThemeProvider>
    );
  }
}

export default connect(state => ({
  uiTheme: state.theme,
}))(Wrapper);
