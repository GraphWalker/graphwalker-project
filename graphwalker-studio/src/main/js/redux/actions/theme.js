
export const THEME_CHANGE_PALETTE_TYPE = 'THEME_CHANGE_PALETTE_TYPE';

export const setPaletteType = (paletteType) => ({
  type: THEME_CHANGE_PALETTE_TYPE,
  payload: {
    paletteType,
  },
});
