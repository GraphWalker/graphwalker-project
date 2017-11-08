import React, { Component } from 'react';
import { withStyles } from 'material-ui/styles';
import Typography from 'material-ui/Typography';

const styles = theme => ({
  container: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-end',
    padding: '0 8px',
    ...theme.mixins.toolbar,
  },
  logo: {
    textTransform: 'uppercase',
    fontVariant: 'small-caps',
    fontWeight: 'bold',
    fontSize: '1.1rem',
  }
});

class Logo extends Component {
  render() {
    const { classes } = this.props;
    return (
      <div className={classes.container}>
        <Typography type="title" noWrap className={classes.logo}>
          GraphWalker
        </Typography>
      </div>
    );
  }
}

export default withStyles(styles, { withTheme: true })(Logo);
