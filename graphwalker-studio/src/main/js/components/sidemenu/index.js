import React, { Component } from 'react';
import { connect } from "react-redux";
import MenuButton from "./menu-button";
import { Divider } from "@blueprintjs/core";
import { addModel, loadTest, newTest } from "../../redux/actions";
import './style.css';

class SideMenu extends Component {

  constructor(props) {
    super(props);
    this.openFileRef = React.createRef();
    this.saveFileRef = React.createRef();
  }

  openTest = (event) => {
    event.preventDefault();
    this.openFileRef.current.click();
  }

  onOpenTest = (event) => {
    const fileReader = new FileReader();
    fileReader.onload = ({ target: { result }}) => this.props.loadTest(result);
    fileReader.readAsText(event.target.files[0]);
  }

  saveTest = (event) => {
    event.preventDefault();

    if (this.props.test.models.length > 0) {
      const data = new Blob([JSON.stringify(this.props.test)], {
        type: 'text/plain'
      });
      this.saveFileRef.current.href = window.URL.createObjectURL(data);
      this.saveFileRef.current.click();
      window.URL.revokeObjectURL(this.saveFileRef.current.href);
    }
  }

  render() {
    return (
      <aside className="sidemenu">
        <MenuButton icon="folder-new" tooltip="New test" onClick={this.props.newTest}/>
        <input ref={this.openFileRef} type="file" accept=".json,.graphml" style={{display:"none"}} onChange={this.onOpenTest}/>
        <MenuButton icon="folder-open" tooltip="Load test" onClick={this.openTest}/>
        <a ref={this.saveFileRef} style={{display:"none"}} download={"test.json"}>d</a>
        <MenuButton icon="floppy-disk" tooltip="Save test" onClick={this.saveTest} />
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

const mapStateToProps = ({ test }) => {
  return {
    test
  }
}

export default connect(mapStateToProps, { addModel, loadTest, newTest })(SideMenu);
