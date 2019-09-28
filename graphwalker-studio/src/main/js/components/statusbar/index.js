import React, { Component } from 'react';
import { connect } from "react-redux";
import { ProgressBar, Intent } from "@blueprintjs/core";
import './style.css';

class StatusBar extends Component {
  render() {
    console.log(this.props.fulfillment)
    return (
      <footer className="statusbar">
        { this.props.isVisible ? <ProgressBar value={this.props.fulfillment} intent={Intent.SUCCESS}/> : null }
      </footer>
    )
  }
}

const mapStateToProps = ({ test: { models }, execution: { running, paused, fulfillment }}) => ({
  isVisible: running || paused,
  fulfillment: (Object.values(fulfillment).reduce((a, b) => a + b, 0) / models.length) || 0
});

export default connect(mapStateToProps)(StatusBar);
