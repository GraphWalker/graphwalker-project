
export const SETTINGS_OPEN_MODAL = 'SETTINGS_OPEN_MODAL';
export const SETTINGS_CLOSE_MODAL = 'SETTINGS_CLOSE_MODAL';

export const openSettings = () => ({
  type: SETTINGS_OPEN_MODAL,
});

export const closeSettings = () => ({
  type: SETTINGS_CLOSE_MODAL,
});
