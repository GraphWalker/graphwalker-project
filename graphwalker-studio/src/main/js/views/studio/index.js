import React, { Component } from 'react';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import { deepOrange500 } from 'material-ui/styles/colors';
import FlatButton from 'material-ui/FlatButton';
import './styles.less';

const muiTheme = getMuiTheme({
  palette: {
    accent1Color: deepOrange500,
  },
});

export default class Studio extends Component {

  render() {
    return (
      <MuiThemeProvider muiTheme={muiTheme}>
        <FlatButton label="OK" primary={true} />
      </MuiThemeProvider>
    );
  }
}
