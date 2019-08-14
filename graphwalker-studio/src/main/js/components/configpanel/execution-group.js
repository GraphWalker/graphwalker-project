import React, { Component } from 'react';
import { connect } from "react-redux";
import { FormGroup, Slider } from "@blueprintjs/core";

class ExecutionGroup extends Component {
  render() {
    return (
      <FormGroup label="Execution Delay" labelInfo="(ms)">
        <div>
          <Slider min={0} max={500} stepSize={1} labelRenderer={false} value={this.props.delay}/>
        </div>
      </FormGroup>
    )
  }
}

const mapStateToProps = ({ test: { execution: { delay } }}) => {
  return {
    delay
  }
};

export default connect(mapStateToProps)(ExecutionGroup);
