import React, { Component } from 'react';
import { Divider } from "@blueprintjs/core";
import FileMenu from "./file-menu";
import ExecutionMenu from "./execution-menu";
import EditorMenu from "./editor-menu";
import './style.css';

export default class SideMenu extends Component {

  render() {
    return (
      <aside className="sidemenu">
        <FileMenu/>
        <Divider/>
        <ExecutionMenu/>
        <Divider/>
        <EditorMenu/>
      </aside>
    )
  }
}
