import React, { Component } from 'react';
import { connect } from "react-redux";
import ElementGroup from "./element-group";
import ModelGroup from "./model-group";
import ExecutionGroup from "./execution-group";
import './style.css';

class ConfigPanel extends Component {

  render() {
    if (this.props.hasModels) {
      return (
        <div className="config-panel">
          <ModelGroup/>
          <ElementGroup/>
          <ExecutionGroup/>
        </div>
      )
    } else {
      return null;
    }
  }
}

const mapStateToProps = ({ test: { models }}) => {
  return {
    hasModels: models.length > 0
  }
};

export default connect(mapStateToProps)(ConfigPanel);
