import React, { Component } from 'react';
import { Divider } from "@blueprintjs/core";
import FileMenu from "./file-menu";
import ModelMenu from "./model-menu";
import './style.css';

export default class SideMenu extends Component {

  render() {
    return (
      <aside className="sidemenu">
        <FileMenu/>
        <Divider/>
        <ModelMenu/>
        <Divider/>
      </aside>
    )
  }
}
