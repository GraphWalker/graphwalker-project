import React, { Component } from 'react';
import PropTypes from 'prop-types';
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

  static propTypes = {
    classes: PropTypes.object,
    onRequestClose: PropTypes.func,
    open: PropTypes.func,
    setPaletteType: PropTypes.func,
    uiTheme: PropTypes.object,
  };

  toggleThemeType = () => {
    this.props.setPaletteType(this.props.uiTheme.paletteType === 'light' ? 'dark' : 'light');
  };

  isDarkTheme = () => {
    return this.props.uiTheme.paletteType === 'dark';
  };

  render() {
    const { classes } = this.props;
    return (
      <Dialog onRequestClose={this.props.onRequestClose}
          open={this.props.open}
      >
        <DialogTitle>{'Settings'}</DialogTitle>
        <DialogContent>
          <FormGroup>
            <FormControlLabel
                control={
                <Switch
                    checked={this.isDarkTheme()}
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
            {'OK'}
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
