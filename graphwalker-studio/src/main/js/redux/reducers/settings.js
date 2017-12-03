import { SETTINGS_OPEN_MODAL, SETTINGS_CLOSE_MODAL } from '../actions/settings';

const initialState = {
  showModal: false,
};

const mapping = {
  [SETTINGS_OPEN_MODAL]: (state, action) => ({
    ...state,
    showModal: true,
  }),
  [SETTINGS_CLOSE_MODAL]: (state, action) => ({
    ...state,
    showModal: false,
  }),
};

function settings(state = initialState, action) {
  let newState = state;
  if (mapping[action.type]) {
    newState = mapping[action.type](state, action);
  }
  return newState;
}

export default settings;
