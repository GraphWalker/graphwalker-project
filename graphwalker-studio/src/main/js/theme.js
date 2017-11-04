import { createMuiTheme } from 'material-ui/styles';
import amber from 'material-ui/colors/amber';
import blueGrey from 'material-ui/colors/blueGrey';

export const theme = createMuiTheme({
  palette: {
    primary: amber,
    secondary: amber,
  },
  status: {
    danger: 'orange',
  },
});
