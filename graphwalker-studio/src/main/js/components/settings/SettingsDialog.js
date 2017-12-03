import React, { Component } from 'react';
import { withStyles } from 'material-ui/styles';
import { connect } from 'react-redux';
import compose from 'recompose/compose';
import Button from 'material-ui/Button';
import Dialog, { DialogTitle, DialogContent, DialogActions } from 'material-ui/Dialog';
import { FormControlLabel, FormGroup } from 'material-ui/Form';
import Switch from 'material-ui/Switch';
import { setPaletteType } from '../../redux/actions/theme';

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
    this.props.setPaletteType(this.props.uiTheme.paletteType === 'light' ? 'dark' : 'light');
    this.setState({ checked: checked });
  };

  render() {
    const { classes } = this.props;
    return (
      <Dialog onRequestClose={this.props.onRequestClose}
          open={this.props.open}
      >
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
          <Button className={classes.button}
              color="primary"
              onClick={this.props.onRequestClose}
              raised
          >
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
  connect(
    state => ({
      uiTheme: state.theme,
    }),
    dispatch => ({
      setPaletteType: (type) => dispatch(setPaletteType(type))
    })),
)(SettingsDialog);
