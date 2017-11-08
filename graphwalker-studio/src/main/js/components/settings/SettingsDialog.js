import React, { Component } from 'react';
import { withStyles } from 'material-ui/styles';
import { connect } from 'react-redux';
import compose from 'recompose/compose';
import Button from 'material-ui/Button';
import Dialog, { DialogTitle, DialogContent, DialogActions } from 'material-ui/Dialog';
import { FormControlLabel, FormGroup } from 'material-ui/Form';
import Switch from 'material-ui/Switch';
import { THEME_CHANGE_PALETTE_TYPE } from '../../redux/actions';

const styles = theme => ({
  button: {
    margin: theme.spacing.unit,
  }
});

class SettingsDialog extends Component {

  state = {
    checked: true,
  };

  toggleThemeType = (event, checked) => {
    this.props.dispatch({
      type: THEME_CHANGE_PALETTE_TYPE,
      payload: {
        paletteType: this.props.uiTheme.paletteType === 'light' ? 'dark' : 'light',
      },
    });
    this.setState({ checked: checked });
  };

  render() {
    const { classes } = this.props;
    return (
      <Dialog open={this.props.open} onRequestClose={this.props.onRequestClose}>
        <DialogTitle>Settings</DialogTitle>
        <DialogContent>
          <FormGroup>
            <FormControlLabel
              control={
                <Switch
                  checked={this.state.checked}
                  onChange={this.toggleThemeType}
                />
              }
              label="Toggle light/dark theme"
            />
          </FormGroup>
        </DialogContent>
        <DialogActions>
          <Button raised color="primary" className={classes.button} onClick={this.props.onRequestClose}>
            OK
          </Button>
        </DialogActions>
      </Dialog>
    );
  }
}

export default compose(
  withStyles(styles, {
    withTheme: true,
  }),
  connect(state => ({
    uiTheme: state.theme,
  })),
)(SettingsDialog);
