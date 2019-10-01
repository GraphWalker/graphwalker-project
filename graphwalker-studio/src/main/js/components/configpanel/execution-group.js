import React, { Component } from 'react';
import { connect } from "react-redux";
import {FormGroup, InputGroup, Slider} from "@blueprintjs/core";
import { setExecutionDelay, updateModel } from "../../redux/actions";
import Group from "./group";

class ExecutionGroup extends Component {
  render() {
    const { delay, generator, setExecutionDelay, updateModel } = this.props;
    return (
      <Group name="Execution" isOpen={true}>
        <FormGroup label="Generator">
          <InputGroup placeholder="Model Generator" value={generator} onChange={({ target: { value }}) => updateModel('generator', value)}/>
        </FormGroup>
        <FormGroup label="Delay" labelInfo="(ms)">
          <div>
            <Slider min={0} max={500} stepSize={1} labelRenderer={false} value={delay} onChange={setExecutionDelay}/>
          </div>
        </FormGroup>
      </Group>
    )
  }
}

const mapStateToProps = ({ test: { models, selectedModelIndex }, execution: { delay } }) => {
  const { generator } = models[selectedModelIndex];
  return {
    delay,
    generator
  }
};

export default connect(mapStateToProps, { setExecutionDelay, updateModel })(ExecutionGroup);
