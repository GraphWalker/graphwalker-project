import React, { Component } from 'react';
import Table, { TableBody, TableCell, TableRow } from 'material-ui/Table';

export default class PropertiesTable extends Component {
  render() {
    return (
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
    );
  }
}
