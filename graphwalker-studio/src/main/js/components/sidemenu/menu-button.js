import React, { Component } from 'react';
import { Icon, Tooltip, Position } from '@blueprintjs/core';

export default class MenuButton extends Component {

  render() {
    return (
      <div className="sidemenu-button">
        <Tooltip content={this.props.tooltip} position={Position.RIGHT}>
          <Icon  iconSize={18} color="#ABABAB" {...this.props} />
        </Tooltip>
      </div>
    )
  }
}
