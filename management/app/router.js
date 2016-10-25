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
    this.route('review', {path:'review/:reviewid'});
    this.route('loading');
    this.route('show', {path:'show/:productid'});
  });
  this.route('approvals');
  this.route('settings');
});

export default Router;
