import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { createMuiTheme, MuiThemeProvider } from 'material-ui/styles';
import indigo from 'material-ui/colors/indigo';
import amber from 'material-ui/colors/amber';
import Studio from './Studio';

function isDark(theme) {
  return theme.paletteType === 'dark';
}

function getTheme(theme) {
  return createMuiTheme({
    palette: {
      primary: isDark(theme) ? amber : indigo,
      secondary: isDark(theme) ? amber : indigo,
      type: theme.paletteType,
    },
    status: {
      danger: 'orange',
    },
  });
}

class Wrapper extends Component {

  static propTypes = {
    uiTheme: PropTypes.object,
  };

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
