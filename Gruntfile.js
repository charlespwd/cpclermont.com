module.exports = function(grunt) {
  // Project configuration.
  grunt.initConfig({
    sass: {
      options: {
        // 'imagePath': 'resources/public/images',
        // 'includePaths': ['resources/assets/styles'],
        'style': 'compressed',
      },
      dist: {
        files: {
          'resources/public/css/style.css': 'resources/assets/styles/style.scss',
          'resources/public/css/style-mobile.css': 'resources/assets/styles/style-mobile.scss',
          'resources/public/css/style-mobilep.css': 'resources/assets/styles/style-mobilep.scss',
          'resources/public/css/style-narrow.css': 'resources/assets/styles/style-narrow.scss',
          'resources/public/css/style-narrower.css': 'resources/assets/styles/style-narrower.scss',
          'resources/public/css/style-normal.css': 'resources/assets/styles/style-normal.scss',
          'resources/public/css/style-wide.css': 'resources/assets/styles/style-wide.scss',
          'resources/public/css/ie/v8.css': 'resources/assets/styles/v8.scss',
        }
      },
    },
    watch: {
      views: {
        files: ['src/cpclermont/views/*'],
        options: {
          livereload: true,
        },
      },
      templates: {
        files: ['resources/templates/**/*.html'],
        options: {
          livereload: true,
        },
      },
      scss: {
        files: ['resources/assets/styles/*'],
        tasks: ['sass'],
        options: {
          livereload: true,
        },
      },
      css: {
        files: ['resources/assets/styles/*'],
        options: {
          livereload: true,
        },
      },
    },
  });

  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-contrib-sass');

  // Default task(s).
  grunt.registerTask('default', ['sass','watch']);
};
