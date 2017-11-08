import React, { Component } from 'react';
import { withStyles } from 'material-ui/styles';
import Divider from 'material-ui/Divider';
import IconButton from 'material-ui/IconButton';
import ListIcon from 'material-ui-icons/List';
import Table, { TableBody, TableCell, TableRow } from 'material-ui/Table';
import Typography from 'material-ui/Typography';

const styles = theme => ({
  container: {
    height: '100%',
  },
  header: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-start',
    padding: '0 8px',
    ...theme.mixins.toolbar,
  }
});

class PropertiesTable extends Component {
  render() {
    const { classes } = this.props;
    return (
      <div className={classes.container}>
        <div className={classes.header}>
          <IconButton onClick={this.handleDrawerClose}>
            <ListIcon/>
          </IconButton>
          <Typography noWrap>
            Properties
          </Typography>
        </div>
        <Divider/>
        <Table>
          <TableBody>
            <TableRow>
              <TableCell>name</TableCell>
              <TableCell>test 1</TableCell>
            </TableRow>
            <TableRow>
              <TableCell>model</TableCell>
              <TableCell>one</TableCell>
            </TableRow>
          </TableBody>
        </Table>
        <Divider/>
      </div>
    );
  }
}

export default withStyles(styles, { withTheme: true })(PropertiesTable);
