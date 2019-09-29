import React, { Component } from 'react';
import styled from 'styled-components';
import {Collapse, Icon} from "@blueprintjs/core";

const GroupHeader = styled.div`
  display: flex;
  align-items: center;
  background-color: #DCDCDC;
  color: #616161;
  text-transform: uppercase;
  font-size: 10px;
  font-weight: bold;
  padding: 2px;
`;

const GroupContent = styled.div`
  padding: 1rem;
`;

export default class Group extends Component {

  render() {
    return (
      <>
        {this.props.isOpen ?
          <GroupHeader>
            <Icon icon="chevron-down"/>
            <span>{this.props.name}</span>
          </GroupHeader>
          :
          <GroupHeader>
            <Icon icon="chevron-right"/>
            <span>{this.props.name}</span>
          </GroupHeader>
        }
        <GroupContent>
          <Collapse isOpen={this.props.isOpen}>
            {this.props.children}
          </Collapse>
        </GroupContent>
      </>
    )
  }
}
