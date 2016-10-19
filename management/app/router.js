import Ember from "ember";
import config from "./config/environment";

const Router = Ember.Router.extend({
  location: config.locationType,
  rootURL: config.rootURL
});

Router.map(function() {
  this.route('login');
  this.route('account', function() {
    this.route('new');
    this.route('settings');
  });
  this.route('product', function() {
    this.route('review', {path:'/:productid/review'},function() {
      this.route('approval');
      this.route('inspect', {path: '/inspect/:reviewid'});
    });
  });
  this.route('approvals');
});

export default Router;
