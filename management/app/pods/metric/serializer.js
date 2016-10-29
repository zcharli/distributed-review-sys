import DS from 'ember-data';

export default DS.RESTSerializer.extend(DS.EmbeddedRecordsMixin, {
  primaryKey: 'id',

  attrs: {
    previews: { embedded: 'always' }
  },

  normalizeResponse(store, primaryModelClass, payload, id, requestType) {
    return this._super(store, primaryModelClass, payload, id, requestType);
  },

  normalize(typeClass, hash, prop) {
    return this._super(typeClass, hash, prop);
  },
});
