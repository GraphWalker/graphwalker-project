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
  width: 100%;
  background-color: #F3F3F3;
  color: #6F6F6F;
`;

const PanelHeader = styled.div`
  display: flex;
  align-items: center;
  min-height: 30px;
  padding-left: 0.5rem;
`;

const PanelLabel = styled.span`
  padding-left: 0.5rem;
  padding-right: 0.75rem;
`;

const PanelContent = styled.div`
  overflow-y: auto;
`;

export default class ConfigPanel extends Component {
  render() {
    return (
      <PanelContainer>
        <PanelHeader>
          <PanelLabel>PROPERTIES</PanelLabel>
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
