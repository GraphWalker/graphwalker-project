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
