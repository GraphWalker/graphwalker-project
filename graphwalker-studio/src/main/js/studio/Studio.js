import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import classNames from 'classnames';
import Drawer from 'material-ui/Drawer';
import Divider from 'material-ui/Divider';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import ListIcon from 'material-ui-icons/List';
import Typography from 'material-ui/Typography';
import IconButton from 'material-ui/IconButton';
import MenuIcon from 'material-ui-icons/Menu';
import ChevronLeftIcon from 'material-ui-icons/ChevronLeft';
import { Controller, Editor, GitHubIcon, Logo, ModelSelector, Project, Settings } from "../components/index";
import PropertiesTable from "../components/editor/PropertiesTable";
import { styles } from '../styles';

class Studio extends Component {

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
            <Toolbar disableGutters={true}>
              <IconButton
                color="contrast"
                aria-label="open drawer"
                onClick={this.handleDrawerOpen}
                className={classNames(classes.menuButton, this.state.open && classes.hide)}
              >
                <MenuIcon />
              </IconButton>
              <Controller />
              <div className={classes.spacer} />
              <GitHubIcon color="contrast"/>
            </Toolbar>
          </AppBar>
          <Drawer
            type="permanent"
            classes={{
              paper: classNames(classes.drawerPaper, !this.state.open && classes.drawerPaperClose),
            }}
            open={this.state.open}
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
                <Editor/>
              </div>
              <Drawer
                type="permanent"
                anchor="right"
                classes={{
                  paper: classNames(classes.propertiesDrawerPaper),
                  docked: classNames(classes.propertiesDrawerDocked)
                }}
                open={true}>
                <div className={classes.propertiesDrawerInner}>
                  <div className={classes.propertiesDrawerHeader}>
                    <IconButton onClick={this.handleDrawerClose}>
                      <ListIcon/>
                    </IconButton>
                    <Typography color="inherit" noWrap>
                      Properties
                    </Typography>
                  </div>
                  <Divider/>
                  <PropertiesTable />
                  <Divider/>
                  <div className={classes.spacer2} />
                </div>
              </Drawer>
            </div>
          </main>
        </div>
      </div>
    );
  }
}

Studio.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(Studio);
