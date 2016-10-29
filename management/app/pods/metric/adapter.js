import DS from 'ember-data';
import Ember from 'ember';
import Inflector from 'ember-inflector';

const inflector = Inflector.inflector;
inflector.uncountable('metric');

export default DS.RESTAdapter.extend({
  constants: Ember.inject.service('constants'),

  host: Ember.computed('constants', function () {
    return this.get('constants.backendApi');
  }),
  namespace: Ember.computed('constants', function () {
    return this.get('constants.namespace');
  }),

  defaultSerializer: '-default',

  findAllUrl: Ember.computed('constants', function () {
    return this.get('host') + this.get('namespace') + "/metric/all";
  }),

  urlForFindAll() {
    return this.get('findAllUrl');
  }
});
