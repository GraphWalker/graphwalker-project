import React, { Component } from 'react';
import { withStyles } from 'material-ui/styles';
import Typography from 'material-ui/Typography';
import IconButton from 'material-ui/IconButton';
import AcUnit from 'material-ui-icons/AcUnit';
import { styles } from '../../styles'

class Logo extends Component {
  render() {
    const { classes } = this.props;
    return (
      <div className={classes.drawerHeader}>
        <IconButton>
          <AcUnit />
        </IconButton>
        <Typography type="title" color="inherit" noWrap>
          GraphWalker
        </Typography>
      </div>
    );
  }
}

export default withStyles(styles, { withTheme: true })(Logo);
