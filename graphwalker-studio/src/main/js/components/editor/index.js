import React, { Component } from 'react';
import { connect } from "react-redux";
import Tabs from 'react-responsive-tabs';
import EditorPanel from "./editor-panel";
import './style.css';

class Editor extends Component {

  getTabs = () => {
    console.log(this.props.models);
    return this.props.models.map((model, index) => ({
      title: model.name,
      getContent: () => <EditorPanel model={model}/>,
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

const mapStateToProps = ({ test: { models }}) => {
  return {
    models
  }
}

export default connect(mapStateToProps)(Editor);
