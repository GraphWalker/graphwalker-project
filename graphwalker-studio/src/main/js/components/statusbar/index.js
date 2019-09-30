import React, { Component } from 'react';
import { connect } from "react-redux";
import { ProgressBar, Intent, Toast, Toaster, Position } from "@blueprintjs/core";
import { stopTest } from "../../redux/actions";
import './style.css';

class StatusBar extends Component {


  render() {
    return (
      <>
        <footer className={this.props.hasIssues ? "statusbar issues" : this.props.fulfillment >= 1 ? "statusbar complete" : "statusbar"}>
          { this.props.isVisible ? <ProgressBar value={this.props.fulfillment} intent={Intent.SUCCESS}/> : null }
        </footer>
        <Toaster position={Position.TOP_RIGHT}>
          {
            Object.values(this.props.issues)
              .map((issue, index) => <Toast key={index}
                                            intent={Intent.DANGER}
                                            timeout={0}
                                            onDismiss={this.props.stopTest}
                                            message={issue} icon="warning-sign" />)
          }
        </Toaster>
      </>
    )
  }
}

const mapStateToProps = ({ test: { models }, execution: { running, paused, fulfillment, issues }}) => ({
  isVisible: running || paused,
  fulfillment: (Object.values(fulfillment).reduce((a, b) => a + b, 0) / models.length) || 0,
  hasIssues: Object.values(issues).length > 0,
  issues
});

export default connect(mapStateToProps, { stopTest })(StatusBar);
