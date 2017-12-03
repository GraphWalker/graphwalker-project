
export const LAYOUT_FULLSCREEN_ENTER = 'LAYOUT_FULLSCREEN_ENTER';
export const LAYOUT_FULLSCREEN_EXIT = 'LAYOUT_FULLSCREEN_EXIT';

export const enterFullscreen = () => ({
  type: LAYOUT_FULLSCREEN_ENTER,
});

export const exitFullscreen = () => ({
  type: LAYOUT_FULLSCREEN_EXIT,
});
