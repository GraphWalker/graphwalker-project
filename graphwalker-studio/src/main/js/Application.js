import React, { Component } from 'react';
import Container from "./components/container";
import StatusBar from "./components/statusbar";
import SideMenu from "./components/sidemenu";
import Editor from "./components/editor";
import ConfigPanel from "./components/configpanel/config-panel";
import { Divider } from "@blueprintjs/core";
import SplitPane from "react-split-pane";

export default class Application extends Component {

  render() {
    return (
      <Container column>
        <Container>
          <SideMenu/>
          <Editor/>
          <Divider/>
          <ConfigPanel/>
        </Container>
        <StatusBar/>
      </Container>
    )
  }
}
