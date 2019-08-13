import React, { Component } from 'react';
import Tabs from 'react-responsive-tabs';
import EditorPanel from "./editor-panel";
import './style.css';

const models = [{ name: 'Model 1', data: '1' }, { name: 'Model 2', data: '2' }];

export default class Editor extends Component {

  getTabs = () => {
    return models.map((model, index) => ({
      title: model.name,
      getContent: () => <EditorPanel/>,
      key: index,
      tabClassName: 'tab',
      panelClassName: 'tab-panel',
    }));
  };

  onRemoveTab = (key, event) => {
    event.stopPropagation();
    console.log('REMOVE');
  };

  render() {
    return (
      <div className="editor-panel">
        <Tabs allowRemove removeActiveOnly
              transform={false}
              onRemove={this.onRemoveTab}
              items={this.getTabs()}
        />
      </div>
    )
  }
}

