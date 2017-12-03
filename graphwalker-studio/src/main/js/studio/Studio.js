import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';

import classNames from 'classnames';
import Drawer from 'material-ui/Drawer';
import Divider from 'material-ui/Divider';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import IconButton from 'material-ui/IconButton';
import MenuIcon from 'material-ui-icons/Menu';
import ChevronLeftIcon from 'material-ui-icons/ChevronLeft';
import { Controller, EditorContainer, Fullscreen, GitHubIcon, Logo, ModelSelector, Project, PropertiesTable, Settings } from "../components";
import { styles } from './styles';

class Studio extends Component {

  static propTypes = {
    classes: PropTypes.object.isRequired,
    theme: PropTypes.object.isRequired,
  };

  state = {
    open: false,
  };

  handleDrawerOpen = () => {
    this.setState({ open: true });
  };

  handleDrawerClose = () => {
    this.setState({ open: false });
  };

  render() {
    const { classes } = this.props;
    return (
      <div className={classes.root}>
        <div className={classes.appFrame}>
          <AppBar className={classNames(classes.appBar, this.state.open && classes.appBarShift)}>
            <Toolbar disableGutters>
              <IconButton
                  aria-label="open drawer"
                  className={classNames(classes.menuButton, this.state.open && classes.hide)}
                  color="contrast"
                  onClick={this.handleDrawerOpen}
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
              paper: classNames(classes.drawerPaper, !this.state.open && classes.drawerPaperClose),
            }}
              open={this.state.open}
              type="permanent"
          >
            <div className={classes.drawerInner}>
              <div className={classes.drawerHeader}>
                <Logo/>
                <div className={classes.spacer} />
                <IconButton onClick={this.handleDrawerClose}>
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
          <main className={classNames(classes.content, this.state.open && classes.contentShift)}>
            <ModelSelector/>
            <Divider/>
            <div style={{ padding: 0, height: '100%', width: '100%', background: '#fff' }}>
              <div style={{ float: 'left', padding: 0, background: '#fff', height: '100%', width: 'calc(100% - 340px)' }}>
                <EditorContainer/>
              </div>
              <Drawer
                  anchor="right"
                  classes={{
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

export default withStyles(styles, { withTheme: true })(Studio);
