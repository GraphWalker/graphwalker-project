import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import { connect } from 'react-redux';
import compose from 'recompose/compose';
import Tabs, { Tab } from 'material-ui/Tabs';
import { setActiveModel } from '../../redux/actions/project';

const styles = theme => ({
  root: {
    flexGrow: 1,
    width: '100%',
    backgroundColor: theme.palette.background.paper,
    color: theme.palette.text.primary,
  },
});

class ModelSelector extends Component {

  static propTypes = {
    activeModelId: PropTypes.number,
    classes: PropTypes.object,
    models: PropTypes.array,
    setActiveModel: PropTypes.func,
  };

  handleChange = (event, value) => {
    console.log(value);
    this.props.setActiveModel(value);
  };

  render() {
    const { classes } = this.props;
    const tabs = [];
    this.props.models.forEach((model, index) => tabs.push(
      <Tab key={index}
          label={model.name}
          value={index}
      />
    ));
    return (
      <div className={classes.root}>
        <Tabs indicatorColor="accent"
            onChange={this.handleChange}
            scrollButtons="on"
            scrollable
            textColor="accent"
            value={this.props.activeModelId}
        >
          { tabs }
        </Tabs>
      </div>
    );
  }
}

export default compose(
  withStyles(styles, {
    withTheme: true,
  }),
  connect(state => ({
    activeModelId: state.project.activeModelId,
    models: state.project.models,
  }),
  dispatch => ({
    setActiveModel: (id) => dispatch(setActiveModel(id))
  })),
)(ModelSelector);
