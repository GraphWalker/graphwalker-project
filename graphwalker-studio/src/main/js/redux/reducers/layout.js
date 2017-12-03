import { LAYOUT_FULLSCREEN_ENTER, LAYOUT_FULLSCREEN_EXIT } from '../actions/layout';

const initialState = {
  isFullscreen: false,
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
};

function layout(state = initialState, action) {
  let newState = state;
  if (mapping[action.type]) {
    newState = mapping[action.type](state, action);
  }
  return newState;
}

export default layout;
