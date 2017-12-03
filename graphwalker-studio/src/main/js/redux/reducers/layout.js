import { LAYOUT_FULLSCREEN_ENTER, LAYOUT_FULLSCREEN_EXIT, LAYOUT_MENU_DRAWER_OPEN, LAYOUT_MENU_DRAWER_CLOSE } from '../actions/layout';

const initialState = {
  isFullscreen: false,
  isMenuDrawerOpen: false,
};

const mapping = {
  [LAYOUT_FULLSCREEN_ENTER]: (state, action) => ({
    ...state,
    isFullscreen: true,
  }),
  [LAYOUT_FULLSCREEN_EXIT]: (state, action) => ({
    ...state,
    isFullscreen: false,
  }),
  [LAYOUT_MENU_DRAWER_OPEN]: (state, action) => ({
    ...state,
    isMenuDrawerOpen: true,
  }),
  [LAYOUT_MENU_DRAWER_CLOSE]: (state, action) => ({
    ...state,
    isMenuDrawerOpen: false,
  }),
};

function layout(state = initialState, action) {
  let newState = state;
  if (mapping[action.type]) {
    newState = mapping[action.type](state, action);
  }
  return newState;
}

export default layout;
