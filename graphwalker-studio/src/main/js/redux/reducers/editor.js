import {EDITOR_PROPERTIES_TOGGLE} from "../actionTypes";

const initialState = {
  showProperties: false
}

export default function(state = initialState, action) {
  switch (action.type) {
    case EDITOR_PROPERTIES_TOGGLE: {
      return {
        ...state,
        showProperties: !state.showProperties
      }
    }
    default: {
      return state;
    }
  }
}
