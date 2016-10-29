import { moduleForComponent, test } from 'ember-qunit';
import hbs from 'htmlbars-inline-precompile';

moduleForComponent('metric-single-stat', 'Integration | Component | metric single stat', {
  integration: true
});

test('it renders', function(assert) {
  // Set any properties with this.set('myProperty', 'value');
  // Handle any actions with this.on('myAction', function(val) { ... });

  this.render(hbs`{{metric-single-stat}}`);

  assert.equal(this.$().text().trim(), '');

  // Template block usage:
  this.render(hbs`
    {{#metric-single-stat}}
      template block text
    {{/metric-single-stat}}
  `);

  assert.equal(this.$().text().trim(), 'template block text');
});
