import React, { Component } from 'react';
import { connect } from "react-redux";
import MenuButton from "./menu-button";
import { Divider } from "@blueprintjs/core";
import { addModel, loadTest, newTest } from "../../redux/actions";
import './style.css';

class SideMenu extends Component {

  constructor(props) {
    super(props)
    this.inputOpenFileRef = React.createRef()
  }
  openTest = (event) => {
    event.preventDefault();
    this.inputOpenFileRef.current.click();
  }

  onOpenTest = (event) => {
    const fileReader = new FileReader();
    fileReader.onload = ({ target: { result }}) => this.props.loadTest(result);
    fileReader.readAsText(event.target.files[0]);
  }


  render() {
    return (
      <aside className="sidemenu">
        <MenuButton icon="folder-new" tooltip="New test" onClick={this.props.newTest}/>
        <input ref={this.inputOpenFileRef} type="file" accept=".json,.graphml" style={{display:"none"}} onChange={this.onOpenTest}/>
        <MenuButton icon="folder-open" tooltip="Load test" onClick={this.openTest}/>
        <MenuButton icon="floppy-disk" tooltip="Save test" />
        <Divider/>
        <MenuButton icon="add" tooltip="Add model" onClick={this.props.addModel}/>
        <MenuButton icon="layout-auto" tooltip="Do layout" />
        <Divider/>
        <MenuButton icon="play" tooltip="Run test" />
        <MenuButton icon="stop" tooltip="Reset test" />
        <MenuButton icon="pause" tooltip="Pause test" />
        <MenuButton icon="step-forward" tooltip="Step test" />
        <Divider/>
      </aside>
    )
  }
}

export default connect(null, { addModel, loadTest, newTest })(SideMenu);
