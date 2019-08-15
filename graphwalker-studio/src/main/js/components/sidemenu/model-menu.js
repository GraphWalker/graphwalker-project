import React, { Component } from 'react';
import { connect } from "react-redux";
import { Button, ButtonGroup } from "@blueprintjs/core";

class ModelMenu extends Component {
  render() {
    return (
      <ButtonGroup minimal={true} vertical={true} large={true} >
        <Button className="sidemenu-button" disabled={this.props.menuDisabled} icon="layout-auto" />
        <Button className="sidemenu-button" disabled={this.props.menuDisabled} icon="play" />
        <Button className="sidemenu-button" disabled={this.props.menuDisabled} icon="step-forward" />
        <Button className="sidemenu-button" disabled={this.props.menuDisabled} icon="stop" />
      </ButtonGroup>
    )
  }
}

const mapStateToProps = ({ test: { models }}) => {
  return {
    menuDisabled: models.length === 0
  }
}

export default connect(mapStateToProps)(ModelMenu);
