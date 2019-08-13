import React, { Component } from 'react';
import { Icon, Tooltip, Position } from '@blueprintjs/core';

export default class MenuButton extends Component {

  constructor(props) {
    super(props);
    this.state = {
      hover: false,
    }
  }

  toggleHover = () => {
    this.setState({
      hover: !this.state.hover
    });
  }

  render() {
    let color = "#EDEDED";
    if (this.state.hover) {
      color = "#FFFFFF"
    }
    return (
      <div className="sidemenu-button" onMouseEnter={this.toggleHover} onMouseLeave={this.toggleHover}>
        <Tooltip content={this.props.tooltip} position={Position.RIGHT}>
          <Icon  iconSize={18} color={color} {...this.props}  />
        </Tooltip>
      </div>
    )
  }
}
