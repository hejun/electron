/** @type {import("stylelint").Config} */
export default {
  extends: ['stylelint-config-standard-scss', 'stylelint-config-recess-order', 'stylelint-prettier/recommended'],
  ignoreFiles: ['**/*.js', '**/*.ts', '**/*.jsx', '**/*.tsx'],
  overrides: [
    {
      files: ['**/*.(vue|html)'],
      customSyntax: 'postcss-html'
    },
    {
      files: ['**/*.(scss|sass)'],
      customSyntax: 'postcss-scss'
    }
  ]
}
