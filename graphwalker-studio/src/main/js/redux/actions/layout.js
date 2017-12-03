
export const LAYOUT_FULLSCREEN_ENTER = 'LAYOUT_FULLSCREEN_ENTER';
export const LAYOUT_FULLSCREEN_EXIT = 'LAYOUT_FULLSCREEN_EXIT';
export const LAYOUT_MENU_DRAWER_OPEN = 'LAYOUT_MENU_DRAWER_OPEN';
export const LAYOUT_MENU_DRAWER_CLOSE = 'LAYOUT_MENU_DRAWER_CLOSE';

export const enterFullscreen = () => ({
  type: LAYOUT_FULLSCREEN_ENTER,
});

export const exitFullscreen = () => ({
  type: LAYOUT_FULLSCREEN_EXIT,
});

export const openMenuDrawer = () => ({
  type: LAYOUT_MENU_DRAWER_OPEN,
});

export const closeMenuDrawer = () => ({
  type: LAYOUT_MENU_DRAWER_CLOSE,
});
