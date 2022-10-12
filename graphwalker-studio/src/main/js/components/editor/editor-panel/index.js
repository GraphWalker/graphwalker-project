import React, {Component} from 'react';
import styled from 'styled-components';
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
