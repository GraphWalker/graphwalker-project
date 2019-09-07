import React, { Component } from 'react';
import styled from 'styled-components';
import {Divider, Icon} from "@blueprintjs/core";
import ModelGroup from "./model-group";
import ElementGroup from "./element-group";
import "./style.css"
import ExecutionGroup from "./execution-group";

const PanelContainer = styled.div`
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #F3F3F3;
`;

const PanelHeader = styled.div`
  display: flex;
  align-items: center;
  min-height: 30px;
  background-color: #2C2C2C;
  color: #EDEDED;
  padding-left: 0.5rem;
`;

const PanelLabel = styled.span`
  padding-left: 0.5rem;
  padding-right: 0.75rem;
`;

const PanelContent = styled.div`
  padding: 1rem;
  overflow-y: auto;
`;

export default class ConfigPanel extends Component {
  render() {
    return (
      <PanelContainer>
        <PanelHeader>
          <Icon iconSize={14} icon="properties"/>
          <PanelLabel>Properties</PanelLabel>
        </PanelHeader>
        <PanelContent>
          <ModelGroup/>
          <ElementGroup/>
          <ExecutionGroup/>
        </PanelContent>
      </PanelContainer>
    );
  }
}
