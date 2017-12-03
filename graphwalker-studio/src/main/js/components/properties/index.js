import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import { connect } from 'react-redux';
import compose from 'recompose/compose';
import Divider from 'material-ui/Divider';
import IconButton from 'material-ui/IconButton';
import ListIcon from 'material-ui-icons/List';
import Table, { TableBody, TableCell, TableRow } from 'material-ui/Table';
import Typography from 'material-ui/Typography';

const styles = theme => ({
  root: {
    height: '100%',
  },
  header: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-start',
    padding: '0 8px',
  },
});

class PropertiesTable extends Component {

  static propTypes = {
    classes: PropTypes.object,
    model: PropTypes.object,
  };

  render() {
    const { classes } = this.props;
    const properties = [];
    Object.keys(this.props.model.properties).forEach((property, key) => properties.push((
      <TableRow key={key}>
        <TableCell>{property}</TableCell>
        <TableCell>{this.props.model.properties[property]}</TableCell>
      </TableRow>
    )));
    return (
      <div className={classes.root}>
        <div className={classes.header}>
          <IconButton onClick={this.handleDrawerClose}>
            <ListIcon/>
          </IconButton>
          <Typography noWrap
              type="subheading"
          >
            Properties
          </Typography>
        </div>
        <Divider/>
        <Table>
          <TableBody>
            { properties }
          </TableBody>
        </Table>
        <Divider/>
      </div>
    );
  }
}

export default compose(
  withStyles(styles, {
    withTheme: true,
  }),
  connect(state => ({
    model: state.project.models[state.project.activeModelId],
  }))
)(PropertiesTable);
