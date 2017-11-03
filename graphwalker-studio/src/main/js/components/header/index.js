import React, { Component } from 'react';
import { Layout, Icon } from 'antd';
import Github from '../github';
import './styles.less';

const { Header } = Layout;

export class StudioHeader extends Component {

  render() {
    return (
      <Header style={{ background: '#fff', padding: 0 }}>
        <Icon
          className="trigger"
          type={this.props.collapsed ? 'menu-unfold' : 'menu-fold'}
          onClick={this.props.toggle}
        />
        <Github />
      </Header>
    )
  }
}
