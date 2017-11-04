import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import Divider from 'material-ui/Divider';
import Tabs, { Tab } from 'material-ui/Tabs';
import EditorContainer from './container';

const styles = theme => ({
  root: {
    flexGrow: 1,
    width: '100%',
    height: '100%',
    backgroundColor: theme.palette.background.paper,
  },
});

class Editor extends Component {
  state = {
    value: 0,
  };

  handleChange = (event, value) => {
    this.setState({ value });
  };

  render() {
    const { classes } = this.props;
    const { value } = this.state;
    return (
      <div className={classes.root}>
        <Tabs value={value} onChange={this.handleChange} scrollable scrollButtons="auto">
          <Tab label="New Arrivals in the Longest Text of Nonfiction" />
          <Tab label="Item Two" />
          <Tab label="Item Three" />
          <Tab label="Item Four" />
          <Tab label="Item Five" />
          <Tab label="Item Six" />
          <Tab label="Item Seven" />
        </Tabs>
        <Divider/>
        {value === 0 && <EditorContainer>Item One</EditorContainer>}
        {value === 1 && <EditorContainer>Item Two</EditorContainer>}
        {value === 2 && <EditorContainer>Item Three</EditorContainer>}
        {value === 3 && <EditorContainer>Item Four</EditorContainer>}
        {value === 4 && <EditorContainer>Item Five</EditorContainer>}
        {value === 5 && <EditorContainer>Item Six</EditorContainer>}
        {value === 6 && <EditorContainer>Item Seven</EditorContainer>}
      </div>
    );
  }
}

export default withStyles(styles, { withTheme: true })(Editor);
