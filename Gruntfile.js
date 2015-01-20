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
