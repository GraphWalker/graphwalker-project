import React, { Component } from 'react';
import { Tabs, Button } from 'antd';
import './styles.less';

const TabPane = Tabs.TabPane;

export class GraphEditor extends Component {

  constructor(props) {
    super(props);
    this.newTabIndex = 0;
    const panes = [
      { title: 'Tab 1', content: 'Content of Tab 1', key: '1', closable: false },
      { title: 'Tab 2', content: 'Content of Tab 2', key: '2' },
    ];
    this.state = {
      activeKey: panes[0].key,
      panes,
    };
    this.operations = <Button>Extra Action</Button>;
  }

  onChange(activeKey) {
    this.setState({ activeKey });
  }

  onEdit(targetKey, action) {
    switch (action) {
      case 'add': this.add(); break;
      case 'remove': this.remove(targetKey); break;
    }
  }

  add() {
    const panes = this.state.panes;
    const activeKey = `newTab${this.newTabIndex++}`;
    panes.push({ title: 'New Tab', content: 'Content of new Tab', key: activeKey });
    this.setState({ panes, activeKey });
  }

  remove(targetKey) {
    let activeKey = this.state.activeKey;
    let lastIndex = 0;
    this.state.panes.forEach((pane, i) => {
      if (pane.key === targetKey) {
        lastIndex = i - 1;
      }
    });
    const panes = this.state.panes.filter(pane => pane.key !== targetKey);
    if (lastIndex >= 0 && activeKey === targetKey) {
      activeKey = panes[lastIndex].key;
    }
    this.setState({ panes, activeKey });
  }

  render() {
    return (
      <Tabs
        onChange={(active) => this.onChange(active)}
        activeKey={this.state.activeKey}
        animated={false}
        onEdit={(targetKey, action) => this.onEdit(targetKey, action)}
      >
        {this.state.panes.map(pane =>
          <TabPane tab={pane.title} key={pane.key} closable={pane.closable}>
            {pane.content}
          </TabPane>
        )}
      </Tabs>
    )
  }
}
