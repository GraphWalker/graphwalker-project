import React, { Component } from 'react';
import { connect } from "react-redux";
import { FormGroup, Slider } from "@blueprintjs/core";
import { updateExecution } from "../../redux/actions";

class ExecutionGroup extends Component {
  render() {
    const { delay, updateExecution } = this.props;
    return (
      <FormGroup label="Execution Delay" labelInfo="(ms)">
        <div>
          <Slider min={0} max={500} stepSize={1} labelRenderer={false} value={delay} onChange={updateExecution}/>
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

export default connect(mapStateToProps, { updateExecution })(ExecutionGroup);
