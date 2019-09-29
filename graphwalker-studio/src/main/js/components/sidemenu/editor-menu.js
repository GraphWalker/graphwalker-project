import React, { Component } from 'react';
import { connect } from "react-redux";
import { Button, ButtonGroup } from "@blueprintjs/core";
import { toggleProperties} from "../../redux/actions";

class EditorMenu extends Component {

  render() {
    return (
      <ButtonGroup minimal={true} vertical={true} large={true}>
        <Button disabled={this.props.isMenuDisabled} className="sidemenu-button" icon="properties" onClick={ this.props.toggleProperties } />
      </ButtonGroup>
    );
  }
}

const mapStateToProps = ({ test: { models }}) => {
  return {
    isMenuDisabled: models.length === 0
  }
}

export default connect(mapStateToProps, { toggleProperties })(EditorMenu);
