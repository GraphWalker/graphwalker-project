import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import { connect } from 'react-redux';
import compose from 'recompose/compose';
import classNames from 'classnames';
import Drawer from 'material-ui/Drawer';
import Divider from 'material-ui/Divider';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import IconButton from 'material-ui/IconButton';
import MenuIcon from 'material-ui-icons/Menu';
import ChevronLeftIcon from 'material-ui-icons/ChevronLeft';
import { Controller, EditorContainer, Fullscreen, GitHubIcon, Logo, ModelSelector, Project, PropertiesTable, Settings } from "../components";
import { openMenuDrawer, closeMenuDrawer } from '../redux/actions/layout';
import { styles } from './styles';

class Studio extends Component {

  static propTypes = {
    classes: PropTypes.object.isRequired,
    closeMenuDrawer: PropTypes.func,
    isMenuDrawerOpen: PropTypes.bool,
    openMenuDrawer: PropTypes.func,
    theme: PropTypes.object.isRequired,
  };

  render() {
    const { classes, closeMenuDrawer, isMenuDrawerOpen, openMenuDrawer } = this.props;
    return (
      <div className={classes.root}>
        <div className={classes.appFrame}>
          <AppBar className={classNames(classes.appBar, isMenuDrawerOpen && classes.appBarShift)}>
            <Toolbar disableGutters>
              <IconButton
                  aria-label="open drawer"
                  className={classNames(classes.menuButton, isMenuDrawerOpen && classes.hide)}
                  color="contrast"
                  onClick={openMenuDrawer}
              >
                <MenuIcon />
              </IconButton>
              <Controller />
              <div className={classes.spacer} />
              <Fullscreen/>
              <GitHubIcon color="contrast"/>
            </Toolbar>
          </AppBar>
          <Drawer
              classes={{
              paper: classNames(classes.drawerPaper, !isMenuDrawerOpen && classes.drawerPaperClose),
            }}
              open={isMenuDrawerOpen}
              type="permanent"
          >
            <div className={classes.drawerInner}>
              <div className={classes.drawerHeader}>
                <Logo/>
                <div className={classes.spacer} />
                <IconButton onClick={closeMenuDrawer}>
                  <ChevronLeftIcon />
                </IconButton>
              </div>
              <Divider/>
              <Project/>
              <Divider/>
              <div className={classes.spacer2} />
              <Divider/>
              <Settings/>
            </div>
          </Drawer>
          <main className={classNames(classes.content, isMenuDrawerOpen && classes.contentShift)}>
            <ModelSelector/>
            <Divider/>
            <div style={{ padding: 0, height: '100%', width: '100%', background: '#fff' }}>
              <div style={{ float: 'left', padding: 0, background: '#fff', height: '100%', width: 'calc(100% - 340px)' }}>
                <EditorContainer/>
              </div>
              <Drawer anchor="right" classes={{
                    paper: classNames(classes.propertiesDrawerPaper),
                    docked: classNames(classes.propertiesDrawerDocked)
                  }}
                  open
                  type="permanent"
              >
                  <PropertiesTable />
              </Drawer>
            </div>
          </main>
        </div>
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
    isMenuDrawerOpen: state.layout.isMenuDrawerOpen,
    models: state.project.models,
  }),
  dispatch => ({
    closeMenuDrawer: () => dispatch(closeMenuDrawer()),
    openMenuDrawer: () => dispatch(openMenuDrawer()),
  })),
)(Studio);
