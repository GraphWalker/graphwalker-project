import { THEME_CHANGE_PALETTE_TYPE } from '../actions/theme';

const initialState = {
  paletteType: 'dark',
};

const mapping = {
  [THEME_CHANGE_PALETTE_TYPE]: (state, action) => ({
    ...state,
    paletteType: action.payload.paletteType,
  }),
};

function theme(state = initialState, action) {
  let newState = state;
  if (mapping[action.type]) {
    newState = mapping[action.type](state, action);
  }
  return newState;
}

export default theme;
