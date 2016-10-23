import DS from 'ember-data';
import Ember from 'ember';
import Inflector from 'ember-inflector';

const inflector = Inflector.inflector;
inflector.uncountable('product');

export default DS.RESTAdapter.extend({
  constants: Ember.inject.service('constants'),
  session: Ember.inject.service('session'),

  host: Ember.computed('constants', function () {
    return this.get('constants.backendApi');
  }),
  namespace: Ember.computed('constants', function () {
    return this.get('constants.namespace');
  }),
  // headers: Ember.computed('session.authToken', function () {
  //   return {
  //     // 'CLIENT_ID': this.get('session.clientId'),
  //   };
  // }),
  defaultSerializer: '-default',

  findAllUrl: Ember.computed('contants', function () {
    return this.get('host') + this.get('namespace') + "/product/all";
  }),

  urlForFindAll(modelName, snapshot) {
    console.log(modelName, snapshot);
    return this.get('findAllUrl');
  }
});
