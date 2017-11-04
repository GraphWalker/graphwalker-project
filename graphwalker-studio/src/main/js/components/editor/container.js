import React, { Component } from 'react';

export default class EditorContainer extends Component {
  render() {
    return (
      <div style={{ padding: 0, background: '#fff', height: '100%' }}>{this.props.children}</div>
    );
  }
}
