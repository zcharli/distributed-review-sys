import DS from 'ember-data';

export default DS.RESTSerializer.extend({
  primaryKey: 'id',
  normalizeResponse(store, primaryModelClass, payload, id, requestType) {
    return this._super(store, primaryModelClass, payload, id, requestType);
  },
});
