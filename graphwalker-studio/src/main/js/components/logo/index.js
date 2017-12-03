import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import Toolbar from 'material-ui/Toolbar';
import Typography from 'material-ui/Typography';

const styles = theme => ({
  toolbar: {
    flexGrow: 1,
    flexDirection: 'column',
    alignItems: 'flex-start',
    justifyContent: 'center',
  },
  title: {
    color: theme.palette.primary[500],
  },
  version: {
    paddingLeft: '5px',
  }
});

class Logo extends Component {

  static propTypes = {
    classes: PropTypes.object,
  };

  render() {
    const { classes } = this.props;
    return (
      <Toolbar className={classes.toolbar}>
        <Typography className={classes.title}
            color="inherit"
            type="title"
        >
          {'GraphWalker'}
        </Typography>
        <Typography className={classes.version}
            type="caption"
        >
          {'4.0.0-SNAPSHOT'}
        </Typography>
      </Toolbar>
    );
  }
}

export default withStyles(styles, { withTheme: true })(Logo);
