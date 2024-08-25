// https://github.com/lint-staged/lint-staged?tab=readme-ov-file#using-js-configuration-files
// lint-staged.config.js
// import micromatch from 'micromatch'

// https://www.cnblogs.com/jiaoshou/p/12250278.html
module.exports = {
  'framework-dependencies/**/*.{java,tk}|**/pom.xml': (filenames) =>
    `cd framework-dependencies && ./mvnw spotless:apply -DspotlessFiles=${filenames.join(
      ','
    )}`,
  'framework-infra/**/*.{java,tk}|**/pom.xml': (filenames) =>
    `cd framework-infra && ./mvnw spotless:apply -DspotlessFiles=${filenames.join(
      ','
    )}`,
  '**/!(pnpm-lock).{json,js,yml,yaml}': (filenames) =>
    `prettier --write --ignore-unknown ${filenames.join(' ')}`,
}
