import React, { Component } from 'react';
import { connect } from "react-redux";
import Container from "./components/container";
import StatusBar from "./components/statusbar";
import SideMenu from "./components/sidemenu";
import Editor from "./components/editor";
import ConfigPanel from "./components/configpanel/config-panel";
import { Divider } from "@blueprintjs/core";
import SplitPane from "react-split-pane";
import Banner from "./graphwalker.inline.svg";
import './style.css';

class Application extends Component {

  render() {
    if (this.props.showBanner) {
      return (
        <Container column>
          <Container>
            <SideMenu/>
            <Banner className="banner"/>
          </Container>
        </Container>
      )
    } else {
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
}

const mapStateToProps = ({ test: { models }}) => {
  return {
    showBanner: models.length === 0
  }
};

export default connect(mapStateToProps)(Application);
