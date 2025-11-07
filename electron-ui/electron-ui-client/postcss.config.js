import postcssPresetEnv from 'postcss-preset-env'
import cssnano from 'cssnano'

/** @type {import("postcss-load-config").Config} */
export default {
  plugins: [
    postcssPresetEnv({ features: {} }),
    process.env.NODE_ENV === 'production' ? cssnano({ preset: 'default' }) : void 0
  ]
}
