import React, { Component } from 'react';
import { withStyles } from 'material-ui/styles';
import Typography from 'material-ui/Typography';
import { styles } from '../../styles'

class Logo extends Component {
  render() {
    const { classes } = this.props;
    return (
      <div className={classes.drawerHeader}>
        <Typography type="title" noWrap className={classes.logo}>
          GraphWalker
        </Typography>
      </div>
    );
  }
}

export default withStyles(styles, { withTheme: true })(Logo);
