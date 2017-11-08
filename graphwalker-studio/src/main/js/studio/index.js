import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { createMuiTheme, MuiThemeProvider } from 'material-ui/styles';
import Studio from './Studio';
import actionTypes from '../redux/actions';

function getTheme(theme) {
  return createMuiTheme({
    palette: {
      type: theme.paletteType,
    },
  });
}

class Wrapper extends Component {

  componentDidMount() {
    setInterval(() => {
      this.props.dispatch({
        type: actionTypes.THEME_CHANGE_PALETTE_TYPE,
        payload: {
          paletteType: this.props.uiTheme.paletteType === 'light' ? 'dark' : 'light',
        },
      });
    }, 5000);
  }

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
