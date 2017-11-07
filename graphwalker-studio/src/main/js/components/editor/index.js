import React, { Component } from 'react';
import { withStyles } from 'material-ui/styles';
import EditorContainer from './EditorContainer';
import { styles } from '../../styles'

class Editor extends Component {

  render() {
    return (
      <EditorContainer/>
    );
  }
}

export default withStyles(styles, { withTheme: true })(Editor);
