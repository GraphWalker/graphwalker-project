import React, { Component } from 'react';
import { connect } from "react-redux";
import { FormGroup, Slider } from "@blueprintjs/core";
import { setExecutionDelay } from "../../redux/actions";

class ExecutionGroup extends Component {
  render() {
    const { delay, setExecutionDelay } = this.props;
    return (
      <FormGroup label="Execution Delay" labelInfo="(ms)">
        <div>
          <Slider min={0} max={500} stepSize={1} labelRenderer={false} value={delay} onChange={setExecutionDelay}/>
        </div>
      </FormGroup>
    )
  }
}

const mapStateToProps = ({ execution: { delay } }) => {
  return {
    delay
  }
};

export default connect(mapStateToProps, { setExecutionDelay })(ExecutionGroup);
