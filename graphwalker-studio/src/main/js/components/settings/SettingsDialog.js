import React, { Component } from 'react';
import Dialog, { DialogTitle } from 'material-ui/Dialog';

export default class SettingsDialog extends Component {
  render() {
    const { ...props } = this.props;
    return (
      <Dialog {...props}>
        <DialogTitle>TODO: Settings</DialogTitle>
      </Dialog>
    );
  }
}
