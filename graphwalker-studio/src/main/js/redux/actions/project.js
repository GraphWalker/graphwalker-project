
export const PROJECT_SET_ACTIVE_MODEL = 'PROJECT_SET_ACTIVE_MODEL';

export const setActiveModel = (id) => ({
  type: PROJECT_SET_ACTIVE_MODEL,
  payload: {
    id,
  },
});
