/**
 * Common functionality.
 */
define(['angular', './services/helper', './services/playRoutes', './filters', './semantic', 
	'./directives/benchmark_result_row',
	'./directives/building_info',
	'./directives/graph',
	'./directives/required'
	],
    function(angular) {
  'use strict';

  return angular.module('benchmark.common', ['common.helper', 'common.playRoutes', 'common.filters',
    'common.semantic']);
});
