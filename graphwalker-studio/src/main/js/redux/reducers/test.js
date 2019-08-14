import {ADD_MODEL, LOAD_TEST, NEW_TEST} from "../actionTypes";

const initialState = {
  models: []
};

export default function(state = initialState, action) {
  switch (action.type) {
    case ADD_MODEL: {
      return {
        ...state,
        models: [action.payload, ...state.models]
      }
    }
    case LOAD_TEST: {
      return {
        ...state,
        ...JSON.parse(action.payload.content)
      }
    }
    case NEW_TEST: {
      return {
        ...initialState,
        models: [action.payload]
      }
    }
    default:
      return state;
  }
}
