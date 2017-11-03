import React, { Component } from 'react';
import { Icon } from 'antd';
import './styles.less';

const url = 'https://github.com/GraphWalker/graphwalker-project';

export default class Github extends Component {

  render() {
    return (
      <Icon
        className="github"
        type="github"
        onClick={() => window.open(url, '_blank').focus()}
      />
    )
  }
}
