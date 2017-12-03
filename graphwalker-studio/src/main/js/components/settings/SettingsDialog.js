import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import { connect } from 'react-redux';
import compose from 'recompose/compose';
import Button from 'material-ui/Button';
import Dialog, { DialogTitle, DialogContent, DialogActions } from 'material-ui/Dialog';
import { FormControlLabel, FormGroup } from 'material-ui/Form';
import Switch from 'material-ui/Switch';
import { closeSettings } from '../../redux/actions/settings';
import { setPaletteType } from '../../redux/actions/theme';

const styles = theme => ({
  button: {
    margin: theme.spacing.unit,
  }
});

class SettingsDialog extends Component {

  static propTypes = {
    classes: PropTypes.object,
    closeSettings: PropTypes.func,
    setPaletteType: PropTypes.func,
    showModal: PropTypes.bool,
    uiTheme: PropTypes.object,
  };

  toggleThemeType = () => {
    this.props.setPaletteType(this.props.uiTheme.paletteType === 'light' ? 'dark' : 'light');
  };

  isDarkTheme = () => {
    return this.props.uiTheme.paletteType === 'dark';
  };

  render() {
    const { classes, closeSettings, showModal } = this.props;
    return (
      <Dialog onRequestClose={closeSettings} open={showModal}>
        <DialogTitle>{'Settings'}</DialogTitle>
        <DialogContent>
          <FormGroup>
            <FormControlLabel
                control={<Switch checked={this.isDarkTheme()} onChange={this.toggleThemeType}/>}
                label="Toggle light/dark theme"
            />
          </FormGroup>
        </DialogContent>
        <DialogActions>
          <Button className={classes.button} color="primary" onClick={closeSettings} raised>
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
      showModal: state.settings.showModal,
      uiTheme: state.theme,
    }),
    dispatch => ({
      closeSettings: () => dispatch(closeSettings()),
      setPaletteType: (type) => dispatch(setPaletteType(type))
    })),
)(SettingsDialog);
