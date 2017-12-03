import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import { connect } from 'react-redux';
import compose from 'recompose/compose';
import { openSettings } from '../../redux/actions/settings';
import List, { ListItem, ListItemIcon, ListItemText } from 'material-ui/List';
import SettingsIcon from 'material-ui-icons/Settings';
import SettingsDialog from './SettingsDialog';

const styles = theme => ({
});

class Settings extends Component {

  static propTypes = {
    openSettings: PropTypes.func,
  };

  render() {
    return (
      <div>
        <List>
          <ListItem button onClick={this.props.openSettings}>
            <ListItemIcon>
              <SettingsIcon />
            </ListItemIcon>
            <ListItemText primary="Settings" />
          </ListItem>
        </List>
        <SettingsDialog />
      </div>
    );
  }
}

export default compose(
  withStyles(styles, {
    withTheme: true,
  }),
  connect(state => ({
    }),
    dispatch => ({
      openSettings: () => dispatch(openSettings()),
    })),
)(Settings);
