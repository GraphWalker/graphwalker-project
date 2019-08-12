import React, { Component } from 'react';
import './style.css';

export default class Container extends Component {
  render() {
    const containerType = this.props.column ? "container-column": "container-row";
    return (
      <div className={containerType}>
        {this.props.children}
      </div>
    )
  }
}
