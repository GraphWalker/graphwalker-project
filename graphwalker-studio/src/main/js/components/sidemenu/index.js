import React, { Component } from 'react';
import './style.css';
import MenuButton from "./menu-button";
import {Divider} from "@blueprintjs/core";

export default class SideMenu extends Component {
  render() {
    return (
      <aside className="sidemenu">
        <MenuButton icon="folder-new" tooltip="New test"/>
        <MenuButton icon="folder-open" tooltip="Load test"/>
        <MenuButton icon="floppy-disk" tooltip="Save test" />
        <Divider/>
        <MenuButton icon="add" tooltip="Add model" />
        <MenuButton icon="layout-auto" tooltip="Do layout" />
        <Divider/>
        <MenuButton icon="play" tooltip="Run test" />
        <MenuButton icon="stop" tooltip="Reset test" />
        <MenuButton icon="pause" tooltip="Pause test" />
        <MenuButton icon="step-forward" tooltip="Step test" />
        <Divider/>
        <MenuButton icon="cog" tooltip="Settings" />
      </aside>
    )
  }
}
