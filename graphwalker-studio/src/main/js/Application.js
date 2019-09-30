import React, { Component } from 'react';
import { connect } from "react-redux";
import Container from "./components/container";
import StatusBar from "./components/statusbar";
import SideMenu from "./components/sidemenu";
import Editor from "./components/editor";
import ConfigPanel from "./components/configpanel";
import { Divider } from "@blueprintjs/core";
import Banner from "./graphwalker.inline.svg";
import PanelGroup from "react-panelgroup";
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
            {this.props.showProperties ?
              <PanelGroup borderColor="#F3F3F3" panelWidths={[{ size: 400, resize: "dynamic" }, { resize: "stretch" }]}>
                <ConfigPanel/>
                <Editor/>
              </PanelGroup>
              :
              <PanelGroup borderColor="#F3F3F3" panelWidths={[{ resize: "stretch" }]}>
                <Editor/>
              </PanelGroup>
            }
          </Container>
          <StatusBar/>
        </Container>
      )
    }
  }
}

const mapStateToProps = ({ test: { models }, editor: { showProperties }}) => {
  return {
    showBanner: models.length === 0,
    showProperties
  }
};

export default connect(mapStateToProps)(Application);
