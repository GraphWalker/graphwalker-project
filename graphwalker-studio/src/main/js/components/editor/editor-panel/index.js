import React, { Component, createRef } from 'react';
import styled from 'styled-components';
import { ResizeSensor } from "@blueprintjs/core";
import EditorComponent from "./editor-component";

const EditorPanelContainer = styled.div`
  height: 100%;
  overflow: hidden;
`;

export default class EditorPanel extends Component {

  render() {
    return (
      <EditorPanelContainer>
        <EditorComponent/>
      </EditorPanelContainer>
    );
  }
}
