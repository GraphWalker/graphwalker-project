import React from 'react';
import { render } from 'react-dom';
import { MuiThemeProvider } from 'material-ui/styles';
import { theme } from './theme';
import Studio from './views/studio';
import 'typeface-roboto';
import './styles.less';

function Application() {
  return (
    <MuiThemeProvider theme={theme}>
      <Studio />
    </MuiThemeProvider>
  );
}

render(<Application />, document.getElementById('studio'));
