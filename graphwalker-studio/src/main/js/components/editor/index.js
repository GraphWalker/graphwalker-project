import React, { Component } from 'react';
import styled from 'styled-components';
import EditorTabs from './editor-tabs';
import EditorPanel from "./editor-panel";

const EditorContainer = styled.div`
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  background-color: #FFFFFF;
`;

export default class Editor extends Component {
  render() {
    return (
      <EditorContainer>
        <EditorTabs/>
        <EditorPanel/>
      </EditorContainer>
    )
  }
}




/*
//import Tabs from 'react-responsive-tabs';
import EditorPanel from "./editor-panel";
import { selectModel, closeModel } from "../../redux/actions";

import './style.css';
import { Icon } from "@blueprintjs/core";
import {Tabs, DragTabList, DragTab, PanelList, Panel, ExtraButton} from 'react-tabtab';
import {simpleSwitch} from 'react-tabtab/lib/helpers/move';
//import * as customStyle from 'react-tabtab/lib/themes/material-design';
import customStyle from './tabs-style';




export default class Editor extends Component {

  constructor(props) {
    super(props);
    this.handleTabChange = this.handleTabChange.bind(this);
    this.handleTabSequenceChange = this.handleTabSequenceChange.bind(this);
    this.state = {
      activeIndex: 0,
    }
  }

  handleTabChange(index) {
    this.setState({activeIndex: index});
  }

  handleTabSequenceChange({oldIndex, newIndex}) {
    const {tabs} = this.state;
    const updateTabs = simpleSwitch(tabs, oldIndex, newIndex);
    this.setState({tabs: updateTabs, activeIndex: newIndex});
  }

  render() {
    const {activeIndex} = this.state;
    return (
      <Tabs activeIndex={activeIndex}
            onTabChange={this.handleTabChange}
            onTabSequenceChange={this.handleTabSequenceChange}
            customStyle={customStyle}
            ExtraButton={
              <ExtraButton className="EXTRA_BUTTON">
                <Icon icon="plus"/>
              </ExtraButton>
            }
      >
        <DragTabList>
          <DragTab closable>DragTab1</DragTab>
          <DragTab closable>DragTab2</DragTab>
        </DragTabList>
        <PanelList>
          <Panel>Content1</Panel>
          <Panel>Content2</Panel>
        </PanelList>
      </Tabs>
    )
  }
}
*/
/*
class Editor extends Component {

  getTabs = () => {
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
    this.props.closeModel(key);
  };

  render() {
    return (
      <div className="editor-panel">
        <Tabs allowRemove removeActiveOnly
              transform={false}
              onRemove={this.onRemoveTab}
              items={this.getTabs()}
              onChange={this.props.selectModel}
        />
      </div>
    )
  }
}

const mapStateToProps = ({ test: { models }}) => {
  return {
    models
  }
};

export default connect(mapStateToProps, { selectModel, closeModel })(Editor);
*/
