import { globalIgnores } from 'eslint/config'
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript'
import pluginVue from 'eslint-plugin-vue'
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting'

export default defineConfigWithVueTs(
  globalIgnores(['**/dist/**']),
  pluginVue.configs['flat/essential'],
  vueTsConfigs.recommended,
  skipFormatting,
  {
    files: ['**/*.{js,mjs,cjs,jsx,ts,mts,tsx,vue}'],
    rules: {
      'vue/script-indent': ['error', 2, { baseIndent: 0, switchCase: 0, ignores: [] }],
      'vue/multi-word-component-names': 'off',
      'comma-dangle': 'error',
      'comma-spacing': 'error',
      'no-unused-vars': 'error',
      'no-var': 'error',
      'no-debugger': 'warn',
      'no-console': 'warn',
      'no-alert': 'warn'
    }
  }
)
