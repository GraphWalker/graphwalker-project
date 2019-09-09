import React, { Component } from 'react';
import { connect } from "react-redux";
import { Button, ButtonGroup } from "@blueprintjs/core";
import { runTest, pauseTest, stepTest, stopTest } from "../../redux/actions";

class ExecutionMenu extends Component {
  render() {
    const { disabled, running, paused, visited, runTest, pauseTest, stepTest, stopTest } = this.props;
    return (
      <ButtonGroup minimal={true} vertical={true} large={true} >
        {running ?
          <Button className="sidemenu-button" disabled={disabled} icon="pause" onClick={pauseTest}/> :
          <Button className="sidemenu-button" disabled={disabled} icon="play" onClick={runTest} />
        }
        <Button className="sidemenu-button" disabled={disabled || !paused} icon="step-forward" onClick={stepTest} />
        <Button className="sidemenu-button" disabled={disabled || !(running || paused || Object.keys(visited).length > 0)} icon="stop" onClick={stopTest} />
      </ButtonGroup>
    )
  }
}

const mapStateToProps = ({ test: { models }, execution: { running, paused, visited }}) => {
  return {
    disabled: models.length === 0,
    running,
    paused,
    visited
  }
}

export default connect(mapStateToProps, { runTest, pauseTest, stepTest, stopTest })(ExecutionMenu);
