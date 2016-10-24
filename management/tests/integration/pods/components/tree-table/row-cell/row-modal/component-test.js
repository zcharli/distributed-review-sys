import { moduleForComponent, test } from 'ember-qunit';
import hbs from 'htmlbars-inline-precompile';

moduleForComponent('tree-table/row-cell/row-modal', 'Integration | Component | tree table/row cell/row modal', {
  integration: true
});

test('it renders', function(assert) {
  // Set any properties with this.set('myProperty', 'value');
  // Handle any actions with this.on('myAction', function(val) { ... });

  this.render(hbs`{{tree-table/row-cell/row-modal}}`);

  assert.equal(true, true);

  // Template block usage:
  this.render(hbs`
    {{#tree-table/row-cell/row-modal}}
      template block text
    {{/tree-table/row-cell/row-modal}}
  `);

  assert.equal(true, true);
});
