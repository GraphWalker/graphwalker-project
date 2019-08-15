import React, { Component } from 'react';
import { connect } from "react-redux";
import { Button, ButtonGroup } from "@blueprintjs/core";
import {addModel, loadTest, newTest} from "../../redux/actions";

class FileMenu extends Component {

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
      <ButtonGroup minimal={true} vertical={true} large={true}>
        <Button className="sidemenu-button" icon="plus" onClick={this.props.addModel} />
        <input ref={this.openFileRef} type="file" accept=".json,.graphml" style={{display:"none"}} onChange={this.onOpenTest}/>
        <Button className="sidemenu-button" icon="folder-open" onClick={this.openTest}/>
        <a ref={this.saveFileRef} style={{display:"none"}} download={"test.json"}/>
        <Button className="sidemenu-button" icon="floppy-disk" onClick={this.saveTest} disabled={this.props.saveDisabled}/>
      </ButtonGroup>
    )
  }
}

const mapStateToProps = ({ test }) => {
  return {
    test,
    saveDisabled: test.models.length === 0
  }
}

export default connect(mapStateToProps, { addModel, loadTest, newTest })(FileMenu);
