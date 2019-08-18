import React, { Component } from 'react';
import { connect } from "react-redux";
import styled from 'styled-components';
import { Button, Icon, Menu, MenuDivider, MenuItem, Popover, Position } from "@blueprintjs/core";
import { addModel, selectModel, closeModel, closeAllModels } from "../../../redux/actions";
import "./style.css";

const TabsContainer = styled.div`
  display: flex;
  height: 30px;
`;

const TabList = styled.section`
  display: flex;
  overflow-x: auto;
  ::-webkit-scrollbar {
    display: none;
  }
  background-color: #F3F3F3;
`;

const Tab = styled.div`  
  display: flex;
  align-items: center;
  min-width: fit-content;
  background-color: #ECECEC;
  color: #5c7080;
  padding: 0.5rem 0.5rem 0.5rem 1rem;
  border-right: 1px solid #FFFFFF;
  cursor: pointer;
  ${props => props.active ?
  `
    background-color: #FFFFFF;
  `: null}
`;

const TabLabel = styled.span`
  white-space: nowrap;
  min-width: 80px;
  user-select: none;
  padding-left: 0.5rem;
  padding-right: 0.75rem;
`;

const TabCloseAction = styled.div`
  display: flex;
  z-order: 1;
`;

const TabActionList = styled.div`
  display: flex;
  flex: 1;
  justify-content: flex-end;
  align-items: center;
  background-color: #F3F3F3;
`;

const TabAction = styled.div`
  display: flex;
  justify-content: center;
  align-self: center;
  min-width: 30px;
`;

class EditorTabs extends Component {

  onRemoveTab = (event, index) => {
    event.stopPropagation();
    this.props.closeModel(index);
  };

  render() {
    const { models, selectedModelIndex, addModel, selectModel, closeModel, closeAllModels } = this.props;
    const moreMenu = (
      <Menu>
        <MenuItem icon="cross" text="Close All" onClick={closeAllModels} />
      </Menu>
    );
    const tabs = models.map((model, index) => (
      <Tab key={index} onClick={() => selectModel(index)} active={selectedModelIndex === index}>
        <Icon iconSize={10} icon="graph" />
        <TabLabel>{model.name}</TabLabel>
        <TabCloseAction>
          <Icon iconSize={10} icon="cross" onClick={(event) => this.onRemoveTab(event, index)}/>
        </TabCloseAction>
      </Tab>
    ));
    return (
      <TabsContainer>
        <TabList>
          {tabs}
        </TabList>
        <TabActionList>
          <Button className="tabs-button" icon="plus" onClick={addModel}/>
          <Popover content={moreMenu} position={Position.BOTTOM_LEFT} minimal boundary="viewport">
            <Button className="tabs-button" icon="more"/>
          </Popover>
        </TabActionList>
      </TabsContainer>
    )
  }
}

const mapStateToProps = ({ test: { models, selectedModelIndex }}) => {
  return {
    models,
    selectedModelIndex
  }
};

export default connect(mapStateToProps, { addModel, selectModel, closeModel, closeAllModels })(EditorTabs);
